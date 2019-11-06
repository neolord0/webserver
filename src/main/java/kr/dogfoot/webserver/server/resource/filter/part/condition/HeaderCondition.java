package kr.dogfoot.webserver.server.resource.filter.part.condition;

import kr.dogfoot.webserver.httpMessage.header.HeaderItem;
import kr.dogfoot.webserver.httpMessage.header.HeaderList;
import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.HeaderValue;
import kr.dogfoot.webserver.parser.util.ParserException;

public class HeaderCondition implements HeaderConditionInterface {
    private HeaderSort header;
    private CompareOperator compareOperator;
    private String value;


    public HeaderCondition() {
    }

    public HeaderCondition(HeaderSort header, CompareOperator operator, String value) {
        this.header = header;
        this.compareOperator = operator;
        this.value = value;
    }

    @Override
    public boolean isMatch(HeaderList headerList) {
        HeaderItem headerItem = headerList.getHeader(this.header);

        if (compareOperator == CompareOperator.IsExist) {
            return compareExist(headerItem);
        }

        if (headerItem != null) {
            HeaderValue headerValue;
            try {
                headerValue = headerItem.updateValueObj();
            } catch (ParserException e) {
                headerValue = null;
            }
            if (headerValue != null) {
                return headerValue.compare(compareOperator, value);
            }
        }
        return false;
    }

    private boolean compareExist(HeaderItem header) {
        if (value.equalsIgnoreCase("true")) {
            return header != null;
        } else {
            return header == null;
        }
    }

    public HeaderSort header() {
        return header;
    }

    public void header(HeaderSort header) {
        this.header = header;
    }

    public CompareOperator compareOperator() {
        return compareOperator;
    }

    public void compareOperator(CompareOperator compareOperator) {
        this.compareOperator = compareOperator;
    }

    public String value() {
        return value;
    }

    public void value(String value) {
        this.value = value;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(header.toString()).append(" ").append(compareOperator.toString()).append(" ").append(value);
        return sb.toString();
    }
}
