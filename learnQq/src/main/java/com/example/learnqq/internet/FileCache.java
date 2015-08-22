package com.example.learnqq.internet;

import java.io.File;

import android.content.Context;
import android.os.Environment;

public class FileCache {
	private static final String DIR_NAME = "MY_LEARN_QQ";
	private File cacheDir;
	/**
	 * 用文件保存下载的图片，如果不存在改文件则创建文件
	 * @param context activity对象
	 */
	public FileCache(Context context){
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			cacheDir = new File(Environment.getExternalStorageDirectory(), DIR_NAME);
		}else{
			cacheDir = context.getCacheDir();
		}
		
		if(!cacheDir.exists()){
			cacheDir.mkdir();
		}
	}
	
	/**
	 * 从文件中读取URL指定的图片
	 * @param url 图片对应的网址
	 * @return 图片的文件
	 */
	public File getFile(String url){
		String filename = String.valueOf(url.hashCode());
		File f = new File(cacheDir, filename);
		return f;
	}
			
	/**
	 * 清除图片文件
	 */
	public void clear(){
		File[] files = cacheDir.listFiles();
		if(files == null){
			return;
		}else{
			for(File f: files){
				f.delete();
			}
		}
	}

}
