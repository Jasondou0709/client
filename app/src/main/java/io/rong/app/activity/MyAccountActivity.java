package io.rong.app.activity;


import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar.LayoutParams;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import io.rong.app.DemoContext;
import io.rong.app.R;
import io.rong.app.utils.Constants;

import com.example.testpic.Bimp;
import com.example.testpic.FileUtils;
import com.example.testpic.TestPicActivity;
import com.sea_monster.resource.Resource;

import io.rong.imkit.widget.AsyncImageView;

/**
 * Created by Administrator on 2015/3/2.
 */
public class MyAccountActivity extends BaseActionBarActivity implements View.OnClickListener {
	
	private static final String TAG = "MyAccountActivity";
    private static final int RESULTCODE = 10;
    ;
    /**
     * 头像
     */
    private RelativeLayout mMyPortrait;
    /**
     * 昵称
     */
    private RelativeLayout mMyUsername;

    private TextView mTVUsername;
    private AsyncImageView mImgMyPortrait;
    private String mUserName;
    private Context mContext = null;
    private PopupWindow popupWindow;
	private HashMap<String, SoftReference<Bitmap>> imageCache = new HashMap<String, SoftReference<Bitmap>>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.de_ac_myaccount);
        initView();

    }


    protected void initView() {
        getSupportActionBar().setTitle(R.string.de_actionbar_myacc);

        mImgMyPortrait = (AsyncImageView) findViewById(R.id.img_my_portrait);
        mMyPortrait = (RelativeLayout) findViewById(R.id.rl_my_portrait);
        mMyUsername = (RelativeLayout) findViewById(R.id.rl_my_username);
        mTVUsername = (TextView) findViewById(R.id.tv_my_username);
        if (DemoContext.getInstance().getSharedPreferences() != null) {
//            String userId = DemoContext.getInstance().getSharedPreferences().getString("DEMO_USER_ID", null);
            mUserName = DemoContext.getInstance().getSharedPreferences().getString("DEMO_USER_NAME", null);
            String userPortrait = DemoContext.getInstance().getSharedPreferences().getString("DEMO_USER_PORTRAIT", "defalte");
            mImgMyPortrait.setResource(new Resource(Uri.parse(userPortrait)));
            mTVUsername.setText(mUserName.toString());
        }

        /*if(fileIsExists(Environment.getExternalStorageDirectory() + "/Baomeng/userdata/portarit.png"))
        {
        	Log.i("MyAccountActivity","true");
        	mImgMyPortrait.setImageBitmap(BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/Baomeng/userdata/portarit.png"));
        }*/        
        mMyPortrait.setOnClickListener(this);
        mMyUsername.setOnClickListener(this);
        mContext=this;
//        mResourceHandler = new ResourceHandler.Builder().enableBitmapCache().build(this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    	Bitmap bitmap = null;
    	
    	Log.i("select image","result->  path="+path+" requestCode="+requestCode+" TAKE_PICTURE="+TAKE_PICTURE);
        switch (resultCode) {
            case Constants.FIX_USERNAME_REQUESTCODE:
                if (data != null) {
                    mTVUsername.setText(data.getStringExtra("UPDATA_RESULT"));
                    mUserName = data.getStringExtra("UPDATA_RESULT");
                }
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
        switch (requestCode) {
    	case TAKE_PICTURE:
//    		mImgMyPortrait.setImageBitmap(BitmapFactory.decodeFile(path));
			Intent intent_picture = new Intent(MyAccountActivity.this,ClipPictureActivity.class);
			intent_picture.putExtra("clipfilepath",path );
			startActivityForResult(intent_picture, CLIP_PICTURE);       		
    		break;  
    	case TAKE_PICTURE_ALBUM:
    	    Bundle b=data.getExtras(); //data为B中回传的Intent
    	    String str=b.getString("path");//str即为回传的值
    	    Log.i("return picture","MyAccountActivity:str->"+str);
    	    
			Intent intent = new Intent(MyAccountActivity.this,ClipPictureActivity.class);
			intent.putExtra("clipfilepath", str);
			startActivityForResult(intent, CLIP_PICTURE);    
    		break;   
    	case CLIP_PICTURE:
    		String portrait = data.getStringExtra("portraituri");
    		mImgMyPortrait.setResource(new Resource(portrait));
    		if (DemoContext.getInstance() != null) {
    			DemoContext.getInstance().setPortrait(portrait);
    		}
    		SharedPreferences.Editor edit = DemoContext.getInstance().getSharedPreferences().edit();
            edit.putString("DEMO_USER_PORTRAIT", portrait);
            edit.apply();
            SetPortraitTask task = new SetPortraitTask();
            task.execute(portrait);
//			byte[] bis = data.getByteArrayExtra("bitmap");
//			bitmap = BitmapFactory.decodeByteArray(bis, 0, bis.length);
//			SoftReference<Bitmap> reference = imageCache.get("portarit");
//			bitmap = reference.get(); 
//			if(bitmap!=null)
//			{
//				mImgMyPortrait.setImageBitmap(bitmap);
//			}else
//			{
//				Toast.makeText(MyAccountActivity.this, "更新头像失败",Toast.LENGTH_LONG).show(); 
//			}
    		break;
    		
    }
  
        
    }

    private void showPopupWindow(View view) {

        // 一个自定义的布局，作为显示的内容
        View contentView = LayoutInflater.from(mContext).inflate(
        R.layout.de_ac_popup_window, null);
        popupWindow = new PopupWindow(contentView,
        LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, true);
        
        // 设置按钮的点击事件
        Button buttonphoto = (Button) contentView.findViewById(R.id.buttonphoto);
        buttonphoto.setOnClickListener(this);
        Button buttonalbum = (Button) contentView.findViewById(R.id.buttonalbum);
        buttonalbum.setOnClickListener(this);
        popupWindow.setTouchable(true);


        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        // 我觉得这里是API的一个bug
//        popupWindow.setBackgroundDrawable(getResources().getDrawable(
//                R.drawable.selectmenu_bg_downward));
        popupWindow.setBackgroundDrawable(new BitmapDrawable());

        // 设置好参数之后再show
        popupWindow.showAsDropDown(view);

    }

    @Override
    public void onClick(View v) {
    	switch (v.getId()) {  
    		case  R.id.buttonphoto:  
    			photo();
    			popupWindow.dismiss();
    			break;
    		case R.id.buttonalbum:
    			Intent intent = new Intent(MyAccountActivity.this,SelectPictureActivity.class);
//    			startActivity(intent);
    			startActivityForResult(intent, TAKE_PICTURE_ALBUM);    			
    			popupWindow.dismiss();
    			break;
            case R.id.rl_my_portrait://头像
            	showPopupWindow(v);
                break;

            case R.id.rl_my_username://昵称
                Intent intent_username = new Intent(this, UpdateNameActivity.class);
                intent_username.putExtra("USERNAME", mUserName);
                startActivityForResult(intent_username, RESULTCODE);
                break;
    	}
    }    
    
	private static final int TAKE_PICTURE = 0x000000;
	private static final int TAKE_PICTURE_ALBUM = 0x000001;	
	private static final int CLIP_PICTURE = 0x000002;	
	
	private String path = "";
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
			Log.i("select image","photo->  path="+path);
			Uri imageUri = Uri.fromFile(file);
			openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
			openCameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
			startActivityForResult(openCameraIntent, TAKE_PICTURE);
		}
		else{ 
			Toast.makeText(MyAccountActivity.this, "没有储存卡",Toast.LENGTH_LONG).show(); 
			} 
	}	    

	public boolean fileIsExists(String strFile)  
    {  
        try  
        {  
            File f=new File(strFile);  
            if(!f.exists())  
            {  
                    return false;  
            }  
  
        }  
        catch (Exception e)  
        {  
            return false;  
        }  
  
        return true;  
    } 

	private class SetPortraitTask extends AsyncTask<String, Void, String> {

    	@Override
		protected String doInBackground(String... arg0) {
    		HttpClient client = new DefaultHttpClient();

			HttpPost httpPost = new HttpPost("http://moments.daoapp.io/api/v1.0/users/changeportrait");
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("portrait", arg0[0]));
	     

			String result = null;
			try {
				String md5 = LoginActivity.password;
				String encoding  = Base64.encodeToString(new String(LoginActivity.username +":"+md5).getBytes(), Base64.NO_WRAP);
				Log.d(TAG, "password= " + md5 + "userName = " + LoginActivity.username + "encoding:" + encoding);
				httpPost.setHeader("Authorization", "Basic " + encoding);
				httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
				HttpResponse response = client.execute(httpPost);
				Log.d(TAG, "searchuser result code = " + response.getStatusLine().getStatusCode());
				if (response.getStatusLine().getStatusCode() == 200) {
					result = EntityUtils.toString(response.getEntity());
					Log.d(TAG, "searchuser result = " + result);
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
					String status = jsonObject.getString("status");   
					if (status.equalsIgnoreCase("200")) {
						Log.d(TAG, "set portrait success");
					} else {
						Log.d(TAG, "set portrait fail");
					}
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}			
			}  
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

		}
	    protected void onPostExecute(String result) {
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();			
//			fianBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
//			byte[] bitmapByte = baos.toByteArray();
//			Intent intent=new Intent();
//			intent.putExtra("bitmap", bitmapByte);
//			setResult(Activity.RESULT_OK,intent);
	    	Log.d(TAG, "portraituri:" + "http://ppzimg.daoapp.io/" + result);
	    	Intent intent = new Intent();
	    	intent.putExtra("portraituri", "http://ppzimg.daoapp.io/" + result);
	    	setResult(RESULT_OK, intent);
			finish();
	    }
		
		
	};
}
