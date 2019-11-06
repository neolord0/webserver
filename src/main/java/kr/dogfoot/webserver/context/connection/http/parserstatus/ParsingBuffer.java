package kr.dogfoot.webserver.context.connection.http.parserstatus;

import kr.dogfoot.webserver.util.bytes.BytesUtil;

public class ParsingBuffer {
    private static final int Initial_Size = 512;

    private byte[] data;
    private int length;


    public ParsingBuffer() {
        data = new byte[Initial_Size];
        length = 0;
    }

    public String newString() {
        return BytesUtil.newString(data, 0, length);
    }

    public byte[] newBytes() {
        return BytesUtil.newBytes(data, 0, length);
    }

    public byte[] data() {
        return data;
    }

    public void into(byte b) {
        if (length + 1 >= data.length) {
            extend();
        }
        data[length] = b;
        length++;
    }

    private void extend() {
        byte[] newData = new byte[data.length * 2];
        System.arraycopy(data, 0, newData, 0, data.length);
        data = newData;
    }

    public int length() {
        return length;
    }

    public void reset() {
        length = 0;
    }
}
