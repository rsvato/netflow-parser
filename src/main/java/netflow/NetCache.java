/**
 * Copyright (C) 2013 Svyatoslav Reyentenko <rsvato@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
