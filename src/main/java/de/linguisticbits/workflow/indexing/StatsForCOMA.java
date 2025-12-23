/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package de.linguisticbits.workflow.indexing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.jdom.JDOMException;
import org.xml.sax.SAXException;
import org.zumult.io.FileIO;
import org.zumult.io.IOHelper;
import org.zumult.objects.TokenList;
import org.zumult.objects.Transcript;
import org.zumult.objects.implementations.COMATranscript;
import org.zumult.objects.implementations.DefaultTokenList;

/**
 *
 * @author bernd
 */
public class StatsForCOMA {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length==1){
            pathToComa = args[0];
        } else if (args.length!=0){
            System.out.println("Usage: StatsForComa path/to/Coma.coma");
            System.exit(0);            
        }
        
        try {
            new StatsForCOMA().doit();
        } catch (IOException | SAXException | ParserConfigurationException | XPathExpressionException | TransformerException | JDOMException ex) {
            Logger.getLogger(StatsForCOMA.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    static String pathToComa = "D:\\ZUMULT-UT\\TGDP\\TGDP.coma";
    

    private void doit() throws IOException, SAXException, ParserConfigurationException, XPathExpressionException, TransformerException, JDOMException {
        String corpusID = "TGDP";
        File comaFile = new File(pathToComa);
        org.jdom.Document outDocument = new org.jdom.Document(new org.jdom.Element("corpus-statistics").setAttribute("id", corpusID));
        
        
        
        
        TokenList allTokenList = new DefaultTokenList("transcription");
        
        org.jdom.Document comaDocument = org.exmaralda.exakt.utilities.FileIO.readDocumentFromLocalFile(comaFile);
        Path topLevelPath = comaFile.getParentFile().toPath();
        List communications = org.jdom.xpath.XPath.selectNodes(comaDocument, "//Communication");
        List speakers = org.jdom.xpath.XPath.selectNodes(comaDocument, "//Speaker");
        List allTranscriptions = org.jdom.xpath.XPath.selectNodes(comaDocument, "//Transcription");
        System.out.println("[StatsForComa] " + communications.size() + " speech events found in " + comaFile.getAbsolutePath());


        int countInterviews = communications.size();
        int countSpeakers = speakers.size();
        int countTranscripts = allTranscriptions.size();
        outDocument.getRootElement().setAttribute("speakers", Integer.toString(countSpeakers));
        outDocument.getRootElement().setAttribute("interviews", Integer.toString(countInterviews));
        outDocument.getRootElement().setAttribute("transcripts", Integer.toString(countTranscripts));



        int count = 0;
        
        int i=0;
        for (Object o2 : communications){
            org.jdom.Element communication = (org.jdom.Element)o2;
            TokenList seTokenList = new DefaultTokenList("transcription");
            org.jdom.Element seElement = new org.jdom.Element("interview");
            String speechEventID = communication.getAttributeValue("Id");
            seElement.setAttribute("id", speechEventID);
            outDocument.getRootElement().addContent(seElement);
            
            List transcriptions = org.jdom.xpath.XPath.selectNodes(communication, "descendant::Transcription");
            for (Object o : transcriptions){
                org.jdom.Element transcriptionElement = (org.jdom.Element)o;
                String transcriptID = transcriptionElement.getAttributeValue("Id");
                count++;
                String nsLink = transcriptionElement.getChildText("NSLink");
                //String eafFilename = nsLink.replaceAll("\\.xml", ".eaf");
                Path resolvedPath = topLevelPath.resolve(nsLink);
                File xmlFile = resolvedPath.toFile();
                //System.out.println("[StatsForComa] Processing " + count + " of " + transcriptions.size() + ": " + transcriptID + " / " + xmlFile.getAbsolutePath());
                Transcript transcript = new COMATranscript(IOHelper.readDocument(xmlFile));
                int types = transcript.getNumberOfTypes();
                int tokens = transcript.getNumberOfTokens();
                TokenList thisTokenList = transcript.getTokenList("transcription");
                allTokenList = allTokenList.merge(thisTokenList);
                seTokenList = seTokenList.merge(thisTokenList);
                

                org.jdom.Element transcriptElement = new org.jdom.Element("transcript");
                transcriptElement.setAttribute("id", transcriptID);
                transcriptElement.setAttribute("tokens", Integer.toString(tokens));
                transcriptElement.setAttribute("types", Integer.toString(types));

                double duration = transcript.getEndTime() - transcript.getStartTime();
                transcriptElement.setAttribute("duration", Double.toString(duration));                    

                seElement.addContent(transcriptElement);
                
            }
            seElement.setAttribute("types", Integer.toString(seTokenList.getNumberOfTypes()));
            if (i%50==0){
                System.out.println("[StatsForComa] processed " + i + " of " + communications.size() + " Speech events ("+ count + " transcripts)");
            }
            i++;
            
        }
        
        File STATS_FILE = new File(comaFile.getParentFile(), comaFile.getName().substring(0, comaFile.getName().indexOf(".")) + ".stats");
        
        
        outDocument.getRootElement().setAttribute("types", Integer.toString(allTokenList.getNumberOfTypes()));
        
        FileIO.writeDocumentToLocalFile(STATS_FILE, outDocument);
        
        System.out.println("[StatsForComa] Stats for " + corpusID + " calculated and written to " + STATS_FILE.getAbsolutePath());
        
        
    }
    
}
