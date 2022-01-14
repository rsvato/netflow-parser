/**
 * Copyright (C) 2005-2013 rsvato <rsvato@gmail.com>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package netflow;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetworkDefinition {
    private static final Logger log = LoggerFactory.getLogger(NetworkDefinition.class);

    private final Integer networkId;
    private final InetAddress networkAddress;
    private final InetAddress netmask;
    private final String snetmask;
    private final String saddress;
    private InetAddress returnAddress;
    private long addressAsLong;
    private long maskAsLong;
    private long broadcast;

    public NetworkDefinition(Integer nid, String network, String netmask, String returnAd) {
        try {
            this.networkId = nid;
            this.networkAddress = InetAddress.getByName(network);
            this.netmask = InetAddress.getByName(netmask);
            this.snetmask = netmask;
            this.saddress = network;
            this.returnAddress = InetAddress.getByName(returnAd);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public NetworkDefinition(Integer nid, String network, String netmask) {
        try {
            this.networkId = nid;
            this.networkAddress = InetAddress.getByName(network);
            this.netmask = InetAddress.getByName(netmask);
            this.saddress = network;
            this.snetmask = netmask;
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public static long addrToLong(InetAddress address) {
        byte[] rawIP = address.getAddress();
        return convertToLong(rawIP);
    }

    public static long addrToLong(String addr) {
        String pattern = ".";
        int ind, cur = 0;
        int max = 4;
        byte[] rawIp = new byte[4];
        while ((ind = addr.indexOf(pattern)) != -1 && cur < max) {
            rawIp[cur++] = (byte) Integer.parseInt(addr.substring(0, ind));
            addr = addr.substring(ind + 1);
            if (!addr.contains(pattern)) {
                rawIp[cur++] = (byte) Integer.parseInt(addr);
            }
        }
        return convertToLong(rawIp);
    }

    private static long convertToLong(byte[] rawIP) {
        return ((long) (rawIP[0] & 0xff) << 24 | (rawIP[1] & 0xff) << 16 |
                (rawIP[2] & 0xff) << 8 | (rawIP[3] & 0xff)) & 0xffffffffL;
    }

    public boolean isMyAddress(String address) {
        boolean result = false;
        if ("255.255.255.255".equals(snetmask)) {
            return saddress.equals(address);
        }

        if (address == null || saddress == null) { // at least first octet
            return false;
        }

        if (address.equals(saddress)) {
            return true;
        }

        if (!saddress.startsWith(address.substring(0, address.indexOf(".")))) { //first octet
            return false;
        }

        try {
            //InetAddress foreign = InetAddress.getByName(address);
            if (addressAsLong == 0) {
                addressAsLong = addrToLong(networkAddress);
            }
            if (maskAsLong == 0) {
                maskAsLong = addrToLong(netmask);
            }
            long fa = addrToLong(address);
            if ((addressAsLong & maskAsLong) == (fa & maskAsLong)) {
                if (getBroadcastAddress() == fa) {
                    result = getBroadcastAddress() == addressAsLong; // say, 10.0.4.1/32 - very rare and illegal, but used
                } else {
                    result = true;
                }
            }
            if (returnAddress != null && !result) {
                result = fa == addrToLong(returnAddress);
            }
        } catch (Exception e) {
            log.error("Bad host: {}", address, e);
        }
        return result;
    }

    private long getBroadcastAddress() {
        if (broadcast == 0) {
            broadcast = ((addressAsLong | (~(maskAsLong) & 0xff)));
        }
        return broadcast;
    }

    private InetAddress getReadableBroadcast() throws UnknownHostException {
        long addr = getBroadcastAddress();
        return InetAddress.getByName(String.valueOf(addr));
    }

    public Integer getNetworkId() {
        return networkId;
    }

    public String toString() {
        return networkId + ". [address=" + networkAddress + "; mask=" + netmask + "]";
    }


    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NetworkDefinition that = (NetworkDefinition) o;

        if (!netmask.equals(that.netmask)) return false;
        if (!networkAddress.equals(that.networkAddress)) return false;
        if (!networkId.equals(that.networkId)) return false;
        return !(returnAddress != null ? !returnAddress.equals(that.returnAddress) : that.returnAddress != null);

    }

    public int hashCode() {
        int result;
        result = networkId.hashCode();
        result = 31 * result + networkAddress.hashCode();
        result = 31 * result + netmask.hashCode();
        result = 31 * result + (returnAddress != null ? returnAddress.hashCode() : 0);
        return result;
    }
}
