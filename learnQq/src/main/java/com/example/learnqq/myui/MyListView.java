package com.example.learnqq.myui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.learnqq.R;

import java.util.Date;

/**
 * @author 自定义ListView可以实现下拉刷新
 *
 */
public class MyListView extends ListView implements OnScrollListener{

	/**
	 * @author 刷新监听器，在要刷新lisview时，进行一些刷新操作
	 *
	 */
	public interface OnRefreshListener {
		void OnRefresh();
	}

	private static final String TAG = "TouchTest";
	private final static int RELEASE_TO_REFRESH = 0;
	private final static int PULL_TO_REFRESH = 1;
	private final static int REFRESHING = 2;
	private final static int DONE = 3;
	private final static int LOADING = 4;
	public boolean canScroll = false;//当前ListView是否能够移动

	/**
	 * 在屏幕下滑的距离与ListView实际移动的距离的比例
	 */
	private final static int RATIO = 3;
	private LayoutInflater inflater;

	/**
	 *headerView 头部视图
	 *lvHeaderTipsTv 头部提醒
	 *lvHeaderLastUpdatedTv 最近更新提示
	 *lvHeaderArrowIv 头部箭头
	 *lvHeaderProgressBar 更新进度条
	 *
	 */
	private LinearLayout headerView;
	private TextView lvHeaderTipsTv;
	private TextView lvHeaderLastUpdatedTv;
	private ImageView lvHeaderArrowIv;
	private ProgressBar lvHeaderProgressBar;

	/**
	 * 头部内容高度
	 */
	private int headerContentHeight;

	/**
	 * animation 箭头由下拉刷新到释放刷新
	 * reverseAnimation 箭头由释放刷新到下拉刷新
	 */
	private RotateAnimation animation;
	private RotateAnimation reverseAnimation;

	/**
	 * startY 下拉屏幕是的y值
	 */
	private int startY;
	/**
	 * 当前头部的状态
	 */
	private int state;
	/**
	 * 箭头是否反向
	 */
	private boolean isBack;

	/**
	 * 当前屏幕下拉的开始位置的y值是否被记录
	 */
	private boolean isRecored;

	private OnRefreshListener refreshListener;
	private boolean isRefreshable;


	public MyListView(Context context) {
		super(context);
		init(context);
	}

	public MyListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}


	/**
	 * 对ListView进行一些初始化操作
	 * @param context
	 */
	private void init(Context context) {
		setCacheColorHint(context.getResources().getColor(android.R.color.holo_red_light));
		inflater = LayoutInflater.from(context);
		headerView = (LinearLayout) inflater.inflate(R.layout.lv_header, null);
		lvHeaderTipsTv = (TextView) headerView.findViewById(R.id.lvHeaderTipsTv);
		lvHeaderLastUpdatedTv = (TextView) headerView.findViewById(R.id.lvHeaderLastUpdatedTv);
		lvHeaderArrowIv = (ImageView) headerView.findViewById(R.id.lvHeaderArrowIv);

		lvHeaderArrowIv.setMinimumWidth(70);
		lvHeaderArrowIv.setMinimumHeight(50);

		lvHeaderProgressBar = (ProgressBar) headerView.findViewById(R.id.lvHeaderProgressBar);
		measureView(headerView);

		headerContentHeight = headerView.getMeasuredHeight();
		// 设置内边距，正好距离顶部为一个负的整个布局的高度，正好把头部隐藏
		headerView.setPadding(0, -1 * headerContentHeight, 0, 0);
		// 重绘一下
		headerView.invalidate();
		// 将下拉刷新的布局加入ListView的顶部
		addHeaderView(headerView, null, false);
		// 设置滚动监听事件
		setOnScrollListener(this);
		//设置箭头变化动画
		animation = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		animation.setInterpolator(new LinearInterpolator());
		animation.setDuration(250);
		animation.setFillAfter(true);

		reverseAnimation = new RotateAnimation(-180, 0,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		reverseAnimation.setInterpolator(new LinearInterpolator());
		reverseAnimation.setDuration(250);
		reverseAnimation.setFillAfter(true);

		// 一开始的状态就是下拉刷新完的状态，所以为DONE
		state = DONE;
		// 是否能够刷新
		isRefreshable = false;

	}


	private void measureView(View child) {
		ViewGroup.LayoutParams params = child.getLayoutParams();
		if(params == null){
			params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0, params.width);
		int lpHeight = params.height;
		int childHeightSpec;
		if(lpHeight > 0){
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
		}else{
			childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		}

		child.measure(childWidthSpec, childHeightSpec);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if(isRefreshable){
			switch(ev.getAction()){
				case MotionEvent.ACTION_DOWN:
					Log.i(TAG, "listView Action_down");
					if(!isRecored){
						isRecored = true;
						startY = (int) ev.getY();
					}
					break;
				case MotionEvent.ACTION_UP:
					Log.i(TAG, "listView Action_up");
					if(state != REFRESHING && state != LOADING){
						if(state == PULL_TO_REFRESH){
							state = DONE;
							changeHeaderViewByState();
						}

						if(state == RELEASE_TO_REFRESH){
							state = REFRESHING;
							changeHeaderViewByState();
							onLvRefresh();
						}
					}
					isRecored = false;
					isBack = false;
					break;
				case MotionEvent.ACTION_MOVE:
					Log.i(TAG, "listView Action_move");
					int tempY = (int) ev.getY();
					if(!isRecored){
						isRecored = true;
						startY = tempY;
					}

					if(state != REFRESHING && isRecored && state != LOADING){
						if(state == RELEASE_TO_REFRESH){
							setSelection(0);
							if(((tempY - startY) / RATIO < headerContentHeight) && (tempY - startY) > 0){
								state = PULL_TO_REFRESH;
								changeHeaderViewByState();
							}else if(tempY - startY <= 0){
								state = DONE;
								changeHeaderViewByState();
							}
						}
						// 还没有到达显示松开刷新的时候,DONE或者是PULL_To_REFRESH状态
						if (state == PULL_TO_REFRESH) {
							setSelection(0);
							// 下拉到可以进入RELEASE_TO_REFRESH的状态
							if ((tempY - startY) / RATIO >= headerContentHeight) {// 由done或者下拉刷新状态转变到松开刷新
								state = RELEASE_TO_REFRESH;
								isBack = true;
								changeHeaderViewByState();
							}
							// 上推到顶了
							else if (tempY - startY <= 0) {// 由DOne或者下拉刷新状态转变到done状态
								state = DONE;
								changeHeaderViewByState();
							}
						}
						if(state == DONE){
							if(tempY - startY > 0){
								state = PULL_TO_REFRESH;
								changeHeaderViewByState();
							}
						}

						if(state == PULL_TO_REFRESH){
							headerView.setPadding(0, -1 * headerContentHeight + (tempY - startY) / RATIO, 0, 0);
						}

						if(state == RELEASE_TO_REFRESH){
							headerView.setPadding(0, (tempY - startY) / RATIO - headerContentHeight, 0, 0);
						}
					}
					break;
				default:
					break;
			}
		}
		return super.onTouchEvent(ev);
	}

	private void onLvRefresh() {
		if(refreshListener != null){
			refreshListener.OnRefresh();
		}

	}

	private void changeHeaderViewByState() {
		switch(state){
			case RELEASE_TO_REFRESH:
				lvHeaderArrowIv.setVisibility(View.VISIBLE);
				lvHeaderProgressBar.setVisibility(View.GONE);
				lvHeaderTipsTv.setVisibility(View.VISIBLE);
				lvHeaderLastUpdatedTv.setVisibility(View.VISIBLE);

				lvHeaderArrowIv.clearAnimation();
				lvHeaderArrowIv.startAnimation(animation);
				break;
			case PULL_TO_REFRESH:
				lvHeaderProgressBar.setVisibility(View.GONE);
				lvHeaderTipsTv.setVisibility(View.VISIBLE);
				lvHeaderLastUpdatedTv.setVisibility(View.VISIBLE);
				lvHeaderArrowIv.setVisibility(View.VISIBLE);
				if(isBack){
					isBack = false;
					lvHeaderArrowIv.clearAnimation();
					lvHeaderArrowIv.startAnimation(reverseAnimation);
					lvHeaderTipsTv.setText("下拉刷新！");
				}else{
					lvHeaderTipsTv.setText("下拉刷新！");
				}
				break;

			case REFRESHING:
				headerView.setPadding(0, 0, 0, 0);
				lvHeaderProgressBar.setVisibility(View.VISIBLE);
				lvHeaderArrowIv.clearAnimation();
				lvHeaderArrowIv.setVisibility(View.GONE);
				lvHeaderTipsTv.setVisibility(View.VISIBLE);
				break;
			case DONE:
				headerView.setPadding(0, -1 * headerContentHeight, 0, 0);
				lvHeaderProgressBar.setVisibility(View.GONE);
				lvHeaderArrowIv.clearColorFilter();
				lvHeaderArrowIv.setImageResource(R.drawable.arrow);
				lvHeaderTipsTv.setText("下拉刷新");
				lvHeaderLastUpdatedTv.setVisibility(View.VISIBLE);
				break;

		}

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
						 int visibleItemCount, int totalItemCount) {
		if(firstVisibleItem == 0){
			isRefreshable = true;
		}else{
			isRefreshable = false;
		}

	}

	public void setonRefreshListener(OnRefreshListener refreshListener) {
		this.refreshListener = refreshListener;
		isRefreshable = true;
	}

	@Override
	public void setAdapter(ListAdapter adapter){
		lvHeaderLastUpdatedTv.setText("最近更新：" + new Date().toLocaleString());;
		super.setAdapter(adapter);
	}

	public void onRefreshComplete() {
		state = DONE;
		lvHeaderLastUpdatedTv.setText("最近更新:" + new Date().toLocaleString());
		changeHeaderViewByState();
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if(!canScroll){
			Log.i(TAG, "cann't move!  canScrolle" + canScroll);
			return false;
		}
		Log.i(TAG, "can move!!  canScrolle" + canScroll);
		return true;
	}


}
