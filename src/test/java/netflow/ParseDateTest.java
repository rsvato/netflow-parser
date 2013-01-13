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

import junit.framework.TestCase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParsePosition;
import java.util.Date;
import java.util.Locale;

/**
 * @author slava
 * @version $Id $
 */
public class ParseDateTest extends TestCase {
    private static String date = "01:40:00 Mon 21 Mar 2005";

    public void testDateParse(){
        DateFormat df = new SimpleDateFormat("HH:mm:ss EEE dd MMM yyyy", Locale.ENGLISH);

        Date result = df.parse(date, new ParsePosition(0));
        System.out.println(result);
        assertNotNull(result);
    }

    public void testDateArgument(){
        Date d = Utils.parseArgument("2008-11-10");
        System.out.println(d);
        assertNotNull(d);
    }
}
