package com.example.testpic;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.microdemo.MyApplication;
import io.rong.app.R;
import com.example.microdemo.util.FastjsonUtil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class PublishedActivity extends Activity
{

	private GridView noScrollgridview;
	private GridAdapter adapter;
	private TextView activity_selectimg_send;
	private EditText newPostEt;
	private List<String> classIdList = new ArrayList<String>();

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_selectimg);
		Init();
	}

	@SuppressLint("NewApi")
	public void Init()
	{
		if(getIntent().hasExtra("GroupIdList")) {
			classIdList = getIntent().getStringArrayListExtra("GroupIdList");
		}
		newPostEt = (EditText) findViewById(R.id.makepost);
		noScrollgridview = (GridView) findViewById(R.id.noScrollgridview);
		noScrollgridview.setSelector(new ColorDrawable(Color.TRANSPARENT));
		adapter = new GridAdapter(this);
		adapter.update1();
		noScrollgridview.setAdapter(adapter);
		noScrollgridview.setOnItemClickListener(new OnItemClickListener()
		{

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3)
			{
				if (arg2 == Bimp.bmp.size())
				{
					new PopupWindows(PublishedActivity.this, noScrollgridview);
				} else
				{
					Intent intent = new Intent(PublishedActivity.this,
							PhotoActivity.class);
					intent.putExtra("ID", arg2);
					startActivity(intent);
				}
			}
		});
		activity_selectimg_send = (TextView) findViewById(R.id.activity_selectimg_send);
		activity_selectimg_send.setOnClickListener(new OnClickListener()
		{

			public void onClick(View v)
			{
				String text = newPostEt.getText().toString();
				newpostTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, text);

			}
		});
	}
	static class postreplycls {
		String result;
		String id;
		public String getResult() {
			return result;
		}
		public void setResult(String result) {
			this.result = result;
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
	}
	private AsyncTask<String,Void,String> newpostTask = new AsyncTask<String,Void,String>(){

		@Override
		protected String doInBackground(String... params) {
			List<String> list = new ArrayList<String>();
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost post = new HttpPost("http://ppzimg.daoapp.io/upload");
			for (int i = 0; i < Bimp.drr.size(); i++) {
				String Str = Bimp.drr.get(i).substring(
						Bimp.drr.get(i).lastIndexOf("/") + 1,
						Bimp.drr.get(i).lastIndexOf("."));
				String postfix = Bimp.drr.get(i).substring(Bimp.drr.get(i).lastIndexOf('.') + 1);
				//list.add(FileUtils.SDPATH + Str + ".JPEG");
				FileEntity entity = new FileEntity(new File(Bimp.drr.get(i)),
						postfix);
				post.setEntity(entity);
				post.setHeader("Content-type", postfix);
				HttpResponse response = null;
				String content = null;
				try {
					response = httpClient.execute(post);
					content = EntityUtils.toString(response.getEntity());
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				String id = content.substring(content.indexOf("md5")+6,content.indexOf("size")-3);
				//postreplycls result = FastjsonUtil.json2object(content, postreplycls.class);
				Log.d("=====",id);
				list.add("http://ppzimg.daoapp.io/"+id);

			}
			
	        HttpPost httppost = new HttpPost("http://moments.daoapp.io/api/v1.0/posts/");
	        httppost.setHeader("Authorization", "Basic " + MyApplication.getBase64Code());
	
	        JSONObject jsonParam = new JSONObject();
	        JSONArray array = new JSONArray();
	        array.addAll(list);
	        
	        jsonParam.put("content", params[0]);// 标题
	        jsonParam.put("urls", array);
	        jsonParam.put("class_id", (classIdList != null && classIdList.size() == 0) ? "" :classIdList.get(0));
	        jsonParam.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
	        StringEntity entity = null;
			try {
				entity = new StringEntity(jsonParam.toString(), "utf-8");
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}// 解决中文乱码问题
	        entity.setContentEncoding("UTF-8");
	        entity.setContentType("application/json");
	        httppost.setEntity(entity);
	        System.out.println("executing request " + httppost.getRequestLine());
	
	        HttpResponse response = null;
	        String postid = null ;
			try {
				response = httpClient.execute(httppost);
				
				HttpEntity ret = response.getEntity();
				String str = EntityUtils.toString(ret);
				postreplycls result = FastjsonUtil.json2object(
						str, postreplycls.class);
				postid = result.id;
				Log.d("=====",postid + "");

			}catch (ParseException  e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }catch(IOException e){
	        	e.printStackTrace();
	        }
			// 高清的压缩图片全部就在 list 路径里面了
			// 高清的压缩过的 bmp 对象 都在 Bimp.bmp里面
			// 完成上传服务器后 .........
			FileUtils.deleteDir();
			return postid;
		}
	    protected void onPostExecute(String result) {
	    	finish();
	    }
		
		
	};

	@SuppressLint("HandlerLeak")
	public class GridAdapter extends BaseAdapter
	{
		private LayoutInflater inflater; // 视图容器
		private int selectedPosition = -1;// 选中的位置
		private boolean shape;

		public boolean isShape()
		{
			return shape;
		}

		public void setShape(boolean shape)
		{
			this.shape = shape;
		}

		public GridAdapter(Context context)
		{
			inflater = LayoutInflater.from(context);
		}

		public void update1()
		{
			loading1();
		}

		public int getCount()
		{
			return (Bimp.bmp.size() + 1);
		}

		public Object getItem(int arg0)
		{

			return null;
		}

		public long getItemId(int arg0)
		{

			return 0;
		}

		public void setSelectedPosition(int position)
		{
			selectedPosition = position;
		}

		public int getSelectedPosition()
		{
			return selectedPosition;
		}

		/**
		 * ListView Item设置
		 */
		public View getView(int position, View convertView, ViewGroup parent)
		{
			//final int coord = position;
			ViewHolder holder = null;
			
			System.out.println("测试下表="+position);
			if (convertView == null)
			{

				convertView = inflater.inflate(R.layout.item_published_grida,
						parent, false);
				holder = new ViewHolder();
				holder.image = (ImageView) convertView
						.findViewById(R.id.item_grida_image);
				convertView.setTag(holder);
			} else
			{
				holder = (ViewHolder) convertView.getTag();
			}
			
			holder.image.setVisibility(View.VISIBLE);

			if (position == Bimp.bmp.size())
			{
				holder.image.setImageBitmap(BitmapFactory.decodeResource(
						getResources(), R.drawable.icon_addpic_unfocused));
				
			} else
			{
				holder.image.setImageBitmap(Bimp.bmp.get(position));
			}
			
			if (position == 9)
			{
				holder.image.setVisibility(View.GONE);
			}

			return convertView;
		}

		public class ViewHolder
		{
			public ImageView image;
		}

		Handler handler = new Handler()
		{
			public void handleMessage(Message msg)
			{
				switch (msg.what)
				{
				case 1:
					adapter.notifyDataSetChanged();
					break;
				}
				super.handleMessage(msg);
			}
		};

		public void loading1()
		{
			new Thread(new Runnable()
			{
				public void run()
				{
					while (true)
					{
						if (Bimp.max == Bimp.drr.size())
						{
							Message message = new Message();
							message.what = 1;
							handler.sendMessage(message);
							break;
						} else
						{
							try
							{
								String path = Bimp.drr.get(Bimp.max);
								System.out.println(path);
								Bitmap bm = Bimp.revitionImageSize(path);
								Bimp.bmp.add(bm);
								String newStr = path.substring(
										path.lastIndexOf("/") + 1,
										path.lastIndexOf("."));
								FileUtils.saveBitmap(bm, "" + newStr);
								Bimp.max += 1;
								Message message = new Message();
								message.what = 1;
								handler.sendMessage(message);
							} catch (IOException e)
							{

								e.printStackTrace();
							}
						}
					}
				}
			}).start();
		}
	}

	public String getString(String s)
	{
		String path = null;
		if (s == null)
			return "";
		for (int i = s.length() - 1; i > 0; i++)
		{
			s.charAt(i);
		}
		return path;
	}

	protected void onRestart()
	{
		adapter.update1();
		super.onRestart();
	}

	public class PopupWindows extends PopupWindow
	{

		public PopupWindows(Context mContext, View parent)
		{
			
			 super(mContext);

			View view = View
					.inflate(mContext, R.layout.item_popupwindows, null);
			view.startAnimation(AnimationUtils.loadAnimation(mContext,
					R.anim.fade_ins));
			LinearLayout ll_popup = (LinearLayout) view
					.findViewById(R.id.ll_popup);
			ll_popup.startAnimation(AnimationUtils.loadAnimation(mContext,
					R.anim.push_bottom_in_2));

			setWidth(LayoutParams.FILL_PARENT);
			setHeight(LayoutParams.FILL_PARENT);
			setBackgroundDrawable(new BitmapDrawable());
			setFocusable(true);
			setOutsideTouchable(true);
			setContentView(view);
			showAtLocation(parent, Gravity.BOTTOM, 0, 0);
			update();

			Button bt1 = (Button) view
					.findViewById(R.id.item_popupwindows_camera);
			Button bt2 = (Button) view
					.findViewById(R.id.item_popupwindows_Photo);
			Button bt3 = (Button) view
					.findViewById(R.id.item_popupwindows_cancel);
			bt1.setOnClickListener(new OnClickListener()
			{
				public void onClick(View v)
				{
					photo();
					dismiss();
				}
			});
			bt2.setOnClickListener(new OnClickListener()
			{
				public void onClick(View v)
				{
					Intent intent = new Intent(PublishedActivity.this,
							TestPicActivity.class);
					startActivity(intent);
					dismiss();
				}
			});
			bt3.setOnClickListener(new OnClickListener()
			{
				public void onClick(View v)
				{
					dismiss();
				}
			});

		}
	}

	private static final int TAKE_PICTURE = 0x000000;
	private String path = "";

	public void onConfigurationChanged(Configuration config) 
	{ 
	    super.onConfigurationChanged(config); 
	} 
	
	public void photo()
	{
		String status=Environment.getExternalStorageState(); 
		if(status.equals(Environment.MEDIA_MOUNTED)) 
		{
			File dir=new File(Environment.getExternalStorageDirectory() + "/myimage/"); 
			if(!dir.exists())dir.mkdirs(); 
			
			Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			File file = new File(dir, String.valueOf(System.currentTimeMillis())
					+ ".jpg");
			path = file.getPath();
			Uri imageUri = Uri.fromFile(file);
			openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
			openCameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
			startActivityForResult(openCameraIntent, TAKE_PICTURE);
		}
		else{ 
			Toast.makeText(PublishedActivity.this, "没有储存卡",Toast.LENGTH_LONG).show(); 
			} 
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);  
		switch (requestCode)
		{
		case TAKE_PICTURE:
			if (Bimp.drr.size() < 9 && resultCode == -1)
			{
				Bimp.drr.add(path);
			}
			break;
		}
	}

}
