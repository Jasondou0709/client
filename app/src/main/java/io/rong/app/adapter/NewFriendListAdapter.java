package io.rong.app.adapter;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.List;
import com.sea_monster.resource.Resource;
import io.rong.app.R;
import io.rong.app.model.ApiResult;
import io.rong.app.model.RequestInfo;
import io.rong.imkit.widget.AsyncImageView ;

/**
 * Created by Bob on 2015/3/26.
 */

public class NewFriendListAdapter extends android.widget.BaseAdapter {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private List<RequestInfo> mResults;
    OnItemButtonClick mOnItemButtonClick;

    public OnItemButtonClick getOnItemButtonClick() {
        return mOnItemButtonClick;
    }

    public void setOnItemButtonClick(OnItemButtonClick onItemButtonClick) {
        this.mOnItemButtonClick = onItemButtonClick;
    }

    public NewFriendListAdapter(List<RequestInfo> results, Context context){
        this.mResults = results;
        this.mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }


    @Override
    public int getCount() {
    	if(mResults != null) {
    		return mResults.size();
    	} else {
    		return 0;
    	}
    }

    @Override
    public Object getItem(int position) {
        return mResults.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("NewApi")
	@Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if(convertView == null || convertView.getTag() == null){
            convertView = mLayoutInflater.inflate(R.layout.de_item_friend,parent,false);
            viewHolder = new ViewHolder();
            viewHolder.mFrienduUserName = (TextView) convertView.findViewById(R.id.item_friend_username);
//            viewHolder.mFrienduStateNo = (ImageView) convertView.findViewById(R.id.item_friend_state_no);
//            viewHolder.mFrienduStateYes = (ImageView) convertView.findViewById(R.id.item_friend_state_yes);
            viewHolder.mFrienduState = (TextView) convertView.findViewById(R.id.item_friend_state);
            viewHolder.mPortraitImg = (AsyncImageView) convertView.findViewById(R.id.item_friend_portrait);
            convertView.setTag(viewHolder);
        }else{
            convertView.getTag();
        }

        if(viewHolder != null) {
        	if(mResults.get(position).getPortrait() == null || (mResults.get(position).getPortrait() != null && mResults.get(position).getPortrait().equalsIgnoreCase(""))) {
        		viewHolder.mPortraitImg.setDefaultDrawable(mContext.getResources().getDrawable(R.drawable.rc_default_portrait));
        	} else {
        		Resource res = new Resource(mResults.get(position).getPortrait());
        		viewHolder.mPortraitImg.setResource(res);
        	}
            viewHolder.mFrienduUserName.setText(mResults.get(position).getName());
            viewHolder.mFrienduState.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mOnItemButtonClick !=null)
                        mOnItemButtonClick.onButtonClick(position, v,mResults.get(position).getStatus());
                }
            });
                switch (mResults.get(position).getStatus()){
                    case 0://好友
                    	if (mResults.get(position).getClassId() != null) {
                    		viewHolder.mFrienduState.setText("请求添加到班级：" + mResults.get(position).getClassName());
                    	} else {
                    		viewHolder.mFrienduState.setText("请求添加你为好友");
                    	}
                        break;
                    case 1://已添加
                    	if (mResults.get(position).getClassId() != null) {
                    		viewHolder.mFrienduState.setText("已添加到班级：" + mResults.get(position).getClassName());
                    	} else {
                    		viewHolder.mFrienduState.setText("已添加为好友");
                    	}                     
                        viewHolder.mFrienduState.setBackground(null);
                        break;

                }
        }

        return convertView;
    }

    public interface OnItemButtonClick{
        public boolean onButtonClick(int position, View view,int status);

    }

    static class ViewHolder{
        TextView mFrienduUserName;

        TextView mFrienduState;
        AsyncImageView mPortraitImg;
    }
}
