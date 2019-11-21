package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.server.resource.filter.part.condition.CompareOperator;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;

import java.util.ArrayList;

public class HeaderValueUpgrade extends HeaderValue {
    private static final String[] Zero_Array = new String[0];
    private ArrayList<String> productList;

    public HeaderValueUpgrade() {
        productList = new ArrayList<String>();
    }

    @Override
    public HeaderSort sort() {
        return HeaderSort.Upgrade;
    }

    @Override
    public void parseValue(byte[] value) throws ParserException {
        ParseState ps = ParseState.pooledObject();
        ps.ioff = 0;
        ps.bufend = value.length;
        ps.separator = HttpString.Comma;
        ps.spaceIsSep = false;

        while (ByteParser.nextItem(value, ps) >= 0) {
            String product = ps.toString(value);

            if (product != null) {
                productList.add(product);
            }
        }
        ParseState.release(ps);
    }

    @Override
    public byte[] combineValue() {
        OutputBuffer buffer = OutputBuffer.pooledObject();
        buffer.appendStringArray(HttpString.Comma, productList.toArray());
        byte[] ret = buffer.getBytes();
        OutputBuffer.release(buffer);
        return ret;
    }

    public void addProduct(String product) {
        productList.add(product);
    }

    public String[] products() {
        return productList.toArray(Zero_Array);
    }
}
