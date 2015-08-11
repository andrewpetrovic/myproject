package com.example.andrea.demodata;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.itic.mobile.util.database.JSONHandler;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 从json文件中解析数据并批量存储
 *
 * @author Andrea Ji
 */
public abstract class AppDataHandler {

    private static final String TAG = "AppDataHandler";

    private static final String DEFAULT_TIMESTAMP = "Sat, 1 Jan 2000 00:00:00 GMT";

    private static final String SP_KEY_DATA_TIMESTAMP = "data_timestamp";

//    private static final String DATA_KEY_CONTACT_TYPES = "contact_types";
//    private static final String DATA_KEY_CONTACTS = "contacts";
//    private static final String DATA_KEY_JOBS = "jobs";

    Context mContext = null;

    protected HashMap<String, JSONHandler> mHandlerForKey = new HashMap<String, JSONHandler>();
    protected List<String> mTopLevelPath;

    private int mContentProviderOperationsDone = 0;

    private Uri baseContentUri;

    public AppDataHandler(Context ctx,Uri mBaseContentUri) {
        mContext = ctx;
        baseContentUri = mBaseContentUri;
    }

    /**
     * 为每一个数据类型创建handler,由AppDataHandler统一调用
     */
    public abstract void mappingJsonHandler();


    /**
     * 设置需要通知的ContentObserver path
     */
    public void setTopLevelPath(String[] paths) {
        mTopLevelPath = Arrays.asList(paths);
    }

    public void applyConferenceData(String[] dataBodies, String dataTimestamp, boolean downloadsAllowed) throws IOException {
        Log.i(TAG, "Applying data from " + dataBodies.length + " files");

        // 为每一个数据类型创建handler
        mappingJsonHandler();

        // 调用这些handler处理json
        Log.i(TAG, "Processing " + dataBodies.length + " JSON objects.");
        for (int i = 0; i < dataBodies.length; i++) {
            Log.i(TAG, "Processing json object #" + (i + 1) + " of " + dataBodies.length);
            processDataBody(dataBodies[i]);
        }

        // 创建必要的content provider operations
        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        for (Map.Entry<String, JSONHandler> entry : mHandlerForKey.entrySet()) {
            Log.i(TAG, "Building content provider operations for: " + entry.getKey());
            entry.getValue().makeContentProviderOperations(batch);
            Log.i(TAG, "Content provider operations so far: " + batch.size());
        }
        Log.i(TAG, "Total content provider operations: " + batch.size());

        // 更新数据
        Log.i(TAG, "Applying " + batch.size() + " content provider operations.");
        try {
            int operations = batch.size();
            if (operations > 0) {
                mContext.getContentResolver().applyBatch(Contract.CONTENT_AUTHORITY, batch);
            }
            Log.i(TAG, "Successfully applied " + operations + " content provider operations.");
            mContentProviderOperationsDone += operations;
        } catch (RemoteException ex) {
            Log.e(TAG, "RemoteException while applying content provider operations.");
            throw new RuntimeException("Error executing content provider batch operation", ex);
        } catch (OperationApplicationException ex) {
            Log.e(TAG, "OperationApplicationException while applying content provider operations.");
            throw new RuntimeException("Error executing content provider batch operation", ex);
        }

        // 通知顶级路径
        Log.d(TAG, "Notifying changes on all top-level paths on Content Resolver.");
        ContentResolver resolver = mContext.getContentResolver();
        if (mTopLevelPath != null && mTopLevelPath.size() != 0) {
            for (String path : mTopLevelPath.toArray(new String[]{})) {
                Uri uri = baseContentUri.buildUpon().appendPath(path).build();
                resolver.notifyChange(uri, null);
            }
        }
        // 更新时间戳
        setDataTimestamp(dataTimestamp);
        Log.d(TAG, "Done applying conference data.");
    }

    public int getContentProviderOperationsDone() {
        return mContentProviderOperationsDone;
    }

    public void setDataTimestamp(String timestamp) {
        Log.d(TAG, "Setting data timestamp to: " + timestamp);
        PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString(
                SP_KEY_DATA_TIMESTAMP, timestamp).commit();
    }

    public String getDataTimestamp() {
        return PreferenceManager.getDefaultSharedPreferences(mContext).getString(
                SP_KEY_DATA_TIMESTAMP, DEFAULT_TIMESTAMP);
    }

    private void processDataBody(String dataBody) throws IOException {
        JsonReader reader = new JsonReader(new StringReader(dataBody));
        JsonParser parser = new JsonParser();
        try {
            reader.setLenient(true); // To err is human

            // 整个文件作为JSON对象
            reader.beginObject();

            while (reader.hasNext()) {
                // key值是contact_type
                String key = reader.nextName();
                if (mHandlerForKey.containsKey(key)) {
                    // pass the value to the corresponding handler
                    mHandlerForKey.get(key).process(parser.parse(reader));
                } else {
                    Log.w(TAG, "Skipping unknown key in conference data json: " + key);
                    reader.skipValue();
                }
            }
            reader.endObject();
        } finally {
            reader.close();
        }
    }
}
