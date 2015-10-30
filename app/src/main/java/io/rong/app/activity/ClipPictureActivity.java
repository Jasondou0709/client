package io.rong.app.activity;

import io.rong.app.R;
import io.rong.app.ui.ClipView;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.example.microdemo.util.FastjsonUtil;
import com.example.testpic.Bimp;
import com.example.testpic.FileUtils;
//import com.example.testpic.PublishedActivity.postreplycls;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

	public class ClipPictureActivity extends Activity implements OnTouchListener,
			OnClickListener
	{
		ImageView srcPic;
		Button sure;
		ClipView clipview;
		
		// These matrices will be used to move and zoom image
		Matrix matrix = new Matrix();
		Matrix savedMatrix = new Matrix();

		// We can be in one of these 3 states
		static final int NONE = 0;
		static final int DRAG = 1;
		static final int ZOOM = 2;
		private static final String TAG = "11";
		int mode = NONE;

		// Remember some things for zooming
		PointF start = new PointF();
		PointF mid = new PointF();
		float oldDist = 1f;
		private HashMap<String, SoftReference<Bitmap>> imageCache = new HashMap<String, SoftReference<Bitmap>>();


		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_clippicture);
			
			srcPic = (ImageView) this.findViewById(R.id.src_pic);
    	    Bundle bun=getIntent().getExtras(); 
    	    String clipfilepath=bun.getString("clipfilepath");
			 Bitmap bm = BitmapFactory.decodeFile(clipfilepath); 
			 srcPic.setImageBitmap(bm);//不会变形
			 
			srcPic.setOnTouchListener(this);

			sure = (Button) this.findViewById(R.id.sure);
			sure.setOnClickListener(this);
			
		}

		/*����ʵ���˶�㴥���Ŵ���С���͵����ƶ�ͼƬ�Ĺ��ܣ��ο�����̳�Ĵ���*/
		public boolean onTouch(View v, MotionEvent event)
		{
			ImageView view = (ImageView) v;
			// Handle touch events here...
			switch (event.getAction() & MotionEvent.ACTION_MASK)
				{
				case MotionEvent.ACTION_DOWN:
					savedMatrix.set(matrix);
					// �O�ó�ʼ�cλ��
					start.set(event.getX(), event.getY());
					Log.d(TAG, "mode=DRAG");
					mode = DRAG;
					break;
				case MotionEvent.ACTION_POINTER_DOWN:
					oldDist = spacing(event);
					Log.d(TAG, "oldDist=" + oldDist);
					if (oldDist > 10f)
					{
						savedMatrix.set(matrix);
						midPoint(mid, event);
						mode = ZOOM;
						Log.d(TAG, "mode=ZOOM");
					}
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_UP:
					mode = NONE;
					Log.d(TAG, "mode=NONE");
					break;
				case MotionEvent.ACTION_MOVE:
					if (mode == DRAG)
					{
						// ...
						matrix.set(savedMatrix);
						matrix.postTranslate(event.getX() - start.x, event.getY()
								- start.y);
					} else if (mode == ZOOM)
					{
						float newDist = spacing(event);
						Log.d(TAG, "newDist=" + newDist);
						if (newDist > 10f)
						{
							matrix.set(savedMatrix);
							float scale = newDist / oldDist;
							matrix.postScale(scale, scale, mid.x, mid.y);
						}
					}
					break;
				}

			view.setImageMatrix(matrix);
			return true; // indicate event was handled
		}

		/** Determine the space between the first two fingers */
		private float spacing(MotionEvent event)
		{
			float x = event.getX(0) - event.getX(1);
			float y = event.getY(0) - event.getY(1);
			return FloatMath.sqrt(x * x + y * y);
		}

		/** Calculate the mid point of the first two fingers */
		private void midPoint(PointF point, MotionEvent event)
		{
			float x = event.getX(0) + event.getX(1);
			float y = event.getY(0) + event.getY(1);
			point.set(x / 2, y / 2);
		}

		Bitmap fianBitmap;
		@SuppressLint("NewApi") public void onClick(View v)
		{
			fianBitmap = getBitmap();
			saveMyBitmap("portarit", fianBitmap);
//			String text = newPostEt.getText().toString();
//			newpostTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, text);
			PortraitupdateTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);

		}

		private Bitmap getBitmap()
		{
			getBarHeight();
			Bitmap screenShoot = takeScreenShot();
		
			clipview = (ClipView)this.findViewById(R.id.clipview);
			int width = clipview.getWidth();
			int height = clipview.getHeight();
			Bitmap finalBitmap = Bitmap.createBitmap(screenShoot,
					(width - height / 2) / 2, height / 4 + titleBarHeight + statusBarHeight, height / 2, height / 2);
			return finalBitmap;
		}

		int statusBarHeight = 0;
		int titleBarHeight = 0;

		private void getBarHeight()
		{
			Rect frame = new Rect();
			this.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
			statusBarHeight = frame.top;
			
			int contenttop = this.getWindow()
					.findViewById(Window.ID_ANDROID_CONTENT).getTop();
			// statusBarHeight
			titleBarHeight = contenttop - statusBarHeight;
			
			Log.v(TAG, "statusBarHeight = " + statusBarHeight
					+ ", titleBarHeight = " + titleBarHeight);
		}

		private Bitmap takeScreenShot()
		{
			View view = this.getWindow().getDecorView();
			view.setDrawingCacheEnabled(true);
			view.buildDrawingCache();
			return view.getDrawingCache();
		}

		private void saveMyBitmap(String bitName,Bitmap mBitmap)
		{
//			if (!TextUtils.isEmpty(bitName) && mBitmap != null)
//			{
//				imageCache.put(bitName, new SoftReference<Bitmap>(mBitmap));
//			}		

			File dir=new File(Environment.getExternalStorageDirectory() + "/Baomeng/userdata/"); 
			if(!dir.exists())dir.mkdirs(); 		
			File f = new File(dir,bitName + ".png");
			  try {
			   f.createNewFile();
			  } catch (IOException e) {
			   // TODO Auto-generated catch block
			   Log.i("ClipPictureActivity","在保存图片时出错："+e.toString());
			  }
			  FileOutputStream fOut = null;
			  try {
			   fOut = new FileOutputStream(f);
			  } catch (FileNotFoundException e) {
			   e.printStackTrace();
			  }
			  mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
			  try {
			   fOut.flush();
			  } catch (IOException e) {
			   e.printStackTrace();
			  }
			  try {
			   fOut.close();
			  } catch (IOException e) {
			   e.printStackTrace();
			  }
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
		private AsyncTask<String,Void,String> PortraitupdateTask = new AsyncTask<String,Void,String>(){

			@Override
			protected String doInBackground(String... params) {
				String filepath = Environment.getExternalStorageDirectory()+ "/Baomeng/userdata/";
				List<String> list = new ArrayList<String>();
				HttpClient httpClient = new DefaultHttpClient();
				HttpPost post = new HttpPost("http://ppzimg.daoapp.io/upload");

					FileEntity entity = new FileEntity(new File(Environment.getExternalStorageDirectory()+"/Baomeng/userdata/portarit.png"),
							"png");
					post.setEntity(entity);
					post.setHeader("Content-type", "png");
					HttpResponse response = null;
					String content = null;
					try {
						response = httpClient.execute(post);
						content = EntityUtils.toString(response.getEntity());
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Log.i("ClipPictureActivity","在上传图片时出错ParseException："+e.toString());
					} catch (IOException e) {
						e.printStackTrace();
						Log.i("ClipPictureActivity","在上传图片时出错IOException："+e.toString());
					}
					String id = content.substring(content.indexOf("md5")+6,content.indexOf("size")-3);
					//postreplycls result = FastjsonUtil.json2object(content, postreplycls.class);
					Log.i("=====","update protrait successful id->"+id);
					list.add("http://ppzimg.daoapp.io/"+id);
					FileUtils.deleteDir(filepath);	
					return id;

				
//		        HttpPost httppost = new HttpPost("http://moments.daoapp.io/api/v1.0/posts/");
//		        httppost.setHeader("Authorization", "Basic " + MyApplication.getBase64Code());
//		
//		        JSONObject jsonParam = new JSONObject();
//		        JSONArray array = new JSONArray();
//		        array.addAll(list);
//		        
//		        jsonParam.put("content", params[0]);// 标题
//		        jsonParam.put("urls", array);
////		        jsonParam.put("class_id", (classIdList != null && classIdList.size() == 0) ? "" :classIdList.get(0));
//		        jsonParam.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
//		        StringEntity entity = null;
//				try {
//					entity = new StringEntity(jsonParam.toString(), "utf-8");
//				} catch (UnsupportedEncodingException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}// 解决中文乱码问题
//		        entity.setContentEncoding("UTF-8");
//		        entity.setContentType("application/json");
//		        httppost.setEntity(entity);
//		        System.out.println("executing request " + httppost.getRequestLine());
//		
//		        HttpResponse response = null;
//		        String postid = null ;
//				try {
//					response = httpClient.execute(httppost);
//					
//					HttpEntity ret = response.getEntity();
//					String str = EntityUtils.toString(ret);
////					postreplycls result = FastjsonUtil.json2object(
////							str, postreplycls.class);
////					postid = result.id;
////					Log.d("=====",postid + "");
//
//				}catch (ParseException  e) {
//		            // TODO Auto-generated catch block
//		            e.printStackTrace();
//		        }catch(IOException e){
//		        	e.printStackTrace();
//		        }
//				// 高清的压缩图片全部就在 list 路径里面了
//				// 高清的压缩过的 bmp 对象 都在 Bimp.bmp里面
//				// 完成上传服务器后 .........
//				FileUtils.deleteDir();
//				return postid;
			}
		    protected void onPostExecute(String result) {
//				ByteArrayOutputStream baos = new ByteArrayOutputStream();			
//				fianBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//				byte[] bitmapByte = baos.toByteArray();
//				Intent intent=new Intent();
//				intent.putExtra("bitmap", bitmapByte);
//				setResult(Activity.RESULT_OK,intent);
		    	setResult(Activity.RESULT_OK);
				finish();
		    }
			
			
		};

		
		
	}