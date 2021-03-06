package kr.dogfoot.webserver.loader;

import kr.dogfoot.webserver.server.host.Host;
import kr.dogfoot.webserver.server.host.proxy_info.*;
import kr.dogfoot.webserver.util.string.StringUtils;
import org.w3c.dom.*;

public class ForProxyInfo {
    public static void set(Host host, Element element) {
        NodeList nodeList = element.getChildNodes();
        int count = nodeList.getLength();
        for (int index = 0; index < count; index++) {
            Node node = nodeList.item(index);
            String nodeName = node.getNodeName();
            if (SettingXML.Proxy_Info_Node.equalsIgnoreCase(nodeName)) {
                addProxyInfo(host.addNewProxyInfo(), (Element) node);
            }
        }
    }

    private static void addProxyInfo(ProxyInfo proxyInfo, Element element) {
        NamedNodeMap attrMap = element.getAttributes();
        int count = attrMap.getLength();
        for (int index = 0; index < count; index++) {
            Attr attr = (Attr) attrMap.item(index);
            String attrName = attr.getName();
            if (SettingXML.Balance_Attr.equalsIgnoreCase(attrName)) {
                createBackendServerManager(proxyInfo, attr.getValue());
            } else if (SettingXML.Applied_URL_Pattern_Attr.equalsIgnoreCase(attrName)) {
                proxyInfo.appliedURLPattern(attr.getValue());
            }
        }

        NodeList nodeList = element.getChildNodes();
        int count2 = nodeList.getLength();
        for (int index = 0; index < count2; index++) {
            Node node = nodeList.item(index);
            String nodeName = node.getNodeName();
            if (SettingXML.Backend_Server_Node.equalsIgnoreCase(nodeName)) {
                addBackendServer(proxyInfo, (Element) node);
            } else if (SettingXML.Proxy_Filters_Node.equalsIgnoreCase(nodeName)) {
                ForProxyFilters.set(proxyInfo, (Element) node);
            } else if (SettingXML.Cache_Option_Node.equalsIgnoreCase(nodeName)) {
                setCacheOption(proxyInfo.cacheOption(), (Element) node);
            }

        }
    }

    private static void createBackendServerManager(ProxyInfo proxyInfo, String balance) {
        if (SettingXML.Round_Robin_Value.equalsIgnoreCase(balance)) {
            proxyInfo.createBackendServerManagerForRoundRodin();
        } else if (SettingXML.Least_Connection_Value.equalsIgnoreCase(balance)) {
            proxyInfo.createBackendServerManagerForLeastConnection();
        }
    }

    private static void addBackendServer(ProxyInfo proxyInfo, Element element) {
        BackendServerInfo backend = proxyInfo.backendServerManager().addNewBackendServer();

        String ip_or_domain = "";
        int port = 0;

        NamedNodeMap attrMap = element.getAttributes();
        int count = attrMap.getLength();
        for (int index = 0; index < count; index++) {
            Attr attr = (Attr) attrMap.item(index);
            String attrName = attr.getName();
            if (SettingXML.Protocol_Attr.equalsIgnoreCase(attrName)) {
                backend.protocol(Protocol.fromString(attr.getValue()));
            } else if (SettingXML.IP_Or_Domain_Attr.equalsIgnoreCase(attrName)) {
                ip_or_domain = element.getAttribute(SettingXML.IP_Or_Domain_Attr);
            } else if (SettingXML.Port_Attr.equalsIgnoreCase(attrName)) {
                port = Integer.parseInt(element.getAttribute(SettingXML.Port_Attr));
            } else if (SettingXML.Keep_Alive_Timeout_Attr.equalsIgnoreCase(attrName)) {
                backend.keepAlive_timeout(XMLUtil.toDeltaSecond(attr.getValue()));
            } else if (SettingXML.Idle_Timeout_Attr.equalsIgnoreCase(attrName)) {
                backend.idle_timeout(XMLUtil.toDeltaSecond(attr.getValue()));
            }
        }

        backend.address(ip_or_domain, port);
    }

    private static void setCacheOption(CacheOption cacheOption, Element element) {
        NamedNodeMap attrMap = element.getAttributes();
        int count = attrMap.getLength();
        for (int index = 0; index < count; index++) {
            Attr attr = (Attr) attrMap.item(index);
            String attrName = attr.getName();
            if (SettingXML.Use_Attr.equalsIgnoreCase(attrName)) {
                cacheOption.use(XMLUtil.toBoolean(attr.getValue()));
            } else if (SettingXML.Default_Expires_Attr.equalsIgnoreCase(attrName)) {
                cacheOption.defaultExpires(XMLUtil.toDeltaSecond(attr.getValue()));
            }
        }
    }
}
