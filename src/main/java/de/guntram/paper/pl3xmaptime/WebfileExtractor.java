package de.guntram.paper.pl3xmaptime;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.bukkit.Bukkit;

public class WebfileExtractor {
    
    private static final String CSSLINK = "<link rel=\"stylesheet\" href=\"css/worldtime.css\" crossOrigin=\"anonymous\" />";
    private static final String JSLINK = "<script type=\"module\" src=\"js/modules/WorldTime.js\" crossOrigin=\"anonymous\"></script>";
    
    public static boolean initWebDirectory(Path path) {
        File myJar;
        try {
            myJar = new File(WebfileExtractor.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException ex) {
            Bukkit.getLogger().log(Level.WARNING, "Can't find my own jar to extract web resources from", ex);
            return false;
        }
        try {
            extractWebFiles(myJar, path.toFile());
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.WARNING, "Error extracting web resources", ex);
            return false;
        }
        
        try {
            patchIndexHtml(path);
        } catch (IOException ex) {
            Bukkit.getLogger().log(Level.WARNING, "Error patching the index file", ex);
            return false;
        }
        return true;
    }

    private static void extractWebFiles(File myJar, File outputDir) throws IOException {
        try (ZipInputStream zipStream = new ZipInputStream(new FileInputStream(myJar))) {
            ZipEntry entry;
            while ((entry = zipStream.getNextEntry())!=null) {
                String entryName = entry.getName();
                if (entry.isDirectory() || !entryName.startsWith("web/")) {
                    continue;
                }
                File outputFile = new File(outputDir, entryName.substring(4));
                if (!(outputFile.exists())) {
                    extractZipEntry(zipStream, outputFile);
                }
            }
        }
    }
    
    private static void extractZipEntry(ZipInputStream zipStream, File outputFile) throws IOException {
        outputFile.getParentFile().mkdirs();
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            byte[] buf=new byte[16384];
            int length;
            while ((length=zipStream.read(buf, 0, buf.length))>=0) {
                fos.write(buf, 0, length);
            }
        }
    }

    private static void patchIndexHtml(Path webDir) throws IOException {
        Path currentPath = webDir.resolve("index.html");
        Path tempPath = webDir.resolve("index.html.tmp");
        
        try (
                BufferedReader reader = Files.newBufferedReader(currentPath);
                BufferedWriter writer = Files.newBufferedWriter(tempPath)
        ) {
            String line;
            boolean foundJS = false, foundCSS = false, foundNoPatch = false;

            while ((line = reader.readLine()) != null) {
                if (line.contains("js/modules/WorldTime.js")) {
                    foundJS = true;
                }
                if (line.contains("css/worldtime.css")) {
                    foundCSS = true;
                }
                if (line.toLowerCase().contains("notimepatch")) {
                    foundNoPatch = true;
                }
                if (line.contains("<body") && !foundNoPatch) {
                    if (!foundCSS) {
                        writer.write(CSSLINK);
                        writer.newLine();
                    }
                    if (!foundJS) {
                        writer.write(JSLINK);
                        writer.newLine();
                    }
                }
                writer.write(line);
                writer.newLine();
            }
        }
        
        Files.move(tempPath, currentPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    }
}
