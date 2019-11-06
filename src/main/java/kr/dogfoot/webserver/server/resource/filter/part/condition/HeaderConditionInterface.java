package kr.dogfoot.webserver.server.resource.filter.part.condition;

import kr.dogfoot.webserver.httpMessage.header.HeaderList;

public interface HeaderConditionInterface {
    boolean isMatch(HeaderList headerList);
}
