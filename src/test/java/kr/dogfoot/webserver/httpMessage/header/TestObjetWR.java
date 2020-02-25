package kr.dogfoot.webserver.httpMessage.header;

import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValue;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValueVary;
import kr.dogfoot.webserver.httpMessage.request.MethodType;

import java.io.*;
import java.util.Base64;
import java.util.Map;

public class TestObjetWR {
    public static void main(String[] args) {
        MethodType requestMethod = MethodType.POST;
        HeaderValueVary varyHeader;
        Map<HeaderSort, HeaderValue> selectionFields;

        byte[] etag = {'1', '2', '3' ,'a', 'b', 'c'};
        long date = 28457;
        long age;
        long requestTime;
        long responseTime;
        boolean heuristic;
        long freshnessLifetime;

        byte[] serialized = null;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(requestMethod);
                oos.writeObject(etag);
                oos.writeObject(date);
                serialized = baos.toByteArray();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(Base64.getEncoder().encodeToString(serialized));;

        Object object;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(serialized)) {
            try (ObjectInputStream ois = new ObjectInputStream(bais)) {
                // 역직렬화된 Member 객체를 읽어온다.
                object = ois.readObject();
                System.out.println((MethodType) object);
                object = ois.readObject();
                System.out.println(new String((byte[])object));
                object = ois.readObject();
                System.out.println((long) object);

            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
