package io.rong.app.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.sea_monster.exception.BaseException;
import com.sea_monster.network.AbstractHttpRequest;
import com.sea_monster.network.ApiCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import io.rong.app.DemoContext;
import io.rong.app.R;
import io.rong.app.adapter.NewFriendListAdapter;
import io.rong.app.message.DeAgreedFriendRequestMessage;
import io.rong.app.model.ApiResult;
import io.rong.app.model.Friends;
import io.rong.app.model.RequestInfo;
import io.rong.app.model.Status;
import io.rong.app.provider.RequestProvider;
import io.rong.app.ui.LoadingDialog;
import io.rong.app.ui.WinToast;
import io.rong.app.utils.Constants;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.UserInfo;

/**
 * Created by Bob on 2015/3/26.
 */
public class NewFriendListActivity extends BaseApiActivity implements Handler.Callback {

    private static final String TAG = NewFriendListActivity.class.getSimpleName();
    private AbstractHttpRequest<Friends> getFriendHttpRequest;
    private AbstractHttpRequest<Status> mRequestFriendHttpRequest;

    private ListView mNewFriendList;
    private NewFriendListAdapter adapter;
    private List<RequestInfo> mResultList;
    private LoadingDialog mDialog;
    private Handler mHandler = new Handler();
    private String mUserId;
    private String mClassId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.de_ac_new_friendlist);
        initView();

    }

    protected void initView() {
        getSupportActionBar().setTitle(R.string.de_new_friends);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.de_actionbar_back);
        mNewFriendList = (ListView) findViewById(R.id.de_new_friend_list);
        //mDialog = new LoadingDialog(this);
        mResultList = new ArrayList<>();
        Cursor cur = getContentResolver().query(RequestProvider.CONTENT_URI, null, null, null, null);
		if (cur != null) {
			while(cur.moveToNext()) {
				String userId = cur.getString(cur.getColumnIndex(RequestProvider.RequestConstants.USERID));
				String userName = cur.getString(cur.getColumnIndex(RequestProvider.RequestConstants.USERNAME));
				String portrait = cur.getString(cur.getColumnIndex(RequestProvider.RequestConstants.PORTRAIT));
				int status = cur.getInt(cur.getColumnIndex(RequestProvider.RequestConstants.STATUS));
				String classId = cur.getString(cur.getColumnIndex(RequestProvider.RequestConstants.CLASSID));
				String className = cur.getString(cur.getColumnIndex(RequestProvider.RequestConstants.CLASSNAME));
				mResultList.add(new RequestInfo(userId, userName, portrait, status, classId, className));
			}
			cur.close();
		}
		adapter = new NewFriendListAdapter(mResultList, NewFriendListActivity.this);
        mNewFriendList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        adapter.setOnItemButtonClick(mOnItemButtonClick);

        /*if (DemoContext.getInstance() != null) {
            getFriendHttpRequest = DemoContext.getInstance().getDemoApi().getNewFriendlist(this);
            if (mDialog != null && !mDialog.isShowing()) {
                mDialog.show();
            }
        }*/
        /*Intent in = new Intent();
        in.setAction(MainActivity.ACTION_DMEO_RECEIVE_MESSAGE);
        in.putExtra("has_message", false);
        sendBroadcast(in);*/
    }
    
    public void searchRequest() {
		new Thread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						if(mResultList != null) {
							mResultList.clear();
						}
						Cursor cur = getContentResolver().query(RequestProvider.CONTENT_URI, null, null, null, null);
						if (cur != null) {
							while(cur.moveToNext()) {
								String userId = cur.getString(cur.getColumnIndex(RequestProvider.RequestConstants.USERID));
								String userName = cur.getString(cur.getColumnIndex(RequestProvider.RequestConstants.USERNAME));
								String portrait = cur.getString(cur.getColumnIndex(RequestProvider.RequestConstants.PORTRAIT));
								int status = cur.getInt(cur.getColumnIndex(RequestProvider.RequestConstants.STATUS));
								String classId = cur.getString(cur.getColumnIndex(RequestProvider.RequestConstants.CLASSID));
								String className = cur.getString(cur.getColumnIndex(RequestProvider.RequestConstants.CLASSNAME));
								mResultList.add(new RequestInfo(userId, userName, portrait, status, classId, className));
							}
							cur.close();
						}
					}
				}).start();
    }
    
    private void updateAdapter() {
        if (adapter != null) {
            adapter = null;
        }
        adapter = new NewFriendListAdapter(mResultList, NewFriendListActivity.this);
        mNewFriendList.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        adapter.setOnItemButtonClick(mOnItemButtonClick);

    }

    @Override
    public void onCallApiSuccess(AbstractHttpRequest request, Object obj) {
        if (getFriendHttpRequest!= null && getFriendHttpRequest == request) {
            if (mDialog != null)
                mDialog.dismiss();

            if (obj instanceof Friends) {
                final Friends friends = (Friends) obj;
                if (friends.getCode() == 200) {
                    if (friends.getResult().size() != 0) {
                        for (int i = 0; i < friends.getResult().size(); i++) {
       //                     mResultList.add(friends.getResult().get(i));
                        }
                        adapter = new NewFriendListAdapter(mResultList, NewFriendListActivity.this);
                        mNewFriendList.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        adapter.setOnItemButtonClick(mOnItemButtonClick);
                    }
                }
            }
        } else if (mRequestFriendHttpRequest == request) {
        }
    }

    NewFriendListAdapter.OnItemButtonClick mOnItemButtonClick = new NewFriendListAdapter.OnItemButtonClick() {

        @Override
        public boolean onButtonClick(final int position, View view, int status) {
            switch (status) {
                case 0://请求被添加
                	    mUserId = mResultList.get(position).getId();
                	    mClassId = mResultList.get(position).getClassId();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                            	if (mClassId != null) {
                            		ConfirmClassTask confirmClassTask = new ConfirmClassTask();
                            		confirmClassTask.execute();
                            	} else {
                            		ConfirmFriendTask confirmFriendTask = new ConfirmFriendTask();
                            		confirmFriendTask.execute();
                            	}
                                /*mRequestFriendHttpRequest = DemoContext.getInstance().getDemoApi().processRequestFriend(mResultList.get(position).getId(), "1", new ApiCallback<Status>() {
                                    @Override
                                    public void onComplete(AbstractHttpRequest<Status> statusAbstractHttpRequest, Status status) {

                                        UserInfo info = new UserInfo(mResultList.get(position).getId(), mResultList.get(position).getUsername(), mResultList.get(position).getPortrait() == null ? null : Uri.parse(mResultList.get(position).getPortrait()));
                                        if (DemoContext.getInstance() != null) {
                                            if(DemoContext.getInstance().hasUserId(mResultList.get(position).getId())){
                                                DemoContext.getInstance().updateUserInfos(mResultList.get(position).getId(), "1");
                                            }else{
                                                DemoContext.getInstance().insertOrReplaceUserInfo(info, "1");
                                            }
                                            ApiResult apiResult = mResultList.get(position);
                                            apiResult.setStatus(1);
                                            mResultList.set(position, mResultList.get(position));

                                            Message mess = Message.obtain();
                                            mess.obj = mResultList;
                                            mess.what = 1;
                                            mHandler.sendMessage(mess);
                                        }
                                        sendMessage(mResultList.get(position).getId());
                                    }

                                    @Override
                                    public void onFailure(AbstractHttpRequest<Status> statusAbstractHttpRequest, BaseException e) {
                                        if (mDialog != null)
                                            mDialog.dismiss();
                                    }
                                });*/
                            }
                        });

                    break;
                case 1://请求被拒绝
                    break;
            }

            return false;
        }
    };

    
    @Override
    public void onCallApiFailure(AbstractHttpRequest request, BaseException e) {
        if (getFriendHttpRequest == request) {
            if (mDialog != null)
                mDialog.dismiss();
            WinToast.toast(this, "获取失败");
        }
    }


   private class ConfirmClassTask extends AsyncTask<Void, Void, Integer> {
		
		@Override
		protected Integer doInBackground(Void... arg0) {
			HttpClient client = new DefaultHttpClient();

			HttpPost httpPost = new HttpPost("http://moments.daoapp.io/api/v1.0/class/confirm");

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("class_id", mClassId));
			nameValuePairs.add(new BasicNameValuePair("user_id", mUserId));
			Log.d(TAG, "class_id:" + mClassId + " user_id:" + mUserId);
			
			String result = null;
			try {
				String md5 = LoginActivity.password;
				String encoding  = Base64.encodeToString(new String(LoginActivity.username +":"+md5).getBytes(), Base64.NO_WRAP);
				Log.d("====", "password= " + md5 + "userName = " + LoginActivity.username + "encoding:" + encoding);
				//HttpGet httpGet = new HttpGet("http://moments.daoapp.io/api/v1.0/class/search" + "?name=" + groupName);
				httpPost.setHeader("Authorization", "Basic " + encoding);
				httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
				HttpResponse response = client.execute(httpPost);
				Log.d("====", "result code = " + response.getStatusLine().getStatusCode());
				if (response.getStatusLine().getStatusCode() == 200) {      									
					return 200;
				} else {      									
					return 401;
				}

			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 401;
		}

		@Override
		protected void onPostExecute(Integer result) {
			if (result == 200) {
				ContentValues values = new ContentValues();
				values.put(RequestProvider.RequestConstants.STATUS, 1);
				getContentResolver().update(RequestProvider.CONTENT_URI, values, RequestProvider.RequestConstants.USERID + "=" + mUserId + " and " + RequestProvider.RequestConstants.ISCLASS + " = 1", null);
				
				{
					if(mResultList != null) {
	            		mResultList.clear();
	            	}
	                Cursor cur = getContentResolver().query(RequestProvider.CONTENT_URI, null, null, null, null);
	        		if (cur != null) {
	        			while(cur.moveToNext()) {
	        				String userId = cur.getString(cur.getColumnIndex(RequestProvider.RequestConstants.USERID));
	        				String userName = cur.getString(cur.getColumnIndex(RequestProvider.RequestConstants.USERNAME));
	        				String portrait = cur.getString(cur.getColumnIndex(RequestProvider.RequestConstants.PORTRAIT));
	        				int status = cur.getInt(cur.getColumnIndex(RequestProvider.RequestConstants.STATUS));
	        				String classId = cur.getString(cur.getColumnIndex(RequestProvider.RequestConstants.CLASSID));
	        				String className = cur.getString(cur.getColumnIndex(RequestProvider.RequestConstants.CLASSNAME));
	        				mResultList.add(new RequestInfo(userId, userName, portrait, status, classId, className));
	        			}
	        			cur.close();
	        		}
	        		adapter = new NewFriendListAdapter(mResultList, NewFriendListActivity.this);
	                mNewFriendList.setAdapter(adapter);
	            	adapter.notifyDataSetChanged();
            	}
				Intent in = new Intent();
		        in.setAction(MainActivity.ACTION_DMEO_AGREE_REQUEST);
		        sendBroadcast(in);
				Toast.makeText(NewFriendListActivity.this, "已成功添加到班级", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(NewFriendListActivity.this, "添加到班级失败", Toast.LENGTH_LONG).show();
			}
		}

	}
    
   
   private class ConfirmFriendTask extends AsyncTask<Void, Void, Integer> {
		
		@Override
		protected Integer doInBackground(Void... arg0) {
			HttpClient client = new DefaultHttpClient();

			HttpPost httpPost = new HttpPost("http://moments.daoapp.io/api/v1.0/user/confirm");

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("id", mUserId));
			Log.d(TAG, "user_id:" + mUserId);
			
			String result = null;
			try {
				String md5 = LoginActivity.password;
				String encoding  = Base64.encodeToString(new String(LoginActivity.username +":"+md5).getBytes(), Base64.NO_WRAP);
				Log.d("====", "password= " + md5 + "userName = " + LoginActivity.username + "encoding:" + encoding);
				//HttpGet httpGet = new HttpGet("http://moments.daoapp.io/api/v1.0/class/search" + "?name=" + groupName);
				httpPost.setHeader("Authorization", "Basic " + encoding);
				httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
				HttpResponse response = client.execute(httpPost);
				Log.d("====", "result code = " + response.getStatusLine().getStatusCode());
				if (response.getStatusLine().getStatusCode() == 200) {      									
					return 200;
				} else {      									
					return 401;
				}

			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 401;
		}

		@Override
		protected void onPostExecute(Integer result) {
			if (result == 200) {
				ContentValues values = new ContentValues();
				values.put(RequestProvider.RequestConstants.STATUS, 1);
				getContentResolver().update(RequestProvider.CONTENT_URI, values, RequestProvider.RequestConstants.USERID + "=" + mUserId+ " and " + RequestProvider.RequestConstants.ISCLASS + " = 0", null);
				
				{
					if(mResultList != null) {
	            		mResultList.clear();
	            	}
	                Cursor cur = getContentResolver().query(RequestProvider.CONTENT_URI, null, null, null, null);
	        		if (cur != null) {
	        			while(cur.moveToNext()) {
	        				String userId = cur.getString(cur.getColumnIndex(RequestProvider.RequestConstants.USERID));
	        				String userName = cur.getString(cur.getColumnIndex(RequestProvider.RequestConstants.USERNAME));
	        				String portrait = cur.getString(cur.getColumnIndex(RequestProvider.RequestConstants.PORTRAIT));
	        				int status = cur.getInt(cur.getColumnIndex(RequestProvider.RequestConstants.STATUS));
	        				String classId = cur.getString(cur.getColumnIndex(RequestProvider.RequestConstants.CLASSID));
	        				String className = cur.getString(cur.getColumnIndex(RequestProvider.RequestConstants.CLASSNAME));
	        				mResultList.add(new RequestInfo(userId, userName, portrait, status, classId, className));
	        			}
	        			cur.close();
	        		}
	        		adapter = new NewFriendListAdapter(mResultList, NewFriendListActivity.this);
	                mNewFriendList.setAdapter(adapter);
	            	adapter.notifyDataSetChanged();
            	}
				Intent in = new Intent();
		        in.setAction(MainActivity.ACTION_DMEO_AGREE_REQUEST);
		        sendBroadcast(in);
				
				Toast.makeText(NewFriendListActivity.this, "已成功添加好友", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(NewFriendListActivity.this, "添加好友失败", Toast.LENGTH_LONG).show();
			}
		}

	}
    /**
     * 添加好友成功后，向对方发送一条消息
     *
     * @param id 对方id
     */
    private void sendMessage(String id) {
        final DeAgreedFriendRequestMessage message = new DeAgreedFriendRequestMessage(id, "agree");
        if (DemoContext.getInstance() != null) {
            //获取当前用户的 userid
            String userid = DemoContext.getInstance().getSharedPreferences().getString("DEMO_USERID", "defalte");
            String username = DemoContext.getInstance().getSharedPreferences().getString("DEMO_USER_NAME", "defalte");
            String userportrait = DemoContext.getInstance().getSharedPreferences().getString("DEMO_USER_PORTRAIT", "defalte");

            UserInfo userInfo = new UserInfo(userid,username,Uri.parse(userportrait));
            //把用户信息设置到消息体中，直接发送给对方，可以不设置，非必选项
            message.setUserInfo(userInfo);
            if (RongIM.getInstance() != null) {

                //发送一条添加成功的自定义消息，此条消息不会在ui上展示
                RongIM.getInstance().getRongIMClient().sendMessage(Conversation.ConversationType.PRIVATE, id, message, null,null, new RongIMClient.SendMessageCallback() {
                    @Override
                    public void onError(Integer messageId, RongIMClient.ErrorCode e) {
                        Log.e(TAG, Constants.DEBUG + "------DeAgreedFriendRequestMessage----onError--");
                        if (mDialog != null)
                            mDialog.dismiss();
                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        Log.e(TAG, Constants.DEBUG + "------DeAgreedFriendRequestMessage----onSuccess--" + message.getMessage());
                        if (mDialog != null)
                            mDialog.dismiss();
                    }
                });
            }
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.de_add_friend_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Constants.SEARCH_REQUESTCODE) {

            if (adapter != null) {
                adapter = null;
                mResultList.clear();
            }

            if (DemoContext.getInstance() != null) {
                getFriendHttpRequest = DemoContext.getInstance().getDemoApi().getNewFriendlist(this);

            }
            if (mDialog != null && !mDialog.isShowing()) {
                mDialog.show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.icon:
                Intent intent = new Intent(NewFriendListActivity.this, SearchFriendActivity.class);
                startActivityForResult(intent, Constants.FRIENDLIST_REQUESTCODE);
                break;

            case android.R.id.home:
                finish();

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 0:
            	updateAdapter();
            case 1:
                searchRequest();
                break;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        if (adapter != null) {
            adapter = null;
        }
        super.onDestroy();
    }
}
