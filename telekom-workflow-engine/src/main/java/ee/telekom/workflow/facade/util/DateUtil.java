package ee.telekom.workflow.facade.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil{

    public static String formatDate( Date date ){
        return date == null ? null : new SimpleDateFormat( "dd.MM.yyyy HH:mm:ss:SSS" ).format( date );
    }

    public static Date min( Date date1, Date date2 ){
        if( date1 == null ){
            return date2;
        }
        if( date2 == null ){
            return date1;
        }
        return date1.before( date2 ) ? date1 : date2;
    }

}
