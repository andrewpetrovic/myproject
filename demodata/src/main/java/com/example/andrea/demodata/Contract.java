package com.example.andrea.demodata;

import android.net.Uri;
import android.provider.BaseColumns;

public class Contract {

    public static final String CONTENT_AUTHORITY = "com.example.andrea.demodata";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    private static final String PATH_CONTACTS = "contacts";

    public static final String[] TOP_LEVEL_PATHS = {
            PATH_CONTACTS
    };

    interface ContactsColumns {
        String CONTACT_ID = "contact_id";
        String CONTACT_COLOR = "contact_color";
        String CONTACT_TYPE = "contact_type";
        String CONTACT_NAME = "contact_name";
        String ORG_NAME = "org_name";
        String POST = "post";
        String TEL_OFFICE = "tel_office";
        String TEL_CELL = "tel_cell";
        String SIM_IMSI = "tel_imsi";
        String TEL_HOME = "tel_home";
        String EMAIL = "email";
        String FAX = "fax";
        String TXLMLID = "txlmuid";
        String PXH = "pxh";
        String SORT_KEY = "sort_key";
        String CONTACT_IMPORT_HASHCODE = "contact_import_hashcode";
    }

    public static class Contacts implements ContactsColumns, BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CONTACTS).build();

        public static Uri buildContactUri(String contactId) {
            return CONTENT_URI.buildUpon().appendPath(contactId).build();
        }
    }
}
