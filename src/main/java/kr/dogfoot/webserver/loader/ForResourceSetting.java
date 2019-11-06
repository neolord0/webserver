package kr.dogfoot.webserver.loader;

import kr.dogfoot.webserver.loader.resourcesetting.*;
import org.w3c.dom.*;

public class ForResourceSetting {
    public static void set(ResourceSetting resourceSetting, Element element) {
        NodeList nodeList = element.getChildNodes();
        int count2 = nodeList.getLength();
        for (int index = 0; index < count2; index++) {
            Node node = nodeList.item(index);
            String nodeName = node.getNodeName();

            if (SettingXML.Directory_Node.equalsIgnoreCase(nodeName)) {
                DirectorySetting ds = resourceSetting.newDirectorySetting();
                setCommonProperty(ds, (Element) node);
                setDirectorySetting(ds, (Element) node);
            } else if (SettingXML.Virtual_Directory_Node.equalsIgnoreCase(nodeName)) {
                VirtualDirectorySetting vds = resourceSetting.newVirtualDirectorySetting();
                setCommonProperty(vds, (Element) node);
                setDirectorySetting(vds, (Element) node);
                setVirtualDirectorySetting(vds, (Element) node);
            } else if (SettingXML.File_Node.equalsIgnoreCase(nodeName)) {
                FileSetting fs = resourceSetting.newFileSetting();
                setCommonProperty(fs, (Element) node);
            }
        }
    }

    private static void setCommonProperty(SettingItem settingItem, Element element) {
        NamedNodeMap attrMap = element.getAttributes();
        int count = attrMap.getLength();
        for (int index = 0; index < count; index++) {
            Attr attr = (Attr) attrMap.item(index);
            String attrName = attr.getName();
            if (SettingXML.URL_Attr.equalsIgnoreCase(attrName)) {
                settingItem.url(attr.getValue());
            }
        }

        NodeList nodeList = element.getChildNodes();
        int count2 = nodeList.getLength();
        for (int index = 0; index < count2; index++) {
            Node node = nodeList.item(index);
            String nodeName = node.getNodeName();

            if (SettingXML.Filters_Node.equalsIgnoreCase(nodeName)) {
                ForFilters.set(settingItem, (Element) node);
            }
        }
    }

    private static void setDirectorySetting(DirectorySetting directorySetting, Element element) {
        NodeList nodeList = element.getChildNodes();
        int count2 = nodeList.getLength();
        for (int index = 0; index < count2; index++) {
            Node node = nodeList.item(index);
            String nodeName = node.getNodeName();

            if (SettingXML.Default_File_Node.equalsIgnoreCase(nodeName)) {
                setDefaultFile(directorySetting, (Element) node);
            } else if (SettingXML.Not_Serviced_Files_Node.equalsIgnoreCase(nodeName)) {
                setNotServicedFiles(directorySetting, (Element) node);
            } else if (SettingXML.Directory_Node.equalsIgnoreCase(nodeName)) {
                DirectorySetting ds = directorySetting.newChildDirectorySetting();
                setCommonProperty(ds, (Element) node);
                setDirectorySetting(ds, (Element) node);
            } else if (SettingXML.Virtual_Directory_Node.equalsIgnoreCase(nodeName)) {
                VirtualDirectorySetting vds = directorySetting.newChildVirtualDirectorySetting();
                setCommonProperty(vds, (Element) node);
                setDirectorySetting(vds, (Element) node);
                setVirtualDirectorySetting(vds, (Element) node);
            } else if (SettingXML.File_Node.equalsIgnoreCase(nodeName)) {
                FileSetting fs = directorySetting.newChildFileSetting();
                setCommonProperty(fs, (Element) node);
            }
        }
    }

    private static void setDefaultFile(DirectorySetting directorySetting, Element element) {
        directorySetting.defaultFile(element.getTextContent());
    }

    private static void setNotServicedFiles(DirectorySetting directorySetting, Element element) {
        NodeList nodeList = element.getChildNodes();
        int count2 = nodeList.getLength();
        for (int index = 0; index < count2; index++) {
            Node node = nodeList.item(index);
            String nodeName = node.getNodeName();
            if (SettingXML.Not_Serviced_File_Node.equalsIgnoreCase(nodeName)) {
                addNotServicedFile(directorySetting.newNotServicedFile(), (Element) node);
            }
        }
    }

    private static void addNotServicedFile(NotServicedFile nsf, Element element) {
        NamedNodeMap attrMap = element.getAttributes();
        int count = attrMap.getLength();
        for (int index = 0; index < count; index++) {
            Attr attr = (Attr) attrMap.item(index);
            String attrName = attr.getName();
            if (SettingXML.Name_Pattern_Attr.equalsIgnoreCase(attrName)) {
                nsf.namePatten(attr.getValue());
            } else if (SettingXML.Inheritable_Attr.equalsIgnoreCase(attrName)) {
                nsf.inheritable(SettingXML.True_Value.equalsIgnoreCase(attr.getValue()));
            }
        }
    }


    private static void setVirtualDirectorySetting(VirtualDirectorySetting vds, Element element) {
        NodeList nodeList = element.getChildNodes();
        int count2 = nodeList.getLength();
        for (int index = 0; index < count2; index++) {
            Node node = nodeList.item(index);
            String nodeName = node.getNodeName();

            if (SettingXML.Source_Path_Node.equalsIgnoreCase(nodeName)) {
                vds.sourcePath(SettingXML.getCDATA((Element) node));
            }
        }
    }
}
