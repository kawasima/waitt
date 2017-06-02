package net.unit8.waitt.feature.tracer.util;

import java.util.Calendar;
import java.util.Date;

/**
 * @author kawasima
 */
public class ISO8601Formatter {
    public static String format(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        String sb = lpad(cal.get(Calendar.YEAR), 4) +
                '-' +
                lpad(cal.get(Calendar.MONTH) + 1, 2) +
                '-' +
                lpad(cal.get(Calendar.DAY_OF_MONTH), 2) +
                'T' +
                lpad(cal.get(Calendar.HOUR_OF_DAY), 2) +
                ':' +
                lpad(cal.get(Calendar.MINUTE), 2) +
                ':' +
                lpad(cal.get(Calendar.SECOND), 2) +
                '.' +
                lpad(cal.get(Calendar.MILLISECOND), 3);
        return sb;
    }

    private static String lpad(int dateElement, int length) {
        String elStr = Integer.toString(dateElement);
        int elStrLen = elStr.length();
        if (elStrLen >= length) {
            return elStr.substring(elStrLen - elStr.length());
        } else {
            StringBuilder sb = new StringBuilder(length);
            for (int i=0; i < length - elStrLen; i++)
                sb.append(' ');
            sb.append(elStr);
            return sb.toString();
        }
    }
}
