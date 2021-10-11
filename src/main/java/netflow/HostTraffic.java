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

class HostTraffic {
    private final String hostAddress;
    private long inputBytes;
    private long outputBytes;
    private final Integer networkId;

    public HostTraffic(String hostAddress, Integer networkId) {
        this.hostAddress = hostAddress;
        this.networkId = networkId;
    }

    public void addInput(long bytes){
        this.inputBytes += bytes;
    }

    public void addOutput(long bytes){
        this.outputBytes += bytes;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public long getInputBytes() {
        return inputBytes;
    }

    public long getOutputBytes() {
        return outputBytes;
    }

    public Integer getNetworkId(){
        return networkId;
    }

    public String toString(){
        return hostAddress + " network " + getNetworkId()
                + " [in: " + inputBytes + "; out: " + outputBytes + "]";
    }
}
