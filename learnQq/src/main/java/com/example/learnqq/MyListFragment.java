package com.example.learnqq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

/**
 * 滑动菜单对应的Fragement内容
 *
 */
public class MyListFragment extends Fragment {
	private View contentView;
	private SimpleAdapter myAdapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		List data = new ArrayList<Map>();
		Map item = new HashMap();
		item.put("img", R.drawable.sun);
		item.put("text", "开通会员");
		data.add(item);
		item = new HashMap();
		item.put("img", R.drawable.star);
		item.put("text", "qq钱包");
		data.add(item);
		String from[] = {"img", "text"};
		int to[] = {R.id.img, R.id.text};
		contentView = inflater.inflate(R.layout.fragment1, container, false);
		ListView myList = (ListView) contentView.findViewById(R.id.listFunction);
		myAdapter = new SimpleAdapter(getActivity(), data, R.layout.fragment1_adapter,
				from, to);
		myList.setAdapter(myAdapter);
		myList.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(getActivity(), OtherActivity.class);
				getActivity().startActivity(intent);
			}
		});
		return contentView;
	}




}
