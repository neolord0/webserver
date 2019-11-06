package kr.dogfoot.webserver.util.http;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class HttpDateMaker {

    private static byte[][] bdays = {{(byte) 'P', (byte) 'a', (byte) 'd'},
            {(byte) 'S', (byte) 'u', (byte) 'n'},
            {(byte) 'M', (byte) 'o', (byte) 'n'},
            {(byte) 'T', (byte) 'u', (byte) 'e'},
            {(byte) 'W', (byte) 'e', (byte) 'd'},
            {(byte) 'T', (byte) 'h', (byte) 'u'},
            {(byte) 'F', (byte) 'r', (byte) 'i'},
            {(byte) 'S', (byte) 'a', (byte) 't'}};

    private static byte[][] bmonthes = {{(byte) 'J', (byte) 'a', (byte) 'n'},
            {(byte) 'F', (byte) 'e', (byte) 'b'},
            {(byte) 'M', (byte) 'a', (byte) 'r'},
            {(byte) 'A', (byte) 'p', (byte) 'r'},
            {(byte) 'M', (byte) 'a', (byte) 'y'},
            {(byte) 'J', (byte) 'u', (byte) 'n'},
            {(byte) 'J', (byte) 'u', (byte) 'l'},
            {(byte) 'A', (byte) 'u', (byte) 'g'},
            {(byte) 'S', (byte) 'e', (byte) 'p'},
            {(byte) 'O', (byte) 'c', (byte) 't'},
            {(byte) 'N', (byte) 'o', (byte) 'v'},
            {(byte) 'D', (byte) 'e', (byte) 'c'}};

    private static Calendar cal = null;

    private byte[] buffer;
    private int index;
    private Date now;

    private HttpDateMaker() {
        buffer = new byte[29];
        index = 0;
    }

    private static void createUTCCalendar() {
        if (cal == null) {
            TimeZone tz = TimeZone.getTimeZone("UTC");
            cal = Calendar.getInstance(tz);
        }
    }


    public static byte[] makeBytes(Long date) {
        createUTCCalendar();

        HttpDateMaker maker = new HttpDateMaker();
        maker.setDate(date);
        maker.dayOfWeek();
        maker.day();
        maker.month();
        maker.year();
        maker.hour();
        maker.minute();
        maker.second();
        maker.gmt();
        return maker.buffer;
    }


    private void setDate(Long date) {
        Date now = new Date(date.longValue());
        cal.setTime(now);
    }

    private void dayOfWeek() {
        int dayofweek = cal.get(Calendar.DAY_OF_WEEK);

        for (int i = 0; i < 3; i++) {
            buffer[index++] = bdays[dayofweek][i];
        }

        buffer[index++] = (byte) ',';
        buffer[index++] = (byte) ' ';
    }

    private void day() {
        int day = cal.get(Calendar.DAY_OF_MONTH);
        if (day < 10) {
            buffer[index++] = 48;
            buffer[index++] = (byte) (48 + day);
        } else {
            buffer[index++] = (byte) (48 + (day / 10));
            buffer[index++] = (byte) (48 + (day % 10));
        }
        buffer[index++] = (byte) ' ';
    }

    private void month() {
        int month = cal.get(Calendar.MONTH);
        for (int i = 0; i < 3; i++) {
            buffer[index++] = bmonthes[month][i];
        }
        buffer[index++] = (byte) ' ';
    }

    private void year() {
        int year = cal.get(Calendar.YEAR);
        // not y10k compliant
        buffer[index + 3] = (byte) (48 + (year % 10));
        year = year / 10;
        buffer[index + 2] = (byte) (48 + (year % 10));
        year = year / 10;
        buffer[index + 1] = (byte) (48 + (year % 10));
        year = year / 10;
        buffer[index] = (byte) (48 + year);
        index += 4;
        buffer[index++] = (byte) ' ';
    }

    private void hour() {
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if (hour < 10) {
            buffer[index++] = (byte) 48;
            buffer[index++] = (byte) (48 + hour);
        } else {
            buffer[index++] = (byte) (48 + (hour / 10));
            buffer[index++] = (byte) (48 + (hour % 10));
        }
        buffer[index++] = (byte) ':';
    }

    private void minute() {
        int minute = cal.get(Calendar.MINUTE);
        if (minute < 10) {
            buffer[index++] = (byte) 48;
            buffer[index++] = (byte) (48 + minute);
        } else {
            buffer[index++] = (byte) (48 + (minute / 10));
            buffer[index++] = (byte) (48 + (minute % 10));
        }
        buffer[index++] = (byte) ':';
    }

    private void second() {
        int second = cal.get(Calendar.SECOND);
        if (second < 10) {
            buffer[index++] = (byte) 48;
            buffer[index++] = (byte) (48 + second);
        } else {
            buffer[index++] = (byte) (48 + (second / 10));
            buffer[index++] = (byte) (48 + (second % 10));
        }
        buffer[index++] = (byte) ' ';
    }

    private void gmt() {
        buffer[index++] = (byte) 'G';
        buffer[index++] = (byte) 'M';
        buffer[index++] = (byte) 'T';
    }
}
