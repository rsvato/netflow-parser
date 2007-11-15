package netflow;

/**
 * @author slava
 * @version $Id $
 */
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
