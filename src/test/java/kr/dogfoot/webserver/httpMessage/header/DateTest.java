package kr.dogfoot.webserver.httpMessage.header;

import kr.dogfoot.webserver.util.http.HttpDateMaker;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.TimeZone;

public class DateTest {
    public static void main(String[] args) {
        int year = 2020;
        int month = 1;
        int day = 16;
        int hour = 4;
        int minute = 0;

        long date1 = Date.UTC(year - 1900, month - 1, day,
                hour, minute, 0);

        Date now = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        long date2 = now.getTime();


        System.out.println(now.getTime()+ " | " +  new String(HttpDateMaker.makeBytes(now.getTime())));
        System.out.println(date1 + " | " +  new String(HttpDateMaker.makeBytes(date1)));
        System.out.println(date2 + " | " +  new String(HttpDateMaker.makeBytes(date2)));

        System.out.println((date2 - date1) / 1000);
   }
}