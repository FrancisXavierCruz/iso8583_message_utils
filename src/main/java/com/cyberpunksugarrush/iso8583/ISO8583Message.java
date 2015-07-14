package com.cyberpunksugarrush.iso8583;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * TODO:
 * 1. Parsing a message with header
 * 2. Sort fields
 * 3. Message builder
 */

/**
 *
 * @author xavier
 */
public class ISO8583Message
{
    private final String isoMessage;
    private final boolean hasHeader;
    private static final String BREAK = System.getProperty("line.separator"); // http://www.coderanch.com/t/411039/java/java/insert-line-character-String
    private static final int MTI_LENGTH = 4;
    private static final int HEX_LENGTH_PRIMARY = 16;
    private static final int HEX_LENGTH_EXTENDED = 32;
    private static final Properties prop;
    private static final HashMap<String, Object[]> DATA_ELEMENTS;
    private boolean hasExtendedBitmap;
    public final Parser parser;
    
    static
    {
        prop = new Properties();
        
        try
        {
            prop.load(new FileInputStream(new File("./src/main/resources/config/iso8583.properties")));
        }
        catch (IOException ex)
        {
            Logger.getLogger(ISO8583Message.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        DATA_ELEMENTS = new HashMap<>();
        
        for (String s : prop.stringPropertyNames())
        {
            String[] str = ((String) prop.get(s)).split(",");
            String fieldType = str[0];
            Integer fieldLength = Integer.parseInt(str[1]);
            
            DATA_ELEMENTS.put(s, new Object[] {fieldType, fieldLength});
        }
    }
    
    public ISO8583Message(String isoMessage, boolean hasHeader)
    {
        this.isoMessage = isoMessage;
        this.hasHeader = hasHeader;
        parser = new Parser();
    }
    
    /**
     * 
     * @see https://stackoverflow.com/questions/9246326/convert-hexadecimal-string-hex-to-a-binary-string
     * @param hexString
     * @return 
     */
    private String hexToBin(String hexString)
    {
        return new BigInteger(hexString, 16).toString(2);
    }
    
    private String getMTI()
    {
        return isoMessage.substring(0, MTI_LENGTH);
    }
    
    private String getBitmap()
    {
        Character firstHexValue = isoMessage.charAt(MTI_LENGTH);
        char field1 = hexToBin(firstHexValue.toString()).charAt(0);
        String binary = null;
        
        if (field1 == '1')
        {
            hasExtendedBitmap = true;
            binary = hexToBin(isoMessage.substring(MTI_LENGTH, MTI_LENGTH + HEX_LENGTH_EXTENDED));
        }
        else if (field1 == '0')
        {
            hasExtendedBitmap = false;
            binary = hexToBin(isoMessage.substring(MTI_LENGTH, MTI_LENGTH + HEX_LENGTH_PRIMARY));
        }
        
        return binary;
    }
    
    private HashMap<String, String> getData()
    {
        HashMap<String, String> data = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        
        // check the first bitmap
        
        if (hasExtendedBitmap)
        {
            sb.append(isoMessage.substring(MTI_LENGTH + HEX_LENGTH_EXTENDED));            
            data.put("1", getBitmap().substring(getBitmap().length() / 2));
        }
        else
        {
            sb.append(isoMessage.substring(MTI_LENGTH + HEX_LENGTH_PRIMARY));
        }
        
        // continue with the rest of the bitmap
        
        for (Integer i = 2; i <= getBitmap().length(); i++)
        {
            int endIndex = (int) DATA_ELEMENTS.get(i.toString())[1];
            
            if (getBitmap().charAt(i - 1) == '1')
            {
                try
                {
                    data.put(i.toString(), sb.substring(0, endIndex));
                    sb.delete(0, endIndex);
                }
                catch (StringIndexOutOfBoundsException ex)
                {
                    data.put(i.toString(), sb.substring(0, sb.length()));
                }
            }
        }
        
        return data;
    }
    
    public HashMap<String, String> getParsedMessage()
    {
        HashMap<String, String> parsedMessage = new HashMap<>();
        
        parsedMessage.put("MTI", getMTI());
        parsedMessage.put("Bitmap", getBitmap());
        
        for (String key : getData().keySet())
        {
            parsedMessage.put(key, getData().get(key));
        }        
        
        return parsedMessage;
    }
    
    public class Parser
    {
        /**
         * Just for kicks.
         */
        public void doYerThang()
        {
            StringBuilder sb = new StringBuilder();
            
            for (String key : getParsedMessage().keySet())
            {
                sb.append(key).append(" : ").append(getParsedMessage().get(key))
                        .append(BREAK);
            }
            
            System.out.println("ISO : " + isoMessage);
            System.out.println(sb);
        }
    }
}
