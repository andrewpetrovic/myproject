package com.itic.mobile.zfyj.qh.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.itic.mobile.accounts.AccountUtils;
import com.itic.mobile.util.database.SelectionBuilder;
import com.itic.mobile.util.logcat.LogUtils;
import com.itic.mobile.zfyj.qh.Config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * ContentProvider
 * @author Andrea Ji
 */
public class Provider extends ContentProvider {

    private static final String TAG = "Provider";

    private Database mOpenHelper;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static final int CONTACTS = 400;
    private static final int CONTACT_ID_TYPES = 401;
    private static final int CONTACTS_MY_CONTACTS = 402;
    private static final int CONTACTS_ID = 403;
    private static final int CONTACTS_SEARCH = 404;

    private static final int CONTACT_TYPES = 200;

    private static final int MY_CONTACTS = 300;

    private static final int MY_JOBS = 500;

    private static final int JOBS = 600;
    private static final int JOBS_ID = 601;
    private static final int JOBS_MY_JOBS = 602;
    private static final int NEW_JOB = 603;

    /**
     * 构建UriMatcher
     *
     * @return {@code UriMatcher}
     */
    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = Contract.CONTENT_AUTHORITY;

        matcher.addURI(authority, "contacts", CONTACTS);
        matcher.addURI(authority, "contacts/search/*", CONTACTS_SEARCH);
        matcher.addURI(authority, "contacts/*/contact_types", CONTACT_ID_TYPES);
        matcher.addURI(authority, "contacts/*/my_contacts", CONTACTS_MY_CONTACTS);
        matcher.addURI(authority, "contacts/*", CONTACTS_ID);


        matcher.addURI(authority, "contact_types", CONTACT_TYPES);
        matcher.addURI(authority, "my_contacts", MY_CONTACTS);

        matcher.addURI(authority,"jobs",JOBS);
        matcher.addURI(authority,"jobs/get_job/*",JOBS_ID);
        matcher.addURI(authority,"jobs/*/my_jobs",JOBS_MY_JOBS);
        matcher.addURI(authority,"new_job",NEW_JOB);
        matcher.addURI(authority,"my_jobs",MY_JOBS);
        return matcher;
    }

    /**
     * 在onCreate中初始化Database Helper
     *
     * @return {@code Database}
     */
    @Override
    public boolean onCreate() {
        mOpenHelper = new Database(getContext());
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CONTACTS:
                return Contract.Contacts.CONTENT_TYPE;
            case CONTACT_ID_TYPES:
                return Contract.ContactTypes.CONTENT_TYPE;
            case CONTACTS_MY_CONTACTS:
                return Contract.Contacts.CONTENT_TYPE;
            case CONTACTS_ID:
                return Contract.Contacts.CONTENT_ITEM_TYPE;
            case CONTACTS_SEARCH:
                return Contract.Contacts.CONTENT_TYPE;
            case CONTACT_TYPES:
                return Contract.ContactTypes.CONTENT_TYPE;
            case MY_CONTACTS:
                return Contract.MyContacts.CONTENT_TYPE;
            case JOBS:
                return Contract.Jobs.CONTENT_TYPE;
            case JOBS_ID:
                return Contract.Jobs.CONTENT_ITEM_TYPE;
            case NEW_JOB:
                int i = 0;
                return Contract.Jobs.CONTENT_ITEM_TYPE_NEW;
            case MY_JOBS:
                return Contract.MyJobs.CONTENT_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        final int match = sUriMatcher.match(uri);
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "uri=" + uri + " match=" + match + " proj=" + Arrays.toString(projection) +
                    " selection=" + selection + " args=" + Arrays.toString(selectionArgs) + ")");
        }

        switch (match) {
            case CONTACTS_SEARCH: {
                /* Query statement
                    SELECT DISTINCT contact_name, tel_cell, tel_office, tel_home, org_name FROM contacts where contact_name LIKE ?
                    UNION
                    SELECT DISTINCT contact_name, tel_cell, tel_office, tel_home, org_name FROM contacts where tel_cell LIKE ?
                    UNION
                    SELECT DISTINCT contact_name, tel_cell, tel_office, tel_home, org_name FROM contacts where tel_office LIKE ?
                    UNION
                    SELECT DISTINCT contact_name, tel_cell, tel_office, tel_home, org_name FROM contacts where tel_home LIKE ?
                    UNION
                    SELECT DISTINCT contact_name, tel_cell, tel_office, tel_home, org_name FROM contacts where org_name LIKE ?
                 */
                if (projection == null) {
                    throw new IllegalArgumentException("projection can't be null...");
                }
                Set<String> CONTACT_COLUMNS = new HashSet<String>();
                for (int i = 0; i < projection.length; i++) {
                    CONTACT_COLUMNS.add(projection[i]);
                }
                Set<String> columnsPresentInTable = new HashSet<String>(CONTACT_COLUMNS);
                String searchKey = "%" + Contract.Contacts.getSearchQuery(uri) + "%";
                //build UnionSubQueryString
                SelectionBuilder nameUnionSubQueryStringBuilder = new SelectionBuilder();
                nameUnionSubQueryStringBuilder.table(Database.Tables.CONTACTS);
                String nameUnionSubQueryString = nameUnionSubQueryStringBuilder.buildUnionSubQueryString(
                        true, null, projection, columnsPresentInTable, 0, null, Contract.Contacts.CONTACT_NAME + " LIKE ?", null, null);
                SelectionBuilder phoneNumberUnionSubQueryStringBuilder = new SelectionBuilder();
                phoneNumberUnionSubQueryStringBuilder.table(Database.Tables.CONTACTS);
                String phoneNumberUnionSubQueryString = nameUnionSubQueryStringBuilder.buildUnionSubQueryString(
                        true, null, projection, columnsPresentInTable, 0, null, Contract.Contacts.TEL_CELL + " LIKE ?", null, null);
                SelectionBuilder telOfficeUnionSubQueryStringBuilder = new SelectionBuilder();
                telOfficeUnionSubQueryStringBuilder.table(Database.Tables.CONTACTS);
                String telOfficeUnionSubQueryString = nameUnionSubQueryStringBuilder.buildUnionSubQueryString(
                        true, null, projection, columnsPresentInTable, 0, null, Contract.Contacts.TEL_OFFICE + " LIKE ?", null, null);
                SelectionBuilder telHomeUnionSubQueryStringBuilder = new SelectionBuilder();
                telHomeUnionSubQueryStringBuilder.table(Database.Tables.CONTACTS);
                String telHomeUnionSubQueryString = nameUnionSubQueryStringBuilder.buildUnionSubQueryString(
                        true, null, projection, columnsPresentInTable, 0, null, Contract.Contacts.TEL_HOME + " LIKE ?", null, null);
                SelectionBuilder orgNameUnionSubQueryStringBuilder = new SelectionBuilder();
                orgNameUnionSubQueryStringBuilder.table(Database.Tables.CONTACTS);
                String orgNameUnionSubQueryString = nameUnionSubQueryStringBuilder.buildUnionSubQueryString(
                        true, null, projection, columnsPresentInTable, 0, null, Contract.Contacts.ORG_NAME + " LIKE ?", null, null);
                //Union Query
                SelectionBuilder unionQueryBuilder = new SelectionBuilder();
                String sql = unionQueryBuilder.buildUnionQueryString(true,
                        new String[]{
                                nameUnionSubQueryString,
                                phoneNumberUnionSubQueryString,
                                telOfficeUnionSubQueryString,
                                telHomeUnionSubQueryString,
                                orgNameUnionSubQueryString},
                        sortOrder,null);
                Cursor cursor = unionQueryBuilder.unionQuery(db, sql,
                        new String[]{
                                searchKey, searchKey, searchKey, searchKey, searchKey
                        });
                Context context = getContext();
                if (null != context) {
                    cursor.setNotificationUri(context.getContentResolver(), uri);
                }
                return cursor;
            }
            default: {
                final SelectionBuilder builder = buildExpandedSelection(uri, match);
                boolean distinct = !TextUtils.isEmpty(uri.getQueryParameter(Contract.QUERY_PARAMETER_DISTINCT));
                Cursor cursor = builder
                        .where(selection, selectionArgs)
                        .query(db, distinct, projection, sortOrder, null);
                Context context = getContext();
                if (null != context) {
                    cursor.setNotificationUri(context.getContentResolver(), uri);
                }
                return cursor;
            }
        }
    }

    private void addTypesFilter(SelectionBuilder builder, String typesFilter, Uri uri) {
        // Note: for context, remember that contact queries are done on a join of contacts
        // and the contact_types relationship table, and are GROUP'ed BY the contact ID.
        String[] requiredTypes = typesFilter.split(",");
        if (requiredTypes.length == 0) {
            // filtering by 0 tags -- no-op
            return;
        } else if (requiredTypes.length == 1) {
            // filtering by only one types, so a simple WHERE clause suffices
            builder.where(Contract.ContactTypes.TYPE_ID + "=?", requiredTypes[0]);
            //如果filter 不是 TYPE_DWTXL，查询条件需要增加当前登录用户
            if (!requiredTypes[0].equals(Config.ContactsTypes.CATEGORY_DWTXL)) {
                builder.where(Contract.MyContacts.ACCOUNT_NAME + "=?", getCurrentAccountName(uri, true));
            }
        } else {
            // Filtering by multiple tags, so we must add a WHERE clause with an IN operator,
            // and add a HAVING statement to exclude groups that fall short of the number
            // of required tags. For example, if requiredTags is { "X", "Y", "Z" }, and a certain
            // contact only has types "X" and "Y", it will be excluded by the HAVING statement.
            String questionMarkTuple = makeQuestionMarkTuple(requiredTypes.length);
            builder.where(Contract.ContactTypes.TYPE_ID + " IN " + questionMarkTuple, requiredTypes);
            builder.having("COUNT(" + Qualified.CONTACTS_CONTACT_ID + ") >= " + requiredTypes.length);
        }
    }

    private void addDateFilter(SelectionBuilder builder ,String date,Uri uri){
        builder.where(Contract.Jobs.DATE + "=?", date);
    }

    /**
     * Returns a tuple of question marks. For example, if count is 3, returns "(?,?,?)".
     */
    private String makeQuestionMarkTuple(int count) {
        if (count < 1) {
            return "()";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(?");
        for (int i = 1; i < count; i++) {
            stringBuilder.append(",?");
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    private SelectionBuilder buildExpandedSelection(Uri uri, int match) {
        final SelectionBuilder builder = new SelectionBuilder();
        switch (match) {
            case CONTACT_TYPES:
                return builder.table(Database.Tables.CONTACT_TYPES);
            case CONTACTS:
                LogUtils.LOGD(TAG, "case  + uri:" + uri.toString());
                String typesFilter = uri.getQueryParameter(Contract.Contacts.QUERY_PARAMETER_TYPE_FILTER);
                builder.table(Database.Tables.CONTACTS_JOIN_CONTACT_TYPE_MY_CONTACTS, getCurrentAccountName(uri, true))
                        .mapToTable(Contract.Contacts._ID, Database.Tables.CONTACTS)
                        .mapToTable(Contract.Contacts.CONTACT_ID, Database.Tables.CONTACTS);
                if (!TextUtils.isEmpty(typesFilter)) {
                    addTypesFilter(builder, typesFilter, uri);
                }
                return builder;
            case CONTACTS_ID:
                final String contactId = Contract.Contacts.getContactId(uri);
                return builder.table(Database.Tables.CONTACTS).where(Qualified.CONTACTS_CONTACT_ID + "=?", contactId);
            case JOBS:
                String dateFilter = uri.getQueryParameter(Contract.Jobs.QUERY_PARAMETER_DATE_FILTER);
                builder.table(Database.Tables.JOBS_JOIN_MY_JOBS,getCurrentAccountName(uri,true))
                        .mapToTable(Contract.Jobs._ID,Database.Tables.JOBS)
                        .mapToTable(Contract.Jobs.JOB_ID,Database.Tables.JOBS);
                if (!TextUtils.isEmpty(dateFilter)){
                    addDateFilter(builder,dateFilter,uri);
                }
                return builder;
            case JOBS_ID:
                final String jobId = Contract.Jobs.getJobId(uri);
                return builder.table(Database.Tables.JOBS).where(Qualified.JOBS_JOB_ID + "=?",jobId);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * 获得当前用户名，如果uri中包含则从uri中取，如果没有，则取当前登录的
     *
     * @param uri
     * @param sanitize
     * @return
     */
    private String getCurrentAccountName(Uri uri, boolean sanitize) {
        String accountName = Contract.getOverrideAccountName(uri);
        if (accountName == null) {
            accountName = AccountUtils.getActiveAccountName(getContext());
        }
        if (sanitize) {
            // sanitize accountName when concatenating (http://xkcd.com/327/)
            accountName = (accountName != null) ? accountName.replace("'", "''") : null;
        }
        return accountName;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.v(TAG, "insert(uri=" + uri + ", values=" + values.toString()
                + ", account=" + getCurrentAccountName(uri, false) + ")");
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        boolean syncToNetwork = !Contract.hasCallerIsSyncAdapterParameter(uri);
        switch (match) {
            case CONTACT_TYPES: {
                db.insertOrThrow(Database.Tables.CONTACT_TYPES, null, values);
                notifyChange(uri);
                return Contract.ContactTypes.buildTypesUri(values.getAsString(Contract.ContactTypes.TYPE_ID));
            }
            case CONTACTS: {
                db.insertOrThrow(Database.Tables.CONTACTS, null, values);
                notifyChange(uri);
                return Contract.Contacts.buildContactUri(values.getAsString(Contract.Contacts.CONTACT_ID));
            }
            case CONTACT_ID_TYPES: {
                db.insertOrThrow(Database.Tables.CONTACT_TYPES_MAP, null, values);
                notifyChange(uri);
                return Contract.ContactTypes.buildTypesUri(values.getAsString(Contract.ContactTypes.TYPE_ID));
            }
            case CONTACTS_MY_CONTACTS: {
                values.put(Contract.MyContacts.ACCOUNT_NAME, getCurrentAccountName(uri, false));
                db.insertOrThrow(Database.Tables.MY_CONTACTS, null, values);
                notifyChange(uri);
                return Contract.Contacts.buildContactUri(values.getAsString(Contract.MyContacts.CONTACT_ID));
            }
            case JOBS:{
                db.insertOrThrow(Database.Tables.JOBS, null, values);
                notifyChange(Contract.Jobs.CONTENT_URI);
                return Contract.Jobs.CONTENT_URI;
            }
            case JOBS_MY_JOBS:{
                values.put(Contract.MyJobs.ACCOUNT_NAME, getCurrentAccountName(uri, false));
                db.insertOrThrow(Database.Tables.MY_JOBS, null, values);
                notifyChange(uri);
                return Contract.Jobs.buildJobUri(values.getAsString(Contract.MyJobs.JOB_ID));
            }
            default: {
                throw new UnsupportedOperationException("Unknown insert uri: " + uri);
            }
        }
    }

    private void notifyChange(Uri uri) {
        // 当不通过sync adapter变更的数据时，通过uri通知相关UI重新load数据
        if (!Contract.hasCallerIsSyncAdapterParameter(uri)) {
            Context context = getContext();
            context.getContentResolver().notifyChange(uri, null);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        String accountName = getCurrentAccountName(uri, false);
        Log.v(TAG, "delete(uri=" + uri + ", account=" + accountName + ")");
        if (uri == Contract.BASE_CONTENT_URI) {
            // Handle whole database deletes (e.g. when signing out)
            deleteDatabase();
            notifyChange(uri);
            return 1;
        }
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final SelectionBuilder builder = buildSimpleSelection(uri);
        final int match = sUriMatcher.match(uri);
        if (match == MY_CONTACTS) {
            builder.where(Contract.MyContacts.ACCOUNT_NAME + "=?", accountName);
        }
        int retVal = builder.where(selection, selectionArgs).delete(db);
        notifyChange(uri);
        return retVal;
    }

    private SelectionBuilder buildSimpleSelection(Uri uri) {
        final SelectionBuilder builder = new SelectionBuilder();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CONTACT_TYPES: {
                return builder.table(Database.Tables.CONTACT_TYPES);
            }
            case CONTACTS: {
                return builder.table(Database.Tables.CONTACTS);
            }
            case CONTACTS_ID:{
                final String contactId = Contract.Contacts.getContactId(uri);
                return builder.table(Database.Tables.CONTACTS)
                        .where(Contract.Contacts.CONTACT_ID + "=?",contactId);
            }
            case CONTACTS_MY_CONTACTS: {
                final String contactId = Contract.Contacts.getContactId(uri);
                return builder.table(Database.Tables.MY_CONTACTS)
                        .where(Contract.MyContacts.CONTACT_ID + "=?", contactId);
            }
            case MY_CONTACTS: {
                return builder.table(Database.Tables.MY_CONTACTS)
                        .where(Contract.MyContacts.ACCOUNT_NAME + "=?", getCurrentAccountName(uri, false));
            }
            case CONTACT_ID_TYPES: {
                final String contactId = Contract.Contacts.getContactId(uri);
                return builder.table(Database.Tables.CONTACT_TYPES_MAP)
                        .where(Contract.Contacts.CONTACT_ID + "=?", contactId);
            }
            case JOBS_MY_JOBS:{
                String jobId = Contract.Jobs.getJobId(uri);
                return builder.table(Database.Tables.MY_JOBS)
                        .where(Contract.MyJobs.JOB_ID + "=?",jobId);
            }
            case JOBS:{
                return builder.table(Database.Tables.JOBS);
            }
            case JOBS_ID:{
                String jobId = Contract.Jobs.getJobId(uri);
                return builder.table(Database.Tables.JOBS).where(Contract.Jobs.JOB_ID + "=?",jobId);
            }
            case MY_JOBS:{
                return builder.table(Database.Tables.MY_JOBS)
                        .where(Contract.MyJobs.ACCOUNT_NAME + "=?",getCurrentAccountName(uri,false));
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri for " + match + ": " + uri);
            }
        }
    }

    private void deleteDatabase() {
        // 等待content provider option 完成，然后删除数据库
        mOpenHelper.close();
        Context context = getContext();
        Database.delectDatabase(context);
        mOpenHelper = new Database(getContext());
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        String accountName = getCurrentAccountName(uri, false);
        Log.v(TAG, "update(uri=" + uri + ", values=" + values.toString()
                + ", account=" + accountName + ")");
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        final SelectionBuilder builder = buildSimpleSelection(uri);
        if (match == MY_CONTACTS) {
            values.remove(Contract.MyContacts.ACCOUNT_NAME);
            builder.where(Contract.MyContacts.ACCOUNT_NAME + "=?", accountName);
        }
        int retVal = builder.where(selection, selectionArgs).update(db, values);
        notifyChange(uri);
        return retVal;
    }

    private interface Qualified {
        String CONTACTS_CONTACT_ID = Database.Tables.CONTACTS + "." + Contract.Contacts.CONTACT_ID;
        String JOBS_JOB_ID = Database.Tables.JOBS + "." + Contract.Jobs.JOB_ID;
    }
}
