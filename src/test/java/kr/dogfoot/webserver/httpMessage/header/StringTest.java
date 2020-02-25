package kr.dogfoot.webserver.httpMessage.header;

public class StringTest {
    public static void main(String[] args) {
        String a = "1234";
        String b = new String(a);
        System.out.println(b);

        a = "5678";
        System.out.println(b);

        byte[] array1 = {12,34};
        byte[] array2 = array1.clone();

        System.out.println(array2[0] + ";" + array2[1]);
        array1[0] = 78;
        System.out.println(array1[0] + ";" + array1[1]);
        System.out.println(array2[0] + ";" + array2[1]);
    }
}
