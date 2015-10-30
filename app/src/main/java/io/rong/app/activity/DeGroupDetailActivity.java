package io.rong.app.activity;

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
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sea_monster.exception.BaseException;
import com.sea_monster.network.AbstractHttpRequest;
import com.sea_monster.resource.Resource;

import io.rong.app.DemoContext;
import io.rong.app.R;
import io.rong.app.adapter.SearchGroupAdapter;
import io.rong.app.model.ClassGroup;
import io.rong.app.model.Status;
import io.rong.app.model.User;
import io.rong.app.ui.LoadingDialog;
import io.rong.app.ui.WinToast;
import io.rong.app.utils.Constants;
import io.rong.imkit.RongIM;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.UserInfo;


/**
 * Created by Bob on 2015/3/26.
 *
 * 搜索好友点详情
 */
public class DeGroupDetailActivity extends BaseApiActivity implements View.OnClickListener {

	private final static String TAG = "DeGroupDetailActivity";
    private AsyncImageView mFriendImg;
    private TextView mFriendName;
    private TextView mClassIntroduce;
    private Button mAddFriend;
    private AbstractHttpRequest<Status> mUserHttpRequest;
    //private LoadingDialog mDialog;
    private String classId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.de_ac_group_detail);
        initView();
        initData();

    }


    protected void initView() {
        getSupportActionBar().setTitle(R.string.public_add_class);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.de_actionbar_back);
        mFriendImg = (AsyncImageView) findViewById(R.id.friend_adapter_img);
        mFriendName = (TextView) findViewById(R.id.de_name);
        mClassIntroduce = (TextView) findViewById(R.id.de_introduce);
        mAddFriend = (Button) findViewById(R.id.de_add_friend);
    }

    protected void initData() {
        mAddFriend.setOnClickListener(this);
        //mDialog = new LoadingDialog(this);

        if (getIntent().hasExtra("SEARCH_USERID")&&getIntent().hasExtra("SEARCH_USERNAME")&&getIntent().hasExtra("SEARCH_PORTRAIT")&&getIntent().hasExtra("SEARCH_INTRODUCE")) {

            mFriendName.setText(getIntent().getStringExtra("SEARCH_USERNAME"));
            mClassIntroduce.setText(getIntent().getStringExtra("SEARCH_INTRODUCE"));
            mFriendImg.setResource(new Resource(getIntent().getStringExtra("SEARCH_PORTRAIT")));
            classId = getIntent().getStringExtra("SEARCH_USERID");
            if (DemoContext.getInstance() != null && DemoContext.getInstance().hasGroup(classId)) {
                mAddFriend.setText("聊天");;
            }
        }

       /* if (getIntent().hasExtra("GROUP")) {
            user = getIntent().getParcelableExtra("USER");
            mFriendName.setText(user.getName());
            mFriendImg.setResource(new Resource(user.getPortraitUri()));
            String userID = DemoContext.getInstance().getSharedPreferences().getString("DEMO_USERID","defalt");
            if(user.getUserId().equals(userID)){
                mAddFriend.setVisibility(View.GONE);
            }else if(user.getUserId().equals("kefu114")){
                mAddFriend.setVisibility(View.GONE);
            }

            if (DemoContext.getInstance() != null && DemoContext.getInstance().searcheUserInfosById(user.getUserId())) {
                mAddFriend.setVisibility(View.GONE);
            }
        }*/

    }

    @Override
    public void onCallApiSuccess(AbstractHttpRequest request, Object obj) {
        if (mUserHttpRequest == request) {
           // if (mDialog != null)
            //    mDialog.dismiss();
            final Status status = (Status) obj;
            if (status.getCode() == 200) {
                WinToast.toast(this,R.string.friend_send_success);
                Log.e("", "--------onCallApiSuccess----发送加入群组请求成功---------");
                Intent intent = new Intent();
                //this.setResult( Constants.PERSONAL_REQUESTCODE, intent);

            }else if(status.getCode() == 301){
                WinToast.toast(this,R.string.friend_send);
            }
        }

    }

    @Override
    public void onCallApiFailure(AbstractHttpRequest request, BaseException e) {
        if (mUserHttpRequest == request) {
        //    if (mDialog != null)
        //        mDialog.dismiss();
            Log.e("", "--------onCallApiSuccess----发送加入群组请求失败---------");
        }
    }

    @Override
	public void onClick(View v) {
		final String targetid = getIntent().getStringExtra("SEARCH_USERID");
		final String classname = getIntent().getStringExtra("SEARCH_USERNAME");
		if (mAddFriend.getText().equals("聊天")) {
			if (RongIM.getInstance() != null)
                RongIM.getInstance().getRongIMClient().joinGroup(targetid, classname, new RongIMClient.OperationCallback() {
                    @Override
                    public void onSuccess() {
                        RongIM.getInstance().startGroupChat(DeGroupDetailActivity.this, targetid, classname);

                    }

                    @Override
                    public void onError(RongIMClient.ErrorCode errorCode) {
                    }
                });
			
		} else {
			if (DemoContext.getInstance() != null && !"".equals(targetid)) {
				if (DemoContext.getInstance() != null) {
					new AsyncTask<Void, Void, String>() {

						@Override
						protected String doInBackground(Void... arg0) {
							HttpClient client = new DefaultHttpClient();

							HttpPost httpPost = new HttpPost("http://moments.daoapp.io/api/v1.0/class/enroll/" + classId);						

							String result = null;
							try {
								String md5 = LoginActivity.password;
								String encoding = Base64.encodeToString(new String(LoginActivity.username + ":" + md5).getBytes(), Base64.NO_WRAP);
								Log.d(TAG, "password= " + md5 + "userName = " + LoginActivity.username + "encoding:" + encoding);								
								httpPost.setHeader("Authorization", "Basic " + encoding);
								HttpResponse response = client.execute(httpPost);
								Log.d(TAG, "result code = " + response.getStatusLine().getStatusCode());
								if (response.getStatusLine().getStatusCode() == 200) {
									result = EntityUtils.toString(response.getEntity());
									Log.d(TAG, "result = " + result);
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
									String resultCode = jsonObject.getString("status");
									if (resultCode.equalsIgnoreCase("200")) {
										Toast.makeText(DeGroupDetailActivity.this, "已发出加入班级请求", Toast.LENGTH_LONG).show();
										DeGroupDetailActivity.this.finish();
									} else {
										String message = jsonObject.getString("message");
										Toast.makeText(DeGroupDetailActivity.this, message, Toast.LENGTH_LONG).show();
									}

								} catch (JSONException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}

							} else {
								Toast.makeText(DeGroupDetailActivity.this, "发送请求失败", Toast.LENGTH_LONG).show();
							}

						}
					}.execute();

				}

			}
		}
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}
