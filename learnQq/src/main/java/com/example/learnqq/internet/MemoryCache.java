package com.example.learnqq.internet;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import android.graphics.Bitmap;

public class MemoryCache {
	private Map<String, SoftReference<Bitmap>> cache = 
			Collections.synchronizedMap(new LinkedHashMap<String, SoftReference<Bitmap>>(10, 1.5f, true));
	
	/**
	 * 根据URL从内存中获取图片
	 * */
	public Bitmap get(String id){
		if(!cache.containsKey(id)){
			return null;
		}
		SoftReference<Bitmap> ref = cache.get(id);
		return ref.get();
	}
	
	public void put(String id, Bitmap bitmap){
		cache.put(id, new SoftReference<Bitmap>(bitmap));
	}
	
	public void clear(){
		cache.clear();
	}

}
