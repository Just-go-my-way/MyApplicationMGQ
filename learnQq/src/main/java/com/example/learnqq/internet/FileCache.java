package com.example.learnqq.internet;

import java.io.File;

import android.content.Context;
import android.os.Environment;

public class FileCache {
	private static final String DIR_NAME = "MY_LEARN_QQ";
	private File cacheDir;
	/**
	 * ���ļ��������ص�ͼƬ����������ڸ��ļ��򴴽��ļ�
	 * @param context activity����
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
	 * ���ļ��ж�ȡURLָ����ͼƬ
	 * @param url ͼƬ��Ӧ����ַ
	 * @return ͼƬ���ļ�
	 */
	public File getFile(String url){
		String filename = String.valueOf(url.hashCode());
		File f = new File(cacheDir, filename);
		return f;
	}
			
	/**
	 * ���ͼƬ�ļ�
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
