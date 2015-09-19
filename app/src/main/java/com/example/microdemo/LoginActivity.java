package com.example.microdemo;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.example.microdemo.util.Str2MD5;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;
import io.rong.app.R;

public class LoginActivity extends FragmentActivity{

	EditText edUser;
	EditText edPassword;
	private static String url = "http://moments.daoapp.io/api/v1.0/";
	@Override
    protected void onCreate(Bundle arg0) {
	    super.onCreate(arg0);
	    this.setContentView(R.layout.login);
	    edUser = (EditText) this.findViewById(R.id.login_user_edit);
	    edPassword = (EditText) this.findViewById(R.id.login_passwd_edit);
	    this.findViewById(R.id.login_login_btn).setOnClickListener(new OnClickListener(){

			@Override
            public void onClick(View v) {
				final String username = edUser.getText().toString();
				final String password = edPassword.getText().toString();
				if(username.length() == 0 || password.length() == 0){
					Toast.makeText(LoginActivity.this, "please input user or password", Toast.LENGTH_LONG).show();
				}else{
					new AsyncTask<Void,Void,Integer>(){

						@Override
                        protected Integer doInBackground(Void... arg0) {
							HttpClient client = new DefaultHttpClient();

							HttpGet httpget = new HttpGet(url);
							// HttpPost httppost = new
							// HttpPost("http://moments.daoapp.io/api/v1.0/posts/");
							httpget.setHeader("user-agent",
							        "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:23.0) Gecko/20100101 Firefox/23.0");
							String md5 = Str2MD5.MD5(password);
							Log.d("====", "encoding = " +md5);
							String encoding  = Base64.encodeToString(new String(
									username+":"+md5).getBytes(),
							        Base64.NO_WRAP);
							Log.d("====", "encoding = " +encoding);
							httpget.setHeader("Authorization", "Basic " + encoding);

							String result = null;
							try {
								HttpResponse response = client.execute(httpget);
								Log.d("====", "result code = " + response.getStatusLine().getStatusCode());
								if(response.getStatusLine().getStatusCode() == 200){
									MyApplication.setBase64Code(encoding);
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
				}
	            
            }
	    	
	    });
    }

	@Override
    protected void onDestroy() {
	    // TODO Auto-generated method stub
	    super.onDestroy();
    }

	@Override
    protected void onPause() {
	    // TODO Auto-generated method stub
	    super.onPause();
    }

	@Override
    protected void onResume() {
	    // TODO Auto-generated method stub
	    super.onResume();
    }
	
}
