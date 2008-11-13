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
