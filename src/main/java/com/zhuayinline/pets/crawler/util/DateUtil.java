package com.zhuayinline.pets.crawler.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Devin Zhang
 * @className DateUtil
 * @description TODO
 * @date 2019/9/25 17:18
 */

public class DateUtil {

    public static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    public static final String DEFAULT_PATTERN2 = "yyyy-MM-dd";

    /**
     * 获取当前时间字符串
     *
     * @return string
     */
    public static String getNow() {
        return date2Str(new Date());
    }

    /**
     * 时间转字符串
     *
     * @param date date
     * @return String
     */
    public static String date2Str(Date date) {
        return date2Str(date, DEFAULT_PATTERN);
    }

    /**
     * 时间转字符串
     *
     * @param date    date
     * @param pattern pattern
     * @return String
     */
    public static String date2Str(Date date, String pattern) {
        SimpleDateFormat sim = new SimpleDateFormat(pattern);
        return sim.format(date);
    }


    /**
     * 字符串转Date
     *
     * @param str str
     * @return
     */
    public static Date str2Date(String str) {
        return str2Date(str, DEFAULT_PATTERN);
    }

    /**
     * 字符串转date
     *
     * @param str     str
     * @param pattern pattern
     * @return Date
     */
    public static Date str2Date(String str, String pattern) {
        SimpleDateFormat sim = new SimpleDateFormat(pattern);
        try {
            return sim.parse(str);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 给date日期增加指定天数
     *
     * @param date,days
     * @return
     */
    public static Date addDays(Date date, int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return calendar.getTime();
    }

    /**
     * 给date日期增加指定分钟
     *
     * @param date,days
     * @return
     */
    public static Date addMinutes(Date date, int minutes) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MINUTE, minutes);
        return calendar.getTime();
    }

    /**
     * 给定时间与当前时间相隔的天数。
     *
     * @param date
     * @return >0 比今天晚多少天，=0同一天，<0比今天早多少天
     */
    public static int betweenDays(Date date) {
        return betweenDays(new Date(), date);
    }


    /**
     * 给定时间与当前时间相隔的天数。
     *
     * @param begin
     * @param end
     * @return >0 begin比end晚多少天，=0同一天，<0 begin比end早多少天
     */
    public static int betweenDays(Date begin, Date end) {
        try {
            begin=new SimpleDateFormat("yyyy-MM-dd").parse(new SimpleDateFormat("yyyy-MM-dd").format(begin));
            end=new SimpleDateFormat("yyyy-MM-dd").parse(new SimpleDateFormat("yyyy-MM-dd").format(end));
            long days=(end.getTime()-begin.getTime())/(1000*3600*24);
            if(days==0) {
                LocalDate a = begin.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                LocalDate b = end.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                return Period.between(a, b).getDays();
            }
            return (int)days;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;

    }

    /**
     * 获取某天的起始时间
     *
     * @return
     */
    public static Date getStartTimeOfDay(Date date) {
        Calendar startOfDate = Calendar.getInstance();
        startOfDate.setTime(date);
        startOfDate.set(Calendar.HOUR_OF_DAY, 0);
        startOfDate.set(Calendar.MINUTE, 0);
        startOfDate.set(Calendar.SECOND, 0);
        startOfDate.set(Calendar.MILLISECOND, 0);
        return startOfDate.getTime();
    }

    /**
     * 获取某天最后一刻
     *
     * @return
     */
    public static Date getEndTimeOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
                23, 59, 59);
        Date endOfDate = calendar.getTime();
        return endOfDate;
    }
}
