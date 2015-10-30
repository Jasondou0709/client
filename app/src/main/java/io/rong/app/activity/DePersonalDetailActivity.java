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
import io.rong.app.model.User;
import io.rong.app.ui.LoadingDialog;
import io.rong.app.ui.WinToast;
import io.rong.app.utils.Constants;
import io.rong.imkit.RongIM;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imlib.model.UserInfo;


/**
 * Created by Bob on 2015/3/26.
 *
 * 搜索好友点详情
 */
public class DePersonalDetailActivity extends BaseApiActivity implements View.OnClickListener {


    private AsyncImageView mFriendImg;
    private TextView mFriendName;
    private Button mAddFriend;
    private AbstractHttpRequest<User> mUserHttpRequest;
    private LoadingDialog mDialog;
    private UserInfo user;
    private String mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.de_ac_personal_detail);
        initView();
        initData();

    }


    protected void initView() {
        getSupportActionBar().setTitle(R.string.de_actionbar_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.de_actionbar_back);
        mFriendImg = (AsyncImageView) findViewById(R.id.friend_adapter_img);
        mFriendName = (TextView) findViewById(R.id.de_name);
        mAddFriend = (Button) findViewById(R.id.de_add_friend);
    }

    protected void initData() {
        mAddFriend.setOnClickListener(this);
        mDialog = new LoadingDialog(this);

        if (getIntent().hasExtra("SEARCH_USERID")&&getIntent().hasExtra("SEARCH_USERNAME")&&getIntent().hasExtra("SEARCH_PORTRAIT")) {
        	mUserId = getIntent().getStringExtra("SEARCH_USERID");
            mFriendName.setText(getIntent().getStringExtra("SEARCH_USERNAME"));
            mFriendImg.setResource(new Resource(getIntent().getStringExtra("SEARCH_PORTRAIT")));
            if (DemoContext.getInstance() != null && DemoContext.getInstance().searcheUserInfosById(mUserId)) {
                mAddFriend.setText("发消息");
            }

        }

        if (getIntent().hasExtra("USER")) {
            user = getIntent().getParcelableExtra("USER");
            mFriendName.setText(user.getName());
            mFriendImg.setResource(new Resource(user.getPortraitUri()));
            String userID = DemoContext.getInstance().getSharedPreferences().getString("DEMO_USERID","defalt");
            if(user.getUserId().equals(userID)){
            	mFriendName.setText(DemoContext.getInstance().getSharedPreferences().getString("DEMO_USERNAME",""));
                mAddFriend.setVisibility(View.GONE);
            }else if(user.getUserId().equals("kefu114")){
                mAddFriend.setVisibility(View.GONE);
            }

            if (DemoContext.getInstance() != null && DemoContext.getInstance().searcheUserInfosById(user.getUserId())) {
            	mAddFriend.setText("发消息");
            }
        }

    }

    @Override
    public void onCallApiSuccess(AbstractHttpRequest request, Object obj) {
        if (mUserHttpRequest == request) {
            if (mDialog != null)
                mDialog.dismiss();
            final User user = (User) obj;
            if (user.getCode() == 200) {
                WinToast.toast(this,R.string.friend_send_success);
                Log.e("", "--------onCallApiSuccess----发送好友请求成功---------");
                Intent intent = new Intent();
                this.setResult( Constants.PERSONAL_REQUESTCODE, intent);

            }else if(user.getCode() == 301){
                WinToast.toast(this,R.string.friend_send);
            }
        }

    }

    @Override
    public void onCallApiFailure(AbstractHttpRequest request, BaseException e) {
        if (mUserHttpRequest == request) {
            if (mDialog != null)
                mDialog.dismiss();
            Log.e("", "--------onCallApiSuccess----发送好友请求失败---------");
        }
    }

    @Override
    public void onClick(View v) {
        String targetid = getIntent().getStringExtra("SEARCH_USERID");
        
        if (mAddFriend.getText().equals("发消息")) {
        	if (RongIM.getInstance() != null && DemoContext.getInstance() != null) {
                if(targetid != null)
                RongIM.getInstance().startPrivateChat(DePersonalDetailActivity.this, targetid, DemoContext.getInstance().getUserInfoById(targetid).getName().toString());
            }       	
        } else {
	        if (DemoContext.getInstance() != null && !"".equals(targetid)) {
	            {
	                AddFriendTask addFriendTask = new AddFriendTask();
	                addFriendTask.execute();
	
	                if (mDialog != null && !mDialog.isShowing()) {
	                    mDialog.show();
	                }
	            }
	        }
        }
    }

    private class AddFriendTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... arg0) {
			HttpClient client = new DefaultHttpClient();

			HttpPost httpPost = new HttpPost("http://moments.daoapp.io/api/v1.0/users/addfriend");

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("id", mUserId));
			Log.d("DePersonalDetailActivity", "userId:" + mUserId);
			
			String result = null;
			try {
				String md5 = LoginActivity.password;
				String encoding  = Base64.encodeToString(new String(LoginActivity.username +":"+md5).getBytes(), Base64.NO_WRAP);
				Log.d("DePersonalDetailActivity", "password= " + md5 + "userName = " + LoginActivity.username + "encoding:" + encoding);
				
				httpPost.setHeader("Authorization", "Basic " + encoding);
			    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
				HttpResponse response = client.execute(httpPost);
				Log.d("DePersonalDetailActivity", "result code = " + response.getStatusLine().getStatusCode());
				if (response.getStatusLine().getStatusCode() == 200) {
					result = EntityUtils.toString(response.getEntity());
					Log.d("DePersonalDetailActivity", "result = " + result);
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
			if (mDialog != null)
                mDialog.dismiss();
			
			if (result != null) {									
				try {
					/** 把json字符串转换成json对象 **/
					JSONObject jsonObject = new JSONObject(result);
					String resultCode = jsonObject.getString("status");
					if (resultCode.equalsIgnoreCase("200")) {
						Toast.makeText(DePersonalDetailActivity.this, "已发出好友请求", Toast.LENGTH_LONG).show();
					} else {
						String message = jsonObject.getString("message");
						Toast.makeText(DePersonalDetailActivity.this, message, Toast.LENGTH_LONG).show();
					}
					
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			} else {
				Toast.makeText(DePersonalDetailActivity.this, "发送请求失败", Toast.LENGTH_LONG).show();
			}

	  }
      }
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}
