/**
 * Copyright (C) 2013 Svyatoslav Reyentenko <rsvato@gmail.com>
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
import java.io.*;

public class TestIPBase{
  public static void main(String[] args) throws IOException{
      if (args.length != 2) {
          System.err.println("Usage: netflow.TestIPBase <address to catch> <file>");
          System.exit(1);
      }
      String address = args[0];
      String fileName = args[1];
      BufferedReader reader = new BufferedReader(new FileReader(fileName));
      String line;
      LineProcessor processor = new LineProcessor();
      while ((line = reader.readLine()) != null) {
          if (!line.startsWith("#")) {
              if (line.indexOf(address) > -1) {
                  System.err.println(line);
                  processor.parseLine(line.split("\\s+"));
              }
          }
      }
  }
}
