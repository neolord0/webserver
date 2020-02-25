package kr.dogfoot.webserver.httpMessage.header;

import kr.dogfoot.webserver.loader.XMLUtil;

public class TestDeltaSecondParserAndDataSize {
    public static void main(String[] args) {
        System.out.println(XMLUtil.toDeltaSecond("10s"));
        System.out.println(XMLUtil.toDeltaSecond("1h"));
        System.out.println(XMLUtil.toDeltaSecond("20m"));
        System.out.println(XMLUtil.toDeltaSecond("1y"));

        System.out.println(XMLUtil.toDataSize("200k 100"));
        System.out.println(XMLUtil.toDataSize("200m"));
        System.out.println(XMLUtil.toDataSize("200g"));
    }
}
