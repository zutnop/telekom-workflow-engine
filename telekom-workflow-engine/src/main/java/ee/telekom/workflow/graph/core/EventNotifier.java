package ee.telekom.workflow.graph.core;

import java.util.LinkedList;
import java.util.List;

import ee.telekom.workflow.graph.GraphInstance;
import ee.telekom.workflow.graph.GraphInstanceEventListener;
import ee.telekom.workflow.graph.GraphWorkItem;
import ee.telekom.workflow.graph.GraphWorkItemEventListener;
import ee.telekom.workflow.graph.Node;
import ee.telekom.workflow.graph.NodeEventListener;
import ee.telekom.workflow.graph.Token;

public class EventNotifier{

    private List<GraphInstanceEventListener> instanceEventListeners = new LinkedList<>();
    private List<GraphWorkItemEventListener> workItemEventListeners = new LinkedList<>();
    private List<NodeEventListener> nodeEventListeners = new LinkedList<>();

    public void registerInstanceEventListener( GraphInstanceEventListener listener ){
        instanceEventListeners.add( listener );
    }

    public void registerWorkItemEventListener( GraphWorkItemEventListener listener ){
        workItemEventListeners.add( listener );
    }

    public void registerNodeEventListener( NodeEventListener listener ){
        nodeEventListeners.add( listener );
    }

    public void fireCreated( GraphInstance instance ){
        for( GraphInstanceEventListener listener : instanceEventListeners ){
            listener.onCreated( instance );
        }
    }

    public void fireStarted( GraphInstance instance ){
        for( GraphInstanceEventListener listener : instanceEventListeners ){
            listener.onStarted( instance );
        }
    }

    public void fireAborting( GraphInstance instance ){
        for( GraphInstanceEventListener listener : instanceEventListeners ){
            listener.onAborting( instance );
        }
    }

    public void fireAborted( GraphInstance instance ){
        for( GraphInstanceEventListener listener : instanceEventListeners ){
            listener.onAborted( instance );
        }
    }

    public void fireCompleted( GraphInstance instance ){
        for( GraphInstanceEventListener listener : instanceEventListeners ){
            listener.onCompleted( instance );
        }
    }

    public void fireCreated( GraphWorkItem workItem ){
        for( GraphWorkItemEventListener listener : workItemEventListeners ){
            listener.onCreated( workItem );
        }
    }

    public void fireCancelled( GraphWorkItem workItem ){
        for( GraphWorkItemEventListener listener : workItemEventListeners ){
            listener.onCancelled( workItem );
        }
    }

    public void fireCompleted( GraphWorkItem workItem ){
        for( GraphWorkItemEventListener listener : workItemEventListeners ){
            listener.onCompleted( workItem );
        }
    }

    public void fireEntering( Token token, Node node ){
        for( NodeEventListener listener : nodeEventListeners ){
            listener.onEntering( token, node );
        }
    }

    public void fireLeft( Token token, Node node ){
        for( NodeEventListener listener : nodeEventListeners ){
            listener.onLeft( token, node );
        }
    }

}
