package com.example.andrea.demodata;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class Database extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "qhyj.db";

    private final Context mContext;

    private static final int VER_2015_RELEASE_A = 1;

    private static final int CUR_DATABASE_VERSION = VER_2015_RELEASE_A;

    public Database(Context context) {
        super(context, DATABASE_NAME, null, CUR_DATABASE_VERSION);
        mContext = context;
    }

    interface Tables {
        String CONTACTS = "contacts";
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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static void delectDatabase(Context context){
        context.deleteDatabase(DATABASE_NAME);
    }
}
