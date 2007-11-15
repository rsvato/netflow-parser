/*
 * Main.java
 *
 * Created on 21 Март 2005 г., 11:17
 */

package netflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author slava
 */
public class Main {

    private static final Log log = LogFactory.getLog(Main.class);

    /**
     * @param args the command line arguments
     * @throws java.io.IOException when file not exists
     */
    public static void main(String[] args) throws IOException{
        if (args.length < 1) {
            System.err.println("Usage: netflow.Main <filename>");
            System.exit(1);
        }
        String fileName = args[0];
        String property = "false";
        try {
            property = System.getProperty("process.all");
        } catch (NullPointerException e) {
            log.info("Variable not defined. Assuming to false");
        }
        boolean processAllFile = Boolean.valueOf(property);
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        int lines = 0;
        int comments = 0;
        int goodLines = 0;
        int oldlines = 0;
        String line;
        DateFormat df = new SimpleDateFormat("HH:mm:ss EEE dd MMM yyyy",
                Locale.ENGLISH);
        long now = System.currentTimeMillis();
        Date guard = new java.util.Date();
        log.info("Begin process " + guard);
        Date last = DatabaseProxy.getInstance().getMaxDate();
        if (last == null) {
            log.debug("Date empty. Creating a new one");
            last = new java.util.Date();
            last.setTime(0L);
        }
        Date newDate = guard;
        log.info(last);
        LineProcessor processor = new LineProcessor();
        while ((line = reader.readLine()) != null) {
            if (!line.startsWith("#")) {
                if (last.before(newDate) || processAllFile) {
                    String[] elements = line.split("\\s+");
                    processor.parseLine(elements);
                    goodLines++;
                } else {
                    oldlines++;
                }
            } else {
                comments++;
                if (line.startsWith("#Time")) {
                    newDate = LineProcessor.parseTime(line, df);
                    log.info("Saving hosts " + newDate);
                }
                HostCache cache = HostCache.getInstance();
                if (line.startsWith("#EndData")
                        && !cache.isEmpty() && newDate != null && !newDate.equals(guard)) {
                    log.info("Saving hosts (finish processing) " + newDate);
                    cache.save(newDate);
                }
            }
            lines++;
        }
        DatabaseProxy.getInstance().doAggregation();
        DatabaseProxy.getInstance().close();
        now = System.currentTimeMillis() - now;
        log.info(lines + " Comments: " + comments + ", Effective lines: " + goodLines + ", Old lines:" + oldlines);
        log.info("Total processing: " + now + " ms");

    }

}
