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
