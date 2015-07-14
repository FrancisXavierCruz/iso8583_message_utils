package tests;

import com.cyberpunksugarrush.iso8583.ISO8583Message;

/**
 *
 * @author xavier
 */
public class TestParser
{
    public static void main(String[] args)
    {
        ISO8583Message iso = new ISO8583Message("0200B2200000001000000000000000800000201234000000010000110722183012345606A5DFGR021ABCDEFGHIJ 1234567890", false);
        
        ISO8583Message iso2 = new ISO8583Message("0800822000000000000004000000000000000516063439749039301", false);
        
        iso2.parser.doYerThang();
    }
}
