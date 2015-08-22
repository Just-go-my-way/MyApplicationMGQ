package com.example.learnqq;

import android.app.ActionBar;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.learnqq.adapter.MyAdapter;
import com.example.learnqq.myui.MyListView;
import com.example.learnqq.myui.MyListView.OnRefreshListener;
import com.example.learnqq.utils.ImageResource;
//import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;


/**
 * 主Activity，添加了PlaceholderFragment显示主体内容
 * slidingMenu显示侧滑菜单
 * 在ActinBar上也设置了菜单
 */
public class MainActivity extends FragmentActivity {
//	SlidingMenu menu; //侧滑菜单的内容
	Menu myMenu;//actionBar上面的菜单
	ActionBar ab;
	MyListView lv;
	Handler testHandle;
	View mainView, behindView;
	private static final String TAg = "MainActivity";

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
			.add(R.id.container, new PlaceholderFragment()).commit();
		}
		/*menu = new SlidingMenu(this);
		menu.setMode(SlidingMenu.LEFT);
//		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
		menu.setShadowWidth(20);
		menu.setShadowDrawable(R.drawable.three);
		//			menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		menu.setBehindOffset(300);
		menu.setFadeDegree(0.9f);
		menu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
		menu.setMenu(R.layout.slidingmenu);*/
		
		
		

		ab = getActionBar();
		ab.setBackgroundDrawable(getResources().getDrawable(R.drawable.back));;
		ab.setIcon(R.drawable.head);
		ab.setDisplayShowTitleEnabled(false);
		ab.setDisplayShowCustomEnabled(true);
		ab.setCustomView(R.layout.cuslay);
		
		/**
		 * 模仿qq的滑动效果
		 * */
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		int middle = displayMetrics.heightPixels / 2;
		float scale = 1, behindScale = 0.5f;

		mainView = findViewById(R.id.container);
		/*mainView.setScaleX(0.5f);
		mainView.setScaleY(0.5f);
		mainView.setPivotX(900);
		mainView.setPivotY(middle);*/
		
		behindView = findViewById(R.id.behind);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		myMenu = menu;
		Log.d(TAg, "onCreateOptionsMenu is called!");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) { 
			Toast.makeText(this, "settings selected!", Toast.LENGTH_SHORT).show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 *显示主体内容
	 *使用了TabHost容器
	 *
	 */
	public class PlaceholderFragment extends Fragment {
		int visableId[] = {R.id.myGroup, R.id.add, R.id.more};
		int currentId = 0;
		int imageButtonId = 0;
		ViewGroup container;
		LinearLayout linearLayout[] = new LinearLayout[3];
		int bImg[] = ImageResource.firstPageBottomImg;

		public PlaceholderFragment() {}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			this.container = container;
			//获取不同页面的布局内容
			linearLayout[0] = (LinearLayout) rootView.findViewById(R.id.firstPage);
			linearLayout[1] = (LinearLayout) rootView.findViewById(R.id.seconePage);
			linearLayout[2] = (LinearLayout) rootView.findViewById(R.id.thirdPage);

			//获取主界面底部的IamgeButton按钮
			final ImageButton bottomImg[] ={(ImageButton) rootView.findViewById(R.id.massage),
					(ImageButton) rootView.findViewById(R.id.contact),
					(ImageButton) rootView.findViewById(R.id.dynamic)};
			//设置图片按钮的点击事件
			OnClickListener clickListener = new OnClickListener(){

				@Override
				public void onClick(View v) {
					imageButtonId = v.getId();
					switch(imageButtonId){
					case R.id.massage:
						ab.setCustomView(R.layout.cuslay);
						change(0);
						currentId = 0;
						break;
					case R.id.contact:
						ab.setCustomView(R.layout.cuslay2);
						change(1);
						currentId = 1;
						break;
					case R.id.dynamic:
						ab.setCustomView(R.layout.cuslay3);
						change(2);
						currentId = 2;
						break;
					}

				}

				private void change(int id) {
					//改变ActionBar内容
					if(currentId != id){
						if(currentId == 0){
							myMenu.setGroupVisible(visableId[currentId], false);
							myMenu.findItem(visableId[id]).setVisible(true).
							setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
						}else{
							myMenu.findItem(visableId[currentId]).setVisible(false);
							if(id == 0){
								myMenu.setGroupVisible(visableId[id], true);
							}else{
								myMenu.findItem(visableId[id]).setVisible(true).
								setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
							}
						}

						//改变页面内容
						linearLayout[id].setVisibility(LinearLayout.VISIBLE);
						linearLayout[currentId].setVisibility(LinearLayout.INVISIBLE);

						//改变底部图片内容
						bottomImg[id].setImageResource(bImg[id * 2 + 1]);
						bottomImg[currentId].setImageResource(bImg[currentId * 2]);
					}

				}

			};

			bottomImg[0].setOnClickListener(clickListener);
			bottomImg[1].setOnClickListener(clickListener);
			bottomImg[2].setOnClickListener(clickListener);
			//为消息列表添加适配器
			lv = (MyListView) rootView.findViewById(R.id.firstList);
			lv.setAdapter(new MyAdapter(getActivity(), lv));
			testHandle = new Handler(){

				@Override
				public void handleMessage(Message msg) {
					if(msg.what == 1){
						lv.onRefreshComplete();
					}
				}
				
			};
			lv.setonRefreshListener(new OnRefreshListener(){

				@Override
				public void OnRefresh() {
					new Thread(){

						@Override
						public void run() {
							try {
								Thread.sleep(1000);
								testHandle.obtainMessage(1).sendToTarget();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
							 
					}.start();
				}
				
			});
			return rootView;
		}
	}

}
