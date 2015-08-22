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
 * ����ͼƬ ����˳��Ϊ �ڴ档�ļ��� ���û�������������
 * */
public class ImageLoader {
	public class PhotosToLoader implements Runnable {
		PhotoToLoad photoToLoad;

		public PhotosToLoader(PhotoToLoad photoToLoad){
			this.photoToLoad = photoToLoad;
		}

		/**
		 * ��ǰͼƬ�Ƿ�ñ����ػ��߱���ʾ��ֻ��ͼƬֻ��ͼƬ�����URL��photoToLoad.url��
		 * ��Ҫ���ص�URL��imageViews.get(photoToLoad.imageView)��һ�²��ܱ�����
		 * �����˵�listView����ʱͼƬ����ʾ����Դ�˷����⣨������ϻ������ڶ���item��ʾ�ڵ�һ�У������һ��item�����õ�һ��item��ʵ����
		 * �����ʹ ��ǰ��item���� ���ڵ�item���� ���ڵ�ͼƬ���� ������˳��ȷ���Ӷ�����ͼƬ��λ�������������Ĭ��û��Ҫ��ͼƬ�Ƕ�Ӧ��Ĭ��ͼƬ��
		 * ������ͼƬ������ͼƬʱҪ�����ص�url��ͼƬ��urlһ�£�
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
			memoryCache.put(photoToLoad.url,  bitmap);//��ͼƬ�������ڴ�

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
	MemoryCache memoryCache = new MemoryCache();//�ڴ��е�ͼƬ
	FileCache fileCache;//�ļ� �е�ͼƬ
	/**
	 * �жϵ�ǰ��ͼƬ����ַ��Ҫ�����ص���ַ�Ƿ�һ��
	 */
	private Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
	ExecutorService executorService;//�̳߳�

	/**
	 * ��ʼ���̳߳ء������ļ�
	 * */
	public ImageLoader(Context context){
		fileCache = new FileCache(context);
		executorService = Executors.newFixedThreadPool(THREAD_NUM);
	}
	
	/**
	 * ��imageView��ͼ����Ϊurlָ����ͼ��
	 * @param url ��ַ
	 * @param imageView ��ʾͼƬ������
	 */
	public void disPlayImage(String url, ImageView imageView){
		imageViews.put(imageView, url);//���浱ǰͼƬ�뵱ǰͼƬ��Ӧ����ַ
		Bitmap bitmap = memoryCache.get(url);
		if(bitmap != null){//ͼƬ�����ڴ���
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
	 * ���ļ����������ȡurlָ����ͼƬ
	 * */
	private Bitmap getBitmap(String url){
		File file = fileCache.getFile(url);
		Bitmap bitmap = decodeFile(file);
		if(bitmap != null){//ͼƬ�������ļ���
			return bitmap;
		}

		//�������ȡͼƬ
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
	 * ����ļ����ڴ滺��
	 * */
	private void clearCache() {
		memoryCache.clear();
		fileCache.clear();
	}

	/**
	 * �����������Ƶ������
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


