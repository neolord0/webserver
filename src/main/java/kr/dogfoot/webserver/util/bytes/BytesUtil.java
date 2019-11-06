package kr.dogfoot.webserver.util.bytes;

public class BytesUtil {
    public static final int compare(byte[] b1, int o1, int l1
            , byte[] b2, int o2, int l2) {
        while ((o1 < l1) && (o2 < l2)) {
            int cmp = (((int) b1[o1]) & 0xff) - (((int) b2[o2]) & 0xff);
            if (cmp != 0)
                return cmp;
            o1++;
            o2++;
        }
        return ((o1 == l1) && (o2 == l2)) ? 0 : l2 - l1;
    }

    public static final int compare(byte[] b1, int o1, int l1, byte[] b2) {
        return compare(b1, o1, l1, b2, 0, b2.length);
    }

    public static final int compare(byte[] base, byte[] compare) {
        return compare(base, 0, base.length, compare, 0, base.length);
    }

    public static int indexOf(byte[] outerArray, byte[] smallerArray) {
        for (int i = 0; i < outerArray.length - smallerArray.length + 1; ++i) {
            boolean found = true;
            for (int j = 0; j < smallerArray.length; ++j) {
                if (outerArray[i + j] != smallerArray[j]) {
                    found = false;
                    break;
                }
            }
            if (found) return i;
        }
        return -1;
    }


    public static final byte[] newBytes(byte[] bytes, int offset, int length) {
        byte[] newBytes = new byte[length];
        System.arraycopy(bytes, offset, newBytes, 0, length);
        return newBytes;
    }

    public static String newString(byte[] bytes, int offset, int length) {
        return new String(bytes, offset, length);
    }

    public static byte[] merge(byte[] bytes1, int offset1, int length1, byte[] bytes2, int offset2, int length2) {
        byte[] merge = new byte[length1 + length2];
        System.arraycopy(bytes1, offset1, merge, 0, length1);
        System.arraycopy(bytes2, offset2, merge, length1, length2);
        return merge;
    }
}
