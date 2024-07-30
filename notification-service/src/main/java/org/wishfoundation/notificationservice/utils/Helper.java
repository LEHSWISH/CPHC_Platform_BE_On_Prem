package org.wishfoundation.notificationservice.utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

public class Helper {

    public static String createFileTempDirectory(String fileName,String base64String){
       try{
           String tempDirPath = System.getProperty("java.io.tmpdir");
           Path tempDir = Paths.get(tempDirPath);
           Path filePath = tempDir.resolve(fileName);
           Files.createFile(filePath);
           Files.write(filePath,decodeBase64ToBytes(base64String));
           System.out.println("File created successfully at: " + filePath);
           return filePath.toString();
       }catch (Exception e){
           e.printStackTrace();
       }
       return null;
    }

    static byte[] decodeBase64ToBytes(String base64String) {
            return Base64.getDecoder().decode(base64String);
    }

}
