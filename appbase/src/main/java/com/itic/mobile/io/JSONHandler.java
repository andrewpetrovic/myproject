package com.itic.mobile.io;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.google.gson.JsonElement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;

/**
 * JSON handler
 * @author Andrea Ji
 * 变更历史:
 * 提交日期  姓名  主线版本  修改原因
 * ---------------------------------------------------------*
 * 2015-02-09  Andrea Ji v1.0.0  第一次提交
 */
public abstract class JSONHandler {

    protected static Context mContext;

    public JSONHandler(Context context) {
        mContext = context;
    }

    /**
     * 生成必要的ContentProviderOperation
     * @param list 通过调用子类的方法此传入list，然后将ContentProviderOperation对象填充进list供方法调用者使用
     */
    public abstract void makeContentProviderOperations(ArrayList<ContentProviderOperation> list);

    /**
     * 处理Json文件
     * @param element 将json串解析成JsonElement，在子类的实现中获取需要json内容
     */
    public abstract void process(JsonElement element);

    /**
     * 读取本地资源文件
     *
     * @param context context
     * @param resource 本地资源文件id
     *
     * @return 本地资源文件内容
     *
     * @throws IOException
     */
    public static String parseResource(Context context, int resource) throws IOException {
        InputStream is = context.getResources().openRawResource(resource);
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            is.close();
        }

        return writer.toString();
    }
}
