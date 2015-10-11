package io.rong.app.activity;

import android.content.Intent;
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
import android.widget.ListView;
import android.widget.Toast;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.rong.app.DemoContext;
import io.rong.app.R;
import io.rong.app.adapter.SearchFriendAdapter;
import io.rong.app.adapter.SearchGroupAdapter;
import io.rong.app.model.ApiResult;
import io.rong.app.model.ClassGroup;
import io.rong.app.model.Friends;
import io.rong.app.model.Groups;
import io.rong.app.model.User1;
import io.rong.app.ui.LoadingDialog;
import io.rong.app.utils.Constants;

import com.sea_monster.exception.BaseException;
import com.sea_monster.network.AbstractHttpRequest;

/**
 * Created by Bob on 2015/3/26.
 */
public class SearchGroupActivity extends BaseApiActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

	private final static String TAG = "SearchGroupActivity";
    private EditText mEtSearch;
    private Button mBtSearch;
    private ListView mListSearch;
    private AbstractHttpRequest<Groups> searchHttpRequest;
    private List<ClassGroup> mResultList;
    private SearchGroupAdapter adapter;
    private LoadingDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.de_ac_search);
        initView();
        initData();

    }


    protected void initView() {
        getSupportActionBar().setTitle(R.string.public_account_search);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.de_actionbar_back);
        mEtSearch = (EditText) findViewById(R.id.de_ui_search);
        mBtSearch = (Button) findViewById(R.id.de_search);
        mListSearch = (ListView) findViewById(R.id.de_search_list);
        mResultList = new ArrayList<>();
        mDialog = new LoadingDialog(this);

    }

    protected void initData() {
        mBtSearch.setOnClickListener(this);
        mListSearch.setOnItemClickListener(this);
    }

    @Override
    public void onCallApiSuccess(AbstractHttpRequest request, Object obj) {
        Log.e("", "------onCallApiSuccess-user.getCode() == 200)--=======---" );
       /* if (searchHttpRequest == request) {
            if (mDialog != null)
                mDialog.dismiss();
            if (mResultList.size() > 0)
                mResultList.clear();
            if (obj instanceof Groups) {
                final Groups groups = (Groups) obj;

                if (groups.getCode() == 200) {
                    if (groups.getResult().size() > 0) {
                        for (int i = 0; i < groups.getResult().size(); i++) {
                            mResultList.add(groups.getResult().get(i));
                            Log.e("", "------onCallApiSuccess-user.getCode() == 200)-----" + groups.getResult().get(0).getId().toString());
                        }
                            adapter = new SearchFriendAdapter(mResultList, SearchGroupActivity.this);
                            mListSearch.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                    }

                }
            }
        }*/

    }

    @Override
    public void onCallApiFailure(AbstractHttpRequest request, BaseException e) {
        if (searchHttpRequest == request) {
            if (mDialog != null)
                mDialog.dismiss();
            Log.e("", "------onCallApiSuccess-user.============onCallApiFailure()--");
        }
    }

    @Override
    public void onClick(View v) {
        if (v.equals(mBtSearch)) {
            final String groupName = mEtSearch.getText().toString();
            if (DemoContext.getInstance() != null) {
                //searchHttpRequest = DemoContext.getInstance().getDemoApi().searchGroupByGroupName(groupName, this);
            	new AsyncTask<Void, Void, String>() {

        			@Override
        			protected String doInBackground(Void... arg0) {
        				HttpClient client = new DefaultHttpClient();

        				HttpPost httpPost = new HttpPost("http://moments.daoapp.io/api/v1.0/class/search");

    					List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
						nameValuePairs.add(new BasicNameValuePair("name", groupName));				
						
        				String result = null;
        				try {
        					String md5 = LoginActivity.password;
        					String encoding  = Base64.encodeToString(new String(LoginActivity.username +":"+md5).getBytes(), Base64.NO_WRAP);
        					Log.d("====", "password= " + md5 + "userName = " + LoginActivity.username + "encoding:" + encoding);
        					//HttpGet httpGet = new HttpGet("http://moments.daoapp.io/api/v1.0/class/search" + "?name=" + groupName);
        					httpPost.setHeader("Authorization", "Basic " + encoding);
        				    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
        					HttpResponse response = client.execute(httpPost);
        					Log.d("====", "search class result code = " + response.getStatusLine().getStatusCode());
        					if (response.getStatusLine().getStatusCode() == 200) {
        						result = EntityUtils.toString(response.getEntity());
        						Log.d(TAG, "search class result = " + result);
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
        					if (mResultList.size() > 0)
     			                mResultList.clear();
        					try {
        							/** 把json字符串转换成json对象 **/
        							JSONObject jsonObject = new JSONObject(result);
        							String resultCode = jsonObject.getString("status");
        							if (resultCode.equalsIgnoreCase("200")) {
        								
        								JSONArray idJson = jsonObject.getJSONArray("classes");
        								for (int i = 0; i < idJson.length(); i++) {
        									JSONObject jsonObject1 = idJson.getJSONObject(i);
        									String id = jsonObject1.getString("id");
        									String name = jsonObject1.getString("name");
        									String portrait = jsonObject1.getString("portrait");
        									String introduce = jsonObject1.getString("introduce");
        									mResultList.add(new ClassGroup(id, name, portrait, introduce));	
        									adapter = new SearchGroupAdapter(mResultList, SearchGroupActivity.this);
        		                            mListSearch.setAdapter(adapter);
        		                            adapter.notifyDataSetChanged();
        								}
        								
								     } else {
									    Toast.makeText(SearchGroupActivity.this, "not found", Toast.LENGTH_LONG).show();
								     }				
        					 } catch (JSONException e1) {
        						// TODO Auto-generated catch block
        						e1.printStackTrace();
        					 }
        				} else {
							Toast.makeText(SearchGroupActivity.this,"not found", Toast.LENGTH_LONG).show();
        				}
        		    }
                  }.execute();

            }

            if (mDialog != null && !mDialog.isShowing()) {
                mDialog.show();
            }
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Constants.PERSONAL_REQUESTCODE) {
            Intent intent = new Intent();
            this.setResult(Constants.SEARCH_REQUESTCODE, intent);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent in = new Intent(this, DeGroupDetailActivity.class);

        in.putExtra("SEARCH_USERID", mResultList.get(position).getId());
        in.putExtra("SEARCH_USERNAME", mResultList.get(position).getClassName());
        in.putExtra("SEARCH_PORTRAIT", mResultList.get(position).getPortrait());
        in.putExtra("SEARCH_INTRODUCE", mResultList.get(position).getIntroduce());
        startActivityForResult(in, Constants.SEARCH_REQUESTCODE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}
