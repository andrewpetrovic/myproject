package com.itic.mobile.zfyj.qh.contacts.io;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.itic.mobile.io.JSONHandler;
import com.itic.mobile.zfyj.qh.contacts.model.ContactType;
import com.itic.mobile.zfyj.qh.provider.Contract;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 通讯录类型 data handler
 * @author Andrea Ji
 */
public class ContactTypesHandler extends JSONHandler {
    private static final String TAG = "ContactTypesHandler";

    private HashMap<String, ContactType> mTypes = new HashMap<String, ContactType>();

    public ContactTypesHandler(Context context) {
        super(context);
    }

    /**
     * 从JsonElement中获取ContactTypes数据
     * @param element 从JsonElement中获取ContactTypes数据
     */
    @Override
    public void process(JsonElement element) {
        for (ContactType type : new Gson().fromJson(element, ContactType[].class)) {
            mTypes.put(type.type_id, type);
        }
    }

    /**
     * 使用ContactTypes的数据结构生成ContentProviderOperation供方法调用者使用
     * @param list 将ContentProviderOperation填充入list供方法调用者使用
     */
    @Override
    public void makeContentProviderOperations(ArrayList<ContentProviderOperation> list) {
        //当mTypes为空，没必要重新生成contact type 数据
        if (mTypes.isEmpty()){
            return;
        }
        Uri uri = Contract.addCallerIsSyncAdapterParameter(
                Contract.ContactTypes.CONTENT_URI);

        list.add(ContentProviderOperation.newDelete(uri).build());
        for (ContactType type : mTypes.values()) {
            ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(uri);
            builder.withValue(Contract.ContactTypes.TYPE_ID, type.type_id);
            builder.withValue(Contract.ContactTypes.TYPE_NAME, type.type_name);
            builder.withValue(Contract.ContactTypes.TYPE_ORDER_IN_CATEGORY, type.order_in_category);
            builder.withValue(Contract.ContactTypes.TYPE_ABSTRACT, type.type_abstract);
            builder.withValue(Contract.ContactTypes.TYPE_COLOR,type.type_color);
            list.add(builder.build());
        }
    }

    public HashMap<String, ContactType> getTagMap() {
        return mTypes;
    }
}
