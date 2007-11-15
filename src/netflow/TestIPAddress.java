package netflow;

public class TestIPAddress{
        public static void main(String[] args){
                if (args.length != 1){
                        System.out.println("Usage: netflow.TestIPAddress <ip-address>");
                        System.exit(1);
                }
                String ipToTest = args[0];
                LineProcessor processor = new LineProcessor();
                NetworkDefinition def = processor.netId(ipToTest);
                if (def != null){
                        System.out.println(def);
                }else{
                        System.out.println("Nothing found, sorry");
                }
        }
}
