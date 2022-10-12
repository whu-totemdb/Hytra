package edu.whu.hyk.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    /**
     * Transform datetime (yyyy-MM-dd HH:mm:ss) format to timestamp in UTC +0
     * @param datetime
     * @return timestamp in seconds
     */
    public static long dateToTimeStamp(String datetime)  {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = simpleDateFormat.parse(datetime);
            return date.getTime() / 1000;
        } catch (Exception e) {
            return -1;
        }

    }



    public static int timeToSeconds(String time) {
        String[] elements = time.split(":");
        int hour = Integer.parseInt(elements[0]);
        int min = Integer.parseInt(elements[1]);
        int sec = Integer.parseInt(elements[2]);
        return hour * 3600 + min * 60 + sec;
    }

    public static String timestampToDate(long ts) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(ts * 1000));
    }

    public static void main(String[] args) {
        System.out.println(dateToTimeStamp("2022-01-07 00:00:01"));
    }
}
