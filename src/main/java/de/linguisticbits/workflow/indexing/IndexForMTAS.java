/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package de.linguisticbits.workflow.indexing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.exmaralda.coma.root.ComaDocument;
import org.exmaralda.exakt.utilities.FileIO;
import org.jdom.JDOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.zumult.indexing.search.SearchIndexer;
import org.zumult.io.IOHelper;
import org.zumult.io.ISOTEINamespaceContext;



/**
 *
 * @author bernd
 */
public class IndexForMTAS {

    
    static String mtasConfigFilePath = "C:\\zumulttgdp\\src\\main\\java\\de\\linguisticbits\\zumult_tgdp";
    
    static String mtasConfigFileName = "tgdp_mtas_config_SB.xml";
    static String indexPath = "D:\\ZUMULT\\INDICES";
    static String indexName = "SB_TGDP_TEST_META";
    static String pathToComa = "D:\\ZUMULT\\TGDP\\TGDP.coma";
    
    ComaDocument comaDocument;
    
    String[] speechEventKeys = {
        "Interview Location", "Recording Date", "Interviewer ID", "Informant ID", "title"
    };

    String[] speakerKeys = {
        "Role", "Sex", "Birth Year", 
        "Birth Location (City)", "Birth Location (State)",
        "Residence Location (City)", "Education Level",
        "Religious Affiliation", "First language(s)",
        "Age of acquisition for second language", "German in formal education"
    };
    

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        if (args.length == 4){
            String fullConfigPath = args[0];
            File configFile = new File(fullConfigPath);
            mtasConfigFilePath = configFile.getParentFile().getAbsolutePath();
            mtasConfigFileName = configFile.getName();
            
            indexPath = args[1];
            indexName = args[2];
            
            pathToComa = args[3];            
        } else if (args.length > 0){
            System.out.println("Usage: IndexForMTAS C:\\path\\to\\config\\tgdp_mtas_config_SB.xml C:\\path\\to\\index index-name pathToComa");
            System.exit(0);
        }
        
        try {
            //new IndexForMTAS().buildIndex();
            new IndexForMTAS().buildIndexFromComa();
        } catch (IOException | JDOMException | SAXException | ParserConfigurationException | XPathExpressionException | TransformerException ex) {
            Logger.getLogger(IndexForMTAS.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public IndexForMTAS() throws IOException, JDOMException {
        org.jdom.Document xmlDocument = FileIO.readDocumentFromLocalFile(pathToComa);
        org.jdom.Element rootElement = xmlDocument.detachRootElement();
        comaDocument = new ComaDocument(rootElement);
        
    }
    
    
    private void buildIndexFromComa() throws JDOMException, IOException, SAXException, ParserConfigurationException, XPathExpressionException, TransformerException{
        Path tempDir = Files.createTempDirectory("MTAS_INDEX");
        System.out.println("[IndexForMTAS] Created temporary directory " + tempDir.toString() + " for augmented transcripts. ");
        File comaFile = new File(pathToComa);
        Path topLevelPath = comaFile.getParentFile().toPath();
        List transcriptions = org.jdom.xpath.XPath.selectNodes(comaDocument, "//Transcription");
        System.out.println("[IndexForMTAS] " + transcriptions.size() + " trancripts found in " + comaFile.getAbsolutePath());
        int count = 0;
        for (Object o : transcriptions){
            org.jdom.Element transcriptionElement = (org.jdom.Element)o;
            String transcriptID = transcriptionElement.getAttributeValue("Id");
            count++;
            String nsLink = transcriptionElement.getChildText("NSLink");
            Path resolvedPath = topLevelPath.resolve(nsLink);
            File xmlFile = resolvedPath.toFile();
            System.out.println("[IndexForMTAS] Processing " + count + " of " + transcriptions.size() + ": " + transcriptID + " / " + xmlFile.getAbsolutePath());
            Document transcriptionDocument = IOHelper.readDocument(xmlFile);
            transferMetadata(transcriptionDocument, transcriptID);
            
            File outFile = new File(tempDir.toFile(), transcriptID + ".xml");
            IOHelper.writeDocument(transcriptionDocument, outFile);
            System.out.println("[IndexForMTAS] Written " + outFile.getAbsolutePath());            
        }
        
        String[] dirs = {
            tempDir.toString()
        };
        
        System.out.println("------------------------------------------------------------");
        System.out.println("------------------------------------------------------------");
        System.out.println("------------------------------------------------------------");

        System.out.println("[IndexForMTAS] STARTING INDEXING TO " + indexPath);
        
        SearchIndexer searchIndexer = new SearchIndexer(mtasConfigFilePath, mtasConfigFileName, indexPath, indexName, dirs);
        searchIndexer.index();

        System.out.println("[IndexForMTAS] INDEXING COMPLETED");

        System.out.println("------------------------------------------------------------");
        System.out.println("------------------------------------------------------------");
        System.out.println("------------------------------------------------------------");
        
        /*System.out.println("[IndexForMTAS] DELETING TEMPORARY DIRECTORY AT " + tempDir.toString());
        for (File f : tempDir.toFile().listFiles()){
            f.delete();
        }        
        Files.delete(tempDir);*/
        System.out.println("[IndexForMTAS] TEMPORARY DIRECTORY DELETED");
        
        System.out.println("[IndexForMTAS] DONE!");
        
        System.out.println("Check files under " + tempDir.toString());
        
        //System.out.println(TEXAS);
        
        
        
        
        
    }
    
    private void transferMetadata(Document transcriptDocument, String transcriptionID) throws XPathExpressionException, JDOMException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        xPath.setNamespaceContext(new ISOTEINamespaceContext());            
        String xp = "//tei:body";
        Element bodyElement = (Element)xPath.evaluate(xp, transcriptDocument.getDocumentElement(), XPathConstants.NODE);                        
        Element spanGrp = transcriptDocument.createElementNS("http://www.tei-c.org/ns/1.0", "tei:spanGrp");
        spanGrp.setAttribute("type", "meta");
        spanGrp.setAttribute("subtype", "time-based");
        bodyElement.appendChild(spanGrp);

        xp = "//tei:body/descendant::tei:w[1]/preceding-sibling::tei:anchor[1]";
        Element firstWhen = (Element)xPath.evaluate(xp, transcriptDocument.getDocumentElement(), XPathConstants.NODE);    
        if (firstWhen==null) return;
        String firstWhenID = firstWhen.getAttribute("synch");

        xp = "//tei:body/descendant::tei:w[last()]/following-sibling::tei:anchor[last()]";
        Element lastWhen = (Element)xPath.evaluate(xp, transcriptDocument.getDocumentElement(), XPathConstants.NODE);                        
        if (lastWhen==null) return;
        String lastWhenID = lastWhen.getAttribute("synch");

        String communicationID = comaDocument.getCommunicationIDForTranscription(transcriptionID);
        for (String key : speechEventKeys){
            String metadataValue = comaDocument.getCommunicationMetadataAsMap().get(communicationID).get(key);
            //System.out.println(key.getID() + " / " + key.getName("en") + ": " + metadataValue);
            Element span = transcriptDocument.createElementNS("http://www.tei-c.org/ns/1.0", "tei:span");
            span.setAttribute("from", firstWhenID);
            span.setAttribute("to", lastWhenID);
            String consolidatedKey = key
                    .replaceAll(" ", "_")
                    .replaceAll("[\\(\\)]", "");
            span.setAttribute("type", consolidatedKey);
            span.setTextContent(metadataValue);
            spanGrp.appendChild(span);     
        }

        Element titleSpan = transcriptDocument.createElementNS("http://www.tei-c.org/ns/1.0", "tei:span");
        titleSpan.setAttribute("from", firstWhenID);
        titleSpan.setAttribute("to", lastWhenID);
        titleSpan.setAttribute("type", "title");
        String sectionTitle = comaDocument.getTranscriptionMetadataAsMap().get(transcriptionID).get("Section Title");
        titleSpan.setTextContent(sectionTitle);
        spanGrp.appendChild(titleSpan);     
        
        List<String> speakerIDs = comaDocument.getSpeakerIDsForCommunication(communicationID);
        for (String speakerID : speakerIDs){
            //if(!existingSpeakers.contains(speakerID)) continue;
            //Speaker speaker = backend.getSpeaker(speakerID);
            System.out.println("  SPEAKER " + speakerID);
            if (comaDocument.getSpeakerMetadataAsMap().get(speakerID)==null) {
                System.out.println("  SPEAKER " + speakerID + " does not exist in COMA!");
                continue;
            }
            
            xp = "//tei:person[tei:idno='" + speakerID + "']";
            Element personElement = (Element)xPath.evaluate(xp, transcriptDocument.getDocumentElement(), XPathConstants.NODE);                        
            String internalSpeakerID = personElement.getAttribute("xml:id");
            xp = "//tei:annotationBlock[@who='" + internalSpeakerID + "']";
            NodeList abs = (NodeList)xPath.evaluate(xp, transcriptDocument.getDocumentElement(), XPathConstants.NODESET);
            for (int i=0; i<abs.getLength(); i++){                
                Element ab = (Element) abs.item(i);
                spanGrp = transcriptDocument.createElementNS("http://www.tei-c.org/ns/1.0", "tei:spanGrp");
                spanGrp.setAttribute("type", "meta");
                spanGrp.setAttribute("subtype", "time-based");                        
                ab.appendChild(spanGrp);       
                for (String key : speakerKeys){                    
                    String metadataValue = comaDocument.getSpeakerMetadataAsMap().get(speakerID).get(key);
                    if (metadataValue==null || metadataValue.trim().length()==0){
                        continue;
                    }
                    //System.out.println(key.getID() + " / " + key.getName("en") + ": " + metadataValue);
                    Element span = transcriptDocument.createElementNS("http://www.tei-c.org/ns/1.0", "tei:span");
                    span.setAttribute("from", ab.getAttribute("start"));
                    span.setAttribute("to", ab.getAttribute("end"));
                    String consolidatedKey = key
                            .replaceAll(" ", "_")
                            .replaceAll("[\\(\\)]", "");
                    span.setAttribute("type", consolidatedKey);
                    span.setTextContent(metadataValue);
                    spanGrp.appendChild(span);                        
                }
            }


        }
        
    }
    
 
    String TEXAS = """
                   ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣾⣿⣾⣿⡦⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
                   ⠀⠀⠀⠀⠀⠀⠀⠀⣀⣀⠀⠀⣰⣿⣿⣿⣿⣷⣀⣀⣀⡀⠀⠀⠀⠀⠀⠀⠀⠀
                   ⠀⠀⠀⠀⠀⠀⠀⠈⠻⢿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡿⠆⠀⠀⠀⠀⠀⠀⠀
                   ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠻⣿⣿⣿⣿⠟⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
                   ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣴⣿⣿⣿⣿⣦⣀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
                   ⠀⠀⠀⠀⠀⠀⠀⢀⣀⣤⣶⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⣶⣤⡀⠀⠀⠀⠀⠀
                   ⠀⠀⠀⠀⠀⢀⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣦⡀⠀⠀⠀
                   ⠀⠀⠀⠀⢠⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣦⡀⠀
                   ⠀⠀⠀⣴⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡇⠉⠻⣿⣿⣿⣿⡆
                   ⠀⢀⣾⣿⣿⣿⠟⠁⢸⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠃⠀⠀⠸⣿⣿⣿⣷
                   ⠀⢸⣿⣿⣿⡏⠀⠀⢸⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠀⠀⠀⠀⣿⣿⣿⣿
                   ⠀⣿⣿⣿⣿⠀⢀⣄⠀⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡏⢠⡄⠀⠀⢹⣿⣿⡟
                   ⠀⣿⣿⣿⠏⠀⢸⣇⠀⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣇⢸⣷⡠⠶⣿⣿⣿⠋
                   ⠰⣿⣿⣿⠀⠈⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡇⠀⢸⣿⡿⠀
                   ⢠⣿⣿⠿⣧⠀⢿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠃⢠⣿⠟⠁⠀
                   ⣿⣿⠃⠀⠈⠀⢸⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠂⠀⠁⠀⠀⠀
                   ⠘⠟⠃⠀⠀⠀⠀⢹⣿⣿⣿⣿⣿⣿⡟⠉⢿⣿⣿⣿⣿⣿⣿⠉⠀⠀⠀⠀⠀⠀
                   ⠀⠀⠀⠀⠀⠀⠀⣾⣿⣿⣿⣿⣿⡟⠀⠀⠈⢿⣿⣿⣿⣿⣿⠀⠀⠀⠀⠀⠀⠀
                   ⠀⠀⠀⠀⠀⠀⠀⣿⣿⣿⣿⣿⡿⠀⠀⠀⠀⠀⣿⣿⣿⣿⣿⠀⠀⠀⠀⠀⠀⠀
                   ⠀⠀⠀⠀⠀⠀⢠⣿⣿⣿⣿⣿⠁⠀⠀⠀⠀⠀⢿⣿⣿⣿⣿⡆⠀⠀⠀⠀⠀⠀
                   ⠀⠀⠀⠀⠀⠀⢸⣿⣿⣿⣿⡇⠀⠀⠀⠀⠀⠀⢸⣿⣿⣿⣿⠀⠀⠀⠀⠀⠀⠀
                   ⠀⠀⠀⠀⠀⠀⣿⣿⣿⣿⣿⠁⠀⠀⠀⠀⠀⠀⢸⣿⣿⣿⣿⠀⠀⠀⠀⠀⠀⠀
                   ⠀⠀⠀⠀⠀⣸⣿⣿⣿⣿⡿⠀⠀⠀⠀⠀⠀⠀⢸⣿⣿⣿⣿⡆⠀⠀⠀⠀⠀⠀
                   ⠀⠀⠀⠀⢀⣿⣿⣿⣿⣿⠁⠀⠀⠀⠀⠀⠀⠀⠘⣿⣿⣿⣿⣧⠀⠀⠀⠀⠀⠀
                   ⠀⠀⠀⠀⢠⣿⣿⣿⣿⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⣿⣿⣿⣿⣿⡆⠀⠀⠀⠀⠀
                   ⠀⠀⠀⠀⢸⣿⣿⣿⣿⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⢿⣿⣿⣿⣿⡇⠀⠀⠀⠀⠀
                   ⠀⠀⠀⣠⣾⣿⣿⣿⠇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠘⠿⠿⠿⣿⣿⣶⣦⠄⠀⠀
                   """;
}


