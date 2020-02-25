package kr.dogfoot.webserver.server.resource.filter;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.httpMessage.util.ResponseSetter;
import kr.dogfoot.webserver.server.Server;
import kr.dogfoot.webserver.server.resource.filter.part.HeaderSetting;
import kr.dogfoot.webserver.server.resource.filter.part.condition.HeaderConditionList;

import java.util.ArrayList;

public class FilterHeaderAdding extends Filter {
    private final static HeaderSetting[] Zero_Array = new HeaderSetting[0];
    private HeaderConditionList addingCondition;
    private ArrayList<HeaderSetting> headerSettings;

    public FilterHeaderAdding() {
        addingCondition = new HeaderConditionList();
        headerSettings = new ArrayList<HeaderSetting>();
    }

    @Override
    public boolean inboundProcess(Context context, Server server) {
        return true;
    }

    @Override
    public boolean outboundProcess(Context context, Server server) {
        if (addingCondition.isMatch(context.response().headerList())) {
            for (HeaderSetting setting : headerSettings) {
                ResponseSetter.setHeader(context.response(), setting.sort(), setting.value().getBytes());
            }
        }
        return true;
    }

    @Override
    public FilterSort sort() {
        return FilterSort.HeaderAdding;
    }

    public HeaderConditionList addingCondition() {
        return addingCondition;
    }

    public HeaderSetting addNewHeaderSetting() {
        HeaderSetting hs = new HeaderSetting();
        headerSettings.add(hs);
        return hs;
    }

    public HeaderSetting[] headerSettings() {
        return headerSettings.toArray(Zero_Array);
    }
}
