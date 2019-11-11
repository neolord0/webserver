package kr.dogfoot.webserver.server.resource.negotiation;

import kr.dogfoot.webserver.httpMessage.header.HeaderSort;
import kr.dogfoot.webserver.server.resource.ResourceDirectory;
import kr.dogfoot.webserver.server.resource.filter.part.condition.CompareOperator;
import kr.dogfoot.webserver.server.resource.filter.part.condition.HeaderCondition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class NegotiationFileParser {
    public static void parse(NegotiationInfo negoInfo, ResourceDirectory parentDirectory, File file) {
        Element root = null;
        try {
            root = openXMLFile(file);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            root = null;
        } catch (SAXException e) {
            e.printStackTrace();
            root = null;
        } catch (IOException e) {
            e.printStackTrace();
            root = null;
        }
        if (root != null && root.getNodeName().equalsIgnoreCase(NegoXML.Root_Node)) {
            parse(negoInfo, parentDirectory, root);
        }
    }

    private static Element openXMLFile(File file) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(file);
        doc.getDocumentElement().normalize();
        return doc.getDocumentElement();
    }

    private static void parse(NegotiationInfo negoInfo, ResourceDirectory parentDirectory, Element element) {
        negoInfo.filename(element.getAttribute(NegoXML.URL_Attr));

        NodeList nodeList = element.getChildNodes();
        int count = nodeList.getLength();
        for (int index = 0; index < count; index++) {
            Node node = nodeList.item(index);
            if (node.getNodeName().equalsIgnoreCase(NegoXML.Variant_Node)) {
                negoInfo.addVariant(parseNegotiationVariant(parentDirectory, (Element) node));
            } else if (node.getNodeName().equalsIgnoreCase(NegoXML.CompareOrder_Node)) {
                parseCompareOrder(negoInfo, node);
            }
        }
    }

    private static NegotiationVariant parseNegotiationVariant(ResourceDirectory parentDirectory, Element element) {
        NegotiationVariant variant = new NegotiationVariant(parentDirectory);
        variant.fileName(element.getAttribute(NegoXML.Filename_Attr));

        NodeList nodeList = element.getChildNodes();
        int count = nodeList.getLength();
        for (int index = 0; index < count; index++) {
            Node node = nodeList.item(index);
            if (node.getNodeName().equalsIgnoreCase(NegoXML.HeaderCondition_Node)) {
                variant.addCondition(parseHeaderCondition((Element) node));
            }
        }
        return variant;
    }

    private static HeaderCondition parseHeaderCondition(Element element) {
        HeaderCondition condition = new HeaderCondition();
        condition.header(HeaderSort.fromString(element.getAttribute(NegoXML.Name_Attr)));
        condition.value(element.getTextContent());
        condition.compareOperator(CompareOperator.Equal);
        return condition;
    }

    private static void parseCompareOrder(NegotiationInfo negoInfo, Node node) {
        String text = node.getTextContent();
        String[] headerSorts = text.split(NegoXML.Comma);
        for (String headerSort : headerSorts) {
            negoInfo.addCompareHeader(HeaderSort.fromString(headerSort.trim()));
        }
    }
}
