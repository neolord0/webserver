package kr.dogfoot.webserver.server.resource.filter;

import kr.dogfoot.webserver.context.Context;
import kr.dogfoot.webserver.httpMessage.request.Request;
import kr.dogfoot.webserver.server.Server;
import kr.dogfoot.webserver.server.resource.filter.part.condition.HeaderConditionList;
import kr.dogfoot.webserver.util.http.HttpString;

import java.nio.ByteBuffer;

public class FilterExpectChecking extends Filter {
    private HeaderConditionList failCondition;

    public FilterExpectChecking() {
        failCondition = new HeaderConditionList();
    }

    @Override
    public boolean inboundProcess(Context context, Server server) {
        boolean continuePerform = true;
        Request request = context.request();
        if (request.hasExpect100Continue()) {
            if (failCondition.isMatch(request.headerList())) {
                context.response(server.objects().responseMaker().get_417ExpectationFail());
                continuePerform = false;
            } else {
                send100Continue(context, server);
            }
        }
        return continuePerform;
    }

    @Override
    public boolean outboundProcess(Context context, Server server) {
        return true;
    }

    @Override
    public FilterSort sort() {
        return FilterSort.ExpectChecking;
    }


    private void send100Continue(Context context, Server server) {
        ByteBuffer temp = ByteBuffer.wrap(HttpString.Response100Continue);

        server.bufferSender().sendBufferToClient(context, temp, false);
    }


    public HeaderConditionList failCondition() {
        return failCondition;
    }
}
