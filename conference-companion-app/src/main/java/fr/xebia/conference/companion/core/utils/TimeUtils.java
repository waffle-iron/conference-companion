package fr.xebia.conference.companion.core.utils;

import android.content.Context;
import android.text.format.DateUtils;

import java.text.DateFormat;
import java.util.Date;
import java.util.Formatter;

public class TimeUtils {

    public static String formatShortDate(Context context, Date date) {
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb);
        return DateUtils.formatDateRange(context, formatter, date.getTime(), date.getTime(),
                DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_NO_YEAR).toString();
    }

    public static String formatShortTime(Context context, Date time) {
        return DateFormat.getTimeInstance(DateFormat.SHORT).format(time);
    }
}
