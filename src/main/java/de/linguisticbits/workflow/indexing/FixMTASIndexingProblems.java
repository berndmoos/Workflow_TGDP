/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package de.linguisticbits.workflow.indexing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.exmaralda.coma.root.ComaDocument;
import org.exmaralda.common.jdomutilities.IOUtilities;
import org.exmaralda.exakt.utilities.FileIO;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.xml.sax.SAXException;



/**
 *
 * @author bernd
 */
public class FixMTASIndexingProblems {

    
    static String pathToComa = "D:\\ZUMULT\\TGDP\\TGDP.coma";
    
    ComaDocument comaDocument;
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        if (args.length == 1){
            pathToComa = args[0];            
        } else if (args.length > 0){
            System.out.println("Usage: FixMTASIndexingProblems pathToComa");
            System.exit(0);
        }
        
        try {
            //new IndexForMTAS().buildIndex();
            new FixMTASIndexingProblems().findIndexingProblems();
        } catch (IOException | JDOMException | SAXException | ParserConfigurationException | XPathExpressionException | TransformerException ex) {
            Logger.getLogger(FixMTASIndexingProblems.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public FixMTASIndexingProblems() throws IOException, JDOMException {
        org.jdom.Document xmlDocument = FileIO.readDocumentFromLocalFile(pathToComa);
        org.jdom.Element rootElement = xmlDocument.detachRootElement();
        comaDocument = new ComaDocument(rootElement);
        
    }
    
    
    private void findIndexingProblems() throws JDOMException, IOException, SAXException, ParserConfigurationException, XPathExpressionException, TransformerException{
        StringBuilder problemString = new StringBuilder();
        
        File comaFile = new File(pathToComa);
        Path topLevelPath = comaFile.getParentFile().toPath();
        List transcriptions = org.jdom.xpath.XPath.selectNodes(comaDocument, "//Transcription");
        System.out.println("[FixMTASIndexingProblems] " + transcriptions.size() + " trancripts found in " + comaFile.getAbsolutePath());
        int count = 0;
        for (Object o : transcriptions){
            boolean changed = false;
            org.jdom.Element transcriptionElement = (org.jdom.Element)o;
            String transcriptID = transcriptionElement.getAttributeValue("Id");
            count++;
            String nsLink = transcriptionElement.getChildText("NSLink");
            Path resolvedPath = topLevelPath.resolve(nsLink);
            File xmlFile = resolvedPath.toFile();
            System.out.println("[FixMTASIndexingProblems] Processing " + count + " of " + transcriptions.size() + ": " + transcriptID + " / " + xmlFile.getAbsolutePath());
            org.jdom.Document transcriptionDocument = FileIO.readDocumentFromLocalFile(xmlFile);
            
            org.jdom.xpath.XPath xp = org.jdom.xpath.XPath.newInstance("//*[@from]");
            List l = xp.selectNodes(transcriptionDocument);
            Map<String, Integer> positions = new HashMap<>();
            for (Object o2 : l){
                org.jdom.Element e = (org.jdom.Element)o2;
                //System.out.println(IOUtilities.elementToString(e));

                org.jdom.xpath.XPath xp2 = org.jdom.xpath.XPath.newInstance("//*[@xml:id='" + e.getAttributeValue("from") + "']");
                xp2.addNamespace(Namespace.XML_NAMESPACE);
                org.jdom.Element fromElement = (org.jdom.Element) xp2.selectSingleNode(transcriptionDocument);
                int fromPosition = positions.getOrDefault(e.getAttributeValue("from"), org.jdom.xpath.XPath.selectNodes(fromElement, "preceding-sibling::*").size());
                positions.put(e.getAttributeValue("from"), fromPosition);

                org.jdom.xpath.XPath xp3 = org.jdom.xpath.XPath.newInstance("//*[@xml:id='" + e.getAttributeValue("to") + "']");
                xp3.addNamespace(Namespace.XML_NAMESPACE);
                org.jdom.Element toElement = (org.jdom.Element) xp3.selectSingleNode(transcriptionDocument);
                int toPosition = positions.getOrDefault(e.getAttributeValue("to"), org.jdom.xpath.XPath.selectNodes(toElement, "preceding-sibling::*").size());
                positions.put(e.getAttributeValue("to"), toPosition);

                if (fromPosition>toPosition){
                    // there are only 9 such positions in the whole corpus, let's just make them passend
                    problemString.append(xmlFile.getName()).append("\t").append(IOUtilities.elementToString(e)).append("\n");
                    e.setAttribute("to", e.getAttributeValue("from"));
                    changed = true;
                }            
            }
            if (changed){
                System.out.println("[FixMTASIndexingProblems] " + xmlFile.getName() + " changed.");
                FileIO.writeDocumentToLocalFile(xmlFile, transcriptionDocument);
            }
        }
        
        System.out.println("=====================");
        System.out.println(problemString.toString());
        
    }
    
    
}


