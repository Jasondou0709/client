package io.rong.app;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.rong.app.activity.LoginActivity;
import io.rong.app.activity.MainActivity;
import io.rong.app.activity.SOSOLocationActivity;
import io.rong.app.activity.SearchFriendActivity;
import io.rong.app.adapter.SearchFriendAdapter;
import io.rong.app.common.DemoApi;
import io.rong.app.database.DBManager;
import io.rong.app.database.UserInfos;
import io.rong.app.database.UserInfosDao;
import io.rong.app.model.ClassGroup;
import io.rong.app.model.User1;
import io.rong.app.provider.RequestProvider;
import io.rong.imkit.RongIM;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.UserInfo;
import io.rong.message.ContactNotificationMessage;

/**
 * Created by Bob on 2015/1/30.
 */
public class DemoContext  {

    private static DemoContext mDemoContext;
    public Context mContext;
    private DemoApi mDemoApi;
    private HashMap<String, Group> groupMap;
    private ArrayList<UserInfo> mUserInfos;
    private ArrayList<UserInfo> mFriendInfos;
    private ArrayList<ClassGroup> mClassGroups;
    private SharedPreferences mPreferences;
    private RongIM.LocationProvider.LocationCallback mLastLocationCallback;
    private UserInfosDao mUserInfosDao;
    private String requestUserName = "";
    private String classId;
    private String portrait;

    public static DemoContext getInstance() {

        if (mDemoContext == null) {
            mDemoContext = new DemoContext();
        }
        return mDemoContext;
    }

    private DemoContext() {
    }

    private DemoContext(Context context) {
        mContext = context;
        mDemoContext = this;
        //http初始化 用于登录、注册使用
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        RongIM.setLocationProvider(new LocationProvider());

        mDemoApi = new DemoApi(context);

        mUserInfosDao = DBManager.getInstance(mContext).getDaoSession().getUserInfosDao();
    }

    public static void init(Context context) {
        mDemoContext = new DemoContext(context);
    }

    public SharedPreferences getSharedPreferences() {
        return mPreferences;
    }

    public void setSharedPreferences(SharedPreferences sharedPreferences) {
        this.mPreferences = sharedPreferences;
    }

    public void setGroupMap(HashMap<String, Group> groupMap) {
        this.groupMap = groupMap;
    }

    public HashMap<String, Group> getGroupMap() {
        return groupMap;
    }

    public boolean hasGroup(String groupId) {
    	if (groupMap != null) {
    		return groupMap.containsKey(groupId);
    	}
    	return false;
    }
    
    public Group getGroupById(String groupId) {
    	if (groupMap != null) {
    		if (groupMap.containsKey(groupId)) {
    			return groupMap.get(groupId);
    		}
    	}
    	return null;
    }
    
    public void setClassGroups(ArrayList<ClassGroup> classGroup) {
        this.mClassGroups = classGroup;
    }

    public ArrayList<ClassGroup> getClassGroups() {
        return mClassGroups;
    }
    
    public ArrayList<UserInfo> getUserInfos() {
        return mUserInfos;
    }

    public void setUserInfos(ArrayList<UserInfo> userInfos) {
        mUserInfos = userInfos;
    }

    public DemoApi getDemoApi() {
        return mDemoApi;
    }

    /**
     * 删除 userinfos 表
     */
    public void deleteUserInfos() {

        mUserInfosDao.deleteAll();
    }

    /**
     * 更新 好友信息
     *
     * @param targetid
     * @param status
     */
    public void updateUserInfos(String targetid, String status) {

        UserInfos userInfos = mUserInfosDao.queryBuilder().where(UserInfosDao.Properties.Userid.eq(targetid)).unique();
        userInfos.setStatus(status);
        userInfos.setUsername(userInfos.getUsername());
        userInfos.setPortrait(userInfos.getPortrait());
        userInfos.setUserid(userInfos.getUserid());

        mUserInfosDao.update(userInfos);

    }

    /**
     * 向数据库插入数据
     *
     * @param info   用户信息
     * @param status 状态
     */
    public void insertOrReplaceUserInfo(UserInfo info, String status) {

        UserInfos userInfos = new UserInfos();
        userInfos.setStatus(status);
        userInfos.setUsername(info.getName());
        userInfos.setPortrait(String.valueOf(info.getPortraitUri()));
        userInfos.setUserid(info.getUserId());
        mUserInfosDao.insertOrReplace(userInfos);
    }

    public void insertOrReplaceUserInfoList(ArrayList<UserInfo> list, String status) {

        List<UserInfos> userInfos = new ArrayList<>();


    }

    /**
     * 通过userid 查找 UserInfos,判断是否为好友，查找的是本地的数据库
     *
     * @param userId
     * @return
     */
    public boolean searcheUserInfosById(String userId) {

        UserInfos userInfos = mUserInfosDao.queryBuilder().where(UserInfosDao.Properties.Userid.eq(userId)).unique();

        if (userInfos == null)
            return false;

        if (userInfos.getStatus().equals("1") || userInfos.getStatus().equals("3")|| userInfos.getStatus().equals("5")) {
            return true;
        }
        return false;
    }

    /**
     * 通过userid 查找 UserInfo，查找的是本地的数据库
     *
     * @param userId
     * @return
     */
    public UserInfo getUserInfoById(String userId) {

        UserInfos userInfos = mUserInfosDao.queryBuilder().where(UserInfosDao.Properties.Userid.eq(userId)).unique();
        if (userInfos == null && DemoContext.getInstance() != null) {
            return null;
        }

        return new UserInfo(userInfos.getUserid(), userInfos.getUsername(), Uri.parse(userInfos.getPortrait()));
    }

    public boolean hasUserId(String userId) {

        UserInfos userInfos = mUserInfosDao.queryBuilder().where(UserInfosDao.Properties.Userid.eq(userId)).unique();

        if (userInfos == null && userInfos.getUserid() == null) {
            return false;
        }

        return true;
    }
    /**
     * 获得好友列表
     *
     * @return
     */
    public ArrayList<UserInfo> getFriendList() {
        List<UserInfo> userInfoList = new ArrayList<>();

        List<UserInfos> userInfos = mUserInfosDao.queryBuilder().where(UserInfosDao.Properties.Status.eq("1")).list();

        if (userInfos == null)
            return null;

        for (int i = 0; i < userInfos.size(); i++) {
            UserInfo userInfo = new UserInfo(userInfos.get(i).getUserid(), userInfos.get(i).getUsername(), Uri.parse(userInfos.get(i).getPortrait()));

            userInfoList.add(userInfo);
        }
        return (ArrayList) userInfoList;
    }

    /**
     * 根据userids获得好友列表
     *
     * @return
     */
    public ArrayList<UserInfo> getUserInfoList(String[] userIds) {

        List<UserInfo> userInfoList = new ArrayList<>();
        List<UserInfos> userInfosList = new ArrayList<>();
        UserInfo userInfo;
        UserInfos userInfos;

        for (int i = 0; i < userIds.length; i++) {
            userInfos = mUserInfosDao.queryBuilder().where(UserInfosDao.Properties.Userid.eq(userIds[i])).unique();
            userInfosList.add(userInfos);
            if (mUserInfosDao.getKey(userInfosList.get(i)) != null) {
                userInfo = new UserInfo(userInfosList.get(i).getUserid(), userInfosList.get(i).getUsername(), Uri.parse(userInfosList.get(i).getPortrait()));
                userInfoList.add(userInfo);
            }
        }
        if (userInfosList == null)
            return null;


        return (ArrayList) userInfoList;
    }

    /**
     * 通过groupid 获得groupname
     *
     * @param groupid
     * @return
     */
    public String getGroupNameById(String groupid) {
        Group groupReturn = null;
        if (!TextUtils.isEmpty(groupid) && groupMap != null) {

            if (groupMap.containsKey(groupid)) {
                groupReturn = groupMap.get(groupid);
            } else
                return null;

        }
        if (groupReturn != null)
            return groupReturn.getName();
        else
            return null;
    }


    public RongIM.LocationProvider.LocationCallback getLastLocationCallback() {
        return mLastLocationCallback;
    }

    public void setLastLocationCallback(RongIM.LocationProvider.LocationCallback lastLocationCallback) {
        this.mLastLocationCallback = lastLocationCallback;
    }


    class LocationProvider implements RongIM.LocationProvider {

        /**
         * 位置信息提供者:LocationProvider 的回调方法，打开第三方地图页面。
         *
         * @param context  上下文
         * @param callback 回调
         */
        @Override
        public void onStartLocation(Context context, RongIM.LocationProvider.LocationCallback callback) {
            /**
             * demo 代码  开发者需替换成自己的代码。
             */
            DemoContext.getInstance().setLastLocationCallback(callback);
            Intent intent = new Intent(context, SOSOLocationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);//SOSO地图
        }
    }
    
    public void insertRequest(final ContactNotificationMessage contactContentMessage) {
    	Log.d("DemoContext", "insertRequest:" + contactContentMessage.getMessage());
    	if (contactContentMessage.getMessage().equalsIgnoreCase("send request")) {
        	final String classId = contactContentMessage.getExtra();
        	Log.d("DemoContext", "insertRequest:classId:" + classId);
        	//add the request by database
        	{
        		if (contactContentMessage.getSourceUserId() != null) {
        			new AsyncTask<String, Void, String> () {


        	        	@Override
        	    		protected String doInBackground(String... arg0) {
        	    			HttpClient client = new DefaultHttpClient();

        	    			//HttpGet httpGet = new HttpGet("http://moments.daoapp.io/api/v1.0/users/" + userId);
        	    			HttpGet httpGet = new HttpGet(arg0[0]);
        	    			
        	    			String result = null;
        	    			try {
        	    				String md5 = LoginActivity.password;
        	    				String encoding  = Base64.encodeToString(new String(LoginActivity.username +":"+md5).getBytes(), Base64.NO_WRAP);
        	    				Log.d("DemoContext", "password= " + md5 + "userName = " + LoginActivity.username + "encoding:" + encoding);
        	    				httpGet.setHeader("Authorization", "Basic " + encoding);
        	    				HttpResponse response = client.execute(httpGet);
        	    				Log.d("DemoContext", "searchusername result code = " + response.getStatusLine().getStatusCode());
        	    				if (response.getStatusLine().getStatusCode() == 200) {
        	    					result = EntityUtils.toString(response.getEntity());
        	    					Log.d("DemoContext", "searchusername result = " + result);
        	    					return result;
        	    				} else {
        	    					return null;
        	    				}

        	    			} catch (ClientProtocolException e) {
        	    				// TODO Auto-generated catch block
        	    				e.printStackTrace();
        	    			} catch (IOException e) {
        	    				// TODO Auto-generated catch block
        	    				e.printStackTrace();
        	    			}
        	    			return null;
        	    		}

        	    		@Override
        	    		protected void onPostExecute(String str) {
        	    			if (str != null) {    	            
        	    	            try {
        	    					/** 把json字符串转换成json对象 **/
        	    					JSONObject jsonObject = new JSONObject(str);
        	    					String name = jsonObject.getString("username");
        	    					String portrait = jsonObject.getString("portrait");
        	    					ContentValues values = new ContentValues();
        	            			values.put(RequestProvider.RequestConstants.USERID, contactContentMessage.getSourceUserId());
        	            			values.put(RequestProvider.RequestConstants.USERNAME, name);
        	            			values.put(RequestProvider.RequestConstants.PORTRAIT, portrait);
        	            			values.put(RequestProvider.RequestConstants.STATUS, 0);
        	            			if(classId != null && !classId.equalsIgnoreCase("")) {
        	            				values.put(RequestProvider.RequestConstants.CLASSID, classId);      				
        	            				values.put(RequestProvider.RequestConstants.CLASSNAME, getGroupNameById(classId));
        	            				values.put(RequestProvider.RequestConstants.ISCLASS, 1);
        	            			} else {
        	            				values.put(RequestProvider.RequestConstants.ISCLASS, 0);
        	            			}
        	            			mContext.getContentResolver().insert(RequestProvider.CONTENT_URI, values);
        	    				} catch (JSONException e1) {
        	    					// TODO Auto-generated catch block
        	    					e1.printStackTrace();
        	    				}			
        	    			}  
        	    	    }
        	    	
        	    }.execute("http://moments.daoapp.io/api/v1.0/users/" + contactContentMessage.getSourceUserId());
        			
        			/*ContentValues values = new ContentValues();
        			values.put(RequestProvider.RequestConstants.USERID, contactContentMessage.getSourceUserId());
        			values.put(RequestProvider.RequestConstants.USERNAME, getUserNameById(contactContentMessage.getSourceUserId()));
        			values.put(RequestProvider.RequestConstants.PORTRAIT, "");
        			values.put(RequestProvider.RequestConstants.STATUS, 0);
        			if(classId != null && !classId.equalsIgnoreCase("")) {
        				values.put(RequestProvider.RequestConstants.CLASSID, classId);      				
        				values.put(RequestProvider.RequestConstants.CLASSNAME, getGroupNameById(classId));
        				values.put(RequestProvider.RequestConstants.ISCLASS, 1);
        			} else {
        				values.put(RequestProvider.RequestConstants.ISCLASS, 0);
        			}
        			mContext.getContentResolver().insert(RequestProvider.CONTENT_URI, values);*/
        		}
        	}
    	}
    	
    	//已经被加为好友，需要更新朋友列表
    	if (contactContentMessage.getMessage().equalsIgnoreCase("confirm")) {
	    	Intent in = new Intent();
	        in.setAction(MainActivity.ACTION_DMEO_AGREE_REQUEST);
	        mContext.sendBroadcast(in);
    	}
    }
    
    public String getUserNameById(String userId) {
    	SearchUserNameTask task = new SearchUserNameTask();
    	task.execute("http://moments.daoapp.io/api/v1.0/users/" + userId);
    	return requestUserName;
    }
    
    private class SearchUserNameTask extends AsyncTask<String, Void, String> {


        	@Override
    		protected String doInBackground(String... arg0) {
    			HttpClient client = new DefaultHttpClient();

    			//HttpGet httpGet = new HttpGet("http://moments.daoapp.io/api/v1.0/users/" + userId);
    			HttpGet httpGet = new HttpGet(arg0[0]);
    			
    			String result = null;
    			try {
    				String md5 = LoginActivity.password;
    				String encoding  = Base64.encodeToString(new String(LoginActivity.username +":"+md5).getBytes(), Base64.NO_WRAP);
    				Log.d("DemoContext", "password= " + md5 + "userName = " + LoginActivity.username + "encoding:" + encoding);
    				httpGet.setHeader("Authorization", "Basic " + encoding);
    				HttpResponse response = client.execute(httpGet);
    				Log.d("DemoContext", "searchusername result code = " + response.getStatusLine().getStatusCode());
    				if (response.getStatusLine().getStatusCode() == 200) {
    					result = EntityUtils.toString(response.getEntity());
    					Log.d("DemoContext", "searchusername result = " + result);
    					return result;
    				} else {
    					return null;
    				}

    			} catch (ClientProtocolException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    			return null;
    		}

    		@Override
    		protected void onPostExecute(final String str) {
    			if (str != null) {    	            
    	            try {
    					/** 把json字符串转换成json对象 **/
    					JSONObject jsonObject = new JSONObject(str);
    					requestUserName = jsonObject.getString("username");    										
    				} catch (JSONException e1) {
    					// TODO Auto-generated catch block
    					e1.printStackTrace();
    				}			
    			}  
    	    }
    	
    }

   public void setClassId(String id) {
	   classId = id;
   }
   
   public String getClassId() {
	   return classId;
   }
    
   public void setPortrait(String portRait) {
	   portrait = portRait;
   }
   
   public String getPortrait() {
	   return portrait;
   }

}
