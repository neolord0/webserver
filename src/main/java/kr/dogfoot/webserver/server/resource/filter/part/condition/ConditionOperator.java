package kr.dogfoot.webserver.server.resource.filter.part.condition;

public enum ConditionOperator {
    Unknown("", ""),
    And("And", "&"),
    Or("Or", "|");

    private String str;
    private String symbol;

    ConditionOperator(String str, String symbol) {
        this.str = str;
        this.symbol = symbol;
    }

    public static ConditionOperator fromString(String str) {
        if (str != null) {
            for (ConditionOperator co : values()) {
                if (str.compareToIgnoreCase(co.str) == 0) {
                    return co;
                }
            }
        }
        return Unknown;
    }

    @Override
    public String toString() {
        return symbol;
    }
}
