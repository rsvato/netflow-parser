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

import java.io.InputStream;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class DatabaseProxy {
    private final Connection con;
    private static final Log log = LogFactory.getLog(DatabaseProxy.class);
    private static final String CONFIGURATION = "configuration";
    private final Properties queries = new Properties();

    private static final String queriesFile = "/sql/psql-queries.properties";
    private static final String defaultDb = "/config/psql-default.properties";

    public DatabaseProxy() {
        try {
            Properties connectionProps = new Properties();
            try {
                connectionProps = readFileProperties();
            } catch(IOException e) {
                log.warn("Cannot read properties from file " + CONFIGURATION);
            }
            if (connectionProps.isEmpty()) {
                connectionProps = fillDefaultProperties();
            }
            InputStream in = getClass().getResourceAsStream(queriesFile);
            queries.load(in);
            con = createConnection(connectionProps);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Database driver not found", e);
        } catch (SQLException e) {
            throw new IllegalArgumentException("Cannot connect to db due to SQL exception", e);
        } catch (IOException e) {
            throw new IllegalArgumentException("Can't load queries file");
        }
    }

    String getQuery(String key) {
        return queries.getProperty(key);
    }

    private Properties fillDefaultProperties() throws IOException {
        Properties result = new Properties();
        InputStream in = getClass().getResourceAsStream(defaultDb);
        result.load(in);
        return result;
    }

    private Connection createConnection(Properties properties) throws SQLException, ClassNotFoundException {
        Class.forName(properties.getProperty("driver"));
        return DriverManager.getConnection(properties.getProperty("url"),
                properties.getProperty("userName"),
                properties.getProperty("password"));
    }


    private Properties readFileProperties() throws IOException {
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

    public List<NetworkDefinition> getNetworks() {
        String sql = getQuery("network.list.get");
        List<NetworkDefinition> tmp = new ArrayList<>();
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
        String sql = getQuery("network.details.insert");
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
        String sql = getQuery("max.date.get");
        try{
            PreparedStatement pstmt = con.prepareStatement(sql);
            result = doWithStatement(pstmt, rs -> {
                Date result1 = null;
                if (rs.next()){
                    Timestamp t = rs.getTimestamp(1);
                    if (t != null){
                        result1 = new Date();
                        result1.setTime(t.getTime());
                    }
                }
                return result1;
            });
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
        String sql = getQuery("neflow.details.insert");
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


    private Collection<AggregationRecord> askForData(final Integer clientId) throws SQLException {
        String sql = getQuery("aggregations.get");
        PreparedStatement pst = con.prepareStatement(sql);
        pst.setInt(1, clientId);
        return doWithStatement(pst, set -> {
            Collection<AggregationRecord> result = new LinkedList<>();
            while(set.next()){
                AggregationRecord ar = new AggregationRecord(clientId, set.getTimestamp(1), set.getLong(2), set.getLong(3));
                result.add(ar);
            }
            return result;
        });
    }

    public void doAggregation(){
        //todo: the same for doAggregation(Date)
        String sql = getQuery("aggregation.insert");
        String logStr = "doAggregation(): ";
        log.info(logStr + " <<<<");
        try{
            List<Integer> clients = getNetworkedClients();
            PreparedStatement pst = con.prepareStatement(sql);

            for (Integer client : clients) {
                Collection<AggregationRecord> records = askForData(client);
                for (AggregationRecord record : records) {
                    pst.setInt(1, record.getClientId());
                    pst.setTimestamp(2, record.getStamp());
                    pst.setLong(3, record.getInput());
                    pst.setLong(4, record.getOutput());
                    pst.addBatch();
                }
            }

            pst.executeBatch();
            pst.close();
        } catch (SQLException e) {
            log.error(logStr + " Aggregation error: " + e.getMessage());
            e.printStackTrace(System.err);
        }
        log.info(logStr + " >>>>");
    }

    public void doAggregation(Date date){
        if (date == null){
            doAggregation();
            return;
        }

        String logStr = "doAggregation(): ";
        Timestamp start = Utils.getStartDate(date);
        Timestamp end = Utils.getEndDate(date);
        try{
            //todo: optimize as per ticket #21
            String sql = getQuery("aggregation.bydate.insert");
            log.info(logStr + " <<<<");
            List<Integer> clients = getNetworkedClients();
            PreparedStatement pstmt = con.prepareStatement(sql);
            for (Integer client : clients) {
                log.debug("Client " + client);
                start = getStartTimestamp(start, end, client);
                pstmt.setTimestamp(1, start);
                pstmt.setTimestamp(2, end);
                pstmt.setInt(3, client);
                log.debug("Minutes aggregation");
                pstmt.executeUpdate();
                log.debug("Minutes aggregation done");
            }
            pstmt.close();
        } catch (SQLException e) {
            log.error(logStr + " Aggregation error: " + e.getMessage());
            e.printStackTrace(System.err);
        }
        log.info(logStr + " >>>>");
    }

    private Timestamp getStartTimestamp(Timestamp start, Timestamp end, Integer client) {
        Timestamp result = null;
        log.debug("Getting real start ts");
        String maxDate = getQuery("start.timestamp.get");
        try{
            PreparedStatement pst = con.prepareStatement(maxDate);
            pst.setTimestamp(1, start);
            pst.setTimestamp(2, end);
            pst.setInt(3, client);

            result = doWithStatement(pst, rs -> {
                if (rs.next())
                    return rs.getTimestamp(1);
                else
                    return null;
            });

        } catch (SQLException e){
            log.error(" Aggregation error: " + e.getMessage());
            e.printStackTrace(System.err);
        }

        if (result == null){
            log.debug("Impossible to find start within interval: " + start + " " + end);
            result = start;
        }
        log.debug("Real start is: " + result);
        return result;
    }

    private boolean hasRecord(Timestamp dat, String host, Integer networkId) {
        boolean result = false;
        try {
            PreparedStatement pstmt = con.prepareStatement(getQuery("details.exists"));
            pstmt.setTimestamp(1, dat);
            pstmt.setString(2, host);
            pstmt.setInt(3, networkId);
            return doWithStatement(pstmt, ResultSet::next);
        } catch (SQLException e) {
            log.error("Query failed: " + e.getMessage());
        }
        return result;
    }

    public void doDailyAggregation(){
        log.debug("doDailyAggregation(): <<<<");
        try{
            List<AggregationRecord> results = getAggregationResults();
            aggregateResults(results);
        } catch (SQLException e) {
           log.error("Query falied: " + e.getMessage());
       }
        log.debug("doDailyAggregation(): >>>>");
    }

    public void doDailyAggregation(Date d){
        if (d == null) {
            doDailyAggregation();
        }
        log.debug("doDailyAggregation(): <<<<");
        try{
            List<AggregationRecord> results = getAggregationResults(d);
            aggregateResults(results);
        } catch (SQLException e) {
           log.error("Query falied: " + e.getMessage());
       }
        log.debug("doDailyAggregation(): >>>>");
    }

    private void aggregateResults(List<AggregationRecord> results) throws SQLException {
        List<AggregationRecord> toInsert = new ArrayList<>();
        List<AggregationRecord> toUpdate = new ArrayList<>();
        for (AggregationRecord result : results) {
            if (aggregationAlreadyStored(result)) {
                toUpdate.add(result);
            } else {
                toInsert.add(result);
            }
        }
        addAggregationResults(toInsert);
        updateAggregationResults(toUpdate);
    }

    private void updateAggregationResults(List<AggregationRecord> records) throws SQLException {
        if (records.isEmpty()){
            log.debug("Nothing to update");
            return;
        }
        log.debug("updateAggregationResults(): <<<<");
        log.debug(records.size() + " to update");
        PreparedStatement pstmt = con.prepareStatement(getQuery("aggregation.summary.update"));
        for (AggregationRecord record : records) {
            pstmt.setLong(1, record.getInput());
            pstmt.setLong(2, record.getOutput());
            pstmt.setInt(3, record.getClientId());
            pstmt.setDate(4, record.getDate());
            pstmt.addBatch();
        }
        final int[] ints = pstmt.executeBatch();
        log.debug(ints.length + " records updated");
        log.debug("updateAggregationResults(): >>>>");

    }

    private void addAggregationResults(List<AggregationRecord> records) throws SQLException {
        if (records.isEmpty()){
            log.debug("Nothing to insert");
            return;
        }

        log.debug("insertAggregationResults(): <<<<");
        log.debug(records.size() + " to insert");
        PreparedStatement pstmt = con.prepareStatement(getQuery("aggregation.summary.insert"));
        for (AggregationRecord record : records) {
            pstmt.setLong(1, record.getInput());
            pstmt.setLong(2, record.getOutput());
            pstmt.setInt(3, record.getClientId());
            pstmt.setDate(4, record.getDate());
            pstmt.addBatch();
        }
        final int[] ints = pstmt.executeBatch();
        log.debug(ints.length + " records inserted");
        log.debug("insertAggregationResults(): >>>>");
    }

    private List<AggregationRecord> getAggregationResults() throws SQLException {
        log.debug("getAggregationResults(): <<<");
        List<Integer> clients = getNetworkedClients();
        String collect = getQuery("aggregation.results.get");

        PreparedStatement ps = con.prepareStatement(collect);

        List<AggregationRecord> results = new ArrayList<>();
        for (Integer id : clients){
          ps.setInt(1, id);
          ResultSet rs = ps.executeQuery();
          if (rs.next()){
            results.add(new AggregationRecord(rs.getInt(1), rs.getDate(2), rs.getLong(3), rs.getLong(4)));
          }
          rs.close();
        }
        ps.close();
        log.debug("getAggregationResults(): >>>");
        return results;
    }

    private List<Integer> getNetworkedClients() throws SQLException {
        log.debug("Getting user list");
        String unq = getQuery("clients.ids.get");
        PreparedStatement pst = con.prepareStatement(unq);
        return doWithStatement(pst, rs -> {
            List<Integer> clients = new ArrayList<Integer>();
            while (rs.next()) {
                clients.add(rs.getInt(1));
            }
            return clients;
        });
    }

    private List<AggregationRecord> getAggregationResults(Date date) throws SQLException {
        if (date == null){
            return getAggregationResults();
        }
        log.debug("getAggregationResults(date): <<<");
        log.debug("Getting user list");

        Timestamp start = Utils.getStartDate(date);
        Timestamp end = Utils.getEndDate(date);
        log.debug("Parameters: " + start + ", " + end);

        List<Integer> clients = getNetworkedClients();
        String collect = getQuery("aggregations.forday.get");
        PreparedStatement ps = con.prepareStatement(collect);

        final List<AggregationRecord> results = new ArrayList<>();
        for (Integer id : clients){
            ps.setTimestamp(1, start);
            ps.setTimestamp(2, end);
            ps.setInt(3, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                results.add(new AggregationRecord(rs.getInt(1), rs.getDate(2), rs.getLong(3), rs.getLong(4)));
            }
            rs.close();
        }
        ps.close();
        log.debug("getAggregationResults(): >>>");
        return results;
    }


    private boolean aggregationAlreadyStored(AggregationRecord record) throws SQLException {
       String query = getQuery("aggregation.record.exists");
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, record.getClientId());
        ps.setDate(2, record.getDate());
        return doWithStatement(ps, ResultSet::first);
    }

    private<T> T doWithStatement(PreparedStatement statement, ResultSetProcessor<T> processor) throws SQLException {
        ResultSet rs = null;
        try {
            rs = statement.executeQuery();
            return processor.process(rs);
        } finally {
            try {
            if (rs != null) {
                rs.close();
            }
            statement.close();
            } catch (SQLException e) {
                log.error("SQL Exception while cleaning resources", e);
            }
        }
    }

    public void close(){
        try{
            con.close();
        } catch (SQLException e) {
            e.printStackTrace(System.err);
        }
    }

    private class AggregationRecord {
        private int clientId;
        private java.sql.Date date;
        private java.sql.Timestamp stamp;
        private long input;
        private long output;

        public AggregationRecord(int clientId, java.sql.Date date, long input, long output) {
            this.clientId = clientId;
            this.date = date;
            this.input = input;
            this.output = output;
        }

        public AggregationRecord(int clientId, Timestamp date, long input, long output) {
            this.clientId = clientId;
            this.stamp = date;
            this.input = input;
            this.output = output;
        }

        public int getClientId() {
            return clientId;
        }

        public java.sql.Date getDate() {
            return date;
        }

        public long getInput() {
            return input;
        }

        public long getOutput() {
            return output;
        }

        public Timestamp getStamp() {
            return stamp;
        }

        public void setStamp(Timestamp stamp) {
            this.stamp = stamp;
        }
    }

    interface ResultSetProcessor<T> {
        T process(ResultSet rs) throws SQLException;
    }
}
