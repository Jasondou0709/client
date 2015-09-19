package io.rong.app.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.support.v7.app.ActionBar;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.microdemo.MyApplication;
import com.example.microdemo.util.Str2MD5;
import com.sea_monster.exception.BaseException;
import com.sea_monster.network.AbstractHttpRequest;

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

import io.rong.app.DemoContext;
import io.rong.app.R;
import io.rong.app.RongCloudEvent;
import io.rong.app.adapter.SearchGroupAdapter;
import io.rong.app.database.DBManager;
import io.rong.app.database.UserInfos;
import io.rong.app.database.UserInfosDao;
import io.rong.app.model.ApiResult;
import io.rong.app.model.ClassGroup;
import io.rong.app.model.Friends;
import io.rong.app.model.Groups;
import io.rong.app.model.User;
import io.rong.app.ui.EditTextHolder;
import io.rong.app.ui.LoadingDialog;
import io.rong.app.ui.WinToast;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.UserInfo;

/**
 * Created by Bob on 2015/1/30.
 */
public class LoginActivity extends BaseApiActivity implements View.OnClickListener, Handler.Callback, EditTextHolder.OnEditTextFocusChangeListener {
    private static final String TAG = "LoginActivity";
    /**
     * 用户账户
     */
    private EditText mUserNameEt;
    /**
     * 密码
     */
    private EditText mPassWordEt;
    /**
     * 登录button
     */
    private Button mSignInBt;
    /**
     * 设备id
     */
    private String mDeviceId;
    /**
     * 忘记密码
     */
    private TextView mFogotPassWord;
    /**
     * 注册
     */
    private TextView mRegister;
    /**
     * 输入用户名删除按钮
     */
    private FrameLayout mFrUserNameDelete;
    /**
     * 输入密码删除按钮
     */
    private FrameLayout mFrPasswordDelete;
    /**
     * logo
     */
    private ImageView mLoginImg;
    /**
     * 软键盘的控制
     */
    private InputMethodManager mSoftManager;
    /**
     * 是否展示title
     */
    private RelativeLayout mIsShowTitle;
    /**
     * 左侧title
     */
    private TextView mLeftTitle;
    /**
     * 右侧title
     */
    private TextView mRightTitle;


    private static final int REQUEST_CODE_REGISTER = 200;
    public static final String INTENT_USERNAME = "intent_username";
    public static final String INTENT_IMAIL = "intent_email";
    public static final String INTENT_PASSWORD = "intent_password";
    private static final int HANDLER_LOGIN_SUCCESS = 1;
    private static final int HANDLER_LOGIN_FAILURE = 2;
    private static final int HANDLER_LOGIN_HAS_FOCUS = 3;
    private static final int HANDLER_LOGIN_HAS_NO_FOCUS = 4;


    private LoadingDialog mDialog;
    private AbstractHttpRequest<User> loginHttpRequest;
    private AbstractHttpRequest<User> getTokenHttpRequest;
    private AbstractHttpRequest<Friends> getUserInfoHttpRequest;
    private AbstractHttpRequest<Groups> mGetMyGroupsRequest;

    private Handler mHandler;
    private List<User> mUserList;
    private List<ApiResult> mResultList;
    private ImageView mImgBackgroud;
    EditTextHolder mEditUserNameEt;
    EditTextHolder mEditPassWordEt;

    List<UserInfos> friendsList = new ArrayList<UserInfos>();
    UserInfosDao mUserInfosDao;
    String userName;
    private boolean isFirst = false;
    public static String username;
    public static String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.de_ac_login);
        initView();
        initData();
    }

    protected void initView() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        mSoftManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mUserInfosDao = DBManager.getInstance(LoginActivity.this).getDaoSession().getUserInfosDao();
        mLoginImg = (ImageView) findViewById(R.id.de_login_logo);
        mUserNameEt = (EditText) findViewById(R.id.app_username_et);
        mPassWordEt = (EditText) findViewById(R.id.app_password_et);
        mSignInBt = (Button) findViewById(R.id.app_sign_in_bt);
        mRegister = (TextView) findViewById(R.id.de_login_register);
        mFogotPassWord = (TextView) findViewById(R.id.de_login_forgot);
        mImgBackgroud = (ImageView) findViewById(R.id.de_img_backgroud);
        mFrUserNameDelete = (FrameLayout) findViewById(R.id.fr_username_delete);
        mFrPasswordDelete = (FrameLayout) findViewById(R.id.fr_pass_delete);
        mIsShowTitle = (RelativeLayout) findViewById(R.id.de_merge_rel);
        mLeftTitle = (TextView) findViewById(R.id.de_left);
        mRightTitle = (TextView) findViewById(R.id.de_right);
        mUserList = new ArrayList<User>();
        mResultList = new ArrayList<ApiResult>();

        mSignInBt.setOnClickListener(this);
        mRegister.setOnClickListener(this);
        mLeftTitle.setOnClickListener(this);
        mRightTitle.setOnClickListener(this);
        mHandler = new Handler(this);
        mDialog = new LoadingDialog(this);

        mEditUserNameEt = new EditTextHolder(mUserNameEt, mFrUserNameDelete, null);
        mEditPassWordEt = new EditTextHolder(mPassWordEt, mFrPasswordDelete, null);

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Animation animation = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.translate_anim);
                mImgBackgroud.startAnimation(animation);
            }
        });


    }

    protected void initData() {

        if (DemoContext.getInstance() != null) {
            String email = DemoContext.getInstance().getSharedPreferences().getString(INTENT_USERNAME, "");
            String password = DemoContext.getInstance().getSharedPreferences().getString(INTENT_PASSWORD, "");
            mUserNameEt.setText(email);
            mPassWordEt.setText(password);
        }

        TelephonyManager mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mDeviceId = mTelephonyManager.getDeviceId();

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mUserNameEt.setOnClickListener(LoginActivity.this);
                mPassWordEt.setOnClickListener(LoginActivity.this);
                mEditPassWordEt.setmOnEditTextFocusChangeListener(LoginActivity.this);
                mEditUserNameEt.setmOnEditTextFocusChangeListener(LoginActivity.this);
            }
        }, 200);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.app_sign_in_bt://登录


//                initnn();
                userName = mUserNameEt.getEditableText().toString();
                final String passWord = mPassWordEt.getEditableText().toString();
                String name = null;
                if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(passWord)) {
                    WinToast.toast(this, R.string.login_erro_is_null);
                    return;
                }
                
                username = userName;
                password = passWord;
                
                if (mDialog != null && !mDialog.isShowing()) {
                    mDialog.show();
                }
                //发起登录 http请求 (注：非融云SDK接口，是demo接口)
                if (DemoContext.getInstance() != null) {
                    //如果切换了一个用户，token和 cookie 都需要重新获取
                    if (DemoContext.getInstance() != null) {
                        name = DemoContext.getInstance().getSharedPreferences().getString("DEMO_USERNAME", "DEFAULT");
                    }

                    if (!userName.equals(name)) {

                        //loginHttpRequest = DemoContext.getInstance().getDemoApi().login(userName, passWord, this);
                        {
        					new AsyncTask<Void, Void, String>() {
        	
        						@Override
        						protected String doInBackground(Void... arg0) {
        							HttpClient client = new DefaultHttpClient();
        	
        							HttpPost httpPost = new HttpPost("http://moments.daoapp.io/auth/username_login");
        	
        							List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        					        nameValuePairs.add(new BasicNameValuePair("username", userName));
        					        nameValuePairs.add(new BasicNameValuePair("password", passWord));
        	
        							String result = null;
        							try {
        								httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
        								HttpResponse response = client.execute(httpPost);
        								Log.d("====", "result code = " + response.getStatusLine().getStatusCode());
        								if (response.getStatusLine().getStatusCode() == 200) {
        									result = EntityUtils.toString(response.getEntity());
        									Log.d("====", "result = " + result);
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
        						protected void onPostExecute(String result) {
									if (result != null) {									
										try {
											/** 把json字符串转换成json对象 **/
											JSONObject jsonObject = new JSONObject(result);
											String userId = jsonObject.getString("id");
											String resultCode = jsonObject.getString("result");
											loginSuccess(resultCode, userId);
											
										} catch (JSONException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
										}
	
									} else {
										Toast.makeText(LoginActivity.this,"account or password error", Toast.LENGTH_LONG).show();
									}

							    }
        	                  }.execute();
        				}
                        isFirst = true;
                    } else {
                        isFirst = false;
                        String cookie = DemoContext.getInstance().getSharedPreferences().getString("DEMO_COOKIE", "DEFAULT");
                        String token = DemoContext.getInstance().getSharedPreferences().getString("DEMO_TOKEN", "DEFAULT");
                        if (!token.equals("DEFAULT")) {
                        	Log.d(TAG, "token != DEFAULT");
                            httpGetTokenSuccess(token);
                        } else {
                            //loginHttpRequest = DemoContext.getInstance().getDemoApi().login(userName, passWord, this);
                        	new AsyncTask<Void, Void, String>() {
                            	
        						@Override
        						protected String doInBackground(Void... arg0) {
        							HttpClient client = new DefaultHttpClient();
        	
        							HttpPost httpPost = new HttpPost("http://moments.daoapp.io/auth/username_login");
        	
        							List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        					        nameValuePairs.add(new BasicNameValuePair("username", userName));
        					        nameValuePairs.add(new BasicNameValuePair("password", passWord));
        	
        							String result = null;
        							try {
        								httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
        								HttpResponse response = client.execute(httpPost);
        								Log.d("====", "result code = " + response.getStatusLine().getStatusCode());
        								if (response.getStatusLine().getStatusCode() == 200) {
        									result = EntityUtils.toString(response.getEntity());
        									Log.d("====", "result = " + result);
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
        						protected void onPostExecute(String result) {
									if (result != null) {									
										try {
											/** 把json字符串转换成json对象 **/
											JSONObject jsonObject = new JSONObject(result);
											String userId = jsonObject.getString("id");
											String resultCode = jsonObject.getString("result");
											loginSuccess(resultCode, userId);
											
										} catch (JSONException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
										}
	
									} else {
										Toast.makeText(LoginActivity.this,"account or password error", Toast.LENGTH_LONG).show();
									}

							    }
        	                  }.execute();
                        }

                    }
                }
              
                //pengyou quan login
               new AsyncTask<Void,Void,Integer>(){

        			@Override
                    protected Integer doInBackground(Void... arg0) {
        				HttpClient client = new DefaultHttpClient();

        				HttpGet httpget = new HttpGet("http://moments.daoapp.io/api/v1.0/");
        				// HttpPost httppost = new
        				// HttpPost("http://moments.daoapp.io/api/v1.0/posts/");
        				//httpget.setHeader("user-agent",
        				 //       "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:23.0) Gecko/20100101 Firefox/23.0");
        				String md5 = password;
    					String encoding  = Base64.encodeToString(new String(userName +":"+md5).getBytes(), Base64.NO_WRAP);
        				Log.d("====", "encoding = " +encoding);
        				httpget.setHeader("Authorization", "Basic " + encoding);

        				String result = null;
        				try {
        					HttpResponse response = client.execute(httpget);
        					Log.d("====", "result code = " + response.getStatusLine().getStatusCode());
        					if(response.getStatusLine().getStatusCode() == 200){
        						MyApplication.setBase64Code(encoding);
        						//getMicroList(1, true);
        						//MainActivity.lauchThis(LoginActivity.this);
        					}else{
        						//Toast.makeText(LoginActivity.this, "error password", Toast.LENGTH_LONG).show();
        						return 401;
        					}
        					result = EntityUtils.toString(response.getEntity());
        					Log.d("====", "result = " + result);
        				} catch (ClientProtocolException e) {
        					// TODO Auto-generated catch block
        					e.printStackTrace();
        				} catch (IOException e) {
        					// TODO Auto-generated catch block
        					e.printStackTrace();
        				}
        				return 200;
                    }

        			@Override
                    protected void onPostExecute(Integer result) {
        				if(result != 200)
        					Toast.makeText(LoginActivity.this, "error password", Toast.LENGTH_LONG).show();
                        
                    }
        			
        			
        		}.execute();

                break;
            case R.id.de_left://注册
            case R.id.de_login_register://注册
                Intent intent = new Intent(this, RegisterActivity.class);
                startActivityForResult(intent, REQUEST_CODE_REGISTER);
                break;
            case R.id.de_login_forgot://忘记密码
                WinToast.toast(this, "忘记密码");
                break;
            case R.id.de_right://忘记密码
                Intent intent1 = new Intent(this, RegisterActivity.class);
                startActivityForResult(intent1, REQUEST_CODE_REGISTER);
                break;

            case R.id.app_username_et:
            case R.id.app_password_et:
                Message mess = Message.obtain();
                mess.what = HANDLER_LOGIN_HAS_FOCUS;
                mHandler.sendMessage(mess);
                break;

        }
    }

    private void initnn() {

        try {
            /**
             * IMKit SDK调用第二步
             *
             * 建立与服务器的连接
             *
             * 详见API
             * http://docs.rongcloud.cn/api/android/imkit/index.html
             */

       String token1 = "goRD6aEizPwyMroHrDiNy0mKqBR0xzzHiUbhLnyx3yBK3kaUFLWcHXyretl2aBcdo5RjLCLkI6BGlT5sEFtMyDgGdR7yGDOY0c6/gCy2bR4=";
            RongIM.connect(token1, new RongIMClient.ConnectCallback() {
                        @Override
                        public void onTokenIncorrect() {
                            Log.e("LoginActivity", "---------onTokenIncorrect userId----------:");
                        }

                        @Override
                        public void onSuccess(String userId) {
                            Log.e("LoginActivity", "---------onSuccess userId----------:" + userId);

                            RongCloudEvent.getInstance().setOtherListener();

                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode e) {
                            mHandler.obtainMessage(HANDLER_LOGIN_FAILURE).sendToTarget();
                            Log.e("LoginActivity", "---------onError ----------:" + e);
                        }
                    }
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean handleMessage(Message msg) {

        if (msg.what == HANDLER_LOGIN_FAILURE) {

            if (mDialog != null)
                mDialog.dismiss();
            WinToast.toast(LoginActivity.this, R.string.login_failure);
            startActivity(new Intent(this, MainActivity.class));

            finish();

        } else if (msg.what == HANDLER_LOGIN_SUCCESS) {
            if (mDialog != null)
                mDialog.dismiss();
            WinToast.toast(LoginActivity.this, R.string.login_success);

            startActivity(new Intent(this, MainActivity.class));

            finish();

        } else if (msg.what == HANDLER_LOGIN_HAS_FOCUS) {
            mLoginImg.setVisibility(View.GONE);
            mRegister.setVisibility(View.GONE);
            mFogotPassWord.setVisibility(View.GONE);
            mIsShowTitle.setVisibility(View.VISIBLE);
            mLeftTitle.setText(R.string.app_sign_up);
            mRightTitle.setText(R.string.app_fogot_password);
        } else if (msg.what == HANDLER_LOGIN_HAS_NO_FOCUS) {
            mLoginImg.setVisibility(View.VISIBLE);
            mRegister.setVisibility(View.VISIBLE);
            mFogotPassWord.setVisibility(View.VISIBLE);
            mIsShowTitle.setVisibility(View.GONE);
        }

        return false;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE_REGISTER && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                mUserNameEt.setText(data.getStringExtra(INTENT_USERNAME));
                mPassWordEt.setText(data.getStringExtra(INTENT_PASSWORD));
            }
        }

    }

    private void httpLoginSuccess(User user) {


        if (user.getCode() == 200) {

            getTokenHttpRequest = DemoContext.getInstance().getDemoApi().getToken(this);
        }

    }


    private void httpGetTokenSuccess(String token) {

        try {
            /**
             * IMKit SDK调用第二步
             *
             * 建立与服务器的连接
             *
             * 详见API
             * http://docs.rongcloud.cn/api/android/imkit/index.html
             */
            Log.e("LoginActivity", "---------onSuccess gettoken----------:" + token);
            RongIM.connect(token, new RongIMClient.ConnectCallback() {
                        @Override
                        public void onTokenIncorrect() {
                            Log.e("LoginActivity", "---------onTokenIncorrect userId----------:");
                        }

                        @Override
                        public void onSuccess(String userId) {
                            Log.e("LoginActivity", "---------onSuccess userId----------:" + userId);

                            /*if (isFirst) {

                                //getUserInfoHttpRequest = DemoContext.getInstance().getDemoApi().getFriends(LoginActivity.this);
                            	GetMyFriendTask getMyFriendTask = new GetMyFriendTask();
                            	getMyFriendTask.execute();
                                DemoContext.getInstance().deleteUserInfos();

                            } else {
                                final List<UserInfos> list = mUserInfosDao.loadAll();
                                if (list != null && list.size() > 0) {
                                    mHandler.obtainMessage(HANDLER_LOGIN_SUCCESS).sendToTarget();
                                } else {
                                    //请求网络
                                    //getUserInfoHttpRequest = DemoContext.getInstance().getDemoApi().getFriends(LoginActivity.this);
                                	GetMyFriendTask getMyFriendTask = new GetMyFriendTask();
                                	getMyFriendTask.execute();
                                }
                            }*/
                            GetMyFriendTask getMyFriendTask = new GetMyFriendTask();
                        	getMyFriendTask.execute();
                        	
                            SharedPreferences.Editor edit = DemoContext.getInstance().getSharedPreferences().edit();
                            edit.putString("DEMO_USERID", userId);
                            edit.putString("DEMO_USERNAME", userName);
                            edit.apply();

                            RongCloudEvent.getInstance().setOtherListener();

                        }

                        @Override
                        public void onError(RongIMClient.ErrorCode e) {
                            mHandler.obtainMessage(HANDLER_LOGIN_FAILURE).sendToTarget();
                            Log.e("LoginActivity", "---------onError ----------:" + e);
                        }
                    }
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (DemoContext.getInstance() != null) {
            //mGetMyGroupsRequest = DemoContext.getInstance().getDemoApi().getMyGroups(LoginActivity.this);
        	new AsyncTask<Void, Void, String>() {

    			@Override
    			protected String doInBackground(Void... arg0) {
    				HttpClient client = new DefaultHttpClient();

    				HttpGet httpGet = new HttpGet("http://moments.daoapp.io/api/v1.0/users/getmygroups");

    				String result = null;
    				try {
    					String md5 = password;
    					String encoding  = Base64.encodeToString(new String(userName +":"+md5).getBytes(), Base64.NO_WRAP);
    					Log.d(TAG, "password= " + password + "userName = " + userName + "encoding:" + encoding);
    					httpGet.setHeader("Authorization", "Basic " + encoding);
    					HttpResponse response = client.execute(httpGet);
    					Log.d(TAG, "getmygroups result code = " + response.getStatusLine().getStatusCode());
    					if (response.getStatusLine().getStatusCode() == 200) {
    						result = EntityUtils.toString(response.getEntity());
    						Log.d(TAG, "getmygroups result = " + result);
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
    			protected void onPostExecute(String result) {
    				if (result != null) {									
    					getMyGroupSuccess(result);
    				} else {
    					Toast.makeText(LoginActivity.this,"getmygroup error", Toast.LENGTH_LONG).show();
    				}

    		    }
              }.execute();
        }

        if (DemoContext.getInstance() != null) {
            SharedPreferences.Editor editor = DemoContext.getInstance().getSharedPreferences().edit();
            editor.putString(INTENT_PASSWORD, mPassWordEt.getText().toString());
            editor.putString(INTENT_USERNAME, mUserNameEt.getText().toString());
            editor.apply();
        }
    }

    private class GetMyFriendTask extends AsyncTask<Void, Void, String> {

    	@Override
		protected String doInBackground(Void... arg0) {
			HttpClient client = new DefaultHttpClient();

			HttpGet httpGet = new HttpGet("http://moments.daoapp.io/api/v1.0/users/getmyfriends");

			String result = null;
			try {
				String md5 = password;
				String encoding  = Base64.encodeToString(new String(userName +":"+md5).getBytes(), Base64.NO_WRAP);
				Log.d(TAG, "password= " + password + "userName = " + userName + "encoding:" + encoding);
				httpGet.setHeader("Authorization", "Basic " + encoding);
				HttpResponse response = client.execute(httpGet);
				Log.d(TAG, "getmyfriends result code = " + response.getStatusLine().getStatusCode());
				if (response.getStatusLine().getStatusCode() == 200) {
					result = EntityUtils.toString(response.getEntity());
					Log.d(TAG, "getmyfriends result = " + result);
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
				mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        ArrayList<UserInfo> friendLists = new ArrayList<UserInfo>();
                        try {
        					/** 把json字符串转换成json对象 **/
        					JSONObject jsonObject = new JSONObject(str);
        					String resultCode = jsonObject.getString("status");
        					if (resultCode.equalsIgnoreCase("200")) {
        						
        						JSONArray idJson = jsonObject.getJSONArray("friend");
        						for (int i = 0; i < idJson.length(); i++) {
        							Log.i(TAG, "friend id" + i + ":" + idJson.getString(i));
        							JSONObject jsonObject1 = idJson.getJSONObject(i);
        							String id = jsonObject1.getString("id");
        							String name = jsonObject1.getString("username");
        							String portrait = jsonObject1.getString("portrait");
        							
        							UserInfos userInfos = new UserInfos();
                                    userInfos.setUserid(id);
                                    userInfos.setUsername(name);
                                    userInfos.setStatus("1");
                                    if (portrait != null) {
                                        userInfos.setPortrait(portrait);
                                    }
                                    friendsList.add(userInfos);
        							
        						}
        					}      					
        				} catch (JSONException e1) {
        					// TODO Auto-generated catch block
        					e1.printStackTrace();
        				}

                        UserInfos addFriend = new UserInfos();
                        addFriend.setUsername("新好友消息");
                        addFriend.setUserid("10000");
                        addFriend.setPortrait("test");
                        addFriend.setStatus("0");
                        UserInfos customer = new UserInfos();
                        customer.setUsername("客服");
                        customer.setUserid("kefu114");
                        customer.setPortrait("http://jdd.kefu.rongcloud.cn/image/service_80x80.png");
                        customer.setStatus("0");

                        friendsList.add(customer);
                        friendsList.add(addFriend);

                        if (friendsList != null) {
                            for (UserInfos friend : friendsList) {
                                UserInfos f = new UserInfos();
                                f.setUserid(friend.getUserid());
                                f.setUsername(friend.getUsername());
                                f.setPortrait(friend.getPortrait());
                                f.setStatus(friend.getStatus());
                                mUserInfosDao.insertOrReplace(f);
                            }
                        }
                        mHandler.obtainMessage(HANDLER_LOGIN_SUCCESS).sendToTarget();
                    }

                });
				
			} else {
				mHandler.obtainMessage(HANDLER_LOGIN_SUCCESS).sendToTarget();
			}

	    }
	}

    @Override
    public void onCallApiSuccess(AbstractHttpRequest request, Object obj) {
    	Log.i(TAG, "onCallApiSuccess");
        if (mGetMyGroupsRequest != null && mGetMyGroupsRequest.equals(request)) {
            getMyGroupApiSuccess(obj);
        } else if (loginHttpRequest != null && loginHttpRequest.equals(request)) {
            loginApiSuccess(obj);
        } else if (getTokenHttpRequest != null && getTokenHttpRequest.equals(request)) {
            getTokenApiSuccess(obj);
        } else if (getUserInfoHttpRequest != null && getUserInfoHttpRequest.equals(request)) {
            getFriendsApiSuccess(obj);
        }
    }


    @Override
    public void onCallApiFailure(AbstractHttpRequest request, BaseException e) {
    	Log.i(TAG, "onCallApiFailure" + ":" + e.toString());
        if (loginHttpRequest != null && loginHttpRequest.equals(request)) {
            if (mDialog != null)
                mDialog.dismiss();
        } else if (getTokenHttpRequest != null && getTokenHttpRequest.equals(request)) {
            if (mDialog != null)
                mDialog.dismiss();
        }
    }

    /**
     * 获得好友列表
     *
     * @param obj
     */
    private void getFriendsApiSuccess(Object obj) {

        //获取好友列表接口  返回好友数据  (注：非融云SDK接口，是demo接口)

        if (obj instanceof Friends) {
            final Friends friends = (Friends) obj;
            if (friends.getCode() == 200) {
                mHandler.post(new Runnable() {

                    @Override
                    public void run() {
                        ArrayList<UserInfo> friendLists = new ArrayList<UserInfo>();

                        for (int i = 0; i < friends.getResult().size(); i++) {
                            UserInfos userInfos = new UserInfos();

                            userInfos.setUserid(friends.getResult().get(i).getId());
                            userInfos.setUsername(friends.getResult().get(i).getUsername());
                            userInfos.setStatus("1");
                            if (friends.getResult().get(i).getPortrait() != null)
                                userInfos.setPortrait(friends.getResult().get(i).getPortrait());
                            friendsList.add(userInfos);
                        }

                        UserInfos addFriend = new UserInfos();
                        addFriend.setUsername("新好友消息");
                        addFriend.setUserid("10000");
                        addFriend.setPortrait("test");
                        addFriend.setStatus("0");
                        UserInfos customer = new UserInfos();
                        customer.setUsername("客服");
                        customer.setUserid("kefu114");
                        customer.setPortrait("http://jdd.kefu.rongcloud.cn/image/service_80x80.png");
                        customer.setStatus("0");

                        friendsList.add(customer);
                        friendsList.add(addFriend);

                        if (friendsList != null) {
                            for (UserInfos friend : friendsList) {
                                UserInfos f = new UserInfos();
                                f.setUserid(friend.getUserid());
                                f.setUsername(friend.getUsername());
                                f.setPortrait(friend.getPortrait());
                                f.setStatus(friend.getStatus());
                                mUserInfosDao.insertOrReplace(f);
                            }
                        }
                        mHandler.obtainMessage(HANDLER_LOGIN_SUCCESS).sendToTarget();
                    }

                });
            }
        }
    }

    private void getFriendTemp() {
    	mHandler.post(new Runnable() {

            @Override
            public void run() {
                ArrayList<UserInfo> friendLists = new ArrayList<UserInfo>();

                UserInfos addFriend = new UserInfos();
                addFriend.setUsername("新好友消息");
                addFriend.setUserid("10000");
                addFriend.setPortrait("test");
                addFriend.setStatus("0");
                UserInfos customer = new UserInfos();
                customer.setUsername("客服");
                customer.setUserid("kefu114");
                customer.setPortrait("http://jdd.kefu.rongcloud.cn/image/service_80x80.png");
                customer.setStatus("0");

                friendsList.add(customer);
                friendsList.add(addFriend);

                if (friendsList != null) {
                    for (UserInfos friend : friendsList) {
                        UserInfos f = new UserInfos();
                        f.setUserid(friend.getUserid());
                        f.setUsername(friend.getUsername());
                        f.setPortrait(friend.getPortrait());
                        f.setStatus(friend.getStatus());
                        mUserInfosDao.insertOrReplace(f);
                    }
                }
                mHandler.obtainMessage(HANDLER_LOGIN_SUCCESS).sendToTarget();
            }

        });
    }
    
    private void getMyGroupSuccess(String str) {
		if (str != null) {
			List<Group> grouplist = new ArrayList<>();
			ArrayList<ClassGroup> classGrouplist = new ArrayList<>();
			HashMap<String, Group> groupM = new HashMap<String, Group>();
			try {
				/** 把json字符串转换成json对象 **/
				JSONObject jsonObject = new JSONObject(str);
				String resultCode = jsonObject.getString("status");
				if (resultCode.equalsIgnoreCase("200")) {
					
					JSONArray idJson = jsonObject.getJSONArray("groups");
					for (int i = 0; i < idJson.length(); i++) {
						Log.i(TAG, "class id" + i + ":" + idJson.getString(i));
						JSONObject jsonObject1 = idJson.getJSONObject(i);
						String id = jsonObject1.getString("id");
						String name = jsonObject1.getString("name");
						String portrait = jsonObject1.getString("portrait");
						String introduce = jsonObject1.getString("introduce");
						String number = jsonObject1.getString("number");
						String maxNumber = jsonObject1.getString("max_number");
						grouplist.add(new Group(id, name, Uri.parse(portrait)));
						classGrouplist.add(new ClassGroup(id, name, portrait, introduce, number, maxNumber));
						groupM.put(id, new Group(id, name, Uri.parse(portrait)));
					}
				} else {
					return;
				}
			
				
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
				if (DemoContext.getInstance() != null) {
					DemoContext.getInstance().setGroupMap(groupM);
					DemoContext.getInstance().setClassGroups(classGrouplist);
				}

				if (grouplist.size() > 0)
					RongIM.getInstance().getRongIMClient().syncGroup(grouplist, new RongIMClient.OperationCallback() {
										@Override
										public void onSuccess() {
											Log.e(TAG, "---syncGroup-onSuccess---");
										}

										@Override
										public void onError(RongIMClient.ErrorCode errorCode) {
											Log.e(TAG, "---syncGroup-onError---");
										}
									});
			
		}
    
    }
    
    private void getMyGroupApiSuccess(Object obj) {
        if (obj instanceof Groups) {
            final Groups groups = (Groups) obj;

            if (groups.getCode() == 200) {
                List<Group> grouplist = new ArrayList<>();
                if (groups.getResult() != null) {
                    for (int i = 0; i < groups.getResult().size(); i++) {

                        String id = groups.getResult().get(i).getId();
                        String name = groups.getResult().get(i).getName();
                        if (groups.getResult().get(i).getPortrait() != null) {
                            Uri uri = Uri.parse(groups.getResult().get(i).getPortrait());
                            grouplist.add(new Group(id, name, uri));
                        } else {
                            grouplist.add(new Group(id, name, null));
                        }
                    }
                    HashMap<String, Group> groupM = new HashMap<String, Group>();
                    for (int i = 0; i < grouplist.size(); i++) {
                        groupM.put(groups.getResult().get(i).getId(), grouplist.get(i));
                        Log.e("login", "------get Group id---------" + groups.getResult().get(i).getId());
                    }

                    if (DemoContext.getInstance() != null)
                        DemoContext.getInstance().setGroupMap(groupM);

                    if (grouplist.size() > 0)
                        RongIM.getInstance().getRongIMClient().syncGroup(grouplist, new RongIMClient.OperationCallback() {
                            @Override
                            public void onSuccess() {
                                Log.e(TAG, "---syncGroup-onSuccess---");
                            }

                            @Override
                            public void onError(RongIMClient.ErrorCode errorCode) {
                                Log.e(TAG, "---syncGroup-onError---");
                            }
                        });
                }
            } else {
//                    WinToast.toast(this, groups.getCode());
            }
        }
    }

    private void getTokenApiSuccess(Object obj) {

        if (obj instanceof User) {
            final User user = (User) obj;
            if (user.getCode() == 200) {

                httpGetTokenSuccess(user.getResult().getToken());

                SharedPreferences.Editor edit = DemoContext.getInstance().getSharedPreferences().edit();
                edit.putString("DEMO_TOKEN", user.getResult().getToken());
                edit.putBoolean("DEMO_ISFIRST", false);
                edit.apply();
                Log.e(TAG, "------getTokenHttpRequest -success--" + user.getResult().getToken());
            } else if (user.getCode() == 110) {
                WinToast.toast(LoginActivity.this, "请先登陆");
            } else if (user.getCode() == 111) {
                WinToast.toast(LoginActivity.this, "cookie 为空");
            }
        }
    }

    private void loginApiSuccess(Object obj) {

        if (obj instanceof User) {

            final User user = (User) obj;
            Log.i("LoginActivity", "loginApiSuccess:user.getCode():" + user.getCode());
            if (user.getCode() == 200) {
                if (DemoContext.getInstance() != null && user.getResult() != null) {
                    SharedPreferences.Editor edit = DemoContext.getInstance().getSharedPreferences().edit();
                    edit.putString("DEMO_USER_ID", user.getResult().getId());
                    edit.putString("DEMO_USER_NAME", user.getResult().getUsername());
                    edit.putString("DEMO_USER_PORTRAIT", user.getResult().getPortrait());
                    edit.apply();
                    Log.e(TAG, "-------login success------");

                    httpLoginSuccess(user);
                }
            } else if (user.getCode() == 103) {

                if (mDialog != null)
                    mDialog.dismiss();

                WinToast.toast(LoginActivity.this, "密码错误");
            } else if (user.getCode() == 104) {

                if (mDialog != null)
                    mDialog.dismiss();

                WinToast.toast(LoginActivity.this, "账号错误");
            }
        }
    }
    
    private void loginSuccess(String resultCode, String userId) {
    	if (resultCode.equalsIgnoreCase("200")) {
    		SharedPreferences.Editor edit = DemoContext.getInstance().getSharedPreferences().edit();
            edit.putString("DEMO_USER_ID", userId);
            edit.putString("DEMO_USER_NAME", userName);
            edit.apply();
            Log.e(TAG, "-------login success------");
            getHttpToken();
    	} else {
    		WinToast.toast(LoginActivity.this, "账号或密码错误");
    	}
    }

    private void getHttpToken() {

		new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... arg0) {
				HttpClient client = new DefaultHttpClient();

				HttpGet httpGet = new HttpGet("http://moments.daoapp.io/auth/token");

				String result = null;
				try {
					String md5 = password;
					String encoding  = Base64.encodeToString(new String(userName +":"+md5).getBytes(), Base64.NO_WRAP);
					Log.d("====", "password= " + password + "userName = " + userName + "encoding:" + encoding);
					httpGet.setHeader("Authorization", "Basic " + encoding);
					HttpResponse response = client.execute(httpGet);
					Log.d("====", "result code = " + response.getStatusLine().getStatusCode());
					if (response.getStatusLine().getStatusCode() == 200) {
						result = EntityUtils.toString(response.getEntity());
						Log.d("====", "result = " + result);
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
			protected void onPostExecute(String result) {
				if (result != null) {									
					if (result.equalsIgnoreCase("-1")) {
						Toast.makeText(LoginActivity.this,"Token error", Toast.LENGTH_LONG).show();
					} else {
						httpGetTokenSuccess(result);

		                SharedPreferences.Editor edit = DemoContext.getInstance().getSharedPreferences().edit();
		                edit.putString("DEMO_TOKEN", result);
		                edit.putBoolean("DEMO_ISFIRST", false);
		                edit.apply();
		                Log.e(TAG, "------getTokenHttpRequest -success--" + result);
					}

				} else {
					Toast.makeText(LoginActivity.this,"Token error", Toast.LENGTH_LONG).show();
				}

		    }
          }.execute();
	
    }
    
    @Override
    public void onEditTextFocusChange(View v, boolean hasFocus) {
        Message mess = Message.obtain();
        switch (v.getId()) {
            case R.id.app_username_et:
            case R.id.app_password_et:
                if (hasFocus) {
                    mess.what = HANDLER_LOGIN_HAS_FOCUS;
                }
                mHandler.sendMessage(mess);
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
                mSoftManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                Message mess = Message.obtain();
                mess.what = HANDLER_LOGIN_HAS_NO_FOCUS;
                mHandler.sendMessage(mess);
            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        event.getKeyCode();
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_ESCAPE:
                Message mess = Message.obtain();
                mess.what = HANDLER_LOGIN_HAS_NO_FOCUS;
                mHandler.sendMessage(mess);
                break;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

    protected void onPause() {
        super.onPause();
        if (mSoftManager == null) {
            mSoftManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        if (getCurrentFocus() != null) {
            mSoftManager.hideSoftInputFromWindow(getCurrentFocus()
                    .getWindowToken(), 0);// 隐藏软键盘
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (KeyEvent.KEYCODE_BACK == event.getKeyCode()) {


            final AlertDialog.Builder alterDialog = new AlertDialog.Builder(this);
            alterDialog.setMessage("确定退出应用？");
            alterDialog.setCancelable(true);

            alterDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (RongIM.getInstance() == null)
                        RongIM.getInstance().logout();

                    Process.killProcess(Process.myPid());
                }
            });
            alterDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            alterDialog.show();
        }

        return false;
    }

   
}
