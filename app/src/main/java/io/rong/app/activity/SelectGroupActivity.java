package io.rong.app.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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

import com.sea_monster.resource.Resource;

import io.rong.app.DemoContext;
import io.rong.app.R;
import io.rong.app.model.ClassGroup;
import io.rong.imkit.widget.AsyncImageView;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class SelectGroupActivity extends BaseActionBarActivity {
	
	private final static String TAG = "SelectGroupActivity";
	private Button show;
	private TextView comment;
	private ListView lv;
	List<ClassGroup> JIDList = new ArrayList<ClassGroup>();
	Context mContext;
	MyListAdapter adapter;
	List<Integer> listItemID = new ArrayList<Integer>();
	ArrayList<ClassGroup> selectedJIDS = new ArrayList<ClassGroup>();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_group);
		getSupportActionBar().setTitle(R.string.change_group);
		mContext = getApplicationContext();
		show = (Button) findViewById(R.id.show);
		comment = (TextView) findViewById(R.id.select_group_comment);
		lv = (ListView) findViewById(R.id.lvperson);

		initData();
		if (JIDList == null || JIDList.isEmpty()) {
			comment.setVisibility(View.VISIBLE);
			show.setVisibility(View.GONE);
		}
		adapter = new MyListAdapter(JIDList);
		lv.setAdapter(adapter);

		show.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				listItemID.clear();
				for (int i = 0; i < adapter.mChecked.size(); i++) {
					if (adapter.mChecked.get(i)) {
						listItemID.add(i);
					}
				}

				if (listItemID.size() == 0) {
					AlertDialog.Builder builder1 = new AlertDialog.Builder(
							SelectGroupActivity.this);
					builder1.setMessage("您没有选择任何班级，请重新选择");
					builder1.show();
				} else if (listItemID.size() > 1) {
					AlertDialog.Builder builder1 = new AlertDialog.Builder(
							SelectGroupActivity.this);
					builder1.setMessage("您选择了多个班级，只能选择一个班级，请重新选择");
					builder1.show();
				} else {
					selectedJIDS.clear();
					for (int i = 0; i < listItemID.size(); i++) {
						selectedJIDS.add(JIDList.get(listItemID.get(i)));
					}
					SetClassIdTask task = new SetClassIdTask();
					task.execute();
				}
			}
		});
	}

	/**
	 * 模拟数据
	 */
	private void initData() {
		if (DemoContext.getInstance() != null) {
			JIDList = DemoContext.getInstance().getClassGroups();
		}				
	}
    
	private class SetClassIdTask extends AsyncTask<Void, Void, String> {

    	@Override
		protected String doInBackground(Void... arg0) {
			HttpClient client = new DefaultHttpClient();

			HttpPost httpPost = new HttpPost("http://moments.daoapp.io/api/v1.0/users/setdefaultgroup");
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	        nameValuePairs.add(new BasicNameValuePair("defaultclass", selectedJIDS.get(0).getId()));
	     

			String result = null;
			try {
				String md5 = LoginActivity.password;
				String encoding  = Base64.encodeToString(new String(LoginActivity.username +":"+md5).getBytes(), Base64.NO_WRAP);
				Log.d(TAG, "password= " + md5 + "userName = " + LoginActivity.username + "encoding:" + encoding);
				httpPost.setHeader("Authorization", "Basic " + encoding);
				httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
				HttpResponse response = client.execute(httpPost);
				Log.d(TAG, "SetClassIdTask result code = " + response.getStatusLine().getStatusCode());
				if (response.getStatusLine().getStatusCode() == 200) {
					result = EntityUtils.toString(response.getEntity());
					Log.d(TAG, "SetClassIdTask result = " + result);
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
					String resultCode = jsonObject.getString("status");
					if (resultCode.equalsIgnoreCase("200")) {
						Log.d(TAG, "setdefaultgroup(): sucess");
						Toast.makeText(SelectGroupActivity.this, "选择班级成功", Toast.LENGTH_LONG).show();;
						if (DemoContext.getInstance() != null) {
							DemoContext.getInstance().setClassId(selectedJIDS.get(0).getId());
							SharedPreferences.Editor edit = DemoContext.getInstance().getSharedPreferences().edit();
							edit.putString("DEMO_USER_CLASSID", selectedJIDS.get(0).getId());
							edit.apply();
						}						
						Intent intent = new Intent();
						intent.putExtra("CLASSNAME", selectedJIDS.get(0).getClassName());
						intent.putExtra("CLASSPORTRAIT", selectedJIDS.get(0).getPortrait());
						setResult(1, intent);
					} else {
						Log.d(TAG, "setdefaultgroup(): fail");
						Toast.makeText(SelectGroupActivity.this, "选择班级没有成功，请重新操作", Toast.LENGTH_LONG).show();;
					}     					
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}			
			} else {
				Log.d(TAG, "setdefaultgroup(): fail");
				Toast.makeText(SelectGroupActivity.this, "选择班级没有成功，请重新操作", Toast.LENGTH_LONG).show();;
			}
			SelectGroupActivity.this.finish();
	    }
	}
	// 自定义ListView适配器
	class MyListAdapter extends BaseAdapter {
		List<Boolean> mChecked;
		List<ClassGroup> listPerson;
		HashMap<Integer, View> map = new HashMap<Integer, View>();

		public MyListAdapter(List<ClassGroup> list) {
			listPerson = new ArrayList<ClassGroup>();
			listPerson = list;

			mChecked = new ArrayList<Boolean>();
			for (int i = 0; i < list.size(); i++) {
				mChecked.add(false);
			}
		}

		@Override
		public int getCount() {
			return listPerson.size();
		}

		@Override
		public Object getItem(int position) {
			return listPerson.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			ViewHolder holder = null;

			if (map.get(position) == null) {
				Log.e(TAG, "position1 = " + position);

				LayoutInflater mInflater = (LayoutInflater) mContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = mInflater.inflate(R.layout.listitem, null);
				holder = new ViewHolder();
				holder.selected = (CheckBox) view
						.findViewById(R.id.list_select);
				holder.mImageView = (AsyncImageView) findViewById(R.id.search_adapter_img);
				holder.name = (TextView) view.findViewById(R.id.list_name);
				final int p = position;
				map.put(position, view);
				holder.selected.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						CheckBox cb = (CheckBox) v;
						mChecked.set(p, cb.isChecked());
					}
				});
				view.setTag(holder);
			} else {
				Log.e(TAG, "position2 = " + position);
				view = map.get(position);
				holder = (ViewHolder) view.getTag();
			}

			holder.selected.setChecked(mChecked.get(position));
			//holder.mImageView.setResource(new Resource(listPerson.get(position).getPortrait()));
			holder.name.setText(listPerson.get(position).getClassName());

			return view;
		}

	}

	static class ViewHolder {
		CheckBox selected;
		AsyncImageView mImageView;
		TextView name;
	}

}
