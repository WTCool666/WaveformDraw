package com.example.waveform.utils;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.waveform.R;

public class TitleView extends FrameLayout {
    private TextView tv_title,tv_menu;
    private ImageView iv_back;
    public TitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.title, this);
        tv_title = (TextView) findViewById(R.id.tv_title);
        iv_back = (ImageView) findViewById(R.id.iv_back);
        tv_menu = (TextView) findViewById(R.id.tv_menu);
        iv_back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Activity) getContext()).finish();
            }
        });
    }

    public void setBackOnClickListener(OnClickListener listener){
        iv_back.setOnClickListener(listener);
    }

    /**
     * 设置标题
     * @param text
     */
    public void setTitleText(String text) {
        tv_title.setText(text);
    }

    public void setMenuVisibility(int visibility){
        tv_menu.setVisibility(visibility);
    }

    public void setMenuOnClickListener(OnClickListener listener){
        tv_menu.setOnClickListener(listener);
    }
    public void setMenuText(String text){
        tv_menu.setText(text);
    }

    public void setMenuTextSize(float textSize){
        tv_menu.setTextSize(textSize);
    }
    public void setMenuTextColor(int textColor){
        tv_menu.setTextColor(textColor);
    }
    /**
     * 隐藏返回按钮
     */
    public void controlBackImage(int visibility){
        iv_back.setVisibility(visibility);
    }

}
