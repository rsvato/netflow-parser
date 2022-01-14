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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    /**
     * @param args the command line arguments
     * @throws java.io.IOException when netflow file not exists or no aggregation requested
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Usage: netflow.Main <filename> or netflow.Main -aggreg yyyy-MM-dd");
            System.exit(1);
        }

        DatabaseProxy dbProxy = new DatabaseProxy();
        if ("-aggreg".equals(args[0])) {
            if (args.length < 2) {
                System.err.println("Usage: netflow.Main -aggreg yyyy-MM-dd");
                System.exit(1);
            }
            doAggregation(Utils.parseArgument(args[1]), dbProxy);
        } else {
            String fileName = args[0];

            String property = System.getProperty("process.all", "false");
            boolean processAllFile = Boolean.parseBoolean(property);
            long now = importFile(dbProxy, fileName, processAllFile);

            String ag =  System.getProperty("netflow.doAggregation", "true");
            boolean doAggregation = Boolean.parseBoolean(ag);
            if (doAggregation) {
                doAggregation(null, dbProxy);
            }
            now = System.currentTimeMillis() - now;
            log.info("Total processing: " + now + " ms");
        }
        dbProxy.close();
    }

    private static void doAggregation(Date date, DatabaseProxy dbProxy) {
        dbProxy.doAggregation(date);
        dbProxy.doDailyAggregation(date);
    }

    private static long importFile(DatabaseProxy dbProxy, String fileName, boolean processAllFile) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        StringTokenizerParser p = new StringTokenizerParser();
        int lines = 0;
        int comments = 0;
        int goodLines = 0;
        int oldlines = 0;
        String line;
        DateFormat df = new SimpleDateFormat("HH:mm:ss EEE dd MMM yyyy", Locale.ENGLISH);
        long now = System.currentTimeMillis();
        Date guard = new Date();
        log.info("Begin process " + guard);
        Date last = dbProxy.getMaxDate();
        if (last == null) {
            log.info("Date empty. Creating a new one");
            last = new Date();
            last.setTime(0L);
        }
        Date newDate = guard;
        log.info("Last date: {}", last);

        HostCache cache = new HostCache(dbProxy);
        LineProcessor processor = new LineProcessor(cache, dbProxy.getNetworks());
        while ((line = reader.readLine()) != null) {
            if (!line.startsWith("#")) {
                if (last.before(newDate) || processAllFile) {
                    String[] elements = p.parseLine(line);
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

                if (line.startsWith("#EndData")
                        && !cache.isEmpty() && newDate != null && !newDate.equals(guard)) {
                    log.info("Saving hosts (finish processing) " + newDate);
                    cache.save(newDate);
                }
            }
            lines++;
        }
        log.info("Total lines: {}, comments: {}, effective lines: {}, old lines: {}", lines, comments, goodLines, oldlines);
        return now;
    }

}

class StringTokenizerParser {
    public String[] parseLine(String s) {
        StringTokenizer st = new StringTokenizer(s, " ");
        int size = st.countTokens();
        String[] result = new String[size];
        int i = 0;
        while (st.hasMoreTokens()) {
            result[i++] = st.nextToken();
        }
        return result;
    }
}
