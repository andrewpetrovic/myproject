package com.itic.mobile.zfyj.qh.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Helper类，使用 {@link SQLiteDatabase} 存储 {@link Provider} 的数据
 *
 * @author Andrea Ji
 */
public class Database extends SQLiteOpenHelper {

    private static final String TAG = "Database";

    private static final String DATABASE_NAME = "qhyj.db";

    private static final int VER_2015_RELEASE_A = 1;

    private static final int CUR_DATABASE_VERSION = VER_2015_RELEASE_A;

    private final Context mContext;

    interface Tables {
        String CONTACTS = "contacts";
        String CONTACT_TYPES = "contact_types";
        String CONTACT_TYPES_MAP = "contact_types_map";
        String MY_CONTACTS = "my_contacts";
        String JOBS = "jobs";
        String MY_JOBS = "my_jobs";

        String CONTACTS_JOIN_CONTACT_TYPE_MY_CONTACTS = "contacts "
                + "LEFT OUTER JOIN my_contacts ON contacts.contact_id = my_contacts.contact_id "
                + "AND my_contacts.account_name=? "
                + "LEFT OUTER JOIN contact_types_map ON contacts.contact_id = contact_types_map.contact_id ";

        String JOBS_JOIN_MY_JOBS = "jobs "
                + "LEFT OUTER JOIN my_jobs ON jobs.job_id = my_jobs.job_id "
                + "AND my_jobs.account_name=? ";
    }

    /**
     * 使用ContactTypeMap 表关联Contacts表和ContactTypes表
     */
    public interface ContactTypesMap {
        String CONTACT_ID = "contact_id";
        String TYPE_ID = "type_id";
    }


    /**
     * SQL {@code REFERENCES} 子句
     */
    private interface References {
        String CONTACT_ID = "REFERENCES " + Tables.CONTACTS + "(" + Contract.Contacts.CONTACT_ID + ")";
        String TYPE_ID = "REFERENCES " + Tables.CONTACT_TYPES + "(" + Contract.ContactTypes.TYPE_ID + ")";
        String JOB_ID = "REFERENCES " + Tables.JOBS + "(" + Contract.Jobs.JOB_ID +")";
    }

    public Database(Context context) {
        super(context, DATABASE_NAME, null, CUR_DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Tables.CONTACTS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + Contract.Contacts.CONTACT_ID + " TEXT NOT NULL,"
                + Contract.Contacts.CONTACT_COLOR + " TEXT,"
                + Contract.Contacts.CONTACT_TYPE + " TEXT NOT NULL,"
                + Contract.Contacts.CONTACT_NAME + " TEXT NOT NULL,"
                + Contract.Contacts.ORG_NAME + " TEXT,"
                + Contract.Contacts.POST + " TEXT,"
                + Contract.Contacts.TEL_OFFICE + " TEXT,"
                + Contract.Contacts.TEL_CELL + " TEXT,"
                + Contract.Contacts.SIM_IMSI + " TEXT,"
                + Contract.Contacts.TEL_HOME + " TEXT,"
                + Contract.Contacts.EMAIL + " TEXT,"
                + Contract.Contacts.FAX + " TEXT,"
                + Contract.Contacts.TXLMLID + " TEXT,"
                + Contract.Contacts.PXH + " TEXT,"
                + Contract.Contacts.SORT_KEY + " TEXT,"
                + Contract.Contacts.CONTACT_IMPORT_HASHCODE + " TEXT NOT NULL DEFAULT '',"
                + "UNIQUE (" + Contract.Contacts.CONTACT_ID + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE " + Tables.CONTACT_TYPES + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + Contract.ContactTypes.TYPE_ID + " TEXT NOT NULL,"
                + Contract.ContactTypes.TYPE_NAME + " TEXT NOT NULL,"
                + Contract.ContactTypes.TYPE_ORDER_IN_CATEGORY + " INTEGER,"
                + Contract.ContactTypes.TYPE_COLOR + " TEXT NOT NULL,"
                + Contract.ContactTypes.TYPE_ABSTRACT + " TEXT NOT NULL,"
                + "UNIQUE (" + Contract.ContactTypes.TYPE_ID + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE " + Tables.CONTACT_TYPES_MAP + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ContactTypesMap.CONTACT_ID + " TEXT NOT NULL " + References.CONTACT_ID + ","
                + ContactTypesMap.TYPE_ID + " TEXT NOT NULL " + References.TYPE_ID + ","
                + "UNIQUE (" + ContactTypesMap.CONTACT_ID + "," + ContactTypesMap.TYPE_ID + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE " + Tables.MY_CONTACTS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + Contract.MyContacts.CONTACT_ID + " TEXT NOT NULL " + References.CONTACT_ID + ","
                + Contract.MyContacts.ACCOUNT_NAME + " TEXT NOT NULL,"
                + "UNIQUE (" + Contract.MyContacts.CONTACT_ID + "," + Contract.MyContacts.ACCOUNT_NAME + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE " + Tables.JOBS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + Contract.Jobs.JOB_ID + " TEXT NOT NULL,"
                + Contract.Jobs.LDID + " TEXT,"
                + Contract.Jobs.NAME + " TEXT,"
                + Contract.Jobs.DATE + " TEXT,"
                + Contract.Jobs.AM_JOB + " TEXT,"
                + Contract.Jobs.PM_JOB + " TEXT,"
                + Contract.Jobs.NOTE + " TEXT,"
                + Contract.Jobs.JOB_IMPORT_HASHCODE + " TEXT NOT NULL DEFAULT '',"
                + "UNIQUE (" + Contract.Jobs.JOB_ID + ") ON CONFLICT REPLACE)");

        db.execSQL("CREATE TABLE " + Tables.MY_JOBS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + Contract.MyJobs.JOB_ID + " TEXT NOT NULL " + References.JOB_ID + ","
                + Contract.MyJobs.ACCOUNT_NAME + " TEXT NOT NULL,"
                + "UNIQUE (" + Contract.MyJobs.JOB_ID + "," + Contract.MyJobs.ACCOUNT_NAME + ") ON CONFLICT REPLACE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static void delectDatabase(Context context){
        context.deleteDatabase(DATABASE_NAME);
    }
}
