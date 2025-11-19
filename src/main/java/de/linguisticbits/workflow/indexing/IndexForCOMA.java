/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package de.linguisticbits.workflow.indexing;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.zumult.io.IOHelper;

/**
 *
 * @author bernd
 */
public class IndexForCOMA {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length==1){
            pathToComa = args[0];
        } else if (args.length!=0){
            System.out.println("Usage: IndexForCOMA pathToComa");
            System.exit(0);            
        }
        
        try {
            new IndexForCOMA().doit();
        } catch (IOException | SAXException | ParserConfigurationException | XPathExpressionException | TransformerException ex) {
            Logger.getLogger(IndexForCOMA.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private Map<String, String> id2Corpus = new HashMap<>();
    private Map<String, String> id2parentID = new HashMap<>();
    static String pathToComa = "D:\\ZUMULT\\TGDP\\TGDP.coma";
    

    private void doit() throws IOException, SAXException, ParserConfigurationException, XPathExpressionException, TransformerException {
        String corpusID = "TGDP";
        String xp = "//*[@Id]";
        XPath xPath2 = XPathFactory.newInstance().newXPath();
        File comaFile = new File(pathToComa);
        Element root = IOHelper.readDocument(comaFile).getDocumentElement();
        NodeList allIDElements = (NodeList) xPath2.evaluate(xp, root, XPathConstants.NODESET);
        System.out.println("Processing " + allIDElements.getLength() + " elements with ID. ");
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<coma-index>");
        for (int i=0; i<allIDElements.getLength(); i++){
            Element idElement = ((Element)(allIDElements.item(i)));
            String thisID = idElement.getAttribute("Id");
            id2Corpus.put(thisID, corpusID);
            sb.append("<index id=\"").append(thisID).append("\" corpus=\"").append(corpusID).append("\"");
            Element parentElement = (Element) (Node) xPath2.evaluate("ancestor::*[@Id][1]", idElement, XPathConstants.NODE);
            if (parentElement!=null){
                String parentID = parentElement.getAttribute("Id");
                id2parentID.put(thisID, parentID);
                sb.append(" parent=\"").append(parentID).append("\"");
            }
            sb.append("/>");
            if (i%1000==0){
                System.out.println("[" + i + "/" + allIDElements.getLength() + "]");
            }
        } 
        sb.append("</coma-index>");
        
        
        // write index
        Document indexDocument = IOHelper.DocumentFromText(sb.toString());
        File comaIndexFile = new File(comaFile.getParentFile(), comaFile.getName().substring(0, comaFile.getName().indexOf(".")) + ".comaindex");
        IOHelper.writeDocument(indexDocument, comaIndexFile);
        System.out.println("Index written to " + comaIndexFile.getAbsolutePath());
        
    }
    
}
