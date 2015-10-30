package io.rong.app.activity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import io.rong.app.R;
import io.rong.app.activity.MyAccountActivity;

import com.example.testpic.AlbumHelper;
import com.example.testpic.Bimp;
import com.example.testpic.ImageGridAdapter;
import com.example.testpic.ImageGridAdapter.TextCallback;
import com.example.testpic.ImageItem;
import com.example.testpic.PublishedActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class SelectPictureGridActivity extends Activity
{
	public static final String EXTRA_IMAGE_LIST = "imagelist";

	// ArrayList<Entity> dataList;//鐢ㄦ潵瑁呰浇鏁版嵁婧愮殑鍒楄〃
	List<ImageItem> dataList;
	GridView gridView;
	ImageGridAdapter adapter;// 鑷畾涔夌殑閫傞厤鍣�
	AlbumHelper helper;
	Button bt;

	Handler mHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case 0:
				Toast.makeText(SelectPictureGridActivity.this, "最多选择9张图片", 400).show();
				break;

			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_image_grid);

		helper = AlbumHelper.getHelper();
		helper.init(getApplicationContext());

		dataList = (List<ImageItem>) getIntent().getSerializableExtra(
				EXTRA_IMAGE_LIST);

		initView();
		bt = (Button) findViewById(R.id.bt);
		bt.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{

			}

		});
	}

	private void initView()
	{
		gridView = (GridView) findViewById(R.id.gridview);
		gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		adapter = new ImageGridAdapter(SelectPictureGridActivity.this, dataList,
				mHandler);
		gridView.setAdapter(adapter);
		adapter.setTextCallback(new TextCallback()
		{
			public void onListen(int count)
			{
				bt.setText("完成" + "(" + count + ")");
				Log.i("select picture","222222222222");
				Log.i("select picture","count->"+count);
				ArrayList<String> list = new ArrayList<String>();
				Collection<String> c = adapter.map.values();
				Iterator<String> it = c.iterator();
				adapter.map.values().iterator().next();
				Log.i("select picture"," list.size()"+ list.size());
				for (; it.hasNext();)
				{
					list.add(it.next());
				}
				Log.i("select picture"," list.size()"+ list.size());
//				if (Bimp.act_bool)
//				{
//					setResult(Activity.RESULT_OK);
//					Bimp.act_bool = false;
//				}
//				for (int i = 0; i < list.size(); i++)
//				{
//					if (Bimp.drr.size() < 9)
//					{
						Bimp.drr.clear();
						Bimp.drr.add(list.get(0));
						String path = Bimp.drr.get(Bimp.max);
						System.out.println(path);
						Log.i("picture address","path->"+path);
//					}
//				}
				Intent intent=new Intent();
				intent.putExtra("path", path);
				Log.i("SelectPictureGridActivity"," path="+ path);				
				setResult(Activity.RESULT_OK,intent);
				Log.i("SelectPictureGridActivity"," list.size()"+ list.size());
				finish();


			}
		});

		gridView.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				adapter.notifyDataSetChanged();
			}

		});

	}
}
