package io.rong.app.activity;

import java.io.Serializable;
import java.util.List;

import com.example.testpic.AlbumHelper;
import com.example.testpic.ImageBucket;
import com.example.testpic.ImageBucketAdapter;
import com.example.testpic.ImageGridActivity;
import io.rong.app.R;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class SelectPictureActivity extends Activity
{
	// ArrayList<Entity> dataList;//用来装载数据源的列表
	List<ImageBucket> dataList;
	GridView gridView;
	ImageBucketAdapter adapter;// 自定义的适配器
	AlbumHelper helper;
	public static final String EXTRA_IMAGE_LIST = "imagelist";
	public static Bitmap bimap;
	private static final int SELECT_FINISH = 100;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_bucket);

		helper = AlbumHelper.getHelper();
		helper.init(getApplicationContext());

		initData();
		initView();
	}

	/**
	 * 初始化数据
	 */
	private void initData()
	{
		dataList = helper.getImagesBucketList(false);
		bimap = BitmapFactory.decodeResource(getResources(),
				R.drawable.icon_addpic_unfocused);
	}

	/**
	 * 初始化view视图
	 */
	private void initView()
	{
		gridView = (GridView) findViewById(R.id.gridview);
		adapter = new ImageBucketAdapter(SelectPictureActivity.this, dataList);
		gridView.setAdapter(adapter);

		gridView.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				/**
				 * 根据position参数，可以获得跟GridView的子View相绑定的实体类，然后根据它的isSelected状态，
				 * 来判断是否显示选中效果。 至于选中效果的规则，下面适配器的代码中会有说明
				 */
				/**
				 * 通知适配器，绑定的数据发生了改变，应当刷新视图
				 */
				Intent intent = new Intent(SelectPictureActivity.this,
						SelectPictureGridActivity.class);
				intent.putExtra(SelectPictureActivity.EXTRA_IMAGE_LIST,
						(Serializable) dataList.get(position).imageList);
				startActivityForResult(intent, SELECT_FINISH);
			}

		});
	}

	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode!=Activity.RESULT_OK){
            return;
        }
         
        switch (requestCode) {
         
        case SELECT_FINISH:
    	    Bundle b=data.getExtras(); //data为B中回传的Intent
    	    String str=b.getString("path");//str即为回传的值
    	    Log.i("return picture","SelectPictureActivity:str->");
			setResult(Activity.RESULT_OK,data);    	    
            finish();
            break;
 
        default:
            break;
        }
	}
}
