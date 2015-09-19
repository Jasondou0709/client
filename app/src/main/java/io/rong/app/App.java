package io.rong.app;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import io.rong.app.message.DeAgreedFriendRequestMessage;
import io.rong.app.message.DeContactNotificationMessageProvider;
import io.rong.app.photo.PhotoCollectionsProvider;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.widget.provider.CameraInputProvider;
import io.rong.imkit.widget.provider.InputProvider;
import io.rong.imkit.widget.provider.LocationInputProvider;
import io.rong.imkit.widget.provider.VoIPInputProvider;
import io.rong.imlib.model.Conversation;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Created by Bob on 2015/1/30.
 */
public class App extends Application {

    @Override
    public void onCreate() {

        super.onCreate();
        /**
         * IMKit SDK调用第一步 初始化
         * context上下文
         */
        RongIM.init(this);
        
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

        //注册消息类型的时候判断当前的进程是否在主进程
        if ("io.rong.app".equals(getCurProcessName(getApplicationContext()))) {
            /**
             * 融云SDK事件监听处理
             */
            RongCloudEvent.init(this);

            DemoContext.init(this);
            try {
                //注册自定义消息,注册完消息后可以收到自定义消息
                RongIM.registerMessageType(DeAgreedFriendRequestMessage.class);
                //注册消息模板，注册完消息模板可以在会话列表上展示
                RongIM.registerMessageTemplate(new DeContactNotificationMessageProvider());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //Crash 日志
        Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler(this));


    }

    /**
     * 获得当前进程号
     *
     * @param context
     * @return
     */
    public static String getCurProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : activityManager
                .getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return null;
    }

}
