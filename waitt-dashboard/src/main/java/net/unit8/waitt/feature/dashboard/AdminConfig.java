package net.unit8.waitt.feature.dashboard;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileInputStream;
import java.util.logging.Logger;

/**
 * @author kawasima
 */
public class AdminConfig {
    private static final Logger LOG = Logger.getLogger(AdminConfig.class.getName());
    private boolean adminAvailable = false;
    private int adminPort = 1192;

    public void read() {
        File pomFile = new File("pom.xml");
        if (pomFile.exists()) {
            try {
                DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = documentBuilder.parse(new FileInputStream(pomFile));
                XPathFactory factory = XPathFactory.newInstance();
                XPath xpath = factory.newXPath();

                String location = "//features/feature/artifactId[text() = 'waitt-admin']";
                Node waittAdmin = (Node) xpath.evaluate(location, doc, XPathConstants.NODE);
                if (waittAdmin != null) {
                    adminAvailable = true;
                    Node adminPortNode = (Node) xpath.evaluate("//configuration/admin.port", waittAdmin.getParentNode(), XPathConstants.NODE);
                    if (adminPortNode != null) {
                        String portStr = adminPortNode.getTextContent().trim();
                        adminPort = Integer.parseInt(portStr);
                    }
                }
            } catch(Exception e) {
                LOG.info("read pom failure. Disabled an admin feature.");
            }
        }
    }

    public int getAdminPort() {
        return adminPort;
    }

    public boolean isAdminAvailable() {
        return adminAvailable;
    }
}
