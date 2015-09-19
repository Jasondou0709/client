package com.example.microdemo;

import android.app.Application;
import android.util.Base64;

import io.rong.app.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class MyApplication extends Application {

	static String encoding = null;

	@Override
	public void onCreate() {
		super.onCreate();
		DisplayImageOptions defaultOptions = new DisplayImageOptions
				.Builder()
				.showImageForEmptyUri(R.drawable.empty_photo) 
				.showImageOnFail(R.drawable.empty_photo) 
				.cacheInMemory(true)
				.cacheOnDisc(true)
				.build();
		ImageLoaderConfiguration config = new ImageLoaderConfiguration
				.Builder(getApplicationContext())
				.defaultDisplayImageOptions(defaultOptions)
				.discCacheSize(50 * 1024 * 1024)//
				.discCacheFileCount(100)//缓存一百张图片
				.writeDebugLogs()
				.build();
		ImageLoader.getInstance().init(config);
	}
	
	public static String getBase64Code(){
		return encoding;
	}
	public static void  setBase64Code(String username,String password){
		encoding = Base64.encodeToString(new String(
				username+":"+password).getBytes(),
		        Base64.NO_WRAP);
		
	}	
	public static void  setBase64Code(String encode){
		encoding = encode;
		
	}
}
