package kr.dogfoot.webserver.server.resource.filter.part.condition;

import kr.dogfoot.webserver.parser.util.ByteParser;
import kr.dogfoot.webserver.parser.util.ParseState;
import kr.dogfoot.webserver.parser.util.ParserException;

public enum CompareOperator {
    Unknown(""),
    Exist("Exist" ),
    NotExist("NotExist"),
    Include("Include"),
    NotInclude("NotInclude"),
    Equal("Equal"),
    NotEqual("NotEqual"),
    LessThan("LessThan"),
    GreaterThan("GreaterThan"),
    LessEqual("LessEqual"),
    GreaterEqual("GreaterEqual");

    private String str;

    CompareOperator(String str) {
        this.str = str;
    }

    public static CompareOperator fromString(String str) {
        if (str != null) {
            for (CompareOperator co : values()) {
                if (str.compareToIgnoreCase(co.str) == 0) {
                    return co;
                }
            }
        }
        return Unknown;
    }

    public boolean compareWithBytes() {
        switch (this) {
            case Include:
            case NotInclude:
            case Equal:
            case NotEqual:
                return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return this.str;
    }

    public boolean compareWithNumber(double value1, double value2) {
        switch (this) {
            case LessThan:
                return value1 > value2;
            case GreaterThan:
                return value1 < value2;
            case LessEqual:
                return value1 >= value2;
            case GreaterEqual:
                return value1 <= value2;
        }
        return false;
    }

    public boolean compareπWithByte(byte[] value1, byte[] value2) {
        switch (this) {
            case Include:
                return include(value1, value2);
            case NotInclude:
                return !include(value1, value2);
            case Equal:
                return equal(value1, value2);
            case NotEqual:
                return !equal(value1, value2);
        }
        return false;
    }

    private boolean include(byte[] outerArray, byte[] smallerArray) {
        for(int i = 0; i < outerArray.length - smallerArray.length+1; ++i) {
            boolean found = true;
            for(int j = 0; j < smallerArray.length; ++j) {
                if (outerArray[i+j] != smallerArray[j]) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return true;
            }
        }
        return false;
    }

    private boolean equal(byte[] bytes1, byte[] bytes2) {
        if (bytes1.length < bytes2.length) {
            return false;
        }
        int count = bytes2.length;
        for (int index = 0; index < count; index++) {
            if (bytes1[index] != bytes2[index]) {
                return false;
            }
        }
        return true;
    }

    public boolean compareWithDate(Long date1, byte[] dateBytes) {
        ParseState ps = ParseState.pooledObject();
        ps.ioff = 0;
        ps.bufend = dateBytes.length;
        long date2 =  0;
        boolean error = false;
        try {
            date2 = new Long(ByteParser.parseDate(dateBytes, ps));
        } catch (ParserException e) {
            error = true;
        }
        ParseState.release(ps);
        if (error == false) {
            return compareWithNumber(date1, date2);
        }
        return false;
    }
}
