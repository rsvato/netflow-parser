/**
 * Copyright (C) 2005-2013 rsvato <rsvato@gmail.com>
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
package netflow;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HostCache {
    private final DatabaseProxy db;
    private Map<String, HostTraffic> cache;

    public HostCache(DatabaseProxy db) {
        this.db = db;
        this.cache = new HashMap<>();
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

    public void save(Date date){
        if (db != null) {
            db.saveHosts(cache, date);
            cache = new HashMap<>();
        }
    }
    
    public boolean isEmpty(){
    	return cache.isEmpty();
    }
}
