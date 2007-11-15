/**
 * @author slava
 * @version $Id $
 */
package netflow;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//TODO: externalize url and driver and so on...
public class DatabaseProxy {
    private Connection con;
    private static final String url = "jdbc:postgresql://localhost/traffic";
    private static final String driver = "org.postgresql.Driver";
    private static final String userName = "root";
    private static final String password = "test12~";
    private static DatabaseProxy ourInstance = new DatabaseProxy();
    private static final Log log = LogFactory.getLog(DatabaseProxy.class);
    private static final String CONFIGURATION = "configuration";

    private DatabaseProxy() {
        try {
            Properties props;
            try {
                props = readProperties();
            }catch(IOException e){
                log.warn("Cannot read properties from file " + CONFIGURATION);
                props = fillDefaultProperties();
            }
            con = createConnection(props);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Database driver not found", e);
        } catch (SQLException e) {
            throw new IllegalArgumentException("SQL Exception", e);
        }
    }

    private Properties fillDefaultProperties() {
        return new Properties();
    }

    private Connection createConnection(Properties properties) throws SQLException, ClassNotFoundException {
        Class.forName(properties.getProperty("driver", driver));
        return DriverManager.getConnection(properties.getProperty("url", url),
                properties.getProperty("userName", userName),
                properties.getProperty("password", password));
    }


    private Properties readProperties() throws IOException {
       Properties props = new Properties();
            String configFileName = System.getProperty(CONFIGURATION);
            if (configFileName != null){
                File f = new File(configFileName);
                if (f.exists() && f.isFile() && f.canRead()){
                    props.load(new FileInputStream(f));
                }
            }
        return props;
    }

    public static DatabaseProxy getInstance() {
        if (ourInstance == null){
            ourInstance = new DatabaseProxy();
        }
        return ourInstance;
    }


    public List<NetworkDefinition> getNetworks() {
        String sql = "select net, mask, nat_addr, id from networks where client is not null";
        List<NetworkDefinition> tmp = new ArrayList<NetworkDefinition>();
        try {
            PreparedStatement pstmt = con.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                NetworkDefinition nd = new NetworkDefinition(rs.getInt(4), rs.getString(1), rs.getString(2), rs.getString(3));
                tmp.add(nd);
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            throw new Error(e);
        }
        return tmp;
    }

    public void saveNetworks(Map cache, java.util.Date dat) {
        if (cache.size() == 0) {
            log.debug("Nothing to save");
            return;
        }
        log.debug("cache size: " + cache.size() + " " + dat);
        String sql = "insert into netflow_networks_details (network_id, dat, input, output) values (?, ?, ?, ?)";
        try {
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setTimestamp(2, new java.sql.Timestamp(dat.getTime()));
            for (Object key : cache.keySet()) {
                NetworkTraffic traffic = (NetworkTraffic) cache.get(key);
                pstmt.setInt(1, traffic.getNetworkId());
                pstmt.setLong(3, traffic.getInputBytes());
                pstmt.setLong(4, traffic.getOutputBytes());
                pstmt.executeUpdate();
            }
            pstmt.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
    
    public java.util.Date getMaxDate(){
    	java.util.Date result = null;
    	String sql = "select max(dat) from netflow_details";
    	try{
    		PreparedStatement pstmt = con.prepareStatement(sql);
    		ResultSet rs = pstmt.executeQuery();
    		if (rs.next()){
    			Timestamp t = rs.getTimestamp(1);
          if (t != null){
    			  result = new java.util.Date();
    			  result.setTime(t.getTime());
          }
    		}
    	}catch(SQLException e){
    		log.error(e);
    		e.printStackTrace();
    	}
    	return result;
    }
    
    public void saveHosts(Map<String, HostTraffic> cache, java.util.Date date) {
        if (cache.size() == 0) {
            log.debug("Host cache empty");
            return;
        }
        log.debug("Saving "  + cache.size() + " records for " + date);
        String sql = "insert into netflow_details(dat, host, network_id, input, output) values (?, ?, ?, ?, ?)";
        try {
            PreparedStatement pstmt = con.prepareStatement(sql);
            Timestamp t = new java.sql.Timestamp(date.getTime());
            for (String key : cache.keySet()) {
                HostTraffic traffic = cache.get(key);
                if (!hasRecord(t, traffic.getHostAddress(), traffic.getNetworkId())) {
                    pstmt.setTimestamp(1, t);
                    pstmt.setString(2, traffic.getHostAddress());
                    pstmt.setInt(3, traffic.getNetworkId());
                    pstmt.setLong(4, traffic.getInputBytes());
                    pstmt.setLong(5, traffic.getOutputBytes());
                    pstmt.addBatch();
                }
            }
            int[] results = pstmt.executeBatch();
            log.info("saveHosts(): saved " + results.length + " records");
            pstmt.close();
            pstmt.clearParameters();
        } catch (SQLException e) {
            log.error("Saving hosts error: " + e.getMessage());
            SQLException ex = e.getNextException();
            if (ex != null){
                log.error(ex.getMessage());
            }
            e.printStackTrace(System.err);
        }
    }

    public void doAggregation(){
       String sql = "insert into client_ntraffic(client, dat, incoming, outcoming) " +
               "select cl.id, nn_summ.dat, sum(nn_summ.input), sum(nn_summ.output) from cl, nn_summ where " +
               "nn_summ.network_id in (select id from networks where client=cl.id) " +
               "and nn_summ.dat > (select max(dat) from client_ntraffic) group by 1, 2";
        String logStr = "doAggregation(): ";
        log.info(logStr + " <<<<");
        try{
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            log.error(logStr + " Aggregation error: " + e.getMessage());
            e.printStackTrace(System.err);
        }
        log.info(logStr + " >>>>");
    }

    private boolean hasRecord(Timestamp dat, String host, Integer networkId){
        boolean result = false;
        try {
            PreparedStatement pstmt = con.prepareStatement("select count(*) from netflow_details where dat=? and host=? and network_id = ?");
            pstmt.setTimestamp(1, dat);
            pstmt.setString(2, host);
            pstmt.setInt(3, networkId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()){
                int count = rs.getInt(1);
                result = count > 0;
            }
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            log.error("Query failed: " + e.getMessage());
        }
        return result;
    }

    public void close(){
        try{
            con.close();
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
    }

}
