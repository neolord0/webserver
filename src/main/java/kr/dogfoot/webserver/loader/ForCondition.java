package kr.dogfoot.webserver.loader;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.server.resource.filter.part.condition.CompareOperator;
import kr.dogfoot.webserver.server.resource.filter.part.condition.ConditionOperator;
import kr.dogfoot.webserver.server.resource.filter.part.condition.HeaderCondition;
import kr.dogfoot.webserver.server.resource.filter.part.condition.HeaderConditionList;
import org.w3c.dom.*;

public class ForCondition {
    public static void set(HeaderCondition condition, Element element) {
        NamedNodeMap attrMap = element.getAttributes();
        int count = attrMap.getLength();
        for (int index = 0; index < count; index++) {
            Attr attr = (Attr) attrMap.item(index);
            String attrName = attr.getName();
            if (SettingXML.Header_Attr.equalsIgnoreCase(attrName)) {
                condition.header(HeaderSort.fromString(attr.getValue()));
            } else if (SettingXML.Compare_Op_Attr.equalsIgnoreCase(attrName)) {
                condition.compareOperator(CompareOperator.fromString(attr.getValue()));
            } else if (SettingXML.Value_Attr.equalsIgnoreCase(attrName)) {
                condition.value(attr.getValue());
            }
        }
    }

    public static void setList(HeaderConditionList conditionList, Element element) {
        NamedNodeMap attrMap = element.getAttributes();
        int count = attrMap.getLength();
        for (int index = 0; index < count; index++) {
            Attr attr = (Attr) attrMap.item(index);
            String attrName = attr.getName();
            if (SettingXML.Condition_Op_Attr.equalsIgnoreCase(attrName)) {
                conditionList.conditionOperator(ConditionOperator.fromString(attr.getValue()));
            }
        }

        NodeList nodeList = element.getChildNodes();
        int count2 = nodeList.getLength();
        for (int index = 0; index < count2; index++) {
            Node node = nodeList.item(index);
            String nodeName = node.getNodeName();
            if (SettingXML.Condition_Node.equalsIgnoreCase(nodeName)) {
                set(conditionList.addNewHeaderCondition(), (Element) node);
            } else if (SettingXML.Condition_List_Node.equalsIgnoreCase(nodeName)) {
                setList(conditionList.addNewHeaderConditionList(), (Element) node);
            }
        }
    }
}
