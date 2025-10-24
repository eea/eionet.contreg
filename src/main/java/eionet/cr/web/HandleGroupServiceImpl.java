package eionet.cr.web;

import eionet.acl.AccessController;
import eionet.cr.errors.UserExistsException;
import eionet.cr.errors.XmlMalformedException;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;

@Service
public class HandleGroupServiceImpl implements HandleGroupService {

    @Override
    public void addUserToGroup(String username, String groupName) throws UserExistsException, XmlMalformedException {
        try {
            Document document = getDocument();
            Node group = getGroupNode(groupName, document);
            for (int i = 0; i < group.getChildNodes().getLength(); i++) {
                if (group.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element el = (Element) group.getChildNodes().item(i);
                    if (el.getAttribute("userid").contentEquals(username)) {
                        throw new UserExistsException(username + " exists in group " + groupName);
                    }
                }
            }
            Element groupEntry = document.createElement("member");
            groupEntry.setAttribute("userid",username);
            group.appendChild(groupEntry);
            writeResultToFile(document);
        } catch (ParserConfigurationException | IOException | SAXException | TransformerException | XPathExpressionException e) {
            throw new XmlMalformedException(e.getMessage());
        }
    }

    @Override
    public void removeUserFromGroup(String userName, String groupName) throws XmlMalformedException {
        try {
            Document document = getDocument();
            Node group = getGroupNode(groupName, document);
            for (int i = 0; i < group.getChildNodes().getLength(); i++) {
                if (group.getChildNodes().item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element el = (Element) group.getChildNodes().item(i);
                    if (el.getAttribute("userid").contentEquals(userName)) {
                        group.removeChild(group.getChildNodes().item(i));
                        break;
                    }
                }
            }
            removeEmptyLines(document);
            writeResultToFile(document);
        } catch (ParserConfigurationException | IOException | SAXException | TransformerException | XPathExpressionException e) {
            throw new XmlMalformedException(e.getMessage());
        }
    }

    protected void removeEmptyLines(Document document) throws XPathExpressionException {
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPathExpression xpathExp = xpathFactory.newXPath().compile(
                "//text()[normalize-space(.) = '']");
        NodeList emptyTextNodes = (NodeList)
                xpathExp.evaluate(document, XPathConstants.NODESET);
        for (int j = 0; j < emptyTextNodes.getLength(); j++) {
            Node emptyTextNode = emptyTextNodes.item(j);
            emptyTextNode.getParentNode().removeChild(emptyTextNode);
        }
    }

    protected void writeResultToFile(Document document) throws TransformerException {
        DOMSource source = new DOMSource(document);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        StreamResult result = new StreamResult(AccessController.getAclProperties().getFileLocalgroups());
        transformer.transform(source, result);
    }

    protected Node getGroupNode(String groupName, Document document) throws XPathExpressionException {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        String expression = "//group[@id=" + "'" + groupName + "'" + "]";

        XPathExpression expr = xpath.compile(expression);
        NodeList DDAdmingroups =(NodeList) expr.evaluate(document, XPathConstants.NODESET);
        return DDAdmingroups.item(0);
    }

    protected Document getDocument() throws ParserConfigurationException, SAXException, IOException {
        File file = new File(AccessController.getAclProperties().getFileLocalgroups());

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                .newInstance();
        documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        return documentBuilder.parse(file);
    }
}
