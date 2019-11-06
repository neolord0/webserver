package kr.dogfoot.webserver.server.resource.negotiation;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.server.resource.ResourceDirectory;
import kr.dogfoot.webserver.server.resource.ResourceFile;
import kr.dogfoot.webserver.server.resource.filter.part.condition.HeaderCondition;

import java.util.ArrayList;

public class NegotiationVariant extends ResourceFile {
    private static final HeaderCondition[] Zero_Array = new HeaderCondition[0];
    private ArrayList<HeaderCondition> conditionList;

    public NegotiationVariant(ResourceDirectory parentDirectory) {
        super(parentDirectory);

        conditionList = new ArrayList<HeaderCondition>();
    }

    public void addCondition(HeaderCondition condition) {
        conditionList.add(condition);
    }

    public HeaderCondition getCondition(HeaderSort headerSort) {
        for (HeaderCondition condition : conditionList) {
            if (condition.header() == headerSort) {
                return condition;
            }
        }
        return null;
    }

    public HeaderCondition[] conditions() {
        return conditionList.toArray(Zero_Array);
    }
}
