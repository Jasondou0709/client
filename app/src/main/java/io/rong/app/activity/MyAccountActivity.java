package io.rong.app.activity;


import java.io.File;
import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar.LayoutParams;
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
import com.example.testpic.TestPicActivity;
import com.sea_monster.resource.Resource;
import io.rong.imkit.widget.AsyncImageView;

/**
 * Created by Administrator on 2015/3/2.
 */
public class MyAccountActivity extends BaseActionBarActivity implements View.OnClickListener {

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
    		mImgMyPortrait.setImageBitmap(BitmapFactory.decodeFile(path));
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
	
}
