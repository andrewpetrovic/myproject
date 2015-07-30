package com.itic.mobile.zfyj.qh.contacts.io;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.itic.mobile.io.JSONHandler;
import com.itic.mobile.zfyj.qh.Config;
import com.itic.mobile.zfyj.qh.R;
import com.itic.mobile.zfyj.qh.contacts.model.Contact;
import com.itic.mobile.zfyj.qh.provider.Contract;
import com.itic.mobile.zfyj.qh.provider.Database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * 通讯录data handler
 * @author Andrea Ji
 */
public class ContactsHandler extends JSONHandler {

    private static final String TAG = "ContactsHandler";

    private int mDefaultContactColor;

    private HashMap<String, Contact> mContacts = new HashMap<String, Contact>();

    public ContactsHandler(Context context) {
        super(context);
        mDefaultContactColor = mContext.getResources().getColor(R.color.contact_color_default);
    }

    /**
     * 从JsonElement中获取ContactTypes数据
     *
     * @param element 从JsonElement中获取Contacts数据
     */
    @Override
    public void process(JsonElement element) {
        for (Contact contact : new Gson().fromJson(element, Contact[].class)) {
            mContacts.put(contact.contact_id, contact);
        }
    }

    /**
     * 使用Contacts的数据结构生成ContentProviderOperation供方法调用者使用
     *
     * @param list 将ContentProviderOperation填充入list供方法调用者使用
     */
    @Override
    public void makeContentProviderOperations(ArrayList<ContentProviderOperation> list) {
        //当mContacts为空，没必要重新生成contact 数据
        if (mContacts.isEmpty()) {
            return;
        }
        Uri uri = Contract.addCallerIsSyncAdapterParameter(
                Contract.Contacts.CONTENT_URI);

        //获取数据库中contact的contactid 和 hashcode
        HashMap<String, String> contactHashCodes = loadContactHashCodes();
        //hashcode.size > 0时，增量更新contact，否则全量更新contact
        boolean incrementalUpdate = (contactHashCodes != null) && (contactHashCodes.size() > 0);

        HashSet<String> contactsToKeep = new HashSet<String>();

        if (incrementalUpdate) {
            Log.d(TAG, "Doing incremental update for contacts.");
        } else {
            Log.d(TAG, "Doing full (non-incremental) update for contacts.");
            list.add(ContentProviderOperation.newDelete(uri).build());
        }

        int updatedContacts = 0;
        for (Contact contact : mContacts.values()) {
            //获取内存中的contact hashcode
            String hashCode = contact.getImportHashCode();
            contactsToKeep.add(contact.contact_id);

            // add/update contact, if necessary
            if (!incrementalUpdate || !contactHashCodes.containsKey(contact.contact_id) ||
                    !contactHashCodes.get(contact.contact_id).equals(hashCode)) {
                ++updatedContacts;
                boolean isNew = !incrementalUpdate || !contactHashCodes.containsKey(contact.contact_id);
                buildContact(isNew, contact, list);
                buildMyContact(contact, list);
                buildTypesMapping(contact, list);
            }
        }
        // delete contact, if necessary
        int deletedContacts = 0;
        if (incrementalUpdate) {
            for (String contactId : contactHashCodes.keySet()) {
                if (!contactsToKeep.contains(contactId)) {
                    buildDeleteOperation(contactId, list);
                    ++deletedContacts;
                }
            }
        }

        Log.d(TAG, "Contacts: " + (incrementalUpdate ? "INCREMENTAL" : "FULL") + " update. " +
                updatedContacts + " to update, " + "delete. " + deletedContacts + " to delete. New total: " +
                mContacts.size());
    }

    private void buildTypesMapping(Contact contact, ArrayList<ContentProviderOperation> list) {
        final Uri uri = Contract.addCallerIsSyncAdapterParameter(Contract.Contacts.buildContactTypesDirUri(contact.contact_id));
        //删除 已存在的mapping
        list.add(ContentProviderOperation.newDelete(uri).build());
        //插入mapping
        list.add(ContentProviderOperation.newInsert(uri)
                .withValue(Database.ContactTypesMap.CONTACT_ID, contact.contact_id)
                .withValue(Database.ContactTypesMap.TYPE_ID, contact.contact_type).build());
    }

    private void buildMyContact(Contact contact, ArrayList<ContentProviderOperation> list) {
        if (!contact.contact_type.equals(Config.ContactsTypes.CATEGORY_DWTXL)) {
            final Uri uri = Contract.addCallerIsSyncAdapterParameter(Contract.Contacts.buildMyContactDirUri(contact.contact_id));
            //删除已存在的mapping
            list.add(ContentProviderOperation.newDelete(uri).build());
            //插入mapping
            list.add(ContentProviderOperation.newInsert(uri).withValue(Contract.MyContacts.CONTACT_ID, contact.contact_id).build());
        }
    }

    private void buildDeleteOperation(String conatctId, ArrayList<ContentProviderOperation> list) {
        Uri contactUri = Contract.addCallerIsSyncAdapterParameter(
                Contract.Contacts.buildContactUri(conatctId));
        list.add(ContentProviderOperation.newDelete(contactUri).build());
    }

    private void buildContact(boolean isInsert, Contact contact, ArrayList<ContentProviderOperation> list) {
        ContentProviderOperation.Builder builder;
        Uri allContactsUri = Contract
                .addCallerIsSyncAdapterParameter(Contract.Contacts.CONTENT_URI);
        Uri thisContactUri = Contract
                .addCallerIsSyncAdapterParameter(Contract.Contacts.buildContactUri(
                        contact.contact_id));

        if (isInsert) {
            builder = ContentProviderOperation.newInsert(allContactsUri);
        } else {
            builder = ContentProviderOperation.newUpdate(thisContactUri);
        }

        int color = mDefaultContactColor;
        try {
            if (contact.contact_color != 0) {
                color = parseColor(contact.contact_color);
            }
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "Ignoring invalid formatted contact color: " + contact.contact_color);
        }
        builder.withValue(Contract.Contacts.CONTACT_ID, contact.contact_id);
        builder.withValue(Contract.Contacts.CONTACT_COLOR, color);
        builder.withValue(Contract.Contacts.CONTACT_TYPE, contact.contact_type);
        builder.withValue(Contract.Contacts.CONTACT_NAME, contact.contact_name);
        builder.withValue(Contract.Contacts.ORG_NAME, contact.org_name);
        builder.withValue(Contract.Contacts.POST, contact.post);
        builder.withValue(Contract.Contacts.TEL_OFFICE, contact.tel_office);
        builder.withValue(Contract.Contacts.TEL_CELL, contact.tel_cell);
        builder.withValue(Contract.Contacts.SIM_IMSI, contact.sim_imsi);
        builder.withValue(Contract.Contacts.TEL_HOME, contact.tel_home);
        builder.withValue(Contract.Contacts.EMAIL, contact.email);
        builder.withValue(Contract.Contacts.FAX, contact.fax);
        builder.withValue(Contract.Contacts.PXH, contact.pxh);
        builder.withValue(Contract.Contacts.TXLMLID, contact.txlmlid);
        builder.withValue(Contract.Contacts.SORT_KEY, contact.getSortKey());
        builder.withValue(Contract.Contacts.CONTACT_IMPORT_HASHCODE, contact.getImportHashCode());
        list.add(builder.build());
    }

    private int parseColor(int colorID) {
        switch (colorID) {
            case 0:
                return mContext.getResources().getColor(R.color.contact_color_default);
            case 1:
                return mContext.getResources().getColor(R.color.contact_color_brown);
            case 2:
                return mContext.getResources().getColor(R.color.contact_color_green);
            case 3:
                return mContext.getResources().getColor(R.color.contact_color_orange);
            case 4:
                return mContext.getResources().getColor(R.color.contact_color_purple);
            case 5:
                return mContext.getResources().getColor(R.color.contact_color_red);
            default:
                return mContext.getResources().getColor(R.color.contact_color_default);
        }
    }

    private HashMap<String, String> loadContactHashCodes() {
        Uri uri = Contract.addCallerIsSyncAdapterParameter(
                Contract.Contacts.CONTENT_URI);
        Log.d(TAG, "Loading contacts hashcodes for contacts import optimization.");
        Cursor cursor = mContext.getContentResolver().query(uri, ContactHashcodeQuery.PROJECTION,
                null, null, null);
        if (cursor == null || cursor.getCount() < 1) {
            Log.w(TAG, "Warning: failed to load contacts hashcodes. Not optimizing contacts import.");
            if (cursor != null) {
                cursor.close();
            }
            return null;
        }

        HashMap<String, String> hashcodeMap = new HashMap<String, String>();
        while (cursor.moveToNext()) {
            String contactID = cursor.getString(ContactHashcodeQuery.C_ID);
            String hashcode = cursor.getString(ContactHashcodeQuery.CONTACT_IMPORT_HASHCODE);
            hashcodeMap.put(contactID, hashcode == null ? "" : hashcode);
        }
        Log.d(TAG, "Contacts hashcodes loaded for " + hashcodeMap.size() + " contacts.");
        cursor.close();
        return hashcodeMap;
    }

    private interface ContactHashcodeQuery {
        String[] PROJECTION = {
                BaseColumns._ID,
                Contract.Contacts.CONTACT_ID,
                Contract.Contacts.CONTACT_IMPORT_HASHCODE
        };
        int _ID = 0;
        int C_ID = 1;
        int CONTACT_IMPORT_HASHCODE = 2;
    }
}
