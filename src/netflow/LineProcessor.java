package netflow;


import java.util.Date;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.text.DateFormat;
import java.text.ParsePosition;

/**
 * User: slava
 * Date: 09.11.2006
 * Time: 22:58:46
 * Version: $Id$
 */
public class LineProcessor {
    private List<NetworkDefinition> networks;
    private Map<String, NetworkDefinition> cache = new HashMap<String, NetworkDefinition>();

    public LineProcessor(){
        networks = DatabaseProxy.getInstance().getNetworks();
    }

    public static Date parseTime(String line, DateFormat df) {
        int firstSpace = line.indexOf(" "); // first space; beginning of a date
        if (firstSpace > -1) {
            return df.parse(line, new ParsePosition(firstSpace + 1));
        }
        return null;
    }

    public void parseLine(String[] elements) {
        NetworkDefinition input = netId(elements[2]);
        NetworkDefinition output = netId(elements[3]);

        if (input != null && output != null){
            return; // internal traffic
        }

        HostCache cache = HostCache.getInstance();
        if (output != null) {
            cache.addInput(elements[3],
                    new Long(elements[10]), output.getNetworkId());
            cache.addOutput(elements[3],
                    new Long(elements[11]), output.getNetworkId());
        }

        if (input != null){
            cache.addInput(elements[2], Long.decode(elements[11]), input.getNetworkId());
            cache.addOutput(elements[2], Long.decode(elements[10]), input.getNetworkId());
        }
    }

    protected Set<NetworkDefinition> intersection(Set<NetworkDefinition> setx,
                                                      Set<NetworkDefinition> sety){
        Set<NetworkDefinition> result = new HashSet<NetworkDefinition>();
        for (NetworkDefinition o : setx) {
            for (NetworkDefinition o1 : sety) {
               if (o1.getNetworkId().equals(o.getNetworkId())){
                   result.add(o1);
               }
            }
        }
        return result;
    }

    public NetworkDefinition netId(String address) {
        NetworkDefinition result = null;
        result = cache.get(address);
        if (result == null){
          for (NetworkDefinition network : networks) {
            if (network.isMyAddress(address)) {
                result = network;
                cache.put(address, result);
                break;
            }
          }
        }
        return result;
    }
}
