package com.itic.mobile.zfyj.qh.contacts.model;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.support.v4.content.CursorLoader;
import android.util.Log;

import com.itic.mobile.zfyj.qh.provider.Contract;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * ContactTypeMetadata model
 * @author Andrea Ji
 */
public class ContactTypeMetadata {

    private static final String TAG = "ContactTypeMetadata";

    HashMap<String, Type> mTypesById = new HashMap<String, Type>();

    ArrayList<Type> mTypeList = new ArrayList<Type>();

    public ContactTypeMetadata(Cursor cursor) {
        while(cursor.moveToNext()){
            Type type = new Type(cursor.getString(ContactTypeQuery.TYPE_ID),
                    cursor.getString(ContactTypeQuery.TYPE_NAME),
                    cursor.getInt(ContactTypeQuery.TYPE_ORDER_IN_CATEGORY),
                    cursor.getString(ContactTypeQuery.TYPE_ABSTRACT),
                    cursor.getString(ContactTypeQuery.TYPE_COLOR)
            );
            mTypesById.put(type.getID(),type);
            mTypeList.add(type);
        }
        // list 需要实现Comparable接口
        Collections.sort(mTypeList);
    }

    public Type getType(String typeID){
        return mTypesById.containsKey(typeID)?mTypesById.get(typeID):null;
    }

    public List<Type> getTypeList(){
        return Collections.unmodifiableList(mTypeList);
    }

    public static CursorLoader createCursorLoader(Context context){
        return new CursorLoader(context, Contract.ContactTypes.CONTENT_URI,ContactTypeQuery.PROJECTION,null,null,null);
    }

    private interface ContactTypeQuery{
        int _TOKEN = 0x1;
        String[] PROJECTION = {
                BaseColumns._ID,
                Contract.ContactTypes.TYPE_ID,
                Contract.ContactTypes.TYPE_NAME,
                Contract.ContactTypes.TYPE_ORDER_IN_CATEGORY,
                Contract.ContactTypes.TYPE_ABSTRACT,
                Contract.ContactTypes.TYPE_COLOR,
        };
        int _ID = 0;
        int TYPE_ID = 1;
        int TYPE_NAME = 2;
        int TYPE_ORDER_IN_CATEGORY = 3;
        int TYPE_ABSTRACT = 4;
        int TYPE_COLOR = 5;
    }

    static public class Type implements Comparable<Type>{

        private String mID;
        private String mName;
        private int mOrder;
        private String mAbstract;
        private String mColor;

        public Type(String mID, String mName, int mOrder, String mAbstract, String mColor) {
            this.mID = mID;
            this.mName = mName;
            this.mOrder = mOrder;
            this.mAbstract = mAbstract;
            this.mColor = mColor;
        }

        public String getID() {
            return mID;
        }

        public String getName() {
            return mName;
        }

        public int getOrder() {
            return mOrder;
        }

        public String getAbstract() {
            return mAbstract;
        }

        public String getColor() {
            return mColor;
        }

        @Override
        public int compareTo(Type another) {
            return mOrder - another.mOrder;
        }
    }
}
