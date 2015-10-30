package io.rong.app.activity;

import com.sea_monster.resource.Resource;

import io.rong.app.DemoContext;
import io.rong.app.R;
import io.rong.app.utils.Constants;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imlib.model.Group;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ChangeGroupActivity extends BaseActionBarActivity {
	
	private static final String TAG = "ChangeGroupActivity";
	private TextView mComment;
	private TextView mGroupName;
	private AsyncImageView mImageView;
	private Button mSelectButton;
	private Button mChooseButton;
	private String mClassId;
	private RelativeLayout mRelativeLayout;
	private Group mGroup;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.change_group);
        getSupportActionBar().setTitle(R.string.change_group);
        
        mComment = (TextView) findViewById(R.id.change_group_comment);
        mGroupName = (TextView) findViewById(R.id.group_adaper_name);
        mImageView = (AsyncImageView) findViewById(R.id.group_adapter_img);
        mSelectButton = (Button) findViewById(R.id.group_select);
        mChooseButton = (Button) findViewById(R.id.group_choose);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.relativelayout);
        
        if (DemoContext.getInstance() != null) {
			mClassId = DemoContext.getInstance().getClassId();
			Log.d(TAG, "DemoContext.getInstance().getClassId():" + mClassId);
		}
        
        initialData();
	}
	
	private void initialData() {
		if (mClassId == null || mClassId.equalsIgnoreCase("0")) {
			mComment.setText("您没有设置朋友圈中的班级，请设置：");
			mRelativeLayout.setVisibility(View.GONE);
			mChooseButton.setVisibility(View.VISIBLE);
		} else {
			if (DemoContext.getInstance() != null) {
				mGroup = DemoContext.getInstance().getGroupById(mClassId);
			}			
			if (mGroup != null) {
				mGroupName.setText(mGroup.getName());
				Log.d(TAG, "mGroup.getPortraitUri():" + mGroup.getPortraitUri());
				/*if (mGroup.getPortraitUri() != null && !mGroup.getPortraitUri().toString().equalsIgnoreCase("")) {
					mImageView.setResource(new Resource(mGroup.getPortraitUri()));
				}*/
			}
		}
		
		if (mChooseButton != null) {
			mChooseButton.setOnClickListener(new Button.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					startActivityForResult(new Intent(ChangeGroupActivity.this, SelectGroupActivity.class), 20);
				}
			});
		}
		if (mSelectButton != null) {
			mSelectButton.setOnClickListener(new Button.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					startActivityForResult(new Intent(ChangeGroupActivity.this, SelectGroupActivity.class), 20);
				}
			});
		}
	}	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == 1) {
			mGroupName.setText(data.getStringExtra("CLASSNAME"));
			//mImageView.setResource(new Resource(data.getStringExtra("CLASSPORTRAIT")));
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
