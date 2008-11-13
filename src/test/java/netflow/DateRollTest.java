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
