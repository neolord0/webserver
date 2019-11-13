package kr.dogfoot.webserver.util.http;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class HttpDateMaker {
    private static final ThreadLocal<SimpleDateFormat> df
            = new ThreadLocal<SimpleDateFormat>(){
        @Override
        protected SimpleDateFormat initialValue() {
            SimpleDateFormat sdf = new SimpleDateFormat(
                    "EEE, dd MMM yyyy H:m:s z", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
            return sdf;
        }
    };

    public static byte[] makeBytes(Long date) {
        return df.get().format(date).getBytes();
    }
}
