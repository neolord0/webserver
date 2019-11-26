package kr.dogfoot.webserver.loader;

import kr.dogfoot.webserver.server.host.proxy_info.ProxyInfo;
import kr.dogfoot.webserver.server.host.proxy_info.filter.ProxyFilterBasicAuthorization;
import org.w3c.dom.*;

public class ForProxyFilters {
    public static void set(ProxyInfo proxyInfo, Element element) {
        NodeList nodeList = element.getChildNodes();
        int count2 = nodeList.getLength();
        for (int index = 0; index < count2; index++) {
            Node node = nodeList.item(index);
            String nodeName = node.getNodeName();

            if (SettingXML.Basic_Authorization_Node.equalsIgnoreCase(nodeName)) {
                addFilter_BasicAuthorization(proxyInfo, (Element) node);
            }
        }
    }

    private static void addFilter_BasicAuthorization(ProxyInfo proxyInfo, Element element) {
        ProxyFilterBasicAuthorization ba = new ProxyFilterBasicAuthorization();

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
        proxyInfo.addFilter(ba);
    }
}
