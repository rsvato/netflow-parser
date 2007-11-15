package netflow;

import junit.framework.TestCase;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author slava
 * @version $Id $
 */
public class TestAddress extends TestCase{
    public void testAddressEquality(){
        NetworkDefinition nd = new NetworkDefinition(new Integer(1), "10.0.4.0", "255.255.255.0");
        boolean s = nd.isMyAddress("10.0.4.128");
        assertTrue("Address must be ok", s);
    }

    public void testDecip() throws UnknownHostException {
        long decip = NetworkDefinition.addrToLong(InetAddress.getByName("10.0.4.129"));
        assertEquals(167773313, decip);
    }

     public void testAddressEquality2(){
        NetworkDefinition nd = new NetworkDefinition(new Integer(1), "10.0.4.0", "255.255.255.0", "10.0.5.128");
        boolean s = nd.isMyAddress("10.0.5.128");
        assertTrue("Address must be ok", s);
    }

    public void testBrcast(){
       NetworkDefinition nd = new NetworkDefinition(new Integer(1),  "10.0.4.0", "255.255.255.0", "10.0.5.128");
        boolean s = nd.isMyAddress("10.0.4.255");
        assertFalse("Broadcast cannot be in network", s);
    }

    public void testOneAddress(){
       NetworkDefinition nd = new NetworkDefinition(new Integer(1),  "10.0.4.1", "255.255.255.255", "10.0.5.128");
        boolean s = nd.isMyAddress("10.0.4.1");
        assertTrue("Network can include only one address", s);
    }
    
}

