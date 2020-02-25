package kr.dogfoot.webserver.httpMessage.header;

import java.util.ArrayList;

public class TestInteger {
    public static void main(String[] args) {
        Integer c = new Integer(0);
        ArrayList b = new ArrayList();

        System.out.println(System.identityHashCode(c));
        c =  new Integer(c + 1);
        System.out.println(System.identityHashCode(c));

        System.out.println(System.identityHashCode(b));
        b.add(new Long(11));
        System.out.println(System.identityHashCode(b));
    }
}
