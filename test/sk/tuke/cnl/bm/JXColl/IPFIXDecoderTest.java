/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.tuke.cnl.bm.JXColl;

import sk.tuke.cnl.bm.DataException;
import java.nio.ByteBuffer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author veri
 */
public class IPFIXDecoderTest {

    public IPFIXDecoderTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of decodeData method, of class IPFIXDecoder.
     */
    @Test
    public void testDecodeData() throws Exception {
        System.out.println("decodeData");
        String type = "signed32";
        byte[] data = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        ByteBuffer buffer = ByteBuffer.wrap(data);
        String expResult = "-1";
        String result = IpfixDecoder.decode(type, buffer).toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of getHex method, of class IPFIXDecoder.
     */
    @Test
    public void testGetHex() {
        System.out.println("getHex");
        byte[] data = {(byte) 0xAC};
        String expResult = "AC";
        String result = IpfixDecoder.getHex(data);
        assertEquals(expResult, result);
    }

    /**
     * Test of parseMacAddress method, of class IPFIXDecoder.
     */
    @Test
    public void testParseMacAddress() throws DataException {
        System.out.println("parseMacAddress");
        byte[] data = {(byte) 0x74, (byte) 0xE5, (byte) 0x0B, (byte) 0x1A, (byte) 0x3B, (byte) 0xDC};
        String expResult = "74:E5:0B:1A:3B:DC";
        String result = IpfixDecoder.parseMacAddress(data);
        assertEquals(expResult, result);
    }
}
