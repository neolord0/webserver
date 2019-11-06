package kr.dogfoot.webserver.util.bytes;

public class ToBytes {
    private static final int MAX_DIGIT = 20;

    public static byte[] fromLong(long value) {
        byte[] buf = new byte[MAX_DIGIT];
        int bufIndex = 0;

        boolean neg = (value < 0);
        if (!neg) {
            value = -value;
        }

        while (value <= -10) {
            buf[bufIndex++] = ((byte) ('0' - (value % 10)));
            value = value / 10;
        }
        buf[bufIndex++] = (byte) ('0' - value);

        if (neg) {
            buf[bufIndex++] = '-';
        }

        return newReverseArray(buf, bufIndex);
    }

    private static byte[] newReverseArray(byte[] buf, int length) {
        byte[] newBytes = new byte[length];

        for (int i = 0; i < length; i++) {
            newBytes[i] = buf[length - i - 1];
        }

        return newBytes;
    }

    public static byte[] fromInt(int value) {
        return fromLong(value);
    }
}
