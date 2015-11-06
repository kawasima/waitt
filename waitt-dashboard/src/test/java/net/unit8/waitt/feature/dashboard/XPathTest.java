package net.unit8.waitt.feature.dashboard;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author kawasima
 */
public class XPathTest {
    @Test
    public void test() throws Exception {
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("feature.pom.xml");
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
        String location = "//features/feature/artifactId[text() = 'waitt-admin']";
        Node waitAdmin = (Node) xpath.evaluate(location, doc, XPathConstants.NODE);
        assertNotNull(waitAdmin);

        Node adminPortNode = (Node) xpath.evaluate("//configuration/admin.port", waitAdmin.getParentNode(), XPathConstants.NODE);
        assertNotNull(adminPortNode);
        assertEquals("11902", adminPortNode.getTextContent().trim());
    }
}
