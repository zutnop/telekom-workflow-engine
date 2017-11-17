package ee.telekom.workflow.graph;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;

import ee.telekom.workflow.graph.core.EnvironmentImpl;
import ee.telekom.workflow.graph.core.GraphEngineImpl;
import ee.telekom.workflow.graph.node.event.CatchSignal;
import ee.telekom.workflow.graph.node.event.CatchTimer;

public abstract class AbstractGraphTest{

    protected static List<String> EVENTS_0WI = Collections.unmodifiableList( Arrays.asList(
            RecordEventsListener.INSTANCE_CREATED,
            RecordEventsListener.INSTANCE_STARTED,
            RecordEventsListener.INSTANCE_COMPLETED ) );

    protected static List<String> EVENTS_1WI = Collections.unmodifiableList( Arrays.asList(
            RecordEventsListener.INSTANCE_CREATED,
            RecordEventsListener.WORK_ITEM_CREATED + " 1",
            RecordEventsListener.INSTANCE_STARTED,
            RecordEventsListener.WORK_ITEM_COMPLETED + " 1",
            RecordEventsListener.INSTANCE_COMPLETED ) );

    protected static List<String> EVENTS_2WI_SERIAL = Collections.unmodifiableList( Arrays.asList(
            RecordEventsListener.INSTANCE_CREATED,
            RecordEventsListener.WORK_ITEM_CREATED + " 1",
            RecordEventsListener.INSTANCE_STARTED,
            RecordEventsListener.WORK_ITEM_COMPLETED + " 1",
            RecordEventsListener.WORK_ITEM_CREATED + " 1",
            RecordEventsListener.WORK_ITEM_COMPLETED + " 1",
            RecordEventsListener.INSTANCE_COMPLETED ) );

    protected static List<String> EVENTS_2WI_PARALLEL = Collections.unmodifiableList( Arrays.asList(
            RecordEventsListener.INSTANCE_CREATED,
            RecordEventsListener.WORK_ITEM_CREATED + " 2",
            RecordEventsListener.WORK_ITEM_CREATED + " 3",
            RecordEventsListener.INSTANCE_STARTED,
            RecordEventsListener.WORK_ITEM_COMPLETED + " 2",
            RecordEventsListener.WORK_ITEM_COMPLETED + " 3",
            RecordEventsListener.INSTANCE_COMPLETED ) );

    protected void assertConditionalExecution( Graph graph, String testValue, String expectedPath ){
        EnvironmentImpl env = createSingletonEnvironment( GraphFactory.CONDITION_TEST_ATTRIBUTE, testValue );
        EnvironmentImpl expectedEnv = createSingletonEnvironment( GraphFactory.CONDITION_TEST_ATTRIBUTE, testValue );
        expectedEnv.setAttribute( RecordPathScript.ATTRIBUTE, expectedPath );
        assertExecution( graph, env, expectedEnv );
    }

    protected void assertLoopExecution( Graph graph, long loopCount, String expectedPath ){
        EnvironmentImpl expectedEnv = createSingletonEnvironment( "executionCount", loopCount );
        expectedEnv.setAttribute( RecordPathScript.ATTRIBUTE, expectedPath );
        assertExecution( graph, null, expectedEnv );
    }

    protected void assertExecution( Graph graph, String expectedPath ){
        EnvironmentImpl expectedEnv = createSingletonEnvironment( RecordPathScript.ATTRIBUTE, expectedPath );
        assertExecution( graph, null, expectedEnv );
    }

    protected void assertExecution( Graph graph, Environment initialEnv, EnvironmentImpl expectedEnv ){
        RecordEventsListener listener = new RecordEventsListener();
        GraphEngine engine = createEngine( listener );

        GraphInstance instance = engine.start( graph, initialEnv );

        assertActiveTokens( instance, 0 );
        assertActiveWorkItems( instance, 0 );
        Assert.assertTrue( instance.isCompleted() );
        assertEnvironmnent( instance, expectedEnv );
        Assert.assertEquals( EVENTS_0WI, listener.getEvents() );
    }

    protected void assertPath( GraphInstance instance, String expectedPath ){
        String actualPath = (String)instance.getEnvironment().getAttribute( RecordPathScript.ATTRIBUTE );
        Assert.assertEquals( expectedPath, actualPath );
    }

    protected void assertActiveTokens( GraphInstance instance, int count ){
        Assert.assertEquals( count, instance.getActiveTokens().size() );
    }

    protected void assertActiveWorkItems( GraphInstance instance, int count ){
        Assert.assertEquals( count, getActiveWorkItems( instance ).size() );
    }

    protected void assertEnvironmnent( GraphInstance instance, Environment expectedEnv ){
        assertEnvironmnent( instance.getEnvironment(), expectedEnv );
    }

    protected void assertEnvironmnent( Environment actualEnv, Environment expectedEnv ){
        // Iterating both ways to ensure that both environments contain the same attributes!
        for( String name : expectedEnv.getAttributeNames() ){
            Object expected = expectedEnv.getAttribute( name );
            Object actual = actualEnv.getAttribute( name );
            Assert.assertTrue( "Attribute with name " + name + " not equal. Expected: " + expected + ", Actual: " + actual, equals( expected, actual ) );
        }
        for( String name : actualEnv.getAttributeNames() ){
            Object expected = expectedEnv.getAttribute( name );
            Object actual = actualEnv.getAttribute( name );
            Assert.assertTrue( "Attribute with name " + name + " not equal. Expected: " + expected + ", Actual: " + actual, equals( expected, actual ) );
        }
    }

    protected void assertAwaitingSignal( Token token, String signal ){
        Assert.assertTrue( token.isActive() );
        Assert.assertTrue( token.getNode() instanceof CatchSignal );

        GraphWorkItem wi = token.getInstance().getActiveWorkItem( token );

        Assert.assertEquals( signal, wi.getSignal() );
        Assert.assertEquals( WorkItemStatus.NEW, wi.getStatus() );
    }

    protected void assertAwaitingTimer( Token token, long delayInMs ){
        Assert.assertTrue( token.isActive() );
        Assert.assertTrue( token.getNode() instanceof CatchTimer );

        GraphWorkItem wi = token.getInstance().getActiveWorkItem( token );

        // Asserting that the timer is due after expected delay +/- 10 seconds.
        Date min = new Date( System.currentTimeMillis() + delayInMs - 10000 );
        Date max = new Date( System.currentTimeMillis() + delayInMs + 10000 );
        Assert.assertTrue( min.before( wi.getDueDate() ) );
        Assert.assertTrue( max.after( wi.getDueDate() ) );
        Assert.assertEquals( WorkItemStatus.NEW, wi.getStatus() );
    }

    protected void completeWorkItemOnToken( GraphEngineFacade engine, Token token ){
        GraphWorkItem wi = token.getInstance().getActiveWorkItem( token );
        engine.complete( wi );
    }

    private boolean equals( Object a, Object b ){
        if( a == null && b == null ){
            return true;
        }
        if( (a != null && b == null) || (a == null && b != null) ){
            return false;
        }
        if( !a.getClass().equals( b.getClass() ) ){
            return false;
        }
        if( a instanceof int[] ){
            return Arrays.equals( (int[])a, (int[])b );
        }
        else{ // Yes, there are other array types but those are not tested
            return a.equals( b );
        }
    }

    protected List<GraphWorkItem> getActiveWorkItems( GraphInstance instance ){
        List<GraphWorkItem> result = new LinkedList<>();
        for( GraphWorkItem wi : instance.getWorkItems() ){
            if( !WorkItemStatus.COMPLETED.equals( wi.getStatus() )
                    && !WorkItemStatus.CANCELLED.equals( wi.getStatus() ) ){
                result.add( wi );
            }
        }
        return result;
    }

    protected GraphEngineImpl createEngine( RecordEventsListener listener ){
        GraphEngineImpl engine = new GraphEngineImpl();
        engine.registerInstanceEventListener( listener );
        engine.registerWorkItemEventListener( listener );
        return engine;
    }

    protected EnvironmentImpl createSingletonEnvironment( String name, Object value ){
        EnvironmentImpl environment = new EnvironmentImpl();
        environment.setAttribute( name, value );
        return environment;
    }

    protected Token getTokenById( Collection<Token> tokens, long id ){
        for( Token token : tokens ){
            if( token.getId() == id ){
                return token;
            }
        }
        return null;
    }

}
