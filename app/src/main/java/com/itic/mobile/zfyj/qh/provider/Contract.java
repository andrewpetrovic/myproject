package com.itic.mobile.zfyj.qh.provider;

import android.content.Context;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.text.TextUtils;

import com.itic.mobile.accounts.AccountUtils;

import java.util.List;

public class Contract {

    /**
     * Query parameter to create a distinct query.
     */
    public static final String QUERY_PARAMETER_DISTINCT = "distinct";
    public static final String OVERRIDE_ACCOUNTNAME_PARAMETER = "overrideAccount";

    public static final String CONTENT_AUTHORITY = "com.itic.mobile.zfyj.qh";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static final String PATH_CONTACT_TYPES = "contact_types";
    private static final String PATH_CONTACTS = "contacts";
    private static final String PATH_MY_CONTACTS = "my_contacts";
    private static final String PATH_SEARCH = "search";
    private static final String PATH_SEARCH_INDEX = "search_index";
    private static final String PATH_JOBS = "jobs";
    private static final String PATH_MY_JOBS = "my_jobs";
    private static final String PATH_GET_JOB = "get_job";
    private static final String PATH_NEW_JOB = "new_job";

    public static final String[] TOP_LEVEL_PATHS = {
            PATH_CONTACT_TYPES,PATH_CONTACTS
    };

    public static String getOverrideAccountName(Uri uri) {
        return uri.getQueryParameter(OVERRIDE_ACCOUNTNAME_PARAMETER);
    }

    public static boolean hasCallerIsSyncAdapterParameter(Uri uri) {
        return TextUtils.equals("true",uri.getQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER));
    }

    public interface SyncColumns {
        /**
         * Last time this entry was updated or synchronized.
         */
        String UPDATED = "updated";
    }

    interface ContactTypesColumns {
        String TYPE_ID = "type_id";
        String TYPE_NAME = "type_name";
        String TYPE_ORDER_IN_CATEGORY = "type_order_in_category";
        String TYPE_COLOR = "type_color";
        String TYPE_ABSTRACT = "type_abstract";
    }

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

    interface JobsColumns {
        String JOB_ID = "job_id";
        String LDID = "ldid";
        String NAME = "name";
        String DATE = "date";
        String AM_JOB = "am_job";
        String PM_JOB = "pm_job";
        String NOTE = "note";
        String JOB_IMPORT_HASHCODE = "job_import_hashcode";
    }

    /**
     * MyContacts关联Contacts表和account_name，仅用于部门通讯录和公共通讯录
     */
    interface MyContactsColumns {
        String CONTACT_ID = ContactsColumns.CONTACT_ID;
        String ACCOUNT_NAME = "account_name";
    }

    interface MyJobsColumns {
        String JOB_ID = JobsColumns.JOB_ID;
        String ACCOUNT_NAME = "account_name";
    }

    public static class SearchIndex {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SEARCH_INDEX).build();
    }

    public static class ContactTypes implements ContactTypesColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CONTACT_TYPES).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.zfyjqh.contact_type";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.zfyjqh.contact_type";

        /**
         * Default "ORDER BY" clause.
         */
        public static final String DEFAULT_SORT = ContactTypesColumns.TYPE_ORDER_IN_CATEGORY;

        /**
         * Build {@link Uri} that references all tags.
         */
        public static Uri buildTypesUri() {
            return CONTENT_URI;
        }

        /**
         * Build a {@link Uri} that references a given tag.
         */
        public static Uri buildTypesUri(String typeId) {
            return CONTENT_URI.buildUpon().appendPath(typeId).build();
        }

        /**
         * Read {@link #TYPE_ID} from {@link ContactTypes} {@link Uri}.
         */
        public static String getTypesId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static class Jobs implements JobsColumns,BaseColumns{
        public static final String QUERY_PARAMETER_DATE_FILTER = "filter";
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_JOBS).build();
        public static final Uri NEW_JOB_CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_NEW_JOB).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.zfyjqh.jobs";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.zfyjqh.jobs";
        public static final String CONTENT_ITEM_TYPE_NEW =
                "vnd.android.cursor.item/vnd.zfyjqh.jobs.new";

        public static Uri buildDateFilgerUri(String date){
            StringBuilder sb = new StringBuilder();
            if (!TextUtils.isEmpty(date)) {
                sb.append(date.trim());
            }
            if (sb.length() == 0) {
                return CONTENT_URI;
            } else {
                return CONTENT_URI.buildUpon().appendQueryParameter(QUERY_PARAMETER_DATE_FILTER,
                        sb.toString()).build();
            }
        }

        public static Uri buildMyJobDirUri(String jobId) {
            return CONTENT_URI.buildUpon().appendPath(jobId).appendPath(PATH_MY_JOBS).build();
        }

        public static Uri buildJobUri(String jobId) {
            return CONTENT_URI.buildUpon().appendPath(PATH_GET_JOB).appendPath(jobId).build();
        }

        public static Uri buildNewJobUri(){
//            return CONTENT_URI.buildUpon().appendPath(PATH_NEW_JOB).build();
            return NEW_JOB_CONTENT_URI;
        }

        public static String getJobId(Uri uri) {
            return uri.getPathSegments().get(2);
        }
    }

    public static class Contacts implements ContactsColumns,
            SyncColumns, BaseColumns {
        public static final String QUERY_PARAMETER_TYPE_FILTER = "filter";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CONTACTS).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/vnd.zfyjqh.contact";
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/vnd.zfyjqh.contact";

        public static final String SEARCH_SNIPPET = "search_snippet";

        public static Uri buildContactUri(String contactId) {
            return CONTENT_URI.buildUpon().appendPath(contactId).build();
        }

        public static Uri buildContactTypesDirUri(String contactId) {
            return CONTENT_URI.buildUpon().appendPath(contactId).appendPath(PATH_CONTACT_TYPES).build();
        }

        public static Uri buildMyContactDirUri(String contactid) {
            return CONTENT_URI.buildUpon().appendPath(contactid).appendPath(PATH_MY_CONTACTS).build();
        }

        public static Uri buildSearchUri(String query) {
            if (null == query) {
                query = "";
            }
            // convert "lorem ipsum dolor sit" to "lorem* ipsum* dolor* sit*"
            if (query.equals("")){
                query = query.replaceAll(" +", " *") + "*";
            }
            return CONTENT_URI.buildUpon()
                    .appendPath(PATH_SEARCH).appendPath(query).build();
        }

        public static boolean isSearchUri(Uri uri) {
            List<String> pathSegments = uri.getPathSegments();
            return pathSegments.size() >= 2 && PATH_SEARCH.equals(pathSegments.get(1));
        }

        public static long[] getInterval(Uri uri) {
            if (uri == null) {
                return null;
            }
            List<String> segments = uri.getPathSegments();
            if (segments.size() == 3 && segments.get(2).indexOf('-') > 0) {
                String[] interval = segments.get(2).split("-");
                return new long[]{Long.parseLong(interval[0]), Long.parseLong(interval[1])};
            }
            return null;
        }

        public static String getContactId(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getSearchQuery(Uri uri) {
            List<String> segments = uri.getPathSegments();
            if (2 < segments.size()) {
                return segments.get(2);
            }
            return null;
        }

        public static boolean hasFilterParam(Uri uri) {
            return uri != null && uri.getQueryParameter(QUERY_PARAMETER_TYPE_FILTER) != null;
        }

        public static Uri buildTypeFilterUri(String[] requiredTypes) {
            StringBuilder sb = new StringBuilder();
            for (String types : requiredTypes) {
                if (TextUtils.isEmpty(types)) continue;
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append(types.trim());
            }
            if (sb.length() == 0) {
                return CONTENT_URI;
            } else {
                return CONTENT_URI.buildUpon().appendQueryParameter(QUERY_PARAMETER_TYPE_FILTER,
                        sb.toString()).build();
            }
        }

    }

    public static final class MyJobs implements MyJobsColumns,BaseColumns{
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MY_JOBS).build();
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.zfyjqh.myjobs";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.zfyjqh.myjobs";

        public static Uri buildMyJobsUri(Context context, String accountName){
            if (accountName == null){
                accountName = AccountUtils.getActiveAccountName(context);
            }
            return addOverrideAccountName(CONTENT_URI,accountName);
        }
    }

    public static final class MyContacts implements MyContactsColumns,BaseColumns{
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MY_CONTACTS).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.zfyjqh.mycontacts";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.zfyjqh.mycontacts";

        public static Uri buildMyContactsUri(Context context){
            return buildMyContactsUri(context, null);
        }

        public static Uri buildMyContactsUri(Context context, String accountName){
            if (accountName == null){
                accountName = AccountUtils.getActiveAccountName(context);
            }
            return addOverrideAccountName(CONTENT_URI,accountName);
        }
    }

    private static Uri addOverrideAccountName(Uri uri, String accountName) {
        return uri.buildUpon().appendQueryParameter(
                OVERRIDE_ACCOUNTNAME_PARAMETER, accountName).build();
    }

    public static Uri addCallerIsSyncAdapterParameter(Uri uri) {
        return uri.buildUpon().appendQueryParameter(
                ContactsContract.CALLER_IS_SYNCADAPTER, "true").build();
    }
}
