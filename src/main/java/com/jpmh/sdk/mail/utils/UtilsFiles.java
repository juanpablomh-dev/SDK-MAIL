package com.jpmh.sdk.mail.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class UtilsFiles {
    public static String normalizeFileName(String path) {
        path = path.replace("#", "_");
        path = path.replace("%", "_");
        path = path.replace("*", "_");
        path = path.replace(":", "_");
        path = path.replace("<", "_");
        path = path.replace(">", "_");
        path = path.replace("?", "_");
        path = path.replace("/", "_");
        path = path.replace("\\", "_");
        path = path.replace("|", "_");
        path = path.replace("|", "_");
        path = path.replace("@", "_");
        return path;
    }
    
    public static void saveFile(String fileName, InputStream is) throws IOException {
        File f = new File(fileName);
        try (FileOutputStream fos = new FileOutputStream(f)) {
            byte[] buf = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buf)) != -1) {
                fos.write(buf, 0, bytesRead);
            }
        }
    }
}
