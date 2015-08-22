package com.example.learnqq.adapter;

import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.learnqq.R;
import com.example.learnqq.internet.ImageLoader;
import com.example.learnqq.myui.MyListView;
import com.example.learnqq.utils.ImageResource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 设置消息适配器
 * extraX:隐藏的删除位置的宽度
 * imageLoader:图片加载类
 * */
public  class MyAdapter extends BaseAdapter {
	String[] urls = ImageResource.urls;
	private int extraX;
	protected LayoutInflater layInf;
	private static final String TAg = "TouchTest";
	protected Context context;
	protected List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
	ImageLoader imageLoader;
	MyHolder holder;
	//	View moveedView;//保存上一个已经移动的item 实现整个页面只能有一个移动了的item
	private MyListView lv;//用其canScroll控制滚动

	public MyAdapter(Context context, MyListView lv){
		extraX = (int) context.getResources().getDimension(R.dimen.extraX);
		this.lv = lv;
		this.context = context;
		this.layInf = LayoutInflater.from(context);
		Map<String, Object> myMap;
		imageLoader = new ImageLoader(context);
		for(int i = 0; i < 10; i++){
			myMap = new HashMap<String, Object>();
			myMap.put("head", "头像" + i);
			myMap.put("name", "名字" + i);
			myMap.put("msg", "消息" + i);
			myMap.put("time", "时间" + i);
			data.add(myMap);
		}
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		if(view == null){
			view = layInf.inflate(R.layout.fragment_msg, null);
			holder = new MyHolder();
			holder.head = (ImageView) view.findViewById(R.id.head);
			holder.name = (TextView) view.findViewById(R.id.name);
			holder.time = (TextView) view.findViewById(R.id.time);
			holder.msg = (TextView) view.findViewById(R.id.msg);
			view.setTag(holder);
		}else{
			holder = (MyHolder) view.getTag();
		}

		holder.head.setImageResource(R.drawable.ic_launcher);
		imageLoader.disPlayImage(urls[position], holder.head);

		holder.name.setText((String) data.get(position).get("name"));
		holder.time.setText((String) data.get(position).get("time"));
		holder.msg.setText((String) data.get(position).get("msg"));
		final View view1 = view.findViewById(R.id.myhs);

		view1.setOnTouchListener(new OnTouchListener(){
			Point start = new Point(), end = new Point(), last = new Point(), current = new Point();
			boolean moved = false;//是否已经移动过
			int direction = 5;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				switch(action){
					case MotionEvent.ACTION_DOWN:
						start.set((int)event.getX(), (int)event.getY());
						last.set((int)event.getX(), (int)event.getY());
						Log.d(TAg, "Action_down: startx " + start.x);
						return false;
					case MotionEvent.ACTION_MOVE:
						current.set((int)event.getX(), (int)event.getY());
						if(direction == 5){
							direction = moveDirection(start, current);
//						Log.d(TAg, "本次touch事件方向："  + direction);
						}Log.d(TAg, "本次touch事件方向："  + direction);
						moveWay();
						break;
					case MotionEvent.ACTION_UP://可能由于滑动太快不会触发action_up事件
					case MotionEvent.ACTION_CANCEL:
						Log.d(TAg, "action up! cancle(3):" + action);
						end.set((int)event.getX(), (int)event.getY());
						if(moved && direction == 3){
							if((end.x - start.x) > extraX / 2){
								view1.scrollTo(0, 0);
								moved = false;
							}else{
								view1.scrollTo(extraX, 0);
							}
						}else{
							if((start.x - end.x) >= extraX / 2 && direction == 2){
								view1.scrollTo(extraX, 0);
								moved = true;
							}else{
								view1.scrollTo(0, 0);
							}
						}
						direction = 5;
						lv.canScroll = false;
						break;
				}
				return true;
			}

			/**
			 * 根据移动的方式进行相应处理
			 * */
			private int moveWay(){
				if(!moved){//listView 的item未被移动 返回方向乘以2
					switch(direction){
						case 0:
							Log.d(TAg, "未移动 方向上");
							lv.canScroll = true;
							return 0;
						case 1:
							lv.canScroll = true;
							Log.d(TAg, "未移动 方向下");
							return 2;
						case 2://item移动达到边界值后就不会移动了，可以进一步优化下
							if(view1.getScrollX() >= 0 && view1.getScrollX() <= extraX)
								move();
							Log.d(TAg, "未移动 方向左");
							return 4;
						case 3:
							Log.d(TAg, "未移动 方向右");
							return 6;
					}
				}else //listView 的item已被移动 返回方向乘以2加1
					switch(direction){
						case 0:
							Log.d(TAg, "移动 方向上");
							return 1;
						case 1:
							Log.d(TAg, "移动 方向下");
							return 3;
						case 2:

							Log.d(TAg, "移动 方向左");
							return 5;
						case 3:
							if(view1.getScrollX() >= 0 && view1.getScrollX() <= extraX)
								move();
							Log.d(TAg, "移动 方向右");
							return 7;
					}
				return -1;
			}

			/**
			 * 移动item
			 * */
			private void move() {
				view1.scrollBy((int) (last.x -current.x), 0);
				last.x = current.x;
				last.y = current.y;
			}


			/**
			 * 判断屏幕滑动的方向 上下左右 对应值为 0 1 2 3
			 * @return
			 */
			private int moveDirection(Point first, Point second) {
				int distanceX = second.x - first.x;
				int distanceY = second.y - first.y;
				if(Math.abs(distanceX) > Math.abs(distanceY)){
					if(distanceX > 0){
						return 3;
					}else{
						return 2;
					}
				}else{
					if(distanceY > 0)
						return 1;
					else
						return 0;
				}
			}
		});
		return view;

	}


	/**
	 * A placeholder fragment containing a simple view.
	 */
	public  class MyHolder{
		public ImageView head;
		public TextView name;
		public TextView time;
		public TextView msg;
	}
}