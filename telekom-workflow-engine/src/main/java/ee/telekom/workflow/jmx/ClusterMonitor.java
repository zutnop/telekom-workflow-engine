package ee.telekom.workflow.jmx;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import ee.telekom.workflow.core.common.WorkflowEngineConfiguration;
import ee.telekom.workflow.core.lock.LockService;
import ee.telekom.workflow.core.node.Node;
import ee.telekom.workflow.core.node.NodeService;

@Component(ClusterMonitor.BEAN)
@ManagedResource
public class ClusterMonitor{

    // The bean's name is used to reference the bean in an XML application context file.
    // Therefore, we explicitly set the bean name to a constant.
    public static final String BEAN = "clusterMonitor";

    @Autowired
    private WorkflowEngineConfiguration config;
    @Autowired
    private LockService lockService;
    @Autowired
    private NodeService nodeService;

    @ManagedAttribute(description = "Is master node")
    public boolean isMasterNode(){
        return lockService.isOwnLock();
    }

    @ManagedAttribute(description = "Master node name")
    public String getMasterNodeName(){
        String lockOwner = lockService.getLockOwner();
        return lockOwner == null ? "null" : lockOwner;
    }

    @ManagedAttribute(description = "Master lock expiration date")
    public String getMasterLockExpireDate(){
        return formatDate( lockService.getLockExpireDate() );
    }

    @ManagedAttribute(description = "Node status")
    public String getNodeStatus(){
        return nodeService.findOrCreateByName( config.getNodeName() ).getStatus().name();
    }

    @ManagedAttribute(description = "List of all cluster nodes")
    public String getAllClusterNodes(){
        StringBuilder sb = new StringBuilder();
        sb.append( "cluster=" + config.getClusterName() + ", nodes={" );
        for( Iterator<Node> it = nodeService.findAllClusterNodes().iterator(); it.hasNext(); ){
            Node node = it.next();
            sb.append( "[" + node.getNodeName() + ", " + node.getStatus().name() + ", " + formatDate( node.getHeartbeat() ) + "]" );
            if( it.hasNext() ){
                sb.append( ", " );
            }
        }
        sb.append( "}" );
        return sb.toString();
    }

    private String formatDate( Date date ){
        return date == null ? "null" : new SimpleDateFormat( "dd.MM.yyyy HH:mm:ss.S" ).format( date );
    }

}
