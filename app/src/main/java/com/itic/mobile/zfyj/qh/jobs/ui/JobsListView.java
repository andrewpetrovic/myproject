package com.itic.mobile.zfyj.qh.jobs.ui;

import android.content.Context;
import android.util.AttributeSet;

import com.itic.mobile.base.ui.widget.CollectionView;

/**
 * Created by JEEKR on 2015/3/27.
 */
public class JobsListView extends CollectionView{

    public JobsListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
                MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
