package ee.telekom.workflow.api;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import ee.telekom.workflow.graph.GraphFactory;
import ee.telekom.workflow.graph.Node;
import ee.telekom.workflow.graph.core.GraphImpl;
import ee.telekom.workflow.graph.core.TransitionImpl;
import ee.telekom.workflow.graph.node.activity.BeanAsyncCallActivity;
import ee.telekom.workflow.graph.node.activity.BeanCallActivity;
import ee.telekom.workflow.graph.node.activity.HumanTaskActivity;
import ee.telekom.workflow.graph.node.activity.SetAttributeActivity;
import ee.telekom.workflow.graph.node.event.CatchSignal;
import ee.telekom.workflow.graph.node.event.CatchTimer;
import ee.telekom.workflow.graph.node.event.ThrowEscalation;
import ee.telekom.workflow.graph.node.gateway.AndFork;
import ee.telekom.workflow.graph.node.gateway.AndJoin;
import ee.telekom.workflow.graph.node.gateway.CancellingDiscriminator;
import ee.telekom.workflow.graph.node.gateway.OrFork;
import ee.telekom.workflow.graph.node.gateway.XorFork;
import ee.telekom.workflow.graph.node.gateway.XorJoin;
import ee.telekom.workflow.graph.node.gateway.condition.ExpressionLanguageCondition;
import ee.telekom.workflow.graph.node.input.ConstantMapping;
import ee.telekom.workflow.graph.node.input.InputMapping;
import ee.telekom.workflow.graph.node.input.MapMapping;
import ee.telekom.workflow.graph.node.output.MapEntryMapping;

public class WorkflowFactoryDslTest extends AbstractApiTest{

    @Test
    public void only_start_end(){
        WorkflowFactory factory = DslFactory.createWorkFlowFactory( "only_start_end", 1 );
        /* @formatter:off */
        factory
            .start()
            .end();
        /* @formatter:on */

        GraphImpl expected = new GraphImpl( "only_start_end", 1 );

        assertBuildsTo( factory, expected );
    }

    @Test
    public void only_variable(){
        WorkflowFactory factory = DslFactory.createWorkFlowFactory( "only_variable", 1 );
        /* @formatter:off */
        factory
            .start()
            .variable("status").value(1, true)
            .end();
        /* @formatter:on */

        GraphImpl expected = new GraphImpl( "only_variable", 1 );
        Node node1 = new SetAttributeActivity( 1, "status", true );
        expected.setStartNode( node1 );

        assertBuildsTo( factory, expected );
    }

    @Test
    public void only_variables(){
        WorkflowFactory factory = DslFactory.createWorkFlowFactory( "only_variables", 1 );
        /* @formatter:off */
           factory
            .start()
            .variables( "a=x", "b=y" ).call(1, "beanName", "methodName", "parameterName")
            .end();
        /* @formatter:on */

        GraphImpl expected = new GraphImpl( "only_variables", 1 );
        Map<String, String> outputMappings = new LinkedHashMap<>();
        // read as: take "x" from method call result map and store it into environment as "a"
        outputMappings.put( "x", "a" );
        outputMappings.put( "y", "b" );
        InputMapping<?>[] inputMappings = new InputMapping<?>[]{ConstantMapping.of( "parameterName" )};
        Node node1 = new BeanCallActivity( 1, "beanName", "methodName", inputMappings, new MapEntryMapping( outputMappings ) );
        expected.setStartNode( node1 );

        assertBuildsTo( factory, expected );
    }

    @Test
    public void only_call(){
        WorkflowFactory factory = DslFactory.createWorkFlowFactory( "only_call", 1 );
        /* @formatter:off */
        factory
            .start()
            .call( 1, "beanName", "methodName", "parameterName" )
            .end();
        /* @formatter:on */

        GraphImpl expected = new GraphImpl( "only_call", 1 );
        InputMapping<?>[] inputMappings = new InputMapping<?>[]{ConstantMapping.of( "parameterName" )};
        Node node1 = new BeanCallActivity( 1, "beanName", "methodName", inputMappings, null );
        expected.setStartNode( node1 );

        assertBuildsTo( factory, expected );
    }

    @Test
    public void only_call_async(){
        WorkflowFactory factory = DslFactory.createWorkFlowFactory( "only_call_async", 1 );
        /* @formatter:off */
        factory
            .start()
            .callAsync( 1, "beanName", "methodName", "parameterName" )
            .end();
        /* @formatter:on */

        GraphImpl expected = new GraphImpl( "only_call_async", 1 );
        InputMapping<?>[] inputMappings = new InputMapping<?>[]{ConstantMapping.of( "parameterName" )};
        Node node1 = new BeanAsyncCallActivity( 1, "beanName", "methodName", inputMappings, null );
        expected.setStartNode( node1 );

        assertBuildsTo( factory, expected );
    }

    @Test
    public void only_human_task(){
        WorkflowFactory factory = DslFactory.createWorkFlowFactory( "only_human_task", 1 );
        /* @formatter:off */
        factory
            .start()
            .humanTask(1, "roleName", "assignee" ).withAttribute( "reason", "reason").withAttribute( "taskId", "taskName").done()
            .end();
        /* @formatter:on */

        GraphImpl expected = new GraphImpl( "only_human_task", 1 );
        MapMapping argumentsMapping = new MapMapping();
        argumentsMapping.addEntryMapping( "reason", new ConstantMapping<Object>( "reason" ) );
        argumentsMapping.addEntryMapping( "taskId", new ConstantMapping<Object>( "taskName" ) );
        Node node1 = new HumanTaskActivity( 1, "roleName", "assignee", argumentsMapping, null );
        expected.setStartNode( node1 );

        assertBuildsTo( factory, expected );
    }

    @Test
    public void only_signal(){
        WorkflowFactory factory = DslFactory.createWorkFlowFactory( "only_signal", 1 );
        /* @formatter:off */
        factory
            .start()
            .waitSignal(1, "go")
            .end();
        /* @formatter:on */

        GraphImpl expected = new GraphImpl( "only_signal", 1 );
        Node node1 = new CatchSignal( 1, "go" );
        expected.setStartNode( node1 );

        assertBuildsTo( factory, expected );
    }

    @Test
    public void only_timer(){
        WorkflowFactory factory = DslFactory.createWorkFlowFactory( "only_timer", 1 );
        /* @formatter:off */
        factory
            .start()
            .waitTimer(1, "60000")
            .end();
        /* @formatter:on */

        GraphImpl expected = new GraphImpl( "only_timer", 1 );
        Node node1 = new CatchTimer( 1, 60000 );
        expected.setStartNode( node1 );

        assertBuildsTo( factory, expected );
    }

    @Test
    public void only_while_do(){
        WorkflowFactory factory = DslFactory.createWorkFlowFactory( "only_while_do", 1 );
        /* @formatter:off */
        factory
            .start()
            .whileDo(1, "condition1")
            .whileDo()
            .end();
        /* @formatter:on */

        GraphImpl expected = new GraphImpl( "only_while_do", 1 );
        XorFork fork = new XorFork( 1 );
        fork.addCondition( new ExpressionLanguageCondition( "condition1" ), "1_-1" );
        XorJoin join = new XorJoin( -1 );
        expected.setStartNode( fork );
        expected.addNode( join );
        expected.addTransition( new TransitionImpl( "1_-1", fork, join ) );
        expected.addTransition( new TransitionImpl( join, fork ) );

        assertBuildsTo( factory, expected );
    }

    @Test
    public void only_do_while(){
        WorkflowFactory factory = DslFactory.createWorkFlowFactory( "only_do_while", 1 );
        /* @formatter:off */
        factory
            .start()
            .doWhile()
            .doWhile(1, "condition1")
            .end();
        /* @formatter:on */

        GraphImpl expected = new GraphImpl( "only_do_while", 1 );
        XorJoin join = new XorJoin( -1 );
        XorFork fork = new XorFork( 1 );
        fork.addCondition( new ExpressionLanguageCondition( "condition1" ), "1_-1" );
        expected.setStartNode( join );
        expected.addNode( fork );
        expected.addTransition( new TransitionImpl( join, fork ) );
        expected.addTransition( new TransitionImpl( "1_-1", fork, join ) );

        assertBuildsTo( factory, expected );
    }

    @Test
    public void only_if_else(){
        WorkflowFactory factory = DslFactory.createWorkFlowFactory( "only_if_else", 1 );
        /* @formatter:off */
        factory
            .start()
            .if_(1, "condition1")
            .elseIf("condition2")
            .else_()
            .endIf()
            .end();
        /* @formatter:on */

        GraphImpl expected = new GraphImpl( "only_if_else", 1 );
        XorFork fork = new XorFork( 1 );
        fork.addCondition( new ExpressionLanguageCondition( "condition1" ), "1_-1" );
        fork.addCondition( new ExpressionLanguageCondition( "condition2" ), "1_-1" );
        fork.addCondition( null, "1_-1" );
        Node join = new XorJoin( -1 );
        expected.setStartNode( fork );
        expected.addNode( join );
        expected.addTransition( new TransitionImpl( "1_-1", fork, join ) );

        assertBuildsTo( factory, expected );
    }

    @Test
    public void only_and(){
        WorkflowFactory factory = DslFactory.createWorkFlowFactory( "only_and", 1 );
        /* @formatter:off */
        factory
            .start()
            .split(1)
                .branch()
            .joinAll()
            .end();
        /* @formatter:on */

        GraphImpl expected = new GraphImpl( "only_and", 1 );
        Node fork = new AndFork( 1 );
        Node join = new AndJoin( -1 );
        expected.setStartNode( fork );
        expected.addNode( join );
        expected.addTransition( new TransitionImpl( "1_-1", fork, join ) );

        assertBuildsTo( factory, expected );
    }

    @Test
    public void only_or(){
        WorkflowFactory factory = DslFactory.createWorkFlowFactory( "only_and", 1 );
        /* @formatter:off */
        factory
            .start()
            .split(1)
                .branch("condition1")
            .joinFirst()
            .end();
        /* @formatter:on */

        GraphImpl expected = new GraphImpl( "only_and", 1 );
        OrFork fork = new OrFork( 1 );
        fork.addCondition( new ExpressionLanguageCondition( "condition1" ), "1_-1" );
        Node join = new CancellingDiscriminator( -1 );
        expected.setStartNode( fork );
        expected.addNode( join );
        expected.addTransition( new TransitionImpl( "1_-1", fork, join ) );

        assertBuildsTo( factory, expected );
    }

    @Test
    public void only_escalate(){
        WorkflowFactory factory = DslFactory.createWorkFlowFactory( "only_and", 1 );
        /* @formatter:off */
        factory
            .start()
            .split(1)
                .branch()
                    .escalate(2)
            .joinFirst()
            .end();
        /* @formatter:on */

        GraphImpl expected = new GraphImpl( "only_and", 1 );
        Node fork = new AndFork( 1 );
        Node node1 = new ThrowEscalation( 2 );
        Node join = new CancellingDiscriminator( -1 );
        expected.setStartNode( fork );
        expected.addNode( node1 );
        expected.addNode( join );
        expected.addTransition( new TransitionImpl( "1_2", fork, node1 ) );
        expected.addTransition( new TransitionImpl( node1, join ) );

        assertBuildsTo( factory, expected );
    }

    ////////////////////////////////////////////////////////
    ////////////// GraphFactory tests START ////////////////
    ////////////////////////////////////////////////////////

    @Test
    public void sequence_one(){
        assertBuildsTo( DslFactory.sequence_one(), GraphFactory.INSTANCE.sequence_one() );
    }

    @Test
    public void sequence_two(){
        assertBuildsTo( DslFactory.sequence_two(), GraphFactory.INSTANCE.sequence_two() );
    }

    @Test
    public void sequence_three(){
        assertBuildsTo( DslFactory.sequence_three(), GraphFactory.INSTANCE.sequence_three() );
    }

    @Test
    public void synchronzation_one(){
        assertBuildsTo( DslFactory.synchronzation_one(), GraphFactory.INSTANCE.synchronzation_one() );
    }

    @Test
    public void synchronzation_two(){
        assertBuildsTo( DslFactory.synchronzation_two(), GraphFactory.INSTANCE.synchronzation_two() );
    }

    @Test
    public void synchronzation_three(){
        assertBuildsTo( DslFactory.synchronzation_three(), GraphFactory.INSTANCE.synchronzation_three() );
    }

    @Test
    public void synchronzation_two_firstBranchEmpty(){
        assertBuildsTo( DslFactory.synchronzation_two_firstBranchEmpty(), GraphFactory.INSTANCE.synchronzation_two_firstBranchEmpty() );
    }

    @Test
    public void synchronzation_two_secondBranchEmpty(){
        assertBuildsTo( DslFactory.synchronzation_two_secondBranchEmpty(), GraphFactory.INSTANCE.synchronzation_two_secondBranchEmpty() );
    }

    @Test
    public void synchronzation_two_pre_post(){
        assertBuildsTo( DslFactory.synchronzation_two_pre_post(), GraphFactory.INSTANCE.synchronzation_two_pre_post() );
    }

    @Test
    public void synchronization_firstBranchEmpty_pre_post(){
        assertBuildsTo( DslFactory.synchronization_firstBranchEmpty_pre_post(), GraphFactory.INSTANCE.synchronization_firstBranchEmpty_pre_post() );
    }

    @Test
    public void synchronization_secondBranchEmpty_pre_post(){
        assertBuildsTo( DslFactory.synchronization_secondBranchEmpty_pre_post(), GraphFactory.INSTANCE.synchronization_secondBranchEmpty_pre_post() );
    }

    @Test
    public void simplemerge_two(){
        assertBuildsTo( DslFactory.simplemerge_two(), GraphFactory.INSTANCE.simplemerge_two() );
    }

    @Test
    public void simplemerge_two_post(){
        assertBuildsTo( DslFactory.simplemerge_two_post(), GraphFactory.INSTANCE.simplemerge_two_post() );
    }

    @Test
    public void cancelling_discriminator_one(){
        assertBuildsTo( DslFactory.cancelling_discriminator_one(), GraphFactory.INSTANCE.cancelling_discriminator_one() );
    }

    @Test
    public void cancelling_discriminator_one_post(){
        assertBuildsTo( DslFactory.cancelling_discriminator_one_post(), GraphFactory.INSTANCE.cancelling_discriminator_one_post() );
    }

    @Test
    public void cancelling_discriminator_two(){
        assertBuildsTo( DslFactory.cancelling_discriminator_two(), GraphFactory.INSTANCE.cancelling_discriminator_two() );
    }

    @Test
    public void cancelling_discriminator_two_firstBranchEmpty(){
        assertBuildsTo( DslFactory.cancelling_discriminator_two_firstBranchEmpty(), GraphFactory.INSTANCE.cancelling_discriminator_two_firstBranchEmpty() );
    }

    @Test
    public void cancelling_discriminator_two_secondBranchEmpty(){
        assertBuildsTo( DslFactory.cancelling_discriminator_two_secondBranchEmpty(), GraphFactory.INSTANCE.cancelling_discriminator_two_secondBranchEmpty() );
    }

    @Test
    public void cancelling_discriminator_two_pre_post(){
        assertBuildsTo( DslFactory.cancelling_discriminator_two_pre_post(), GraphFactory.INSTANCE.cancelling_discriminator_two_pre_post() );
    }

    @Test
    public void cancelling_discriminator_two_firstBranchEmpty_pre_post(){
        assertBuildsTo( DslFactory.cancelling_discriminator_two_firstBranchEmpty_pre_post(),
                GraphFactory.INSTANCE.cancelling_discriminator_two_firstBranchEmpty_pre_post() );
    }

    @Test
    public void cancelling_discriminator_two_secondBranchEmpty_pre_post(){
        assertBuildsTo( DslFactory.cancelling_discriminator_two_secondBranchEmpty_pre_post(),
                GraphFactory.INSTANCE.cancelling_discriminator_two_secondBranchEmpty_pre_post() );
    }

    @Test
    public void cancelling_discriminator_twice(){
        assertBuildsTo( DslFactory.cancelling_discriminator_twice(), GraphFactory.INSTANCE.cancelling_discriminator_twice() );
    }

    @Test
    public void cancelling_discriminator_and_nested(){
        assertBuildsTo( DslFactory.cancelling_discriminator_and_nested(), GraphFactory.INSTANCE.cancelling_discriminator_and_nested() );
    }

    @Test
    public void cancelling_discriminator_or_nested(){
        assertBuildsTo( DslFactory.cancelling_discriminator_or_nested(), GraphFactory.INSTANCE.cancelling_discriminator_or_nested() );
    }

    @Test
    public void loop_one(){
        assertBuildsTo( DslFactory.loop_one( 3 ), GraphFactory.INSTANCE.loop_one( 3 ) );
    }

    @Test
    public void loop_one_special(){
        assertBuildsTo( DslFactory.loop_one_special( 3 ), GraphFactory.INSTANCE.loop_one_special( 3 ) );
    }

    @Test
    public void loop_two(){
        assertBuildsTo( DslFactory.loop_two( 3 ), GraphFactory.INSTANCE.loop_two( 3 ) );
    }

    @Test
    public void loop_while(){
        assertBuildsTo( DslFactory.loop_while( 3 ), GraphFactory.INSTANCE.loop_while( 3 ) );
    }

    @Test
    public void loop_andForkJoin_twoThreads_before_after(){
        assertBuildsTo( DslFactory.loop_andForkJoin_twoThreads_before_after( 3 ), GraphFactory.INSTANCE.loop_andForkJoin_twoThreads_before_after( 3 ) );
    }

    @Test
    public void loop_cancelling_discriminator_twice(){
        assertBuildsTo( DslFactory.loop_cancelling_discriminator_twice( 3 ), GraphFactory.INSTANCE.loop_cancelling_discriminator_twice( 3 ) );
    }

    @Test
    public void signal_one(){
        assertBuildsTo( DslFactory.signal_one(), GraphFactory.INSTANCE.signal_one() );
    }

    @Test
    public void signal_one_pre_post(){
        assertBuildsTo( DslFactory.signal_one_pre_post(), GraphFactory.INSTANCE.signal_one_pre_post() );
    }

    @Test
    public void signal_two(){
        assertBuildsTo( DslFactory.signal_two(), GraphFactory.INSTANCE.signal_two() );
    }

    @Test
    public void signal_andJoin(){
        assertBuildsTo( DslFactory.signal_andJoin(), GraphFactory.INSTANCE.signal_andJoin() );
    }

    @Test
    public void signal_cancellingDiscriminator(){
        assertBuildsTo( DslFactory.signal_cancellingDiscriminator(), GraphFactory.INSTANCE.signal_cancellingDiscriminator() );
    }

    @Test
    public void signal_parallel_pre_post(){
        assertBuildsTo( DslFactory.signal_parallel_pre_post(), GraphFactory.INSTANCE.signal_parallel_pre_post() );
    }

    @Test
    public void timer_one(){
        assertBuildsTo( DslFactory.timer_one(), GraphFactory.INSTANCE.timer_one() );
    }

    @Test
    public void timer_one_pre_post(){
        assertBuildsTo( DslFactory.timer_one_pre_post(), GraphFactory.INSTANCE.timer_one_pre_post() );
    }

    @Test
    public void timer_two(){
        assertBuildsTo( DslFactory.timer_two(), GraphFactory.INSTANCE.timer_two() );
    }

    @Test
    public void timer_parallel_pre_post(){
        assertBuildsTo( DslFactory.timer_parallel_pre_post(), GraphFactory.INSTANCE.timer_parallel_pre_post() );
    }

    @Test
    public void escalation_three(){
        assertBuildsTo( DslFactory.escalation_three(), GraphFactory.INSTANCE.escalation_three() );
    }

    @Test
    public void beanasynccall_one_pre_post(){
        assertBuildsTo( DslFactory.beanasynccall_one_pre_post(), GraphFactory.INSTANCE.beanasynccall_one_pre_post() );
    }

    @Test
    public void beanasynccall_two(){
        assertBuildsTo( DslFactory.beanasynccall_two(), GraphFactory.INSTANCE.beanasynccall_two() );
    }

    @Test
    public void human_task_one_pre_post(){
        assertBuildsTo( DslFactory.human_task_one_pre_post(), GraphFactory.INSTANCE.human_task_one_pre_post() );
    }

    @Test
    public void human_task_two(){
        assertBuildsTo( DslFactory.human_task_two(), GraphFactory.INSTANCE.human_task_two() );
    }

    @Test
    public void validate_argument_optional_default(){
        assertBuildsTo( DslFactory.validate_input_variable(), GraphFactory.INSTANCE.validate_attribute() );
    }

    ////////////////////////////////////////////////////////
    ////////////// GraphFactory tests END //////////////////
    ////////////////////////////////////////////////////////

}