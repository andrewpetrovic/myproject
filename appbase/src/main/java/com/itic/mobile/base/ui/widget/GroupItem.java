package com.itic.mobile.base.ui.widget;

import com.itic.mobile.base.model.BaseModel;

/**
 * Created by andrew on 2014/9/8.
 */
public class GroupItem {
    public static final int ITEM = 0;
    public static final int SECTION = 1;

    public final int type;
    public final String text;
    public final BaseModel model;

    public int sectionPosition;
    public int listPosition;

    public boolean showDivider = false;

    public GroupItem(int type, String text, BaseModel model) {
        this.type = type;
        this.text = text;
        this.model = model;
    }

    @Override
    public String toString() {
        return text;
    }

    public boolean isShowDivider() {
        return showDivider;
    }

    public void setShowDivider(boolean showDivider) {
        this.showDivider = showDivider;
    }
}
