package com.itic.mobile.zfyj.qh.jobs.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

/**
 * Created by JEEKR on 2015/3/27.
 */
public class JobsDataContainer extends LinearLayout {

    private BaseAdapter adapter;
    private OnClickListener onClickListener = null;

    public JobsDataContainer(Context context) {
        super(context);
    }

    public JobsDataContainer(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public JobsDataContainer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }



    public void bindLinearLayout() {
        int count = adapter.getCount();
//        this.removeAllViews();
        for (int i = 0; i < count; i++) {
            View v = adapter.getView(i, null, null);
            v.setOnClickListener(this.onClickListener);
            if (i == count - 1) {
                LinearLayout ly = (LinearLayout) v;
                ly.removeViewAt(2);
            }
            addView(v, i);
        }
    }

    /**
     * 获取Adapter
     *
     * @return adapter
     */
    public BaseAdapter getAdpater() {
        return adapter;
    }

    /**
     * 设置数据
     *
     * @param adpater
     */
    public void setAdapter(BaseAdapter adpater) {
        this.adapter = adpater;
        bindLinearLayout();
    }

    /**
     * 获取点击事件
     *
     * @return
     */
    public OnClickListener getOnclickListner() {
        return onClickListener;
    }

    /**
     * 设置点击事件
     *
     * @param onClickListener
     */
    public void setOnclickLinstener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }
}
