/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package de.linguisticbits.workflow;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bernd
 */
public class CopyELAN {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new CopyELAN().doit();
        } catch (IOException ex) {
            Logger.getLogger(CopyELAN.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //String SOURCE_TOP_FOLDER = "C:\\Users\\bernd\\Dropbox\\work\\2021_MARGO_TEXAS_GERMAN\\TGDP-Cleanup\\10";
    String SOURCE_TOP_FOLDER = "C:\\Users\\bernd\\Dropbox\\work\\2021_MARGO_TEXAS_GERMAN\\TGDP-Cleanup\\5";
    String TARGET_TOP_FOLDER = "D:\\ZUMULT\\TGDP";

    private void doit() throws IOException {
        File sourceTop = new File(SOURCE_TOP_FOLDER);
        File targetTop = new File(TARGET_TOP_FOLDER);
        File[] targetFolders = targetTop.listFiles((File pathname) -> pathname.isDirectory());
        for (File targetFolder : targetFolders){
            File sourceFolder = new File(sourceTop, targetFolder.getName());
            if (!sourceFolder.exists()){
                System.out.println("No source folder for : " + targetFolder.getName());
                continue;
            }
            File[] eafFiles = sourceFolder.listFiles((File dir, String name) -> name.toLowerCase().endsWith(".eaf"));
            for (File eafFile : eafFiles){
                File targetFile = new File(targetFolder, eafFile.getName());
                System.out.println("Copying " + eafFile.getAbsolutePath() + " to " + targetFile.getAbsolutePath());
                Files.copy(eafFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
    
}
