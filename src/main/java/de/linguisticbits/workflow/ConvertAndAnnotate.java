/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package de.linguisticbits.workflow;

import de.linguisticbits.workflow.data.EAFAnnotator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.exmaralda.common.jdomutilities.IOUtilities;
import org.exmaralda.exakt.utilities.FileIO;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;

/**
 *
 * @author bernd
 */
public class ConvertAndAnnotate {

    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            if (args.length==0){
                String comaPath = "D:\\ZUMULT\\TGDP\\TGDP.coma";
                File comaFile = new File(comaPath);
                new ConvertAndAnnotate().doit(comaFile);
                System.exit(0);
            }
            
            if (args.length!=2){
                System.out.println("Usage: ConvertAndAnnotate /path/to/comafile.coma /path/to/configuration.xml");
                System.exit(0);
            }
            
            String comaPath = args[0];
            File comaFile = new File(comaPath);
            new ConvertAndAnnotate().doit(comaFile);
        } catch (JDOMException | IOException ex) {
            Logger.getLogger(ConvertAndAnnotate.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void doit(File comaFile) throws JDOMException, IOException {
        Document comaDocument = FileIO.readDocumentFromLocalFile(comaFile);
        Path topLevelPath = comaFile.getParentFile().toPath();
        List transcriptions = XPath.selectNodes(comaDocument, "//Transcription");
        System.out.println("[ConvertAndAnnotate] " + transcriptions.size() + " trancripts found in " + comaFile.getAbsolutePath());
        int count = 0;
        for (Object o : transcriptions){
            Element transcriptionElement = (Element)o;
            String transcriptID = transcriptionElement.getAttributeValue("Id");
            count++;
            String nsLink = transcriptionElement.getChildText("NSLink");
            String eafFilename = nsLink.replaceAll("\\.xml", ".eaf");
            Path resolvedPath = topLevelPath.resolve(eafFilename);
            File eafFile = resolvedPath.toFile();
            System.out.println("[ConvertAndAnnotate] Processing " + count + " of " + transcriptions.size() + ": " + transcriptID + " / " + eafFile.getAbsolutePath());

            EAFAnnotator eafAnnotator = new EAFAnnotator(eafFile);
            eafAnnotator.convertAndAnnotate();

            String isoTeiXML = eafAnnotator.getIsoTeiXML();
            Document isoTeiDocument = IOUtilities.readDocumentFromString(isoTeiXML);
            Path isoTeiPath = topLevelPath.resolve(nsLink);
            File isoTeiFile = isoTeiPath.toFile();
            FileIO.writeDocumentToLocalFile(isoTeiFile, isoTeiDocument);
            System.out.println("[ConvertAndAnnotate] Result written to " + isoTeiFile.getAbsolutePath());
        }
        System.out.println("[ConvertAndAnnotate] All transcripts converted and annotated.");
        System.out.println("[ConvertAndAnnotate] TO DO: Calculate COMA index, ZuMult Index, COMA stats.");
        
    }
    
}
