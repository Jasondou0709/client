package io.rong.app.fragment;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EncodingUtils;
import org.apache.http.util.EntityUtils;

import com.example.microdemo.LoginActivity;
import com.example.microdemo.MyApplication;
import com.example.microdemo.adapter.MyListAdapter;
import com.example.microdemo.custonListView.CustomListView;
import com.example.microdemo.custonListView.CustomListView.OnLoadMoreListener;
import com.example.microdemo.custonListView.CustomListView.OnRefreshListener;
import com.example.microdemo.domain.FirendMicroList;
import com.example.microdemo.domain.FirendMicroListDatas;
import com.example.microdemo.domain.FirendsMicro;
import com.example.microdemo.domain.OwnerMicro;
import com.example.microdemo.util.FastjsonUtil;
import com.example.microdemo.util.Str2MD5;
import com.example.testpic.PublishedActivity;
import com.sea_monster.resource.Resource;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import io.rong.app.DemoContext;
import io.rong.app.R;
import io.rong.imkit.RongIM;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imlib.model.Group;

/**
 * Created by Administrator on 2015/3/6.
 */
public class CustomerFragment extends Fragment {

	private static final String TAG = "CustomerFragment";
	private String uid = "";
	private String companykey = "";
	int now = 0;
	int count = 1;
	private String strIcon = "";
	List<FirendMicroListDatas> listdatas = new ArrayList<FirendMicroListDatas>();
	
	private String classId;
	private View header;
	public CustomListView listview;
	public ImageButton selectpic;
	public MyListAdapter mAdapter;
	public OwnerMicro ownerdata;
	private AsyncImageView MicroIcon;
	String res = "";
	String ownerres = "";

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_main, container, false);
		
		try {

			InputStream owner = getResources().openRawResource(R.raw.ownerjson);
			int ownerlength = owner.available();
			byte[] ownerbuffer = new byte[ownerlength];
			owner.read(ownerbuffer);
			ownerres = EncodingUtils.getString(ownerbuffer, "UTF-8");
			owner.close();

			InputStream in = getResources().openRawResource(R.raw.json);

			int length = in.available();

			byte[] buffer = new byte[length];

			in.read(buffer);

			res = EncodingUtils.getString(buffer, "UTF-8");

			in.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		init(view);
		return view;
	}

	private void init(View view) {
		
		getOwnerList();
		if (DemoContext.getInstance() != null) {
			classId = DemoContext.getInstance().getClassId();
			Log.d(TAG, "DemoContext.getInstance().getClassId():" + classId);
		}
		selectpic = (ImageButton) view.findViewById(R.id.ib_right);
		selectpic.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View paramView) {

				Intent intent = new Intent(getActivity(),
				        PublishedActivity.class);
				intent.putExtra("classId", classId);
				startActivity(intent);
			}
		});
		
		header = LayoutInflater.from(getActivity()).inflate(R.layout.micro_list_header,
		        null);
		MicroIcon = (AsyncImageView) header.findViewById(R.id.MicroIcon);
		if (DemoContext.getInstance() != null) {
			if (DemoContext.getInstance().getPortrait() != null && !DemoContext.getInstance().getPortrait().equalsIgnoreCase("") && MicroIcon != null) {
				MicroIcon.setResource(new Resource(DemoContext.getInstance().getPortrait()));
			}
		}
		
		listview = (CustomListView) view.findViewById(R.id.list);
		listview.setVerticalScrollBarEnabled(false);
		listview.setDivider(this.getResources().getDrawable(R.drawable.h_line));
		listview.addHeaderView(header);
		
		mAdapter = new MyListAdapter(getActivity(), listdatas, ownerdata);
		getMicroList(1, true);
		listview.setAdapter(mAdapter);
		//getOwnerList();
		

		listview.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				// TODO Auto-generated method stub
				String s = "下拉刷新";
				getData(s);
			}

		});

		listview.setOnLoadListener(new OnLoadMoreListener() {

			public void onLoadMore() {
				String s = "上拉加载更多";
				getData(s);

			}
		});
	}

	private ImageButton findViewById(int ibRight) {
		// TODO Auto-generated method stub
		return null;
	}

	private void getOwnerList() {
		if (TextUtils.isEmpty(ownerres)) {
			return;
		}

		ownerdata = FastjsonUtil.json2object(ownerres, OwnerMicro.class);
	}

	@SuppressLint("NewApi")
	private void getMicroList(final int i, boolean has) {
		if (TextUtils.isEmpty(res)) {
			return;
		}
		new AsyncTask<String, Void, String>() {
			@Override
			protected void onPostExecute(String result) {
				int firstpage = 1;
				FirendsMicro fm = FastjsonUtil.json2object(result,
				        FirendsMicro.class);
				FirendMicroList fList = fm.getFriendPager();

				{

					if (i == firstpage) {
						listdatas.clear();
						count = 1;
					}

					if (null == fList.getDatas()
					        || fList.getDatas().size() == 0) {
						if (i == firstpage) {
							listview.onRefreshComplete();
						} else {
							listview.onLoadMoreComplete(false);
						}
					} else {
						if (i == firstpage) {
							listview.onRefreshComplete();
						} else {
							listview.onLoadMoreComplete();
						}
						listdatas.addAll(fList.getDatas());
						count++;
					}
					int k = listdatas.size();
					now = k > 0 ? k - 1 : 0;
					mAdapter.notifyDataSetChanged();
				}

			}

			@Override
			protected String doInBackground(String... params) {
				HttpClient client = new DefaultHttpClient();

				HttpGet httpget = new HttpGet(params[0]);
				// HttpPost httppost = new
				// HttpPost("http://moments.daoapp.io/api/v1.0/posts/");
				String md5 = io.rong.app.activity.LoginActivity.password;
				String encoding  = Base64.encodeToString(new String(io.rong.app.activity.LoginActivity.username +":"+md5).getBytes(), Base64.NO_WRAP);
				Log.d("====", "password= " + md5 + "userName = " + io.rong.app.activity.LoginActivity.username + "encoding:" + encoding);
				//HttpGet httpGet = new HttpGet("http://moments.daoapp.io/api/v1.0/class/search" + "?name=" + groupName);
				httpget.setHeader("Authorization", "Basic " + encoding);
				
				

				String result = null;
				try {
					HttpResponse response = client.execute(httpget);
					result = EntityUtils.toString(response.getEntity());
					Log.d("CustomerFragment", "result = " + result);
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return result;
			}
		}.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
		        "http://moments.daoapp.io/api/v1.0/posts/?page="+ i + "&classid=" + classId);

	}

	private void getData(String s) {
	    if (DemoContext.getInstance() != null) {
			classId = DemoContext.getInstance().getClassId();
			Log.d(TAG, "getData().getClassId():" + classId);
		}
	    
		if ("下拉刷新".equals(s)) {

			getMicroList(1, true);

			listview.onRefreshComplete();
		} else {
			getMicroList(count, true);

			listview.onLoadMoreComplete(); // 加载更多完成
		}
	}

	public void LoadList() {
		getMicroList(1, true);
	}

}
