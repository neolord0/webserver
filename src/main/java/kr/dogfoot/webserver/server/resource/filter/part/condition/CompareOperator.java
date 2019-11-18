package kr.dogfoot.webserver.server.resource.filter.part.condition;

public enum CompareOperator {
    Unknown(""),
    IsExist("IsExist" ),
    IsNotExist("IsNotExist"),
    IsInclude("IsInclude"),
    IsNotInclude("IsNotInclude"),
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

    @Override
    public String toString() {
        return this.str;
    }

    public boolean compareNumber(long value1, long value2) {
        switch (this) {
            case Equal:
                return value1 == value2;
            case NotEqual:
                return value1 == value2;
            case LessThan:
                return value1 < value2;
            case GreaterThan:
                return value1 > value2;
            case LessEqual:
                return value1 <= value2;
            case GreaterEqual:
                return value1 >= value2;
        }
        return false;
    }

    public boolean compareπByte(byte[] value1, byte[] value2) {
        switch (this) {
            case Equal:
                return equal(value1, value2);
            case NotEqual:
                return !equal(value1, value2);
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
}
