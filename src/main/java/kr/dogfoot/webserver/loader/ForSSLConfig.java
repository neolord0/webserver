package kr.dogfoot.webserver.loader;

import kr.dogfoot.webserver.server.host.ssl.SSLConfig;
import org.w3c.dom.*;

public class ForSSLConfig {
    public static void set(SSLConfig sslConfig, Element element) {
        NamedNodeMap attrMap = element.getAttributes();
        int count = attrMap.getLength();
        for (int index = 0; index < count; index++) {
            Attr attr = (Attr) attrMap.item(index);
            String attrName = attr.getName();
            if (SettingXML.Key_Alias_Attr.equalsIgnoreCase(attrName)) {
                sslConfig.setKeyAlias(attr.getValue());
            } else if (SettingXML.Certificate_Verification_Attr.equalsIgnoreCase(attrName)) {
                sslConfig.setCertificateVerificationAsString(attr.getValue());
            }
        }

        NodeList nodeList = element.getChildNodes();
        int count2 = nodeList.getLength();
        for (int index = 0; index < count2; index++) {
            Node node = nodeList.item(index);
            String nodeName = node.getNodeName();
            if (SettingXML.Key_Store_Node.equalsIgnoreCase(nodeName)) {
                setKeyStore(sslConfig, (Element) node);
            } else if (SettingXML.Trust_Store_Node.equalsIgnoreCase(nodeName)) {
                setTrustStore(sslConfig, (Element) node);
            }
        }
    }

    private static void setKeyStore(SSLConfig sslConfig, Element element) {
        NamedNodeMap attrMap = element.getAttributes();
        int count = attrMap.getLength();
        for (int index = 0; index < count; index++) {
            Attr attr = (Attr) attrMap.item(index);
            String attrName = attr.getName();
            if (SettingXML.Type_Attr.equalsIgnoreCase(attrName)) {
                sslConfig.setKeystoreType(attr.getValue());
            } else if (SettingXML.Provider_Attr.equalsIgnoreCase(attrName)) {
                sslConfig.setKeystoreProvider(attr.getValue());
            }
        }

        NodeList nodeList = element.getChildNodes();
        int count2 = nodeList.getLength();
        for (int index = 0; index < count2; index++) {
            Node node = nodeList.item(index);
            String nodeName = node.getNodeName();
            if (SettingXML.Path_Node.equalsIgnoreCase(nodeName)) {
                sslConfig.setKeystoreFile(SettingXML.getCDATA((Element) node));
            } else if (SettingXML.Password_Node.equalsIgnoreCase(nodeName)) {
                sslConfig.setKeystorePassword(node.getTextContent());
            }
        }
    }

    private static void setTrustStore(SSLConfig sslConfig, Element element) {
        NamedNodeMap attrMap = element.getAttributes();
        int count = attrMap.getLength();
        for (int index = 0; index < count; index++) {
            Attr attr = (Attr) attrMap.item(index);
            String attrName = attr.getName();
            if (SettingXML.Type_Attr.equalsIgnoreCase(attrName)) {
                sslConfig.setTruststoreType(attr.getValue());
            } else if (SettingXML.Provider_Attr.equalsIgnoreCase(attrName)) {
                sslConfig.setTruststoreProvider(attr.getValue());
            }
        }

        NodeList nodeList = element.getChildNodes();
        int count2 = nodeList.getLength();
        for (int index = 0; index < count2; index++) {
            Node node = nodeList.item(index);
            String nodeName = node.getNodeName();
            if (SettingXML.Path_Node.equalsIgnoreCase(nodeName)) {
                sslConfig.setTruststoreFile(SettingXML.getCDATA((Element) node));
            } else if (SettingXML.Password_Node.equalsIgnoreCase(nodeName)) {
                sslConfig.setTruststorePassword(node.getTextContent());
            }
        }
    }
}
