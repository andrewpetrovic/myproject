package com.itic.mobile.zfyj.qh.test;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.itic.mobile.zfyj.qh.R;

/**
 * 这里用一句话描述这个类的作用
 *
 * @author Andrea Ji
 */
public class TestFragment extends ListFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_test, container, false);
        return root;
    }
}
