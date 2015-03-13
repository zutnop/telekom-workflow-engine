package ee.telekom.workflow.core.workunit;

import java.io.IOException;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

/**
 * A work unit describes an action that the engine should execute.
 * <p>
 * A workflow instance refNum is guaranteed to be not null, because every work unit
 * is related to a workflow instance.<br>
 * The type is never null and defines the kind of action that is to be taken.<br>
 * The work item refNum may be null, if the {@link WorkType#START_WORKFLOW} and {@link WorkType#ABORT_WORKFLOW}.
 * These actions are not related to a particular work item.
 * <p>
 * It implements the {@link DataSerializable} interface because it is held in the
 * distributed HazelCast queue for load balancing among threads and cluster nodes. 
 *
 * @author Christian Klock
 */
public class WorkUnit implements DataSerializable{

    private static final long serialVersionUID = 7537222252639436638L;
    private Long woinRefNum;
    private WorkType type;
    private Long woitRefNum;

    public Long getWoinRefNum(){
        return woinRefNum;
    }

    public void setWoinRefNum( Long woinRefNum ){
        this.woinRefNum = woinRefNum;
    }

    public WorkType getType(){
        return type;
    }

    public void setType( WorkType type ){
        this.type = type;
    }

    public Long getWoitRefNum(){
        return woitRefNum;
    }

    public void setWoitRefNum( Long woitRefNum ){
        this.woitRefNum = woitRefNum;
    }

    @Override
    public void writeData( ObjectDataOutput objectDataOutput ) throws IOException{
        objectDataOutput.writeLong( woinRefNum );
        objectDataOutput.writeUTF( type.toString() );
        objectDataOutput.writeLong( woitRefNum == null ? 0 : woitRefNum );
    }

    @Override
    public void readData( ObjectDataInput objectDataInput ) throws IOException{
        woinRefNum = objectDataInput.readLong();
        type = WorkType.valueOf( objectDataInput.readUTF() );
        woitRefNum = objectDataInput.readLong();
        if( woitRefNum == 0 ){
            woitRefNum = null;
        }
    }

    @Override
    public String toString(){
        return type.getDescription() + ":" + woinRefNum + (woitRefNum != null ? "/" + woitRefNum : "");
    }
}
