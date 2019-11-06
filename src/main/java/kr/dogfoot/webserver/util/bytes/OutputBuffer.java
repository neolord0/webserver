package kr.dogfoot.webserver.util.bytes;

import kr.dogfoot.webserver.util.http.HttpString;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class OutputBuffer {
    private static final int MAX_DIGIT = 20;
    private static ConcurrentLinkedQueue<OutputBuffer> obPool = new ConcurrentLinkedQueue<OutputBuffer>();
    private ArrayList<byte[]> itemList;
    private int length;

    private OutputBuffer() {
        itemList = new ArrayList<byte[]>();
        length = 0;
    }

    public static OutputBuffer pooledObject() {
        OutputBuffer ob = obPool.poll();
        if (ob == null) {
            ob = new OutputBuffer();
        }
        ob.reset();
        return ob;
    }

    public static void release(OutputBuffer ob) {
        obPool.add(ob);
    }

    public static int getWriteSize_Long(long value) {
        int size = 0;

        boolean neg = (value < 0);
        if (!neg) {
            value = -value;
        }

        while (value <= -10) {
            size++;
            value = value / 10;
        }
        size++;

        if (neg) {
            size++;
        }

        return size;
    }

    public static long getWriteSize_QValue(float qvalue) {
        if (qvalue > 1 || qvalue < 0) {
            return 0;
        }
        if (qvalue == 1) {
            return 5;       // q=1.0
        } else if (qvalue == 0) {
            return 3;       // q=0
        } else {
            long size = 2;  // q=
            String str = new Float(qvalue).toString();
            if (str.length() > 5) {
                size += 5;
            } else {
                size += str.length();
            }
            return size;
        }
    }

    public final OutputBuffer append(byte b) {
        byte[] item = new byte[1];
        item[0] = b;
        itemList.add(item);
        length++;

        return this;
    }

    public final OutputBuffer append(char ch) {
        append((byte) ch);
        return this;
    }

    public final OutputBuffer append(int i) {
        append((byte) i);
        return this;
    }

    public final OutputBuffer append(byte[] b) {
        if (b != null && b.length > 0) {
            itemList.add(b);
            length += b.length;
        }
        return this;
    }

    public final OutputBuffer append(byte[] b, int offset, int length) {
        byte[] b2 = new byte[length];
        System.arraycopy(b, offset, b2, 0, length);
        append(b2);
        return this;
    }

    public final OutputBuffer appendLong(long value) {
        appendLong(value, -1, (byte) 0);
        return this;
    }

    public final OutputBuffer appendLong(long value, int padlen, byte pad) {
        byte[] buf = new byte[MAX_DIGIT];
        int bufIndex = 0;

        boolean neg = (value < 0);
        if (!neg) {
            value = -value;
        }

        while (value <= -10) {
            buf[bufIndex++] = ((byte) (HttpString.Zero - (value % 10)));
            padlen--;
            value = value / 10;
        }
        buf[bufIndex++] = (byte) (HttpString.Zero - value);
        padlen--;

        if (neg) {
            buf[bufIndex++] = HttpString.Negative;
            padlen--;
        }
        while (--padlen >= 0) {
            buf[bufIndex++] = pad;
        }

        reverseArray(buf, bufIndex);
        append(buf, 0, bufIndex);
        return this;
    }

    private void reverseArray(byte[] buf, int length) {
        int cnt = length / 2;
        int i = 0;
        int j = length - 1;

        while (--cnt >= 0) {
            byte tmp = buf[j];
            buf[j] = buf[i];
            buf[i] = tmp;

            i++;
            j--;
        }
    }

    public final OutputBuffer appendInt(int value) {
        appendLong(value, -1, (byte) 0);
        return this;
    }

    public final OutputBuffer appendInt(int value, int padlen, byte pad) {
        appendLong(value, padlen, pad);
        return this;
    }

    public final OutputBuffer append(double d) {
        append(Double.toString(d).getBytes());
        return this;
    }

    public final OutputBuffer append(String str) {
        append(str.getBytes());
        return this;
    }

    public final OutputBuffer appendQuoted(String str) {
        append(HttpString.DQuote);
        append(str.getBytes());
        append(HttpString.DQuote);
        return this;
    }

    public final OutputBuffer appendQuoted(byte[] bytes) {
        append(HttpString.DQuote);
        append(bytes);
        append(HttpString.DQuote);
        return this;
    }

    public final OutputBuffer append(String name, byte sep, String value) {
        append(name.getBytes(), sep, value.getBytes());
        return this;
    }

    public final OutputBuffer append(byte[] name, byte sep, byte[] value) {
        append(name);
        append(sep);
        append(value);
        return this;
    }

    public final OutputBuffer append(String name, byte sep, int value) {
        append(name.getBytes());
        append(sep);
        appendInt(value);
        return this;
    }

    public final OutputBuffer appendQuoted(String name, byte sep, String value) {
        append(name.getBytes());
        append(sep);
        append(HttpString.DQuote);
        append(value.getBytes());
        append(HttpString.DQuote);
        return this;
    }

    public final OutputBuffer appendStringArray(byte sep, Object[] arr) {
        boolean first = true;
        for (Object o : arr) {
            if (first) {
                first = false;
            } else {
                append(sep);
                append(HttpString.Space);
            }
            append(o.toString());
        }
        return this;
    }

    public final OutputBuffer appendQValue(float qvalue) {
        if (qvalue > 1 || qvalue < 0) {
            return this;
        }
        if (qvalue == 1) {
            append("q=1.0");
        } else if (qvalue == 0) {
            append("q=0");
        } else {
            append("q=");
            String str = new Float(qvalue).toString();
            if (str.length() > 5) {
                append(str.substring(0, 5));
            } else {
                append(str);
            }
        }
        return this;
    }

    public OutputBuffer appendCRLF() {
        append(HttpString.CRLF);
        return this;
    }

    public OutputBuffer appendHttpVersion(short majorVersion, short minorVersion) {
        append(HttpString.Version_Prefix);
        appendInt(majorVersion);
        append(HttpString.Dot);
        appendInt(minorVersion);
        return this;
    }

    public OutputBuffer appendSP() {
        append(HttpString.Space);
        return this;
    }

    public OutputBuffer appendComma() {
        append(HttpString.Comma);
        return this;
    }

    public final OutputBuffer appendArray(byte sep, Object[] arr) {
        boolean first = true;
        for (AppendableToByte atb : (AppendableToByte[]) arr) {
            if (first) {
                first = false;
            } else {
                append(sep);
                append(HttpString.Space);
            }
            atb.append(this);
        }
        return this;
    }

    public OutputBuffer appendBoundary(byte[] boundary) {
        append(HttpString.BoundaryPrefix);
        append(boundary);
        return this;
    }

    public OutputBuffer appendEndBoundary(byte[] boundary) {
        append(HttpString.BoundaryPrefix);
        append(boundary);
        append(HttpString.BoundaryPrefix);
        return this;
    }

    public final byte[] getBytes() {
        byte[] all = new byte[length];
        int index = 0;
        for (byte[] item : itemList) {
            if (item.length == 1) {
                all[index++] = item[0];
            } else {
                System.arraycopy(item, 0, all, index, item.length);
                index += item.length;
            }
        }
        return all;
    }

    public int getLength() {
        return length;
    }

    public void write(OutputStream os) throws IOException {
        for (byte[] item : itemList) {
            os.write(item);
        }
        os.flush();
        reset();
    }

    public void reset() {
        itemList.clear();
        length = 0;
    }

}
