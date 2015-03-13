package ee.telekom.workflow.graph;

import java.util.LinkedList;
import java.util.List;

public class RecordEventsListener
        implements
        GraphInstanceEventListener,
        GraphWorkItemEventListener,
        NodeEventListener{

    public static final String INSTANCE_CREATED = "instance_created";
    public static final String INSTANCE_STARTED = "instance_started";
    public static final String INSTANCE_ABORTING = "instance_aborting";
    public static final String INSTANCE_ABORTED = "instance_aborted";
    public static final String INSTANCE_COMPLETED = "instance_completed";

    public static final String WORK_ITEM_CREATED = "work_item_created";
    public static final String WORK_ITEM_CANCELLED = "work_item_cancelled";
    public static final String WORK_ITEM_COMPLETED = "work_item_completed";

    public static final String NODE_ENTERING = "entering";
    public static final String NODE_LEFT = "left";

    private List<String> events = new LinkedList<>();

    @Override
    public void onCreated( GraphInstance instance ){
        events.add( INSTANCE_CREATED );
    }

    @Override
    public void onStarted( GraphInstance instance ){
        events.add( INSTANCE_STARTED );
    }

    @Override
    public void onAborting( GraphInstance instance ){
        events.add( INSTANCE_ABORTING );
    }

    @Override
    public void onAborted( GraphInstance instance ){
        events.add( INSTANCE_ABORTED );
    }

    @Override
    public void onCompleted( GraphInstance instance ){
        events.add( INSTANCE_COMPLETED );
    }

    @Override
    public void onCreated( GraphWorkItem workItem ){
        events.add( WORK_ITEM_CREATED + " " + workItem.getToken().getId() );
    }

    @Override
    public void onCancelled( GraphWorkItem workItem ){
        events.add( WORK_ITEM_CANCELLED + " " + workItem.getToken().getId() );
    }

    @Override
    public void onCompleted( GraphWorkItem workItem ){
        events.add( WORK_ITEM_COMPLETED + " " + workItem.getToken().getId() );
    }

    @Override
    public void onEntering( Token token, Node node ){
        events.add( NODE_ENTERING + " " + node.getId() + " " + token.getId() );
    }

    @Override
    public void onLeft( Token token, Node node ){
        events.add( NODE_LEFT + " " + node.getId() + " " + token.getId() );
    }

    public List<String> getEvents(){
        return events;
    }
}
