package kr.dogfoot.webserver.util.bytes;

import kr.dogfoot.webserver.httpMessage.header.valueobj.part.ContentCodingSort;
import kr.dogfoot.webserver.server.object.BufferManager;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.zip.DeflaterInputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Compressor {
    private static final int Buffer_Size = 1000;

    public static byte[] compress(ContentCodingSort contentCoding, File file, BufferManager bufferManager) {
        FileChannel channel = openFile(file);
        if (channel == null) {
            return null;
        }
        ByteArrayOutputStream ba_os = new ByteArrayOutputStream();
        ByteBuffer buffer = bufferManager.pooledBuffer(Buffer_Size);
        int numRead = 0;

        try {
            OutputStream os = createOutputStream(contentCoding, ba_os);

            while ((numRead = channel.read(buffer)) != -1) {
                if (numRead > 0) {
                    os.write(buffer.array(), 0, numRead);
                }
                buffer.clear();
            }

            channel.close();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ba_os.toByteArray();
    }

    private static OutputStream createOutputStream(ContentCodingSort contentCoding, OutputStream os) throws IOException {
        switch (contentCoding) {
            case GZip:
                return new GZIPOutputStream(os);
            case Deflate:
                return new DeflaterOutputStream(os);
            case Compress:
                break;
            case Identity:
                break;
            case BR:
                break;
            case Asterisk:
                break;
            case Unknown:
                break;
        }
        return null;
    }

    public static byte[] compress(ContentCodingSort contentCoding, byte[] bytes) {
        ByteArrayInputStream ba_is = new ByteArrayInputStream(bytes);
        ByteArrayOutputStream ba_os = new ByteArrayOutputStream();
        byte[] buffer = new byte[Buffer_Size];
        int numRead = 0;

        try {
            OutputStream os = createOutputStream(contentCoding, ba_os);

            while ((numRead = ba_is.read(buffer)) != -1) {
                if (numRead > 0) {
                    os.write(buffer, 0, numRead);
                }
            }

            ba_is.close();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ba_os.toByteArray();
    }


    private static FileChannel openFile(File file) {
        FileChannel channel = null;
        try {
            channel = new FileInputStream(file).getChannel();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return channel;
    }

    public static byte[] decompress(ContentCodingSort contentCoding, byte[] bytes) {
        ByteArrayInputStream ba_is = new ByteArrayInputStream(bytes);
        ByteArrayOutputStream ba_os = new ByteArrayOutputStream();
        byte[] buffer = new byte[Buffer_Size];
        int numRead;

        try {
            InputStream is = createInputStream(contentCoding, ba_is);

            while ((numRead = is.read(buffer)) > 0) {
                if (numRead > 0) {
                    ba_os.write(buffer, 0, numRead);
                }
            }

            is.close();
            ba_os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ba_os.toByteArray();
    }

    private static InputStream createInputStream(ContentCodingSort contentCoding, InputStream is) throws IOException {
        switch (contentCoding) {
            case GZip:
                return new GZIPInputStream(is);
            case Deflate:
                return new DeflaterInputStream(is);
            case Compress:
                break;
            case Identity:
                break;
            case BR:
                break;
            case Asterisk:
                break;
            case Unknown:
                break;
        }
        return null;
    }
}
