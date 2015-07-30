package com.itic.mobile.base.dao;

import com.itic.mobile.app.BaseApp;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;
/**
 * 
 * @author andrew
 *
 */
public class DataProvider extends ContentProvider {
	
	public static final String TAG = DataProvider.class.getSimpleName();
	
	public static final Object DBLock = new Object();
	
	public static final String AUTHORITY = "com.itic.mobile.provider";

    public static final String SCHEME = "content://";

    // messages
    public static final String PATH_ZHZL = "/zhzl";
    public static final String PATH_TXLML = "/txlml";
    public static final String PATH_TXLRY = "/txlry";
    public static final String PATH_ZZJG = "/zzjg";
    public static final String PATH_ZQXQ = "/zqxq";
    public static final String PATH_LOGIN = "/login";

    public static final String PATH_ZQ_LIST = "/zqlist";
    public static final String PATH_ZQ_INFO_LIST = "/zqinfolist";
    public static final String PATH_RYTXL_LIST = "/rytxllist";
    public static final String PATH_GGTXL_LIST = "/ggtxllist";
    public static final String PATH_GGTXL = "/ggtxl";
    public static final String PATH_XXY_LIST = "/xxylist";
    public static final String PATH_XXY = "/xxy";
    public static final String PATH_GZZ_LIST = "/gzzlist";
    public static final String PATH_GZZ = "/gzz";
    public static final String PATH_GZZRY_LIST = "/gzzrylist";
    public static final String PATH_GZZRY = "/gzzry";

    /**
     * 灾害种类
     */
    public static final Uri ZHZL_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_ZHZL);
    /**
     * 通讯录目录
     */
    public static final Uri TXLML_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_TXLML);
    /**
     * 通讯录人员
     */
    public static final Uri TXLRY_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_TXLRY);
    /**
     * 组织机构
     */
    public static final Uri ZZJG_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_ZZJG);
    /**
     * 灾情详情
     */
    public static final Uri ZQXQ_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_ZQXQ);
    /**
     * 登录
     */
    public static final Uri LOGIN_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_LOGIN);

    public static final Uri  ZQ_LIST_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_ZQ_LIST);
    public static final Uri  ZQ_INFO_LIST_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_ZQ_INFO_LIST);
    public static final Uri  RYTXL_LIST_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_RYTXL_LIST);
    public static final Uri  GGTXL_LIST_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_GGTXL_LIST);
    public static final Uri  GGTXL_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_GGTXL);
    public static final Uri  XXYLIST_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_XXY_LIST);
    public static final Uri  XXY_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_XXY);
    public static final Uri  GZZLIST_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_GZZ_LIST);
    public static final Uri  GZZ_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_GZZ);
    public static final Uri  GZZRYLIST_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_GZZRY_LIST);
    public static final Uri  GZZRY_CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_GZZRY);



    private static final int ZHZL = 0; //灾害种类
    private static final int TXLML = 1; //通讯录目录
    private static final int TXLRY = 2; //通讯录人员
    private static final int ZZJG = 3; //组织机构
    private static final int ZQXQ = 4; //灾情详情
    private static final int LOGIN = 5; //登录

    private static final int ZQ_LIST = 6;//灾情列表
    private static final int ZQ_INFO_LIST = 7;//灾情初/续/核报列表
    private static final int RYTXL_LIST = 8;//通讯录人员列表
    private static final int GGTXL_LIST = 9;
    private static final int GGTXL = 10;
    private static final int XXY_LIST = 11;
    private static final int XXY = 12;
    private static final int GZZ_LIST = 13;
    private static final int GZZ = 14;
    private static final int GZZRY_LIST = 15;
    private static final int GZZRY = 16;
    
    /*
     * MIME type definitions
     */
    public static final String ZHZL_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.itic.mobile.zhzl";
    public static final String TXLML_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.itic.mobile.txlml";
    public static final String TXLRY_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.itic.mobile.txlry";
    public static final String ZZJG_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.itic.mobile.zzjg";
    public static final String ZQXQ_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.itic.mobile.zqxq";
    public static final String LOGIN_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.itic.mobile.login";

    public static final String ZQ_LIST_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.itic.mobile.zqlist";
    public static final String ZQ_INFO_LIST_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.itic.mobile.zqinfolist";
    public static final String RYTXL_LIST_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.itic.mobile.rytxllist";
    public static final String GGTXL_LIST_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.itic.mobile.ggtxllist";
    public static final String GGTXL_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.itic.mobile.ggtxl";
    public static final String XXY_LIST_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.itic.mobile.xxylist";
    public static final String XXY_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.itic.mobile.xxy";
    public static final String GZZLIST_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.itic.mobile.gzzlist";
    public static final String GZZ_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.itic.mobile.gzz";
    public static final String GZZRYLIST_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.itic.mobile.gzzrylist";
    public static final String GZZRY_CONTENT_TYPE = "vnd.android.cursor.dir/vnd.itic.mobile.gzzry";
    
	private static final UriMatcher sUriMatcher;
	
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, "zhzl", ZHZL);
        sUriMatcher.addURI(AUTHORITY, "txlml", TXLML);
        sUriMatcher.addURI(AUTHORITY, "txlry", TXLRY);
        sUriMatcher.addURI(AUTHORITY, "zzjg", ZZJG);
        sUriMatcher.addURI(AUTHORITY, "zqxq", ZQXQ);
        sUriMatcher.addURI(AUTHORITY, "login", LOGIN);
        sUriMatcher.addURI(AUTHORITY, "zqlist", ZQ_LIST);
        sUriMatcher.addURI(AUTHORITY, "zqinfolist",ZQ_INFO_LIST);
        sUriMatcher.addURI(AUTHORITY, "rytxllist",RYTXL_LIST);
        sUriMatcher.addURI(AUTHORITY, "ggtxllist",GGTXL_LIST);
        sUriMatcher.addURI(AUTHORITY, "ggtxl",GGTXL);
        sUriMatcher.addURI(AUTHORITY,"xxylist",XXY_LIST);
        sUriMatcher.addURI(AUTHORITY,"xxy",XXY);
        sUriMatcher.addURI(AUTHORITY,"gzzlist",GZZ_LIST);
        sUriMatcher.addURI(AUTHORITY,"gzz",GZZ);
        sUriMatcher.addURI(AUTHORITY,"gzzrylist",GZZRY_LIST);
        sUriMatcher.addURI(AUTHORITY,"gzzry",GZZRY);
    }
    
    private String matchTable(Uri uri) {
        String table = "ttt";
//        switch (sUriMatcher.match(uri)) {
//            case ZHZL:
//                table = ZHZLDataHelper.ZHZLDBInfo.TABLE_NAME;
//                break;
//            case TXLML:
//            	table = TXLMLDataHelper.TXLMLDBInfo.TABLE_NAME;
//            	break;
//            case TXLRY:
//            	table = TXLRYDataHelper.TXLRYDBInfo.TABLE_NAME;
//            	break;
//            case ZZJG:
//            	table = ZZJGDataHelper.ZZJGDBInfo.TABLE_NAME;
//            	break;
//            case ZQXQ:
//            	table = ReportDetailDataHelper.ReportDetailDBInfo.TABLE_NAME;
//            	break;
//            case ZQ_LIST:
//                table = ZQListDataHelper.ZQListDBInfo.TABLE_NAME;
//                break;
//            case ZQ_INFO_LIST:
//                table = ReportListDataHelper.ReportListDBInfor.TABLE_NAME;
//                break;
//            case RYTXL_LIST:
//                table = TXLRYListDataHelper.TXLRYListDBInfo.TABLE_NAME;
//                break;
//            case GGTXL_LIST:
//                table = GGTXLListDataHelper.GGTXLListDBInfo.TABLE_NAME;
//                break;
//            case GGTXL:
//                table = GGTXLDataHelper.GGTXLDBInfo.TABLE_NAME;
//                break;
//            case XXY_LIST:
//                table = XXYListDataHelper.XXYListDBInfor.TABLE_NAME;
//                break;
//            case XXY:
//                table = XXYDetailDataHelper.XXYDBInfo.TABLE_NAME;
//                break;
//            case GZZ_LIST:
//                table = GZZListDataHelper.GZZListDBInfo.TABLE_NAME;
//                break;
//            case GZZ:
//                table = GZZDataHelper.GZZDBInfo.TABLE_NAME;
//                break;
//            case GZZRY_LIST:
//                table = GZZRYListDataHelper.GZZRYListDBInfo.TABLE_NAME;
//                break;
//            case GZZRY:
//                table = GZZRYDataHelper.GZZRYDBInfo.TABLE_NAME;
//                break;
//            default:
//                throw new IllegalArgumentException("Unknown URI " + uri);
//        }
        return table;
    }
    
    private static DBHelper mDBHelper;

    public static DBHelper getDBHelper() {
        if (mDBHelper == null) {
            mDBHelper = new DBHelper(BaseApp.getContext());
        }
        return mDBHelper;
    }
    
	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
        synchronized (DBLock) {
            SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
            String table = matchTable(uri);
            queryBuilder.setTables(table);

            SQLiteDatabase db = getDBHelper().getReadableDatabase();
            Cursor cursor = queryBuilder.query(db, // The database to
                    // queryFromDB
                    projection, // The columns to return from the queryFromDB
                    selection, // The columns for the where clause
                    selectionArgs, // The values for the where clause
                    null, // don't group the rows
                    null, // don't filter by row groups
                    sortOrder // The sort order
            );

            cursor.setNotificationUri(getContext().getContentResolver(), uri);
            return cursor;
        }
	}

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        switch (sUriMatcher.match(uri)) {
            case ZHZL:
                return ZHZL_CONTENT_TYPE;
            case ZZJG:
                return ZZJG_CONTENT_TYPE;
            case ZQ_LIST:
                return ZQ_LIST_CONTENT_TYPE;
            case ZQ_INFO_LIST:
                return ZQ_INFO_LIST_CONTENT_TYPE;
            case ZQXQ:
                return ZQXQ_CONTENT_TYPE;
            case RYTXL_LIST:
                return RYTXL_LIST_CONTENT_TYPE;
            case GGTXL_LIST:
                return GGTXL_LIST_CONTENT_TYPE;
            case GGTXL:
                return GGTXL_CONTENT_TYPE;
            case XXY_LIST:
                return XXY_LIST_CONTENT_TYPE;
            case XXY:
                return XXY_CONTENT_TYPE;
            case GZZ_LIST:
                return GZZLIST_CONTENT_TYPE;
            case GZZ:
                return GZZ_CONTENT_TYPE;
            case GZZRY_LIST:
                return GZZRYLIST_CONTENT_TYPE;
            case GZZRY:
                return GZZRY_CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    /**
     * 插入时，某条记录不存在则插入，存在则更新。
     */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
        synchronized (DBLock) {
            String table = matchTable(uri);
            SQLiteDatabase db = getDBHelper().getWritableDatabase();
            long rowId = 0;
            db.beginTransaction();
            try {
                //这里使用replace方法而不是insert方法
                //因为创建表的时候也创建了唯一索引,实现replace的saveOrUpdate操作
                rowId = db.replace(table,null,values);
                db.setTransactionSuccessful();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            } finally {
                db.endTransaction();
            }
            if (rowId > 0) {
                Uri returnUri = ContentUris.withAppendedId(uri, rowId);
                getContext().getContentResolver().notifyChange(uri, null);
                return returnUri;
            }
            throw new SQLException("Failed to insert row into " + uri);
        }
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
        synchronized (DBLock) {
            SQLiteDatabase db = getDBHelper().getWritableDatabase();
            int count = 0;
            String table = matchTable(uri);
            db.beginTransaction();
            try {
                count = db.delete(table, selection, selectionArgs);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            getContext().getContentResolver().notifyChange(uri, null);
            return count;
        }
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
        synchronized (DBLock) {
            SQLiteDatabase db = getDBHelper().getWritableDatabase();
            int count;
            String table = matchTable(uri);
            db.beginTransaction();
            try {
                count = db.update(table, values, selection, selectionArgs);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
            getContext().getContentResolver().notifyChange(uri, null);

            return count;
        }
	}
}
