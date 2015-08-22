package com.example.learnqq.internet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.example.learnqq.R;

/**
 * 加载图片 查找顺序为 内存。文件夹 如果没有则从网络下载
 * */
public class ImageLoader {
	public class PhotosToLoader implements Runnable {
		PhotoToLoad photoToLoad;

		public PhotosToLoader(PhotoToLoad photoToLoad){
			this.photoToLoad = photoToLoad;
		}

		/**
		 * 当前图片是否该被加载或者被显示，只有图片只有图片保存的URL（photoToLoad.url）
		 * 与要下载的URL（imageViews.get(photoToLoad.imageView)）一致才能被加载
		 * 避免了当listView滑动时图片的显示及资源浪费问题（如果向上滑动到第二个item显示在第一行，则最后一个item会重用第一个item的实例。
		 * 这件会使 以前的item下载 现在的item下载 现在的图片设置 发生的顺序不确定从而会让图片错位。解决方法设置默认没有要求图片是对应的默认图片，
		 * 在设置图片、下载图片时要求下载的url与图片的url一致）
		 */
		private boolean imageViewReused(PhotoToLoad photoToLoad){
			String tag = imageViews.get(photoToLoad.imageView);
			if(tag == null || !tag.equals(photoToLoad.url)){
				return true;
			}
			return false;
		}

		@Override
		public void run() {
			if(imageViewReused(photoToLoad)){
				return;
			}

			Bitmap bitmap = getBitmap(photoToLoad.url);
			memoryCache.put(photoToLoad.url,  bitmap);//将图片保存在内存

			if(imageViewReused(photoToLoad)){
				return;
			}

			if(bitmap != null){
				photoToLoad.imageView.setImageBitmap(bitmap);
			}else{
				photoToLoad.imageView.setImageResource(DEFAULT_BG);
			}

		}
	}
	public class PhotoToLoad {
		String url;
		public ImageView imageView;

		public PhotoToLoad(String url, ImageView imageView) {
			this.imageView = imageView;
			this.url = url;
		}

	}

	private static final String TAG = "ImageLoader";
	private static final int TIME_OUT = 30000;
	private static final int DEFAULT_BG = R.drawable.ic_launcher;
	private static final int THREAD_NUM = 5;
	MemoryCache memoryCache = new MemoryCache();//内存中的图片
	FileCache fileCache;//文件 中的图片
	/**
	 * 判断当前的图片的网址和要求下载的网址是否一致
	 */
	private Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
	ExecutorService executorService;//线程池

	/**
	 * 初始化线程池、缓存文件
	 * */
	public ImageLoader(Context context){
		fileCache = new FileCache(context);
		executorService = Executors.newFixedThreadPool(THREAD_NUM);
	}
	
	/**
	 * 将imageView的图像设为url指定的图像
	 * @param url 网址
	 * @param imageView 显示图片的容器
	 */
	public void disPlayImage(String url, ImageView imageView){
		imageViews.put(imageView, url);//保存当前图片与当前图片对应的网址
		Bitmap bitmap = memoryCache.get(url);
		if(bitmap != null){//图片存在内存中
			imageView.setImageBitmap(bitmap);
		}else{
			queuePhoto(url, imageView);
		}

	}

	private void queuePhoto(String url, ImageView imageView) {
		PhotoToLoad photoToLoad = new PhotoToLoad(url, imageView);
		executorService.submit(new PhotosToLoader(photoToLoad));
	}

	/**
	 * 从文件或者网络获取url指定的图片
	 * */
	private Bitmap getBitmap(String url){
		File file = fileCache.getFile(url);
		Bitmap bitmap = decodeFile(file);
		if(bitmap != null){//图片保存在文件中
			return bitmap;
		}

		//从网络获取图片
		try {
			bitmap = null;
			URL imageUrl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
			conn.setConnectTimeout(TIME_OUT);
			conn.setReadTimeout(TIME_OUT);
			conn.setInstanceFollowRedirects(true);
			InputStream inputStream = conn.getInputStream();
			OutputStream outputStream = new FileOutputStream(file);
			copyStream(inputStream, outputStream);
			outputStream.close();
			inputStream.close();
			bitmap = decodeFile(file);
			return bitmap;
		} catch (Throwable ex) {
			if (ex instanceof OutOfMemoryError) {
				clearCache();
			}
			return null;
		}

	}

	/**
	 * 情况文件及内存缓存
	 * */
	private void clearCache() {
		memoryCache.clear();
		fileCache.clear();
	}

	/**
	 * 将输入流复制到输出流
	 * */
	private void copyStream(InputStream inputStream, OutputStream outputStream) {
		int buffer_size = 1024;
		byte[] bytes = new byte[buffer_size];
		int count = 0;
		while(true){
			try {
				count = inputStream.read(bytes, 0, buffer_size);
				if(count == -1){
					break;
				}
				outputStream.write(bytes, 0, count);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private Bitmap decodeFile(File file) {
		Bitmap bitmap = null;
		try {
			FileInputStream fileInputStream = new FileInputStream(file);
			bitmap = BitmapFactory.decodeStream(fileInputStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return bitmap;


	}

}


