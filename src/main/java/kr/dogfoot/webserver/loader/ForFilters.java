package kr.dogfoot.webserver.loader;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.httpMessage.header.valueobj.part.ContentCodingSort;
import kr.dogfoot.webserver.httpMessage.reply.ReplyCode;
import kr.dogfoot.webserver.httpMessage.request.MethodType;
import kr.dogfoot.webserver.loader.resourcesetting.SettingItem;
import kr.dogfoot.webserver.server.resource.filter.*;
import kr.dogfoot.webserver.server.resource.filter.part.HeaderSetting;
import org.w3c.dom.*;

public class ForFilters {
    public static void set(SettingItem settingItem, Element element) {
        NodeList nodeList = element.getChildNodes();
        int count2 = nodeList.getLength();
        for (int index = 0; index < count2; index++) {
            Node node = nodeList.item(index);
            String nodeName = node.getNodeName();

            if (SettingXML.Basic_Authorization_Node.equalsIgnoreCase(nodeName)) {
                addFilter_BasicAuthorization(settingItem, (Element) node);
            } else if (SettingXML.Expect_Checking_Node.equalsIgnoreCase(nodeName)) {
                addFilter_ExpectChecking(settingItem, (Element) node);
            } else if (SettingXML.Allowed_Method_Checking_Node.equalsIgnoreCase(nodeName)) {
                addFilter_AllowedMethodChecking(settingItem, (Element) node);
            } else if (SettingXML.URL_Redirecting_Node.equalsIgnoreCase(nodeName)) {
                addFilter_URLRedirecting(settingItem, (Element) node);
            } else if (SettingXML.Header_Adding_Node.equalsIgnoreCase(nodeName)) {
                addFilter_HeaderAdding(settingItem, (Element) node);
            } else if (SettingXML.Charset_Encoding_Node.equalsIgnoreCase(nodeName)) {
                addFilter_CharsetEncoding(settingItem, (Element) node);
            } else if (SettingXML.Content_Encoding_Node.equalsIgnoreCase(nodeName)) {
                addFilter_ContentEncoding(settingItem, (Element) node);
            }
        }
    }

    private static void addFilter_BasicAuthorization(SettingItem settingItem, Element element) {
        FilterBasicAuthorization ba = new FilterBasicAuthorization();

        NamedNodeMap attrMap = element.getAttributes();
        int count = attrMap.getLength();
        for (int index = 0; index < count; index++) {
            Attr attr = (Attr) attrMap.item(index);
            String attrName = attr.getName();
            if (SettingXML.Realm_Description_Attr.equalsIgnoreCase(attrName)) {
                ba.realmDescription(attr.getValue());
            } else if (SettingXML.Username_Attr.equalsIgnoreCase(attrName)) {
                ba.userName(attr.getValue());
            } else if (SettingXML.Password_Attr.equalsIgnoreCase(attrName)) {
                ba.password(attr.getValue());
            }
        }
        settingItem.addFilter(ba);
    }

    private static void addFilter_ExpectChecking(SettingItem settingItem, Element element) {
        FilterExpectChecking ec = new FilterExpectChecking();
        NodeList nodeList = element.getChildNodes();
        int count2 = nodeList.getLength();
        for (int index = 0; index < count2; index++) {
            Node node = nodeList.item(index);
            String nodeName = node.getNodeName();
            if (SettingXML.Fail_Condition_Node.equalsIgnoreCase(nodeName)) {
                ForCondition.set(ec.failCondition().addNewHeaderCondition(), (Element) node);
            } else if (SettingXML.Fail_Condition_List_Node.equalsIgnoreCase(nodeName)) {
                ForCondition.setList(ec.failCondition(), (Element) node);
            }
        }
        settingItem.addFilter(ec);
    }

    private static void addFilter_AllowedMethodChecking(SettingItem settingItem, Element element) {
        FilterAllowedMethodChecking amc = new FilterAllowedMethodChecking();

        NamedNodeMap attrMap = element.getAttributes();
        int count = attrMap.getLength();
        for (int index = 0; index < count; index++) {
            Attr attr = (Attr) attrMap.item(index);
            String attrName = attr.getName();
            if (SettingXML.Methods_Attr.equalsIgnoreCase(attrName)) {
                setAllowedMethods(amc, attr.getValue());
            }
        }
        settingItem.addFilter(amc);
    }

    private static void setAllowedMethods(FilterAllowedMethodChecking fmc, String value) {
        String[] methods = value.split(SettingXML.Comma);
        for (String method : methods) {
            fmc.addAllowedMethod(MethodType.fromSting(method.trim()));
        }
    }

    private static void addFilter_URLRedirecting(SettingItem settingItem, Element element) {
        FilterURLRedirecting ur = new FilterURLRedirecting();
        NamedNodeMap attrMap = element.getAttributes();
        int count = attrMap.getLength();
        for (int index = 0; index < count; index++) {
            Attr attr = (Attr) attrMap.item(index);
            String attrName = attr.getName();
            if (SettingXML.Rest_Source_URL_Pattern_Attr.equalsIgnoreCase(attrName)) {
                ur.restSourceURLPattern(attr.getValue());
            } else if (SettingXML.Reply_Code_Attr.equalsIgnoreCase(attrName)) {
                ur.replyCode(ReplyCode.fromCode((short) Integer.parseInt(attr.getValue())));
            } else if (SettingXML.Target_URL_Attr.equalsIgnoreCase(attrName)) {
                ur.targetURL(attr.getValue());
            }
        }
        settingItem.addFilter(ur);
    }

    private static void addFilter_HeaderAdding(SettingItem settingItem, Element element) {
        FilterHeaderAdding ha = new FilterHeaderAdding();

        NodeList nodeList = element.getChildNodes();
        int count2 = nodeList.getLength();
        for (int index = 0; index < count2; index++) {
            Node node = nodeList.item(index);
            String nodeName = node.getNodeName();
            if (SettingXML.Adding_Condition_Node.equalsIgnoreCase(nodeName)) {
                ForCondition.set(ha.addingCondition().addNewHeaderCondition(), (Element) node);
            } else if (SettingXML.Adding_Condition_List_Node.equalsIgnoreCase(nodeName)) {
                ForCondition.setList(ha.addingCondition(), (Element) node);
            } else if (SettingXML.Header_List_Node.equalsIgnoreCase(nodeName)) {
                setHeaderList(ha, (Element) node);
            }
        }
        settingItem.addFilter(ha);
    }

    private static void setHeaderList(FilterHeaderAdding ha, Element element) {
        NodeList nodeList = element.getChildNodes();
        int count2 = nodeList.getLength();
        for (int index = 0; index < count2; index++) {
            Node node = nodeList.item(index);
            String nodeName = node.getNodeName();
            if (SettingXML.Header_Node.equalsIgnoreCase(nodeName)) {
                setHeaderSetting(ha.addNewHeaderSetting(), (Element) node);
            }
        }
    }

    private static void setHeaderSetting(HeaderSetting hs, Element element) {
        NamedNodeMap attrMap = element.getAttributes();
        int count = attrMap.getLength();
        for (int index = 0; index < count; index++) {
            Attr attr = (Attr) attrMap.item(index);
            String attrName = attr.getName();
            if (SettingXML.Sort_Attr.equalsIgnoreCase(attrName)) {
                hs.sort(HeaderSort.fromString(attr.getValue()));
            } else if (SettingXML.Value_Attr.equalsIgnoreCase(attrName)) {
                hs.value(attr.getValue());
            }
        }
    }

    private static void addFilter_CharsetEncoding(SettingItem settingItem, Element element) {
        FilterCharsetEncoding ce = new FilterCharsetEncoding();

        NamedNodeMap attrMap = element.getAttributes();
        int count = attrMap.getLength();
        for (int index = 0; index < count; index++) {
            Attr attr = (Attr) attrMap.item(index);
            String attrName = attr.getName();
            if (SettingXML.Source_Charset_Attr.equalsIgnoreCase(attrName)) {
                ce.sourceCharset(attr.getValue());
            } else if (SettingXML.Target_Charset_Attr.equalsIgnoreCase(attrName)) {
                ce.targetCharset(attr.getValue());
            }
        }

        settingItem.addFilter(ce);
    }

    private static void addFilter_ContentEncoding(SettingItem settingItem, Element element) {
        FilterContentEncoding ce = new FilterContentEncoding();

        NamedNodeMap attrMap = element.getAttributes();
        int count = attrMap.getLength();
        for (int index = 0; index < count; index++) {
            Attr attr = (Attr) attrMap.item(index);
            String attrName = attr.getName();
            if (SettingXML.Coding_Attr.equalsIgnoreCase(attrName)) {
                ce.coding(ContentCodingSort.fromString(attr.getValue()));
            }
        }

        NodeList nodeList = element.getChildNodes();
        int count2 = nodeList.getLength();
        for (int index = 0; index < count2; index++) {
            Node node = nodeList.item(index);
            String nodeName = node.getNodeName();
            if (SettingXML.Apply_Condition_Node.equalsIgnoreCase(nodeName)) {
                ForCondition.set(ce.applyCondition().addNewHeaderCondition(), (Element) node);
            } else if (SettingXML.Apply_Condition_List_Node.equalsIgnoreCase(nodeName)) {
                ForCondition.setList(ce.applyCondition(), (Element) node);
            }
        }
        settingItem.addFilter(ce);
    }
}
