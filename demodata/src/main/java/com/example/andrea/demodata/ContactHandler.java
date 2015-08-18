package com.example.andrea.demodata;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.itic.mobile.util.database.JSONHandler;

import java.util.ArrayList;
import java.util.HashMap;


public class ContactHandler extends JSONHandler {

    private HashMap<String, Contact> mContacts = new HashMap<String, Contact>();

    public ContactHandler(Context context) {
        super(context);
    }

    @Override
    public void makeContentProviderOperations(ArrayList<ContentProviderOperation> list) {
        Uri uri = Contract.Contacts.CONTENT_URI;
        list.add(ContentProviderOperation.newDelete(uri).build());
        for (Contact contact : mContacts.values()) {
            ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(uri);
            builder.withValue(Contract.Contacts.CONTACT_ID, contact.contact_id);
            builder.withValue(Contract.Contacts.CONTACT_COLOR, contact.contact_color);
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
    }

    @Override
    public void process(JsonElement element) {
        for (Contact contact : new Gson().fromJson(element, Contact[].class)) {
            mContacts.put(contact.contact_id, contact);
        }
    }
}
