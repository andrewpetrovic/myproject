package com.itic.mobile.zfyj.qh;

import java.util.HashMap;
import java.util.Map;

/**
 * Config
 */
public class Config {
    public static long accessId = 2100085283L;
    public static String accessKey = "A5SJ1H7R8T8X";
    public static String sKey = "7de3b36b62d822bfa18f1cdf0c14977e";

    public static final String ACCOUNT_TYPE = "com.itic.mobile.zfyj.qh.account";
    public static final String SERVER_HOST = "http://211.101.150.10:8881/qhyj";


    //通用
    public static final String KEY_USERID = "a";
    public static final String KEY_USERCATEGORY = "ul";
    public static final String KEY_SUBMIT_TYPE = "t";
    public static final String KEY_FLAG = "flag";
    public static final String KEY_ROWS = "rows";
    public static final String KEY_SID = "sid";
    public static final String VALUE_USERCATEGORY_SERVANT = "0";
    public static final String VALUE_USERCATEGORY_LEADER = "1";
    public static final String VALUE_USERCATEGORY_SECRETARY = "2";

    //登陆
    public static final String KEY_USERNAME = "n";
    public static final String KEY_PWD = "p";
    public static final String KEY_TOKEN = "t";

    //日期
    public static final String KEY_DATE = "d";

    //JOBS
    public static final String KEY_FORM_ID = "l";
    public static final String KEY_AM_JOB = "am";
    public static final String KEY_PM_JOB = "pm";
    public static final String KEY_NOTE = "note";
    public static final String KEY_LDID = "ldid";
    public static final String KEY_NAME = "name";

    public static final int RESULT_STATUS_SUCCESS = 0;
    public static final int RESULT_STATUS_ERROR_SEVER_STATUS = 1;
    public static final int RESULT_STATUS_ERROR_SID = 2;
    public static final int RESULT_STATUS_ERROR_USERNAME_PWD = 3;
    public static final int RESULT_STATUS_ERROR_TOKEN = 4;
    public static final int RESULT_STATUS_ERROR_PARAM = 5;
    public static final int RESULT_STATUS_ERROR_PUSH_REGISTER = 6;
    public static final int RESULT_STATUS_ERROR_CONNECTION = 90;

    //设置、删除 tag
    public static final int BROADCAST_SET_TAG_SUCCESS = 0;
    public static final int BROADCAST_DELETE_TAG_SUCCESS = 1;
    public static final int BROADCAST_SET_TAG_FAIL= 2;
    public static final int BROADCAST_DELETE_TAG_FAIL = 3;

    // 通讯录Tags
    public interface ContactsTypes {
        // type categories
        public static final String CATEGORY_DWTXL = "TYPE_DWTXL";
        public static final String CATEGORY_BMTXL = "TYPE_BMTXL";
        public static final String CATEGORY_GRTXL = "TYPE_GRTXL";

    }
}
