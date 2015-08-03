package com.itic.mobile.util.datetime;

import android.content.Context;

import com.itic.mobile.BuildConfig;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateTimeUtils {

    public static final long SECOND_MILLIS = 1000;
    public static final long MINUTE_MILLIS = 60 * SECOND_MILLIS;
    public static final long HOUR_MILLIS = 60 * MINUTE_MILLIS;
    public static final long DAY_MILLIS = 24 * HOUR_MILLIS;

    private static final long sAppLoadTime = System.currentTimeMillis();

    public static long getCurrentTime(final Context context) {
        if (BuildConfig.DEBUG) {
            return context.getSharedPreferences("mock_data", Context.MODE_PRIVATE)
                    .getLong("mock_current_time", System.currentTimeMillis())
                    + System.currentTimeMillis() - sAppLoadTime;
        } else {
            return System.currentTimeMillis();
        }
    }

    //获取明天正0点时间
    public static long getNextDayTime(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH,1);
        return DateTimeUtils.dateToLang(calendar.getTime());
    }

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
