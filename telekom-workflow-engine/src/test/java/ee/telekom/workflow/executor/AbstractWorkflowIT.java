package ee.telekom.workflow.executor;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ee.telekom.workflow.core.archive.ArchiveDao;
import ee.telekom.workflow.core.common.WorkflowEngineConfiguration;
import ee.telekom.workflow.core.error.ExecutionError;
import ee.telekom.workflow.core.error.ExecutionErrorDao;
import ee.telekom.workflow.core.node.NodeService;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstance;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceDao;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceService;
import ee.telekom.workflow.core.workflowinstance.WorkflowInstanceStatus;
import ee.telekom.workflow.core.workitem.WorkItem;
import ee.telekom.workflow.core.workitem.WorkItemDao;
import ee.telekom.workflow.core.workitem.WorkItemType;
import ee.telekom.workflow.core.workunit.WorkType;
import ee.telekom.workflow.core.workunit.WorkUnit;
import ee.telekom.workflow.core.workunit.WorkUnitService;
import ee.telekom.workflow.executor.marshall.Marshaller;
import ee.telekom.workflow.executor.marshall.TokenState;
import ee.telekom.workflow.executor.plugin.MockPlugin;
import ee.telekom.workflow.graph.GraphFactory;
import ee.telekom.workflow.graph.RecordPathScript;
import ee.telekom.workflow.graph.WorkItemStatus;
import ee.telekom.workflow.util.JsonUtil;

public class AbstractWorkflowIT{

    protected static final String SIGNAL = GraphFactory.SIGNAL;
    protected static final Object SIGNAL_RESULT = "s_result";

    protected static final int TIMER_MS = GraphFactory.TIMER_MS;

    protected static final String BEAN = GraphFactory.BEAN;
    protected static final String METHOD = GraphFactory.METHOD;
    protected static final Object[] TASK_ARGUMENTS = new Object[]{GraphFactory.ARGUMENT};
    protected static final Object TASK_RESULT = GraphFactory.RESULT;

    protected static final String ROLE = GraphFactory.ROLE;
    protected static final String USER = GraphFactory.USER;
    protected static final Map<String, Object> HUMAN_TASK_ARGUMENTS = Collections.singletonMap( GraphFactory.ARGUMENT_KEY, (Object)GraphFactory.ARGUMENT );
    protected static final Object HUMAN_TASK_RESULT = "ht_result";

    protected static final String RESULT_KEY = GraphFactory.RESULT_KEY;

    protected static final String LABEL1 = "label1";
    protected static final String LABEL2 = "label2";

    protected static final String INITIAL_ARGUMENT_KEY = "initial_argument";
    protected static final String INITIAL_ARGUMENT_VALUE = "initial_argument";
    protected static final Map<String, Object> INITIAL_ENV = Collections.singletonMap( INITIAL_ARGUMENT_KEY, (Object)INITIAL_ARGUMENT_VALUE );

    @Autowired
    protected WorkflowInstanceDao workflowInstanceDao;
    @Autowired
    protected WorkflowInstanceService workflowInstanceService;
    @Autowired
    protected WorkItemDao workItemDao;
    @Autowired
    protected ArchiveDao archiveDao;
    @Autowired
    protected ExecutionErrorDao executionErrorDao;
    @Autowired
    protected WorkUnitService workUnitService;
    @Autowired
    protected NodeService nodeService;
    @Autowired
    protected WorkflowEngineConfiguration config;
    private static Level executorLogLevel;

    protected void assertWoin( long refNum,
                               String name,
                               Integer version,
                               Map<String, Object> arguments,
                               boolean stateNull,
                               String label1,
                               String label2,
                               WorkflowInstanceStatus status,
                               String nodeName,
                               boolean locked
            ){
        WorkflowInstance woin;
        if( WorkflowInstanceStatus.EXECUTED.equals( status )
                || WorkflowInstanceStatus.ABORTED.equals( status ) ){
            woin = archiveDao.findWoinByRefNum( refNum );
            if( woin == null || workflowInstanceDao.findByRefNum( refNum ) != null ){
                Assert.fail( "Workflow instance not archived" );
            }
        }
        else{
            woin = workflowInstanceDao.findByRefNum( refNum );
            if( woin == null || archiveDao.findWoinByRefNum( refNum ) != null ){
                Assert.fail( "Workflow instance archived!" );
            }
        }
        Assert.assertEquals( refNum, (long)woin.getRefNum() );
        Assert.assertEquals( name, woin.getWorkflowName() );
        Assert.assertEquals( version, woin.getWorkflowVersion() );
        Assert.assertEquals( arguments, Marshaller.deserializeAttributes( woin.getAttributes() ) );
        Assert.assertEquals( stateNull, woin.getState() == null );
        Assert.assertEquals( label1, woin.getLabel1() );
        Assert.assertEquals( label2, woin.getLabel2() );
        Assert.assertEquals( status, woin.getStatus() );
        Assert.assertEquals( nodeName, woin.getNodeName() );
        Assert.assertEquals( locked, woin.isLocked() );
        if( WorkflowInstanceStatus.ABORTED.equals( status ) || WorkflowInstanceStatus.EXECUTED.equals( status ) ){
            Collection<TokenState> tokenStates = Marshaller.deserializeTokenStates( woin.getState() );
            if( tokenStates != null ){
                for( TokenState tokenState : tokenStates ){
                    Assert.assertFalse( tokenState.isActive() );
                }
            }
        }
    }

    protected void assertActiveWoitsCount( long woinRefNum, int count ){
        List<WorkItem> woits = workItemDao.findActiveByWoinRefNum( woinRefNum );
        Assert.assertEquals( count, woits.size() );
    }

    protected void assertSignalWoit( long refNum, long woinRefNum, int tokenId, WorkItemStatus status, String signal, Object result ){
        assertWoit( refNum, woinRefNum, tokenId, status, signal, null, null, null, null, null, null, result );
    }

    protected void assertTimerWoit( long refNum, long woinRefNum, int tokenId, WorkItemStatus status, int delayInMs ){
        assertWoit( refNum, woinRefNum, tokenId, status, null, delayInMs, null, null, null, null, null, null );
    }

    protected void assertTaskWoit( long refNum,
                                   long woinRefNum,
                                   int tokenId,
                                   WorkItemStatus status,
                                   String bean,
                                   String method,
                                   Object[] arguments,
                                   Object result ){
        assertWoit( refNum, woinRefNum, tokenId, status, null, null, bean, method, null, null, arguments, result );
    }

    protected void assertHumanTaskWoit( long refNum,
                                        long woinRefNum,
                                        int tokenId,
                                        WorkItemStatus status,
                                        String role,
                                        String userName,
                                        Map<String, Object> arguments,
                                        Object result ){
        assertWoit( refNum, woinRefNum, tokenId, status, null, null, null, null, role, userName, arguments, result );
    }

    protected void assertWoit( long refNum,
                               long woinRefNum,
                               int tokenId,
                               WorkItemStatus status,
                               String signal,
                               Integer delayInMs,
                               String bean,
                               String method,
                               String role,
                               String userName,
                               Object arguments,
                               Object result ){
        WorkItem woit = findInArchive( woinRefNum ) ? archiveDao.findWoitByRefNum( refNum ) : workItemDao.findByRefNum( refNum );
        Assert.assertEquals( refNum, (long)woit.getRefNum() );
        Assert.assertEquals( woinRefNum, (long)woit.getWoinRefNum() );
        Assert.assertEquals( tokenId, woit.getTokenId() );
        Assert.assertEquals( status, woit.getStatus() );
        Assert.assertEquals( signal, woit.getSignal() );
        if( delayInMs != null ){
            Date min = DateUtils.addMilliseconds( new Date(), -(int)delayInMs );
            Date max = DateUtils.addMilliseconds( new Date(), delayInMs );
            Assert.assertTrue( min.before( woit.getDueDate() ) );
            Assert.assertTrue( max.after( woit.getDueDate() ) );
        }
        Assert.assertEquals( bean, woit.getBean() );
        Assert.assertEquals( method, woit.getMethod() );
        Assert.assertEquals( role, woit.getRole() );
        Assert.assertEquals( userName, woit.getUserName() );
        if( WorkItemType.TASK.equals( woit.getType() ) ){
            Assert.assertArrayEquals( (Object[])arguments, JsonUtil.deserialize( woit.getArguments(), Object[].class ) );
        }
        else if( WorkItemType.HUMAN_TASK.equals( woit.getType() ) ){
            Assert.assertEquals( arguments, JsonUtil.deserializeHashMap( woit.getArguments(), String.class, Object.class ) );
        }
        Assert.assertEquals( result, JsonUtil.deserialize( woit.getResult() ) );
    }

    private boolean findInArchive( long woinRefNum ){
        if( workflowInstanceDao.findByRefNum( woinRefNum ) != null ){
            return false;
        }
        else if( archiveDao.findWoinByRefNum( woinRefNum ) != null ){
            return true;
        }
        else{
            Assert.fail( "Workflow instance not found!" );
            throw new RuntimeException();
        }
    }

    protected void assertExer( long woinRefNum, Long woitRefNum, String text ){
        ExecutionError exer = executionErrorDao.findByWoinRefNum( woinRefNum );
        Assert.assertNotNull( exer );
        Assert.assertEquals( woinRefNum, (long)exer.getWoinRefNum() );
        Assert.assertEquals( woitRefNum, exer.getWoitRefNum() );
        Assert.assertEquals( text, exer.getErrorText() );
        Assert.assertFalse( exer.getErrorDetails().isEmpty() );
    }

    protected void assertNoExer( long woinRefNum ){
        Assert.assertNull( executionErrorDao.findByWoinRefNum( woinRefNum ) );
    }

    protected long getActiveWoitRefNum( long woinRefNum, int tokenId ){
        return workItemDao.findActiveByWoinRefNumAndTokenId( woinRefNum, tokenId ).getRefNum();
    }

    /**
     * Simulates polling, locking assigning to a cluster node
     */
    protected void simulatePollLockAssign( long woinRefNum, WorkType type, Long woitRefNum ){
        Date now = new Date();
        simulatePollLockAssign( woinRefNum, type, woitRefNum, now );
    }

    protected void simulatePollLockAssign( long woinRefNum, WorkType type, Long woitRefNum, int timeOffset ){
        Date now = DateUtils.addMilliseconds( new Date(), timeOffset + 1000 );
        simulatePollLockAssign( woinRefNum, type, woitRefNum, now );
    }

    protected void simulatePollLockAssign( long woinRefNum, WorkType type, Long woitRefNum, Date now ){
        List<WorkUnit> newWorkUnits = workUnitService.findNewWorkUnits( now );
        WorkUnit wu = null;
        for( WorkUnit workUnit : newWorkUnits ){
            if( ObjectUtils.equals( workUnit.getWoinRefNum(), woinRefNum )
                    && ObjectUtils.equals( workUnit.getType(), type )
                    && ObjectUtils.equals( workUnit.getWoitRefNum(), woitRefNum ) ){
                wu = workUnit;
                break;
            }
        }
        Assert.assertNotNull( wu );
        workUnitService.lock( Collections.singletonList( wu ) );
        workflowInstanceService.updateNodeNameFromNull( wu.getWoinRefNum(), config.getNodeName() );
    }

    protected void sendSignal( long woitRefNum, Object result ){
        workItemDao.updateStatus( woitRefNum, WorkItemStatus.EXECUTED, Collections.singletonList( WorkItemStatus.NEW ) );
        workItemDao.updateResult( woitRefNum, JsonUtil.serialize( result, true ) );
    }

    protected void submitHumanTask( long woitRefNum, Object result ){
        workItemDao.updateStatus( woitRefNum, WorkItemStatus.EXECUTED, Collections.singletonList( WorkItemStatus.NEW ) );
        workItemDao.updateResult( woitRefNum, JsonUtil.serialize( result, true ) );
    }

    protected Map<String, Object> createMap( Object... data ){
        Map<String, Object> result = new HashMap<>();
        if( data == null ){
            return result;
        }
        for( int i = 0; i < data.length; i += 2 ){
            String key = (String)data[i];
            Object value = data[i + 1];
            result.put( key, value );
        }
        return result;
    }

    @BeforeClass
    public static void beforeClass(){
        Logger log = (Logger)LoggerFactory.getLogger( WorkflowExecutorImpl.class );
        executorLogLevel = log.getLevel();
        log.setLevel( Level.ERROR );
    }

    @AfterClass
    public static void afterClass(){
        Logger log = (Logger)LoggerFactory.getLogger( WorkflowExecutorImpl.class );
        log.setLevel( executorLogLevel );
    }

    @Before
    public void prepareTest(){
        nodeService.findOrCreateByName( config.getNodeName() );
    }

    @After
    public void cleanUpAfterTest(){
        MockPlugin.stopThrowingException();
        RecordPathScript.stopThrowingException();
    }

}
