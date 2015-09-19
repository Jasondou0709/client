package io.rong.app.activity;

import io.rong.app.DemoContext;
import io.rong.app.R;
import io.rong.app.ui.LoadingDialog;
import io.rong.app.utils.Constants;

import java.io.IOException;
import java.util.ArrayList;
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
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class CreateGroupActivity extends BaseActivity implements View.OnClickListener {
	private EditText mEtName;
	private EditText mSchoolName;
	private EditText mEtIntroduce;
    private Button mBtCreate;
    private LoadingDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.de_ac_create_group);
        initView();

    }


    protected void initView() {
        getSupportActionBar().setTitle(R.string.create_class);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.de_actionbar_back);
        mEtName = (EditText) findViewById(R.id.input_classname_et);
        mSchoolName = (EditText) findViewById(R.id.input_schoolname_et);
        mEtIntroduce = (EditText) findViewById(R.id.input_introduce_et);
        mBtCreate = (Button) findViewById(R.id.create_class_bt);
        mDialog = new LoadingDialog(this);
        mBtCreate.setOnClickListener(this);

    }



	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.equals(mBtCreate)) {
            final String className = mEtName.getText().toString();
            final String schoolName = mSchoolName.getText().toString();
            final String classIntroduce = mEtIntroduce.getText().toString();
            if (DemoContext.getInstance() != null) {
               // searchHttpRequest = DemoContext.getInstance().getDemoApi().searchGroupByGroupName(groupName, this);
            	new AsyncTask<Void, Void, String>() {

        			@Override
        			protected String doInBackground(Void... arg0) {
        				HttpClient client = new DefaultHttpClient();

        				HttpPost httpPost = new HttpPost("http://moments.daoapp.io/api/v1.0/class/create");

    					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
						nameValuePairs.add(new BasicNameValuePair("name", schoolName + className));
						nameValuePairs.add(new BasicNameValuePair("introduce", classIntroduce));
						Time time = new Time();
						time.setToNow();
						nameValuePairs.add(new BasicNameValuePair("datetime", time.toString()));
						
        				String result = null;
        				try {
        					String md5 = LoginActivity.password;
        					String encoding  = Base64.encodeToString(new String(LoginActivity.username +":"+md5).getBytes(), Base64.NO_WRAP);
        					Log.d("====", "password= " + md5 + "userName = " + LoginActivity.username + "encoding:" + encoding);
        					httpPost.setHeader("Authorization", "Basic " + encoding);
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
								String resultCode = jsonObject.getString("status");
								if (resultCode.equalsIgnoreCase("200")) {
									Toast.makeText(CreateGroupActivity.this, "创建班级成功", Toast.LENGTH_LONG).show();
									Intent in = new Intent();
				                    in.setAction(MainActivity.ACTION_DMEO_GROUP_MESSAGE);
				                    sendBroadcast(in);
								} else {
									Toast.makeText(CreateGroupActivity.this,"创建班级失败", Toast.LENGTH_LONG).show();
								}
								
							} catch (JSONException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}

        				} else {
        					Toast.makeText(CreateGroupActivity.this,"创建班级失败", Toast.LENGTH_LONG).show();
        				}

        		    }
                  }.execute();
            }

        }
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		finish();
		return super.onOptionsItemSelected(item);
	}

}
