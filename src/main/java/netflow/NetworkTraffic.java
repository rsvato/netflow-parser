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

class NetworkTraffic {
    private Integer networkId;
    private long inputBytes;
    private long outputBytes;

    public NetworkTraffic(Integer networkId) {
        this.networkId = networkId;
        this.inputBytes = 0;
        this.outputBytes = 0;
    }

    public void addInput(long bytes){
        this.inputBytes += bytes;
    }

    public void addOutput(long bytes){
        this.outputBytes += bytes;
    }

    public Integer getNetworkId() {
        return networkId;
    }

    public long getInputBytes() {
        return inputBytes;
    }

    public long getOutputBytes() {
        return outputBytes;
    }

    public String toString(){
        return networkId + " [in: " + inputBytes + "; out: " + outputBytes + "]";
    }
}
