package com.distributed.chordApp.cooperativemirroring.server.utilities;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * Class used for reading the setup information from an XML file
 * for both the Client and the Server parameters
 */
public class XMLParser implements Serializable {

    /**
     * Static method used for getting a defined element
     * from the XML setting file for the chord network
     * @param tag
     * @return
     */
    public static synchronized String getXMLElement(String filePath, String tag,Integer occurrence)
    {
        String result = null;
        File file = new File(filePath);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);

            //System.out.println(tag);

            result = doc.getElementsByTagName(tag).item(occurrence).getTextContent();
        } catch (ParserConfigurationException e) {
            System.err.println("\nCannot create a new document builder");
            e.printStackTrace();
        } catch (SAXException e) {
            System.err.println("\nCannot parse the document");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}

