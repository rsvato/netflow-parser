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
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.text.DateFormat;
import java.text.ParsePosition;

public class LineProcessor {
    private final List<NetworkDefinition> networks;
    private final Map<String, NetworkDefinition> cache = new HashMap<>();
    private final HostCache hostCache;

    public LineProcessor(HostCache hostCache, List<NetworkDefinition> networks){
        this.hostCache = hostCache;
        this.networks = networks;
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

        /*if (input != null && output != null){
            return; // internal traffic
        }*/

        if (output != null) {
            hostCache.addInput(elements[3],
                    new Long(elements[10]), output.getNetworkId());
            hostCache.addOutput(elements[3],
                    new Long(elements[11]), output.getNetworkId());
        }

        if (input != null){
            hostCache.addInput(elements[2], Long.decode(elements[11]), input.getNetworkId());
            hostCache.addOutput(elements[2], Long.decode(elements[10]), input.getNetworkId());
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
        NetworkDefinition result;
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
