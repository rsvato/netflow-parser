package netflow;

/**
 * @author slava
 * @version $Id $
 */
class HostTraffic {
    private String hostAddress;
    private long inputBytes;
    private long outputBytes;
    private Integer networkId;

    public HostTraffic(String hostAddress, Integer networkId) {
        this.hostAddress = hostAddress;
        this.inputBytes = 0;
        this.outputBytes = 0;
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
