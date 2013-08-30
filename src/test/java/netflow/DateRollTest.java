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

import junit.framework.TestCase;

import java.util.Date;
import java.sql.Timestamp;

/**
 * @author Svyatoslav Reyentenko
 */
public class DateRollTest extends TestCase {
    public void testDateRoll(){
       Date toTest = new Date();
        Timestamp timestamp = Utils.getStartDate(toTest);
        Timestamp date = Utils.getEndDate(toTest);
        System.out.println(timestamp + " " + date);
        assertTrue("Start should be earlier than end", timestamp.before(date));
    }
}
