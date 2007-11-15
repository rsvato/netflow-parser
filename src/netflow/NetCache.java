/**
 * @author slava
 * @version $Id $
 */
package netflow;

import java.util.Map;
import java.util.HashMap;

public class NetCache {
    private static NetCache ourInstance = new NetCache();
    private Map cache;
    public static NetCache getInstance() {
        return ourInstance;
    }

    private NetCache() {
        this.cache = new HashMap();
    }

    public void addInput(Integer host, long bytes){
        NetworkTraffic toadd = getHost(host);
        toadd.addInput(bytes);
    }

    public void addOutput(Integer host, long bytes){
        NetworkTraffic toadd = getHost(host);
        toadd.addOutput(bytes);
    }

    private NetworkTraffic getHost(Integer host){
        NetworkTraffic result = (NetworkTraffic) cache.get(host);
        if (result == null){
            result = new NetworkTraffic(host);
            cache.put(host, result);
        }
        return result;
    }

    public Map getCache(){
        return cache;
    }

    public void save(java.util.Date date) {
        DatabaseProxy proxy = DatabaseProxy.getInstance();
        proxy.saveNetworks(cache, date);
        cache = new HashMap();
    }
}
