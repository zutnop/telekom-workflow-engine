package ee.telekom.workflow.core.node;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ee.telekom.workflow.core.common.WorkflowEngineConfiguration;

@Service
@Transactional
public class NodeServiceImpl implements NodeService{

    @Autowired
    private NodeDao dao;
    @Autowired
    private WorkflowEngineConfiguration config;

    private Node create( String nodeName ){
        Node node = new Node();
        node.setNodeName( nodeName );
        node.setClusterName( config.getClusterName() );
        node.setStatus( NodeStatus.ENABLE );
        node.setHeartbeat( new Date() );
        dao.create( node );
        return node;
    }

    @Override
    public Node findOrCreateByName( String nodeName ){
        Node node = dao.findByName( config.getClusterName(), nodeName );
        if( node != null ){
            return node;
        }
        else{
            return create( nodeName );
        }
    }

    @Override
    public List<Node> findAllClusterNodes(){
        return dao.findAll( config.getClusterName() );
    }

    @Override
    public void updateHeartbeat( String nodeName ){
        dao.updateHeartbeat( nodeName, new Date() );
    }

    @Override
    public void markEnabled( long refNum ){
        dao.updateStatus( refNum, NodeStatus.ENABLED );
    }

    @Override
    public void markEnable( List<String> nodes ){
        for( String nodeName : nodes ){
            Node node = dao.findByName( config.getClusterName(), nodeName );
            dao.updateStatus( node.getRefNum(), NodeStatus.ENABLE );
        }
    }

    @Override
    public void markEnable( long refNum ){
        dao.updateStatus( refNum, NodeStatus.ENABLE );
    }

    @Override
    public void markDisabled( long refNum ){
        dao.updateStatus( refNum, NodeStatus.DISABLED );
    }

    @Override
    public void markDisable( long refNum ){
        dao.updateStatus( refNum, NodeStatus.DISABLE );
    }

    @Override
    public void markFailed( long refNum ){
        dao.updateStatus( refNum, NodeStatus.FAILED );
    }

    @Override
    public void markDeadNodesFailed(){
        Date criticalDate = getCriticalDate();
        dao.updateStatusWhereDead( config.getClusterName(), criticalDate, NodeStatus.FAILED, NodeStatus.ENABLED );
    }

    @Override
    public void doHeartBeat(){
        String nodeName = config.getNodeName();
        dao.updateHeartbeat( nodeName, new Date() );
    }

    @Override
    public boolean isAlive( String nodeName ){
        Node node = dao.findByName( config.getClusterName(), nodeName );
        Date criticalDate = getCriticalDate();
        return node != null && node.getHeartbeat().after( criticalDate );
    }

    @Override
    public List<String> findFailedNodes(){
        List<Node> nodes = dao.findByStatus( config.getClusterName(), NodeStatus.FAILED );
        List<String> result = new ArrayList<>();
        for( Node node : nodes ){
            result.add( node.getNodeName() );
        }
        return result;
    }

    private Date getCriticalDate(){
        return DateUtils.addSeconds( new Date(), -config.getHeartbeatMaximumPauseSeconds() );

    }

}
