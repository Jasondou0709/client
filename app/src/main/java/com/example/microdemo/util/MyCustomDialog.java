package com.example.microdemo.util;

import io.rong.app.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AbsListView.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

/**
 * 右上点击出现列表pop
 * 
 * @author GURR 2014-9-13
 */
public class MyCustomDialog extends Dialog {

	 //定义回调事件，用于dialog的点击事件
    public interface OnCustomDialogListener{
            public void back(String name);
    }
    
    private String name;
    private OnCustomDialogListener customDialogListener;
    EditText etName;
    private RelativeLayout relayComment;

    public MyCustomDialog(Context context,int theme,String name,OnCustomDialogListener customDialogListener) {
    	
            super(context,theme);
            this.name = name;
            this.customDialogListener = customDialogListener;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) { 
            super.onCreate(savedInstanceState);
            setContentView(R.layout.micro_comment);
            //设置标题
            setTitle(name);
            relayComment=(RelativeLayout) findViewById(R.id.relayComment);
            relayComment.getBackground().setAlpha(0);
            etName = (EditText)findViewById(R.id.microComment);
            Button clickBtn = (Button) findViewById(R.id.microSubmit);
            clickBtn.setOnClickListener(clickListener);
    }
    
    private View.OnClickListener clickListener = new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
            	if(!String.valueOf(etName.getText()).equals("")){
                    customDialogListener.back(String.valueOf(etName.getText()));
                    MyCustomDialog.this.dismiss();
            	}
            }
    };

}
