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
