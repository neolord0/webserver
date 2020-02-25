package kr.dogfoot.webserver.httpMessage.header.valueobj;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;
import kr.dogfoot.webserver.util.bytes.OutputBuffer;
import kr.dogfoot.webserver.util.http.HttpString;
import kr.dogfoot.webserver.util.string.StringUtils;

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
    public void reset() {
        productList.clear();
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
        return buffer.getBytesAndRelease();
    }

    @Override
    public boolean isEqualValue(HeaderValue other) {
        if (other.sort() == HeaderSort.Upgrade) {
            HeaderValueUpgrade other2 = (HeaderValueUpgrade) other;
            int includedCount = 0;
            for (String product : other2.productList) {
                if (isInclude(product)) {
                    includedCount++;
                }
            }
            if (includedCount == other2.productList.size()) {
                return true;
            }
        }
        return false;
    }

    private boolean isInclude(String other) {
        for (String product : productList) {
            if (StringUtils.equalsWithNull(product, other)) {
                return true;
            }
        }
        return false;
    }

    public void addProduct(String product) {
        productList.add(product);
    }

    public String[] products() {
        return productList.toArray(Zero_Array);
    }
}
