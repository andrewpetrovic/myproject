package com.example.andrea.demodata;

import android.content.Context;
import android.net.Uri;

public class AppDataHandlerImpl extends AppDataHandler {

    private ContactHandler mContactHandler;
    private static final String DATA_KEY_CONTACTS = "contacts";


    public AppDataHandlerImpl(Context ctx) {
        super(ctx, Contract.BASE_CONTENT_URI);
        setTopLevelPath(Contract.TOP_LEVEL_PATHS);
    }

    @Override
    public void mappingJsonHandler() {
        mHandlerForKey.put(DATA_KEY_CONTACTS,mContactHandler = new ContactHandler(mContext));
    }
}
