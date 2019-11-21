package kr.dogfoot.webserver.httpMessage.header;

import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValue;
import kr.dogfoot.webserver.server.resource.filter.part.condition.CompareOperator;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CompareHeader {
    @Test
    public void compareHeader() {
        HeaderList headerList = new HeaderList();
        headerList.addHeaderFromBytes(HeaderSort.Content_Length, "10".getBytes());
        headerList.addHeaderFromBytes(HeaderSort.Date, "Wed, 21 Oct 2015 07:00:00 GMT".getBytes());
        headerList.addHeaderFromBytes(HeaderSort.Accept_Charset, "euc-kr:q=1, utf-8:q=1, utf-16le:q=0.9".getBytes());

        assertEquals(headerList.compare(HeaderSort.Content_Length, CompareOperator.Equal, "10"), true);
        assertEquals(headerList.compare(HeaderSort.Content_Length, CompareOperator.GreaterEqual, "11"), true);
        assertEquals(headerList.compare(HeaderSort.Content_Length, CompareOperator.LessEqual, "9"), true);
        assertEquals(headerList.compare(HeaderSort.Content_Type, CompareOperator.NotExist, null), true);
        assertEquals(headerList.compare(HeaderSort.Content_MD5, CompareOperator.Exist, null), false);
        assertEquals(headerList.compare(HeaderSort.Date, CompareOperator.GreaterEqual, "Wed, 21 Oct 2015 08:00:00 GMT"), true);

        assertEquals(headerList.compare(HeaderSort.Accept_Charset, CompareOperator.Include, "utf-8"), true);
    }
}
