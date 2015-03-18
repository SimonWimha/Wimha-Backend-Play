/*
 * File Extension Converter to Convert Mime File Type to Extension
 */
package models.util;

import java.util.HashMap;
import java.util.Map;

public class FileExtensionConverter
{
        private static Map<String, String> extensionMIMETypeMapping;
        private static String defaultMIMEType = "application/octet-stream";
     
        public FileExtensionConverter()
        {
            extensionMIMETypeMapping = new HashMap<String, String>();
            extensionMIMETypeMapping.put("text/plain", "txt");
            extensionMIMETypeMapping.put("text/richtext", "rtf");
            extensionMIMETypeMapping.put("audio/wav", "wav");
            extensionMIMETypeMapping.put("image/gif", "gif");
            extensionMIMETypeMapping.put("image/jpeg", "jpeg");
            extensionMIMETypeMapping.put("image/jpg", "jpg");
            extensionMIMETypeMapping.put("image/png" , "png");
            extensionMIMETypeMapping.put("image/tiff", "tiff");
            extensionMIMETypeMapping.put("image/bmp", "bmp");
       }
    
       public String ToExtensionType(String mime)
       {
           if (mime == null || mime.length() == 0)  {
               return "";           
           }
           
           String lowerMime = mime.toLowerCase();
           if (!extensionMIMETypeMapping.containsKey(mime))
            return "";
           return extensionMIMETypeMapping.get(mime);
       }
   }