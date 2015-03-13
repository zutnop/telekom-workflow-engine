package ee.telekom.workflow.facade.util;

import java.util.ArrayList;
import java.util.List;

public class SqlUtil{

    public static List<List<Long>> partition( List<Long> all, int partitionSize ){
        List<List<Long>> partitions = new ArrayList<>();
        final int count = all.size();
        for( int i = 0; i < count; i += partitionSize ){
            partitions.add( new ArrayList<>( all.subList( i, Math.min( count, i + partitionSize ) ) ) );
        }
        return partitions;
    }

}
