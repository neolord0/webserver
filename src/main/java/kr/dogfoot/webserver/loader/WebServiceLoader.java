package kr.dogfoot.webserver.loader;

import kr.dogfoot.webserver.httpMessage.request.MethodType;
import kr.dogfoot.webserver.loader.resourcesetting.ResourceSetting;
import kr.dogfoot.webserver.server.Server;
import kr.dogfoot.webserver.server.host.Host;
import kr.dogfoot.webserver.server.host.MediaTypeManager;
import kr.dogfoot.webserver.server.object.PooledThreadCount;
import kr.dogfoot.webserver.server.object.ServerProperties;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class WebServiceLoader {
    public static void fromConfigFile(String filepath, Server server) throws Exception {
        Element root = openXMLFile(new File(filepath));
        if (SettingXML.Root_Node.equalsIgnoreCase(root.getNodeName()) == false) {
            throw new Exception("it is not config file.");
        }

        NodeList nodeList = root.getChildNodes();
        int count = nodeList.getLength();
        for (int index = 0; index < count; index++) {
            Node node = nodeList.item(index);
            String nodeName = node.getNodeName();
            if (SettingXML.Properties_Node.equalsIgnoreCase(nodeName)) {
                setProperties(server.objects().properties(), (Element) node);
            } else if (SettingXML.Host_Node.equalsIgnoreCase(nodeName)) {
                addHost(server, (Element) node);
            }
        }
    }

    private static Element openXMLFile(File file) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);
        doc.getDocumentElement().normalize();
        return doc.getDocumentElement();
    }

    private static void setProperties(ServerProperties properties, Element element) {
        NodeList nodeList = element.getChildNodes();
        int count = nodeList.getLength();
        for (int index = 0; index < count; index++) {
            Node node = nodeList.item(index);
            String nodeName = node.getNodeName();
            if (SettingXML.Pooled_Thread_Count.equalsIgnoreCase(nodeName)) {
                setPooledThreadCount(properties.pooledThreadCount(), (Element) node);
            } else if (SettingXML.Server_Header_Node.equalsIgnoreCase(nodeName)) {
                setServerHeader(properties, (Element) node);
            } else if (SettingXML.Keep_Alive_Node.equalsIgnoreCase(nodeName)) {
                setKeepAlive(properties, (Element) node);
            }
        }
    }

    private static void setPooledThreadCount(PooledThreadCount pooledThreadCount, Element element) {
        NamedNodeMap attrMap = element.getAttributes();
        int count = attrMap.getLength();
        for (int index = 0; index < count; index++) {
            Attr attr = (Attr) attrMap.item(index);
            String attrName = attr.getName();

            if (SettingXML.SSL_Handshaking_Attr.equalsIgnoreCase(attrName)) {
                pooledThreadCount.ssl_handshaking(Integer.parseInt(attr.getValue()));
            } else if (SettingXML.Request_Receiving_Attr.equalsIgnoreCase(attrName)) {
                pooledThreadCount.request_receiving(Integer.parseInt(attr.getValue()));
            } else if (SettingXML.Body_Receiving_Attr.equalsIgnoreCase(attrName)) {
                pooledThreadCount.body_receiving(Integer.parseInt(attr.getValue()));
            } else if (SettingXML.Request_Performing_Attr.equalsIgnoreCase(attrName)) {
                pooledThreadCount.request_performing(Integer.parseInt(attr.getValue()));
            } else if (SettingXML.Response_Sending_Attr.equalsIgnoreCase(attrName)) {
                pooledThreadCount.response_sending(Integer.parseInt(attr.getValue()));
            } else if (SettingXML.File_Reading_Attr.equalsIgnoreCase(attrName)) {
                pooledThreadCount.file_reading(Integer.parseInt(attr.getValue()));
            } else if (SettingXML.Buffer_Sending_Attr.equalsIgnoreCase(attrName)) {
                pooledThreadCount.buffer_sending(Integer.parseInt(attr.getValue()));
            } else if (SettingXML.Proxy_Connecting_Attr.equalsIgnoreCase(attrName)) {
                pooledThreadCount.proxy_connecting(Integer.parseInt(attr.getValue()));
            } else if (SettingXML.Ajp_Proxing_Attr.equalsIgnoreCase(attrName)) {
                pooledThreadCount.ajp_proxing(Integer.parseInt(attr.getValue()));
            } else if (SettingXML.Http_Proxing_Attr.equalsIgnoreCase(attrName)) {
                pooledThreadCount.http_proxing(Integer.parseInt(attr.getValue()));
            }
        }
    }

    private static void setServerHeader(ServerProperties properties, Element element) {
        NamedNodeMap attrMap = element.getAttributes();
        int count = attrMap.getLength();
        for (int index = 0; index < count; index++) {
            Attr attr = (Attr) attrMap.item(index);
            String attrName = attr.getName();
            if (SettingXML.Send_Attr.equalsIgnoreCase(attrName)) {
                properties.sendServerHeader(SettingXML.True_Value.equalsIgnoreCase(attr.getValue()));
            }
        }
    }

    private static void setKeepAlive(ServerProperties properties, Element element) {
        NamedNodeMap attrMap = element.getAttributes();
        int count = attrMap.getLength();
        for (int index = 0; index < count; index++) {
            Attr attr = (Attr) attrMap.item(index);
            String attrName = attr.getName();
            if (SettingXML.Timeout_Attr.equalsIgnoreCase(attrName)) {
                properties.keepAlive_timeout(Integer.parseInt(attr.getValue()));
            } else if (SettingXML.Max_Attr.equalsIgnoreCase(attrName)) {
                properties.keepAlive_max(Integer.parseInt(attr.getValue()));
            }
        }
    }

    private static void addHost(Server server, Element element) throws Exception {
        Host host = server.addNewHost();
        NamedNodeMap attrMap = element.getAttributes();
        int count = attrMap.getLength();
        for (int index = 0; index < count; index++) {
            Attr attr = (Attr) attrMap.item(index);
            String attrName = attr.getName();
            if (SettingXML.Name_Attr.equalsIgnoreCase(attrName)) {
                host.name(attr.getValue());
            } else if (SettingXML.Domain_Attr.equalsIgnoreCase(attrName)) {
                host.domain(attr.getValue());
            } else if (SettingXML.Ip_Attr.equalsIgnoreCase(attrName)) {
                host.ipAddress(attr.getValue());
            } else if (SettingXML.Port_Attr.equalsIgnoreCase(attrName)) {
                host.port(Integer.parseInt(attr.getValue()));
            } else if (SettingXML.Default_Host.equalsIgnoreCase(attrName)) {
                host.defaultHost(SettingXML.True_Value.equalsIgnoreCase(attr.getValue()));
            } else if (SettingXML.Default_Charset_Attr.equalsIgnoreCase(attrName)) {
                host.hostObjects().defaultCharset(attr.getValue());
            } else if (SettingXML.Default_Allowed_Methods.equalsIgnoreCase(attrName)) {
                host.hostObjects().defaultAllowedMethods(allowedMethods(attr.getValue()));
            }
        }

        setRootPathNode(host, element);

        NodeList nodeList = element.getChildNodes();
        int count2 = nodeList.getLength();
        for (int index = 0; index < count2; index++) {
            Node node = nodeList.item(index);
            String nodeName = node.getNodeName();
            if (SettingXML.Custom_Media_Types_Node.equalsIgnoreCase(nodeName)) {
                setCustomMediaTypes(host.hostObjects().mediaTypeManager(), (Element) node);
            } else if (SettingXML.SSL_Config_Node.equalsIgnoreCase(nodeName)) {
                ForSSLConfig.set(host.getSSLConfig(), (Element) node);
            } else if (SettingXML.Proxy_Infos_Node.equalsIgnoreCase(nodeName)) {
                ForProxyInfo.set(host, (Element) node);
            }
        }
    }

    private static MethodType[] allowedMethods(String value) {
        ArrayList<MethodType> methodTypeList = new ArrayList<MethodType>();
        String[] methods = value.split(SettingXML.Comma);
        for (String method : methods) {
            methodTypeList.add(MethodType.fromSting(method.trim()));
        }
        return methodTypeList.toArray(new MethodType[0]);
    }

    private static void setRootPathNode(Host host, Element element) throws Exception {
        ResourceSetting resourceSetting = loadReourceSetting(element);

        NodeList nodeList = element.getElementsByTagName(SettingXML.Root_Path_Node);
        if (nodeList.getLength() != 1) {
            throw new Exception("host must have only one root directory.");
        }

        Element child = (Element) nodeList.item(0);
        HostLoader.load(host, SettingXML.getCDATA(child), resourceSetting);
    }

    private static ResourceSetting loadReourceSetting(Element element) {
        ResourceSetting resourceSetting = new ResourceSetting();
        NodeList nodeList = element.getElementsByTagName(SettingXML.Resource_Setting_Node);
        int count = nodeList.getLength();
        if (count > 0) {
            for (int index = 0; index < count; index++) {
                Node node = nodeList.item(index);

                ForResourceSetting.set(resourceSetting, (Element) node);
            }
        }
        return resourceSetting;
    }

    private static void setCustomMediaTypes(MediaTypeManager mediaTypeManager, Element element) {
        NodeList nodeList = element.getChildNodes();
        int count2 = nodeList.getLength();
        for (int index = 0; index < count2; index++) {
            Node node = nodeList.item(index);
            String nodeName = node.getNodeName();
            if (SettingXML.Media_Type_Node.equalsIgnoreCase(nodeName)) {
                addMediaType(mediaTypeManager, (Element) node);
            }
        }
    }

    private static void addMediaType(MediaTypeManager mediaTypeManager, Element element) {
        if (element.hasAttribute(SettingXML.EXT_Attr) && element.hasAttribute(SettingXML.Type_Attr)) {
            String ext = element.getAttribute(SettingXML.EXT_Attr);
            String type = element.getAttribute(SettingXML.Type_Attr);

            if (ext != null && ext.length() > 0 && type != null && type.length() > 0) {
                mediaTypeManager.addCustomMediaType(ext, type);
            }
        }
    }
}


