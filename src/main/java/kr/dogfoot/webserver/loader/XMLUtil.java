package kr.dogfoot.webserver.loader;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class XMLUtil {
    private static String SPACE = " ";
    private static long MINUTE = 60;
    private static long HOUR = MINUTE * 60;
    private static long DAY = 24 * HOUR;
    private static long MONTH = 30 * DAY;
    private static long YEAR = 356 * DAY;

    private static long KILO = 1024;
    private static long MEGA = 1024 * KILO;
    private static long GIGA = 1024 * MEGA;

    public static String getCDATA(Element element) {
        Node child = element.getFirstChild();
        if (child instanceof CharacterData) {
            CharacterData cd = (CharacterData) child;
            return cd.getData();
        }
        return "";
    }

    public static long toDeltaSecond(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return parseDeltaSecond(value);
        }
    }

    private static long parseDeltaSecond(String value) {
        long deltaSecond = 0;
        long each = 0;
        String[] tokens = value.split(SPACE);
        for (String token : tokens) {
            each = parseDeltaSecondToken(token);
            if (each == -1) {
                return -1;
            }
            deltaSecond += each;
        }

        return deltaSecond;
    }

    private static long parseDeltaSecondToken(String token) {
        long value = 0;
        try {
            value = Long.parseLong(token.substring(0, token.length() - 1));
        } catch (NumberFormatException e) {
            return -1;
        }

        char unit = token.charAt(token.length() - 1);
        switch (unit) {
            case 'y':
                return value * YEAR;
            case 'M':
                return value * MONTH;
            case 'd':
                return value * DAY;
            case 'h':
                return value * HOUR;
            case 'm':
                return value * MINUTE;
            case 's':
                return value;
        }

        try {
            return Long.parseLong(token);
        } catch (NumberFormatException e) {
            return -1;
        }
    }


    public static long toDataSize(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return parseDataSize(value);
        }
    }

    private static long parseDataSize(String value) {
        long dataSize = 0;
        long each = 0;
        String[] tokens = value.split(SPACE);
        for (String token : tokens) {
            each = parseDataSizeToken(token);
            if (each == -1) {
                return -1;
            }
            dataSize += each;
        }

        return dataSize;
    }

    private static long parseDataSizeToken(String token) {
        long value = 0;
        try {
            value = Long.parseLong(token.substring(0, token.length() - 1));
        } catch (NumberFormatException e) {
            return -1;
        }

        char unit = token.charAt(token.length() - 1);
        switch (unit) {
            case 'g':
                return value * GIGA;
            case 'm':
                return value * MEGA;
            case 'k':
                return value * KILO;
        }

        try {
            return Long.parseLong(token);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static boolean toBoolean(String value) {
        return SettingXML.True_Value.equalsIgnoreCase(value);
    }

}
