package com.easemob.easeui.utils;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * User: Scoield(553899626@qq.com)
 * Date: 2016-03-04
 * Time: 16:47
 * Description:时间工具类
 * FIXME
 */
public class DateUtils {
    public static long getUnixStamp(){
        return System.currentTimeMillis()/1000;
    }

    /**
     * 得到昨天的日期
     * @return
     */
    public static String getYestoryDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,-1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String yestoday = sdf.format(calendar.getTime());
        return yestoday;
    }

    /**
     * 得到今天的日期
     * @return
     */
    public static  String getTodayDate(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(new Date());
        return date;
    }

    /**
     * 时间戳转化为时间格式
     * @param timeStamp
     * @return
     */
    public static String timeStampToStr(long timeStamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = sdf.format(timeStamp * 1000);
        return date;
    }

    /**
     * 得到日期   yyyy-MM-dd
     * @param timeStamp  时间戳
     * @return
     */
    public static String formatDate(long timeStamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(timeStamp);
        return date;
    }

    /**
     * 得到时间  HH:mm:ss
     * @param timeStamp   时间戳
     * @return
     */
    public static String getTime(long timeStamp,String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        String date = sdf.format(timeStamp);
        return date;
    }

    /**
     * 将一个时间戳转换成提示性时间字符串，如刚刚，1秒前
     *
     * @param timeStamp
     * @return
     */
    public static String convertTimeToFormat(long timeStamp) {
        long curTime =System.currentTimeMillis();
        long time = (curTime - timeStamp)/1000;

        if (time < 3600 * 24 && time >= 0) {
            return getTime(timeStamp,"HH:mm");
        } else if (time >= 3600 * 24 && time < 3600 * 24 *2) {
            return "昨天";
        } else {
            return formatDate(timeStamp);
        }
    }

    /**
     * 将一个时间戳转换成提示性时间字符串，(多少分钟)
     *
     * @param timeStamp
     * @return
     */
    public static String timeStampToFormat(long timeStamp) {
        long curTime =System.currentTimeMillis() / (long) 1000 ;
        long time = curTime - timeStamp;
        return time/60 + "";
    }
}
