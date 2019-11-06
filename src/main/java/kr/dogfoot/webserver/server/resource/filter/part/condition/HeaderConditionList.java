package kr.dogfoot.webserver.server.resource.filter.part.condition;

import kr.dogfoot.webserver.httpMessage.header.HeaderList;

import java.util.ArrayList;
import java.util.Iterator;

public class HeaderConditionList implements HeaderConditionInterface, Iterable<HeaderConditionInterface> {
    private ArrayList<HeaderConditionInterface> list;
    private ConditionOperator conditionOperator;

    public HeaderConditionList() {
        list = new ArrayList<HeaderConditionInterface>();
        conditionOperator = ConditionOperator.And;
    }

    @Override
    public String toString() {
        if (list.size() == 1) {
            return list.get(0).toString();
        } else {
            StringBuffer sb = new StringBuffer();
            sb.append("{");

            boolean first = true;
            for (HeaderConditionInterface hci : list) {
                if (first == true) {
                    first = false;
                } else {
                    sb
                            .append(" ")
                            .append(conditionOperator.toString())
                            .append(" ");
                }
                sb.append(hci.toString());
            }

            sb.append("}");
            return sb.toString();
        }
    }

    @Override
    public boolean isMatch(HeaderList headerList) {
        if (list.isEmpty()) {
            return true;
        }
        if (conditionOperator == ConditionOperator.And) {
            for (HeaderConditionInterface hci : list) {
                if (hci.isMatch(headerList) == false) {
                    return false;
                }
            }
            return true;
        } else if (conditionOperator == ConditionOperator.Or) {
            for (HeaderConditionInterface hci : list) {
                if (hci.isMatch(headerList) == true) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    @Override
    public Iterator<HeaderConditionInterface> iterator() {
        return list.iterator();
    }

    public HeaderCondition addNewHeaderCondition() {
        HeaderCondition hc = new HeaderCondition();
        list.add(hc);
        return hc;
    }

    public HeaderConditionList addNewHeaderConditionList() {
        HeaderConditionList hcl = new HeaderConditionList();
        list.add(hcl);
        return hcl;
    }

    public ConditionOperator conditionOperator() {
        return conditionOperator;
    }

    public void conditionOperator(ConditionOperator conditionOperator) {
        this.conditionOperator = conditionOperator;
    }
}
