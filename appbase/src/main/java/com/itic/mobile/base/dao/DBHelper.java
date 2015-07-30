package com.itic.mobile.base.dao;

//import com.itic.mobile.qhmz.dao.GGTXLDataHelper;
//import com.itic.mobile.qhmz.dao.GGTXLListDataHelper;
//import com.itic.mobile.qhmz.dao.GZZDataHelper;
//import com.itic.mobile.qhmz.dao.GZZListDataHelper;
//import com.itic.mobile.qhmz.dao.GZZRYDataHelper;
//import com.itic.mobile.qhmz.dao.GZZRYListDataHelper;
//import com.itic.mobile.qhmz.dao.TXLMLDataHelper;
//import com.itic.mobile.qhmz.dao.TXLRYDataHelper;
//import com.itic.mobile.qhmz.dao.TXLRYListDataHelper;
//import com.itic.mobile.qhmz.dao.XXYDetailDataHelper;
//import com.itic.mobile.qhmz.dao.XXYListDataHelper;
//import com.itic.mobile.qhmz.dao.ZHZLDataHelper;
//import com.itic.mobile.qhmz.dao.ReportListDataHelper;
//import com.itic.mobile.qhmz.dao.ZQListDataHelper;
//import com.itic.mobile.qhmz.dao.ReportDetailDataHelper;
//import com.itic.mobile.qhmz.dao.ZZJGDataHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 
 * @author andrew
 *
 */
public class DBHelper extends SQLiteOpenHelper {
    // 数据库名
    private static final String DB_NAME = "qhmz.db";

    // 数据库版本
    private static final int VERSION = 1;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
//        ZHZLDataHelper.ZHZLDBInfo.TABLE.create(db);
//        ZZJGDataHelper.ZZJGDBInfo.TABLE.create(db);
//        TXLMLDataHelper.TXLMLDBInfo.TABLE.create(db);
//        ReportDetailDataHelper.ReportDetailDBInfo.TABLE.create(db);
//        ZQListDataHelper.ZQListDBInfo.TABLE.create(db);
//        ReportListDataHelper.ReportListDBInfor.TABLE.create(db);
//        TXLRYListDataHelper.TXLRYListDBInfo.TABLE.create(db);
//        TXLRYDataHelper.TXLRYDBInfo.TABLE.create(db);
//        GGTXLListDataHelper.GGTXLListDBInfo.TABLE.create(db);
//        GGTXLDataHelper.GGTXLDBInfo.TABLE.create(db);
//        XXYListDataHelper.XXYListDBInfor.TABLE.create(db);
//        XXYDetailDataHelper.XXYDBInfo.TABLE.create(db);
//        GZZListDataHelper.GZZListDBInfo.TABLE.create(db);
//        GZZDataHelper.GZZDBInfo.TABLE.create(db);
//        GZZRYListDataHelper.GZZRYListDBInfo.TABLE.create(db);
//        GZZRYDataHelper.GZZRYDBInfo.TABLE.create(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
