/**
 * @author slava
 * @version $Id $
 */
package netflow;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HostCache {
    private static HostCache ourInstance = new HostCache();
    private Map<String, HostTraffic> cache;
    public static HostCache getInstance() {
        return ourInstance;
    }

    private HostCache() {
        this.cache = new HashMap<String, HostTraffic>();
    }

    public void addInput(String host, long bytes, Integer networkId){
        HostTraffic toadd = getHost(host, networkId);
        toadd.addInput(bytes);
    }

    public void addOutput(String host, long bytes, Integer networkId){
        HostTraffic toadd = getHost(host, networkId);
        toadd.addOutput(bytes);
    }

    private HostTraffic getHost(String host, Integer networkId){
        String key = String.valueOf(host.hashCode() * networkId);
        HostTraffic result = cache.get(key);
        if (result == null){
            result = new HostTraffic(host, networkId);
            cache.put(key, result);
        }
        return result;
    }

    public void save(Date dat){
        DatabaseProxy proxy = DatabaseProxy.getInstance();
        proxy.saveHosts(cache, dat);
        cache = new HashMap<String, HostTraffic>();

    }
    
    public boolean isEmpty(){
    	return cache.isEmpty();
    }
}
