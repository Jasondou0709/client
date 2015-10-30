package com.example.microdemo.adapter;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.microdemo.ImagePagerActivity;
import com.example.microdemo.MyApplication;

import io.rong.app.R;
import io.rong.app.fragment.CustomerFragment;
import io.rong.imkit.widget.AsyncImageView;

import com.example.microdemo.domain.FirendMicroListDatas;
import com.example.microdemo.domain.FirstMicroListDatasFirendcomment;
import com.example.microdemo.domain.OwnerMicro;
import com.example.microdemo.util.FastjsonUtil;
import com.example.microdemo.util.MyCustomDialog;
import com.sea_monster.resource.Resource;
//import com.loveplusplus.demo.image.ImagePagerActivity;
//import com.loveplusplus.demo.image.MyGridAdapter;
//import com.loveplusplus.demo.image.NoScrollGridView;



public class MyListAdapter extends BaseAdapter {

	private static final String TAG = "MyListAdapter";
	private LayoutInflater mInflater;
	private Context mContext;// 上下文
	String id; // 当前item在数据中的编号id
	String replyid;// 回复人id
	String replyname;// 回复人姓名
	String ownerid;// 登录用户id
	String ownername;// 登录用户name
	private boolean praise = false;// 是否已经点赞了
									// true:已经点赞了，这样textView上面应该显示“取消”；false:没有点赞，textView上面应该显示“点赞”；默认为false
	private int[] expressionAllImgs;// 图片地址1
	private String[] expressionAllImgNames;// 图片名1
	// 定义操作面板状态常量
	public static final int PANEL_STATE_GONE = 0;
	public static final int PANEL_STATE_VISIABLE = 1;
	// 操作面板状态
	public static int panelState = PANEL_STATE_GONE;
	private List<FirendMicroListDatas> mList = new ArrayList<FirendMicroListDatas>();// json数据
	private FirendMicroListDatas bean = new FirendMicroListDatas();// 总的实体类
	private List<FirstMicroListDatasFirendcomment> fConnent = new ArrayList<FirstMicroListDatasFirendcomment>();// 评论
	private List<String> friendpraise = new ArrayList<String>();// 点赞
	private FirstMicroListDatasFirendcomment f = new FirstMicroListDatasFirendcomment();// 评论完了暂时存到这里
	private boolean submitflag = false;// 提交状态，如果某条消息评论，点赞状态有更新，则置true，否则为false
	
	// String obSid="";//sid表示消息的id
	String sImages = "";
	int indexOf = -1;
	private String praiseflag = "";// 点赞标示，判断这个人有没有点过
	
	private OwnerMicro ownerdata;

	public MyListAdapter(Context context, List<FirendMicroListDatas> list, OwnerMicro mOwnerdata) {
		mInflater = LayoutInflater.from(context);
		mContext = context;
		this.mList = list;
		ownerdata = mOwnerdata;
	}
	
	public void notifyDataSetChangedEx(List<FirendMicroListDatas> mLists) {
		this.mList.clear();
		this.mList = mLists;

		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mList == null ? 0 : mList.size();
	}

	@Override
	public FirendMicroListDatas getItem(int position) {
		return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return Integer.parseInt(getItem(position).getId());
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;

		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater.inflate(R.layout.micro_list_item, null);
			holder.layout = (LinearLayout) convertView
					.findViewById(R.id.layout);
			holder.layoutParise = (LinearLayout) convertView
					.findViewById(R.id.layoutParise);
			holder.layout01 = (LinearLayout) convertView
					.findViewById(R.id.layout01);
			holder.liearLayoutIgnore = (LinearLayout) convertView
					.findViewById(R.id.liearLayoutIgnore);
			holder.text = (TextView) convertView.findViewById(R.id.text);
			holder.view = (TextView) convertView.findViewById(R.id.view);
			holder.time = (TextView) convertView.findViewById(R.id.time);
			holder.avator = (AsyncImageView) convertView.findViewById(R.id.avator);
			holder.name = (TextView) convertView.findViewById(R.id.name);
			holder.content = (TextView) convertView.findViewById(R.id.content);
			holder.btnIgnore = (Button) convertView
					.findViewById(R.id.btnIgnore);
			holder.btnComment = (Button) convertView
					.findViewById(R.id.btnComment);
			holder.btnPraise = (Button) convertView
					.findViewById(R.id.btnPraise);

			holder.gridView = (NoScrollGridView) convertView
					.findViewById(R.id.gridView);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.btnComment.setTag(getItem(position).getId());
		// holder.btnPraise.setTag(getItem(position).getPraiseflag());//点赞标示，用来判断是否点过
		bean = getItem(position);// 总的实体类
		// fImage=bean.getFriendimage();//图片
		fConnent = bean.getFriendcomment();// 评论
		friendpraise = bean.getFriendpraise();// 点赞
		replyname = bean.getUname();
		replyid = bean.getUid();
		id = bean.getId();

		// }

		// ****************************************************

		if (bean.urls != null && bean.urls.length > 0) {
			holder.gridView.setVisibility(View.VISIBLE);
			holder.gridView.setAdapter(new MyGridAdapter(bean.urls, mContext));
			holder.gridView
					.setOnItemClickListener(new AdapterView.OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> parent,
								View view, int position, long id) {
							String[] urls =  ((MyGridAdapter)(parent.getAdapter())).getAllUrls();
							imageBrower(position, urls);
						}
					});
		} else {
			holder.gridView.setVisibility(View.GONE);
		}

		// ****************************************************

		/*
		 * 显示时间 服务器返回的时间是：年-月-日 时：分，所以获取的时候应该是yyyy-MM-dd HH:mm
		 */
		String strTime = bean.getSendtime().trim();
		Log.d("=========","strTime" +strTime);
		if (!"".equals(strTime)) {
			SimpleDateFormat sDateFormat = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm");
			String date = sDateFormat.format(new java.util.Date());

			String t = getTimes(date, strTime);
			Log.i(TAG, "时间差" + t);
			holder.time.setText(t);
		}

		/*
		 * 显示姓名
		 */
		holder.name.setText(bean.getUname());// 姓名
		
		//显示头像
		if (bean.getUsericon() !=null && !bean.getUsericon().equalsIgnoreCase("")) {
			holder.avator.setResource(new Resource(bean.getUsericon()));
		}
		// 加载内容（文字和表情）
		String strExpression = bean.getContent();
		holder.content.setText(Html.fromHtml(strExpression));// 如果要表情的话，把这个去掉，然后把下面的加上就行了
		/*
		 * 引入表情 expressionAllImgs = Expressions.expressionAllImgs;
		 * expressionAllImgNames = Expressions.expressionAllImgNames; int i=0;
		 * String c=""; String s=""; Bitmap bitmap = null;
		 * holder.content.setText(""); if(UtilTool.isProperHTML(strExpression)){
		 * holder.content.append(Html.fromHtml(strExpression)); }
		 * while(i<strExpression.length()){ c=strExpression.substring(i, i+1);
		 * if("[".equals(c)){ s=strExpression.substring(i, i+7); for(int
		 * j=0;j<expressionAllImgNames.length;j++){
		 * if(s.equals(expressionAllImgNames[j])){ i+=7; bitmap = null; bitmap =
		 * BitmapFactory.decodeResource(mContext.getResources(),
		 * expressionAllImgs[j % expressionAllImgs.length]); ImageSpan imageSpan
		 * = new ImageSpan(mContext, bitmap); SpannableString spannableString =
		 * new SpannableString( expressionAllImgNames[j]);
		 * spannableString.setSpan(imageSpan, 0,
		 * expressionAllImgNames[j].length() ,
		 * Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); // 编辑框设置数据
		 * 
		 * holder.content.append(spannableString); } } }else{ i++;
		 * holder.content.append(Html.fromHtml(c)); } }
		 */

		// 显示评论、点赞按钮

		holder.btnIgnore.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				// obSid=holder.btnComment.getTag().toString();
				praise = false;
				for (String p : getItem(position)
						.getFriendpraise()) {
					if (null != p) {
						if (p.equals(
								ownerdata.getOwnername())) {
							praise = true;
							break;
						}
					}
				}

				if (praise) {
					holder.btnPraise.setText("取消?");
				} else {
					holder.btnPraise.setText("点赞?");
				}

				if (1 == panelState) {
					panelState = PANEL_STATE_GONE;
					switchPanelState(holder.liearLayoutIgnore,
							holder.btnComment, holder.btnPraise);
				} else {
					panelState = PANEL_STATE_VISIABLE;
					switchPanelState(holder.liearLayoutIgnore,
							holder.btnComment, holder.btnPraise);
				}
			}
		});

		// 评论按钮
		holder.btnComment.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				// 显示评论的对话框
				MyCustomDialog dialog = new MyCustomDialog(mContext,
						R.style.add_dialog, "评论" + bean.getUname() + "的说说",
						new MyCustomDialog.OnCustomDialogListener() {
							// 点击对话框'提交'以后
							public void back(String content) {
								// 先隐藏再提交评论
								panelState = PANEL_STATE_GONE;
								switchPanelState(holder.liearLayoutIgnore,
										holder.btnComment, holder.btnPraise);
								// submitComment(position,obSid,bean.getCompanykey(),bean.getUid(),bean.getUname(),content);//提交评论
								submitComment(position, bean.getUid(),
										bean.getUname(), content);// 提交评论
							}
						});
				dialog.setCanceledOnTouchOutside(true);// 设置点击Dialog外部任意区域关闭Dialog
				dialog.show();
			}
		});

		// 点赞按钮 praise:是否已经点赞了
		// true:已经点赞了，这样textView上面应该显示“取消”；false:没有点赞，textView上面应该显示“点赞”；默认为false
		holder.btnPraise.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// 先隐藏再提交评论
				panelState = PANEL_STATE_GONE;
				switchPanelState(holder.liearLayoutIgnore, holder.btnComment,
						holder.btnPraise);
				// submitPraise(position,obSid,bean.getCompanykey(),bean.getUid(),bean.getUname());//点赞或取消
				submitPraise(position, bean.getUid(), bean.getUname());// 点赞或取消
			}
		});

		// 显示点赞holder.layoutParise friendpraise
		holder.layoutParise.removeAllViews();
		holder.view.setVisibility(View.GONE);
		holder.layout01.setVisibility(View.GONE);
		if (0 != friendpraise.size()) {// 有数据，控件显示
			holder.layout01.setVisibility(View.VISIBLE);
			holder.layoutParise.setVisibility(View.VISIBLE);

			LinearLayout ll = new LinearLayout(mContext);
			ll.setOrientation(LinearLayout.HORIZONTAL);
			ll.layout(3, 3, 3, 3);

			ImageView i1 = new ImageView(mContext);
			i1.setBackgroundResource(R.drawable.micro_praise_button);
			i1.setLayoutParams(new LayoutParams(20, 18));
			TextView t2 = new TextView(mContext);
			t2.setTextColor(0xff2C78B8);
			t2.setTextSize(11);
			ll.addView(i1);

			StringBuffer uName = new StringBuffer();
			uName.append(" ");
			for (String p : friendpraise) {
				if (null != p) {
					uName.append(p + " ,");
				}
			}
			uName.deleteCharAt(uName.length() - 1);
			t2.setText(uName);
			ll.addView(t2);
			holder.layoutParise.addView(ll);
		}

		// 显示评论
		holder.layout.removeAllViews();
		if (0 != fConnent.size()) {
			holder.layout01.setVisibility(View.VISIBLE);
			holder.layout.setVisibility(View.VISIBLE);
			if (0 != friendpraise.size()) {
				holder.view.setVisibility(View.VISIBLE);
			}
			for (FirstMicroListDatasFirendcomment f : fConnent) {
				if (null != f.getReplyId()) {
					LinearLayout ll = new LinearLayout(mContext);
					ll.setOrientation(LinearLayout.HORIZONTAL);
					ll.layout(3, 3, 3, 3);
					TextView t1 = new TextView(mContext);
					TextView t2 = new TextView(mContext);
					t1.setText(" " + f.getReplyName() + ":");
					t1.setTextColor(0xff2C78B8);
					t1.setTextSize(13);
					t2.setTextSize(13);
					t2.setText(f.getComment());
					ll.addView(t1);
					ll.addView(t2);
					holder.layout.addView(ll);
				}
			}
		}
		return convertView;
	}

	// ****************************************************
	private void imageBrower(int position, String[] urls) {
		Intent intent = new Intent(mContext, ImagePagerActivity.class);
		// 图片url,为了演示这里使用常量，一般从数据库中或网络中获取
		intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_URLS, urls);
		intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_INDEX, position);
		mContext.startActivity(intent);
	}

	// ****************************************************

	/**
	 * 提交评论
	 * 
	 * @param replycompanykey
	 *            公司标识位
	 * @param sid
	 *            消息主键 replyid; 回复人id replyname; 回复人姓名
	 * @param isreplyid
	 *            被回复人ID
	 * @param isreplyname
	 *            被回复人姓名
	 * @param content
	 *            评论内容
	 */
	// private void submitComment(int position, String sid, String companykey,
	// final String isreplyid, final String isreplyname, final String content) {
	private void submitComment(int position, final String isreplyid,
			final String isreplyname, final String content) {
		// TODO Auto-generated method stub
		FirstMicroListDatasFirendcomment p = new FirstMicroListDatasFirendcomment();
		// SimpleDateFormat sDateFormat = new
		// SimpleDateFormat("yyyy-MM-dd HH:mm");
		// String date = sDateFormat.format(new java.util.Date());
		List<FirstMicroListDatasFirendcomment> friendcomment_temp;
		submitflag = true;
		friendcomment_temp = getItem(position).getFriendcomment();
		final String id = getItem(position).getId();
		p.setReplyId(ownerdata.getOwnerid());
		p.setReplyName(ownerdata.getOwnername());
		// p.setId(id);
		p.setIsReplyId(isreplyid);
		p.setIsReplyName(isreplyname);
		p.setComment(content);
		// p.setReplytime(date);
		// p.setUsercommentflag("Y");
		friendcomment_temp.add(p);
		Log.i(TAG, "content->" + content);
		Toast.makeText(mContext, "提交评论", 0).show();
		getItem(position).setFriendcomment(friendcomment_temp);
		
		new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				HttpClient httpClient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(
				        "http://moments.daoapp.io/api/v1.0/posts/" + id+"/comments/");
				httppost.setHeader("Authorization", "Basic " + MyApplication.getBase64Code());

				JSONObject jsonParam = new JSONObject();
				JSONArray array = new JSONArray();

				jsonParam.put("comment", content);// 标题
				jsonParam.put("isreplyname", isreplyname);
				jsonParam.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));

				StringEntity entity = null;
				try {
					entity = new StringEntity(jsonParam.toString(), "utf-8");
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				entity.setContentEncoding("UTF-8");
				entity.setContentType("application/json");
				httppost.setEntity(entity);
				System.out.println("executing request "
				        + httppost.getRequestLine());

				HttpResponse response = null;
				String postid = null;
				try {
					response = httpClient.execute(httppost);

					HttpEntity ret = response.getEntity();
					String str = EntityUtils.toString(ret);
					Log.d("=====", str  + " str = ");

				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
			
		}).start();
		
		
		
		notifyDataSetChanged(); // 提交之后刷新页面，以便及时显示
	}

	
	class myRunnable implements Runnable{
		String itemid;
		public myRunnable(String id){
			this.itemid = id;
		}
		@Override
        public void run() {
			HttpClient httpClient = new DefaultHttpClient();
			//FirendMicroListDatas data = getItem(position);
			HttpPost httppost = new HttpPost(
			        "http://moments.daoapp.io/api/v1.0/posts/" + itemid
			                + "/praise");
			httppost.setHeader("Authorization", "Basic " + MyApplication.getBase64Code());
			System.out.println("executing request " + httppost.getRequestLine());

			HttpResponse response = null;
			String postid = null;
			try {
				response = httpClient.execute(httppost);

				HttpEntity ret = response.getEntity();
				String str = EntityUtils.toString(ret);
				Log.d("====", "reply = " + str);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
            
        }
		
	}
	/**
	 * 点赞 praise:是否已经点赞了
	 * true:已经点赞了，这样textView上面应该显示“取消”；false:没有点赞，textView上面应该显示“点赞”；默认为false
	 * 
	 * @param 被点赞人sid
	 *            消息主键
	 * @param 被点赞人companykey
	 *            公司标识位
	 * @param uid
	 *            点赞人用户ID
	 * @param uname
	 *            被点赞人用户名
	 */
	// private void submitPraise(int position,String sid, String companykey,
	// String uid,String uname) {
	private void submitPraise(int position, String uid, String uname) {

		// TODO Auto-generated method stub
		//FirstMicroListDatasFirendpraise p = new FirstMicroListDatasFirendpraise();
		List<String> friendpraise_temp;
		submitflag = true;
		int count_temp = 0;
		friendpraise_temp = getItem(position).getFriendpraise();
		String p = ownerdata.getOwnername();

		// p.setId(id);
		if (praise) {
			count_temp = friendpraise_temp.size();
			if (count_temp != 0) {
				count_temp--;
				friendpraise_temp.remove(count_temp);
			}
			Toast.makeText(mContext, "取消", 0).show();
		} else {

			friendpraise_temp.add(p);
			Toast.makeText(mContext, "点赞", 0).show();
		}
		getItem(position).setFriendpraise(friendpraise_temp);
		FirendMicroListDatas data = getItem(position);

		new Thread(new myRunnable(data.getId())).start();

		notifyDataSetChanged(); // 提交之后刷新页面，以便及时显示
	}

	/**
	 * 评论点赞，隐藏显示 操作面板显示状态
	 */
	private void switchPanelState(LinearLayout liearLayoutIgnore,
			Button btnComment, Button btnPraise) {
		// TODO Auto-generated method stub
		switch (panelState) {
		case PANEL_STATE_GONE:

			liearLayoutIgnore.setVisibility(View.GONE);
			btnComment.setVisibility(View.GONE);
			btnPraise.setVisibility(View.GONE);
			break;
		case PANEL_STATE_VISIABLE:
			// holder.liearLayoutIgnore.startAnimation(animation);//评论的显示动画
			liearLayoutIgnore.setVisibility(View.VISIBLE);
			btnComment.setVisibility(View.VISIBLE);
			btnPraise.setVisibility(View.VISIBLE);
			break;
		}
	}

	/**
	 * 仿qq或微信的时间显示 时间比较 date 当前时间 strTime 获取的时间
	 */
	private String getTimes(String date, String strTime) {
		// TODO Auto-generated method stub
		String intIime = "";
		long i = -1;// 获取相差的天数
		long i1 = -1;// 获取相差的小时
		long i2 = -1;// 获取相差的分
		long i3 = -1;// 获取相差的
		try {
			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm");
			ParsePosition pos = new ParsePosition(0);
			ParsePosition pos1 = new ParsePosition(0);
			Date dt1 = formatter.parse(date, pos);
			Date dt2 = formatter.parse(strTime, pos1);
			long l = dt1.getTime() - dt2.getTime();

			i = l / (1000 * 60 * 60 * 24);// 获取的如果是0，表示是当天的，如果>0的话是以前发的
			if (0 == i) {// 今天发的
				i1 = l / (1000 * 60 * 60);
				if (0 == i1) {// xx分之前发的
					i2 = l / (1000 * 60);
					// if(0==i2){//xx秒之前发的
					// i3=l/(1000);
					// intIime=i3+"秒以前";
					// }else{
					// intIime=i2+"分以前";
					// }
				} else {
					intIime = i1 + "时以前";// xx小时之前发的
				}

			} else {// 以前发的
				intIime = i + "天以前";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return intIime;
	}

	private static class ViewHolder {
		public TextView name, text, view, time;
		public AsyncImageView avator;
		public Button btnIgnore, btnComment, btnPraise;
		public TextView content;
		public LinearLayout liearLayoutIgnore, layout, layoutParise, layout01;
		NoScrollGridView gridView;
	}

}
