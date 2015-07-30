package com.itic.mobile.util.datetime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeUtils {
    public static final long stringToLong(String values){
        String pattern = "yyyy-MM-dd";
        SimpleDateFormat format = new SimpleDateFormat(pattern);//然后创建一个日期格式化类
        Date convertResult = null;
        try {
            convertResult = format.parse(values);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return convertResult.getTime();
    }

    public static final long dateToLang(Date date){
        return date.getTime();
    }

    public static final String dateToLongString(Date date){
        return Long.toString(dateToLang(date));
    }

    public static final long stringToDateTime(String values){
        String pattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat format = new SimpleDateFormat(pattern);//然后创建一个日期格式化类
        Date convertResult = null;
        try {
            convertResult = format.parse(values);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return convertResult.getTime();
    }

    public static final String dateToString(long timeValue){
        SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd");
        Date dt = new Date(timeValue);
        return sdf.format(dt);
    }

    public static final String datetimeToString(long timeValue){
        SimpleDateFormat sdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dt = new Date(timeValue);
        return sdf.format(dt);
    }
}
