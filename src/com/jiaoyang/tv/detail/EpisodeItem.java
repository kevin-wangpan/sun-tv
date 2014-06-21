package com.jiaoyang.tv.detail;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.jiaoyang.video.tv.R;

public class EpisodeItem extends RelativeLayout {

    private ImageView mMark;
    private Button mButton;

    public EpisodeItem(Context context, AttributeSet attrs) {
        super(context, attrs);

        initView();
    }

    public void setMarkVisible(boolean visible) {
        mMark.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setText(CharSequence text) {
        mButton.setText(text);
    }

    public void setTextSize(int unit, float textSize) {
        mButton.setTextSize(unit, textSize);
    }

    public float getTextSize() {
        return mButton.getTextSize();
    }

    public void setTextGravity(int gravity) {
        mButton.setGravity(gravity);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        mButton.setOnClickListener(l);
    }

    @Override
    public void setTag(Object tag) {
        mButton.setTag(tag);
    }

    @Override
    public void setTag(int key, Object tag) {
        mButton.setTag(key, tag);
    }

    @Override
    public Object getTag() {
        return mButton.getTag();
    }

    @Override
    public Object getTag(int key) {
        return mButton.getTag(key);
    }

    @Override
    public void setBackgroundResource(int resid) {
        int bottom = mButton.getPaddingBottom();
        int top = mButton.getPaddingTop();
        int right = mButton.getPaddingRight();
        int left = mButton.getPaddingLeft();
        mButton.setBackgroundResource(resid);
        mButton.setPadding(left, top, right, bottom);
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.episode_item, this, true);

        mMark = (ImageView) findViewById(R.id.mark);
        mButton = (Button) findViewById(R.id.button);
    }
}
