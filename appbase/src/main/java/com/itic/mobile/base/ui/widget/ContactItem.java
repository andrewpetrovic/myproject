package com.itic.mobile.base.ui.widget;

import com.itic.mobile.base.model.BaseModel;

/**
 * Created by andrew on 2014/9/19.
 */
public class ContactItem {
    public static final int PHONE_BGDH = 0;
    public static final int CELL_PHONE = 1;
    public static final int PHONE_CZDH = 2;
    public static final int EMAIL = 3;
    public static final int WZ = 4;
    public static final int PHONE_JTDH = 5;
    public static final int DIZHI = 6;
    public static final int IDENTITY = 7;
    public static final int PEIXUN = 8;
    public static final int SZDW = 9;
    public static final int ZW = 10;

    public final int type;
    public final String text;
    public final BaseModel model;

    public boolean showPhoneIcon = false;
    public boolean showSMSBtn = false;
    public boolean showDivider = false;

    public ContactItem(int type,String text, BaseModel model){
        this.type = type;
        this.text = text;
        this.model = model;
    }

    @Override
    public String toString() {
        return text;
    }

    public boolean isShowSMSBtn() {
        return showSMSBtn;
    }

    public void setShowSMSBtn(boolean showSMSBtn) {
        this.showSMSBtn = showSMSBtn;
    }

    public boolean isShowPhoneIcon() {
        return showPhoneIcon;
    }

    public void setShowPhoneIcon(boolean showPhoneIcon) {
        this.showPhoneIcon = showPhoneIcon;
    }

    public boolean isShowDivider() {
        return showDivider;
    }

    public void setShowDivider(boolean showDivider) {
        this.showDivider = showDivider;
    }
}
