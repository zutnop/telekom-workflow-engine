package ee.telekom.workflow.graph;

import java.util.Collections;
import java.util.Map;

import ee.telekom.workflow.graph.core.GraphImpl;
import ee.telekom.workflow.graph.core.TransitionImpl;
import ee.telekom.workflow.graph.node.activity.BeanAsyncCallActivity;
import ee.telekom.workflow.graph.node.activity.CreateNewInstanceActivity;
import ee.telekom.workflow.graph.node.activity.HumanTaskActivity;
import ee.telekom.workflow.graph.node.activity.ObjectCallActivity;
import ee.telekom.workflow.graph.node.activity.ScriptActivity;
import ee.telekom.workflow.graph.node.activity.SetAttributeActivity;
import ee.telekom.workflow.graph.node.activity.ValidateAttributeActivity;
import ee.telekom.workflow.graph.node.event.CatchSignal;
import ee.telekom.workflow.graph.node.event.CatchTimer;
import ee.telekom.workflow.graph.node.event.ThrowEscalation;
import ee.telekom.workflow.graph.node.gateway.AndFork;
import ee.telekom.workflow.graph.node.gateway.AndJoin;
import ee.telekom.workflow.graph.node.gateway.CancellingDiscriminator;
import ee.telekom.workflow.graph.node.gateway.OrFork;
import ee.telekom.workflow.graph.node.gateway.XorFork;
import ee.telekom.workflow.graph.node.gateway.XorJoin;
import ee.telekom.workflow.graph.node.gateway.condition.AttributeEqualsCondition;
import ee.telekom.workflow.graph.node.gateway.condition.Condition;
import ee.telekom.workflow.graph.node.gateway.condition.ExpressionLanguageCondition;
import ee.telekom.workflow.graph.node.input.AttributeMapping;
import ee.telekom.workflow.graph.node.input.ConstantMapping;
import ee.telekom.workflow.graph.node.input.InputMapping;
import ee.telekom.workflow.graph.node.input.MapMapping;
import ee.telekom.workflow.graph.node.output.OutputMapping;
import ee.telekom.workflow.graph.node.output.ValueMapping;

public class GraphFactory{

    public static final GraphFactory INSTANCE = new GraphFactory();

    public static final String VALUE1 = "value1";
    public static final String VALUE2 = "value2";
    public static final String VALUE3 = "value3";

    public static final String SIGNAL = "go";
    public static final int TIMER_MS = 60000;

    public static final String BEAN = "bean1";
    public static final String METHOD = "method1";

    public static final String ROLE = "role1";
    public static final String USER = "user1";

    public static final String ARGUMENT_KEY = "argument";
    public static final String ARGUMENT = "argument";

    public static final String RESULT_KEY = "result_key";
    public static final String RESULT = "result";

    public static final String CONDITION_TEST_ATTRIBUTE = "conditionAttribute";

    public static class TestBean{
        public Object method1( String argument ){
            return ARGUMENT.equals( argument ) ? RESULT : null;
        }
    }

    /**
     * <pre>
     * [1]
     * </pre>
     */
    public Graph sequence_one(){
        GraphImpl graph = new GraphImpl( "sequence_one", 1, true );
        Node node1 = createRecordPathNode( 1 );
        graph.setStartNode( node1 );
        return graph;
    }

    /**
     * <pre>
     * [1]--[2]
     * </pre>
     */
    public Graph sequence_two(){
        GraphImpl graph = new GraphImpl( "sequence_two", 1 );

        Node node1 = createRecordPathNode( 1 );
        Node node2 = createRecordPathNode( 2 );

        graph.setStartNode( node1 );
        graph.addNode( node2 );
        graph.addTransition( new TransitionImpl( node1, node2 ) );
        return graph;
    }

    /**
     * <pre>
     * [1]--[2]--[3]
     * </pre>
     */
    public Graph sequence_three(){
        GraphImpl graph = new GraphImpl( "sequence_three", 1 );

        Node node1 = createRecordPathNode( 1 );
        Node node2 = createRecordPathNode( 2 );
        Node node3 = createRecordPathNode( 3 );

        graph.setStartNode( node1 );
        graph.addNode( node2 );
        graph.addNode( node3 );
        graph.addTransition( new TransitionImpl( node1, node2 ) );
        graph.addTransition( new TransitionImpl( node2, node3 ) );
        return graph;
    }

    /**
     * <pre>
     * [AND]--[2]
     * </pre>
     */
    public Graph split_one(){
        GraphImpl graph = new GraphImpl( "split_one", 1 );

        Node fork = new AndFork( 1 );
        Node node2 = createRecordPathNode( 2 );

        graph.setStartNode( fork );
        graph.addNode( node2 );
        graph.addTransition( new TransitionImpl( "1_2", fork, node2 ) );
        return graph;
    }

    /**
     * <pre>
     *     +--[2]
     * [AND]
     *     +--[3]
     * </pre>
     */
    public Graph split_two(){
        GraphImpl graph = new GraphImpl( "split_two", 1 );

        Node fork = new AndFork( 1 );
        Node node2 = createRecordPathNode( 2 );
        Node node3 = createRecordPathNode( 3 );

        graph.setStartNode( fork );
        graph.addNode( node2 );
        graph.addNode( node3 );
        graph.addTransition( new TransitionImpl( "1_2", fork, node2 ) );
        graph.addTransition( new TransitionImpl( "1_3", fork, node3 ) );
        return graph;
    }

    /**
     * <pre>
     *     +--[2]
     * [AND]--[3]
     *     +--[4]
     * </pre>
     */
    public Graph split_three(){
        GraphImpl graph = new GraphImpl( "split_three", 1 );

        Node fork = new AndFork( 1 );
        Node node2 = createRecordPathNode( 2 );
        Node node3 = createRecordPathNode( 3 );
        Node node4 = createRecordPathNode( 4 );

        graph.setStartNode( fork );
        graph.addNode( node2 );
        graph.addNode( node3 );
        graph.addNode( node4 );
        graph.addTransition( new TransitionImpl( "1_2", fork, node2 ) );
        graph.addTransition( new TransitionImpl( "1_3", fork, node3 ) );
        graph.addTransition( new TransitionImpl( "1_4", fork, node4 ) );
        return graph;
    }

    /**
     * <pre>
     *          +--[3]
     * [1]--[AND]
     *          +--[4]
     * </pre>
     */
    public Graph split_two_pre(){
        GraphImpl graph = new GraphImpl( "split_two_pre", 1 );

        Node node1 = createRecordPathNode( 1 );
        Node fork = new AndFork( 2 );
        Node node3 = createRecordPathNode( 3 );
        Node node4 = createRecordPathNode( 4 );

        graph.setStartNode( node1 );
        graph.addNode( fork );
        graph.addNode( node3 );
        graph.addNode( node4 );
        graph.addTransition( new TransitionImpl( node1, fork ) );
        graph.addTransition( new TransitionImpl( "2_3", fork, node3 ) );
        graph.addTransition( new TransitionImpl( "2_4", fork, node4 ) );
        return graph;
    }

    /**
     * <pre>
     * [AND]--[2]--[AND]
     * </pre>
     */
    public Graph synchronzation_one(){
        GraphImpl graph = new GraphImpl( "synchronzation_one", 1 );

        Node fork = new AndFork( 1 );
        Node node2 = createRecordPathNode( 2 );
        Node join = new AndJoin( -1 );

        graph.setStartNode( fork );
        graph.addNode( node2 );
        graph.addNode( join );
        graph.addTransition( new TransitionImpl( "1_2", fork, node2 ) );
        graph.addTransition( new TransitionImpl( node2, join ) );
        return graph;
    }

    /**
     * <pre>
     *     +--[2]--+
     * [AND]       [AND]
     *     +--[3]--+
     * </pre>
     */
    public Graph synchronzation_two(){
        GraphImpl graph = new GraphImpl( "synchronzation_two", 1 );

        Node fork = new AndFork( 1 );
        Node node2 = createRecordPathNode( 2 );
        Node node3 = createRecordPathNode( 3 );
        Node join = new AndJoin( -1 );

        graph.setStartNode( fork );
        graph.addNode( node2 );
        graph.addNode( node3 );
        graph.addNode( join );
        graph.addTransition( new TransitionImpl( "1_2", fork, node2 ) );
        graph.addTransition( new TransitionImpl( "1_3", fork, node3 ) );
        graph.addTransition( new TransitionImpl( node2, join ) );
        graph.addTransition( new TransitionImpl( node3, join ) );
        return graph;
    }

    /**
     * <pre>
     *     +--[2]--+
     * [AND]--[3]--[AND]
     *     +--[4]--+
     * </pre>
     */
    public Graph synchronzation_three(){
        GraphImpl graph = new GraphImpl( "synchronzation_three", 1 );

        Node fork = new AndFork( 1 );
        Node node2 = createRecordPathNode( 2 );
        Node node3 = createRecordPathNode( 3 );
        Node node4 = createRecordPathNode( 4 );
        Node join = new AndJoin( -1 );

        graph.setStartNode( fork );
        graph.addNode( node2 );
        graph.addNode( node3 );
        graph.addNode( node4 );
        graph.addNode( join );
        graph.addTransition( new TransitionImpl( "1_2", fork, node2 ) );
        graph.addTransition( new TransitionImpl( "1_3", fork, node3 ) );
        graph.addTransition( new TransitionImpl( "1_4", fork, node4 ) );
        graph.addTransition( new TransitionImpl( node2, join ) );
        graph.addTransition( new TransitionImpl( node3, join ) );
        graph.addTransition( new TransitionImpl( node4, join ) );
        return graph;
    }

    /**
     * <pre>
     *      +-------+
     *  [AND]       [AND]
     *      +--[2]--+
     * </pre>
     */
    public Graph synchronzation_two_firstBranchEmpty(){
        GraphImpl graph = new GraphImpl( "synchronzation_two_firstBranchEmpty", 1 );

        Node fork = new AndFork( 1 );
        Node node2 = createRecordPathNode( 2 );
        Node join = new AndJoin( -1 );

        graph.setStartNode( fork );
        graph.addNode( node2 );
        graph.addNode( join );
        graph.addTransition( new TransitionImpl( "1_-1", fork, join ) );
        graph.addTransition( new TransitionImpl( "1_2", fork, node2 ) );
        graph.addTransition( new TransitionImpl( node2, join ) );
        return graph;
    }

    /**
     * <pre>
     *      +--[2]--+
     *  [AND]       [AND]
     *      +-------+
     * </pre>
     */
    public Graph synchronzation_two_secondBranchEmpty(){
        GraphImpl graph = new GraphImpl( "synchronzation_two_secondBranchEmpty", 1 );

        Node fork = new AndFork( 1 );
        Node node2 = createRecordPathNode( 2 );
        Node join = new AndJoin( -1 );

        graph.setStartNode( fork );
        graph.addNode( node2 );
        graph.addNode( join );
        graph.addTransition( new TransitionImpl( "1_2", fork, node2 ) );
        graph.addTransition( new TransitionImpl( "1_-1", fork, join ) );
        graph.addTransition( new TransitionImpl( node2, join ) );
        return graph;
    }

    /**
     * <pre>
     *      +--+
     *  [AND]  [AND]
     *      +--+
     * </pre>
     */
    public Graph synchronzation_two_bothBranchesEmpty(){
        GraphImpl graph = new GraphImpl( "synchronzation_two_bothBranchesEmpty", 1 );

        Node fork = new AndFork( 1 );
        Node join = new AndJoin( -1 );

        graph.setStartNode( fork );
        graph.addNode( join );
        graph.addTransition( new TransitionImpl( "1_-1", fork, join ) );
        graph.addTransition( new TransitionImpl( "1_-1_HACK", fork, join ) );
        return graph;
    }

    /**
     * <pre>
     *          +--[3]--+
     * [1]--[AND]       [AND]--[6]
     *          +--[4]--+
     * </pre>
     */
    public Graph synchronzation_two_pre_post(){
        GraphImpl graph = new GraphImpl( "synchronzation_two_pre_post", 1 );

        Node node1 = createRecordPathNode( 1 );
        Node fork = new AndFork( 2 );
        Node node3 = createRecordPathNode( 3 );
        Node node4 = createRecordPathNode( 4 );
        Node join = new AndJoin( -2 );
        Node node6 = createRecordPathNode( 6 );

        graph.setStartNode( node1 );
        graph.addNode( fork );
        graph.addNode( node3 );
        graph.addNode( node4 );
        graph.addNode( join );
        graph.addNode( node6 );
        graph.addTransition( new TransitionImpl( node1, fork ) );
        graph.addTransition( new TransitionImpl( "2_3", fork, node3 ) );
        graph.addTransition( new TransitionImpl( "2_4", fork, node4 ) );
        graph.addTransition( new TransitionImpl( node3, join ) );
        graph.addTransition( new TransitionImpl( node4, join ) );
        graph.addTransition( new TransitionImpl( join, node6 ) );
        return graph;
    }

    /**
     * <pre>
     *          +-------+
     * [1]--[AND]       [AND]--[6]
     *          +--[3]--+
     * </pre>
     */
    public Graph synchronization_firstBranchEmpty_pre_post(){
        GraphImpl graph = new GraphImpl( "synchronization_firstBranchEmpty_pre_post", 1 );
        Node node1 = createRecordPathNode( 1 );
        Node fork = new AndFork( 2 );
        Node node3 = createRecordPathNode( 3 );
        Node join = new AndJoin( -2 );
        Node node6 = createRecordPathNode( 6 );

        graph.setStartNode( node1 );
        graph.addNode( fork );
        graph.addNode( node3 );
        graph.addNode( join );
        graph.addNode( node6 );

        graph.addTransition( new TransitionImpl( node1, fork ) );
        graph.addTransition( new TransitionImpl( "2_-2", fork, join ) );
        graph.addTransition( new TransitionImpl( "2_3", fork, node3 ) );
        graph.addTransition( new TransitionImpl( node3, join ) );
        graph.addTransition( new TransitionImpl( join, node6 ) );
        return graph;
    }

    /**
     * <pre>
     *          +--[3]--+
     * [1]--[AND]       [AND]--[6]
     *          +-------+
     * </pre>
     */
    public Graph synchronization_secondBranchEmpty_pre_post(){
        GraphImpl graph = new GraphImpl( "synchronization_secondBranchEmpty_pre_post", 1 );

        Node node1 = createRecordPathNode( 1 );
        Node fork = new AndFork( 2 );
        Node node3 = createRecordPathNode( 3 );
        Node join = new AndJoin( -2 );
        Node node6 = createRecordPathNode( 6 );

        graph.setStartNode( node1 );
        graph.addNode( fork );
        graph.addNode( node3 );
        graph.addNode( join );
        graph.addNode( node6 );
        graph.addTransition( new TransitionImpl( node1, fork ) );
        graph.addTransition( new TransitionImpl( "2_3", fork, node3 ) );
        graph.addTransition( new TransitionImpl( "2_-2", fork, join ) );
        graph.addTransition( new TransitionImpl( node3, join ) );
        graph.addTransition( new TransitionImpl( join, node6 ) );
        return graph;
    }

    /**
     * <pre>
     *          +--+
     * [1]--[AND]  [AND]--[4]
     *          +--+
     * </pre>
     */
    public Graph synchronization_bothBranchesEmpty_pre_post(){
        GraphImpl graph = new GraphImpl( "synchronization_bothBranchesEmpty_pre_post", 1 );

        Node node1 = createRecordPathNode( 1 );
        Node fork = new AndFork( 2 );
        Node join = new AndJoin( -2 );
        Node node4 = createRecordPathNode( 4 );

        graph.setStartNode( node1 );
        graph.addNode( fork );
        graph.addNode( join );
        graph.addNode( node4 );
        graph.addTransition( new TransitionImpl( node1, fork ) );
        graph.addTransition( new TransitionImpl( "2_-2", fork, join ) );
        graph.addTransition( new TransitionImpl( "2_-2_HACK", fork, join ) );
        graph.addTransition( new TransitionImpl( join, node4 ) );
        return graph;
    }

    /**
     * <pre>
     * [XOR]-(condition_1)-[2]
     * </pre>
     */
    public Graph exclusivechoice_one(){
        GraphImpl graph = new GraphImpl( "exclusivechoice_one", 1 );

        XorFork fork = new XorFork( 1 );
        fork.addCondition( createAttributeEqualsCondition( VALUE1 ), "1_2" );
        Node node2 = createRecordPathNode( 2 );

        graph.setStartNode( fork );
        graph.addNode( node2 );
        graph.addTransition( new TransitionImpl( "1_2", fork, node2 ) );
        return graph;
    }

    /**
     * <pre>
     *     +-(condition_1)-[2]
     * [XOR]
     *     +-(condition_2)-[3]
     * </pre>
     */
    public Graph exclusivechoice_two(){
        GraphImpl graph = new GraphImpl( "exclusivechoice_two", 1 );

        XorFork fork = new XorFork( 1 );
        fork.addCondition( createAttributeEqualsCondition( VALUE1 ), "1_2" );
        fork.addCondition( createAttributeEqualsCondition( VALUE2 ), "1_3" );
        Node node2 = createRecordPathNode( 2 );
        Node node3 = createRecordPathNode( 3 );

        graph.setStartNode( fork );
        graph.addNode( node2 );
        graph.addNode( node3 );
        graph.addTransition( new TransitionImpl( "1_2", fork, node2 ) );
        graph.addTransition( new TransitionImpl( "1_3", fork, node3 ) );
        return graph;
    }

    /**
     * <pre>
     *     +-(${condition_1})-[2]
     * [XOR]
     *     +-(${condition_2})-[3]
     * </pre>
     */
    public Graph exclusivechoice_two_expressionlanguage(){
        GraphImpl graph = new GraphImpl( "exclusivechoice_two_expressionlanguage", 1 );

        XorFork fork = new XorFork( 1 );
        fork.addCondition( new ExpressionLanguageCondition( "conditionAttribute == 'value1' && 2 > 1" ), "1_2" );
        fork.addCondition( new ExpressionLanguageCondition( "conditionAttribute == 'value2' && 5 == 5" ), "1_3" );
        Node node2 = createRecordPathNode( 2 );
        Node node3 = createRecordPathNode( 3 );

        graph.setStartNode( fork );
        graph.addNode( node2 );
        graph.addNode( node3 );
        graph.addTransition( new TransitionImpl( "1_2", fork, node2 ) );
        graph.addTransition( new TransitionImpl( "1_3", fork, node3 ) );
        return graph;
    }

    /**
     * <pre>
     * [XOR]-(default)-[2]
     * </pre>
     */
    public Graph exclusivechoice_default(){
        GraphImpl graph = new GraphImpl( "exclusivechoice_default", 1 );

        XorFork fork = new XorFork( 1 );
        fork.addCondition( null, "1_2" );
        Node node2 = createRecordPathNode( 2 );

        graph.setStartNode( fork );
        graph.addNode( node2 );
        graph.addTransition( new TransitionImpl( "1_2", fork, node2 ) );
        return graph;
    }

    /**
     * <pre>
     *     +-(condition_1)-[2]
     * [XOR]
     *     +---(default)---[3]
     * </pre>
     */
    public Graph exclusivechoice_one_default(){
        GraphImpl graph = new GraphImpl( "exclusivechoice_one_default", 1 );

        XorFork fork = new XorFork( 1 );
        fork.addCondition( createAttributeEqualsCondition( VALUE1 ), "1_2" );
        fork.addCondition( null, "1_3" );
        Node node2 = createRecordPathNode( 2 );
        Node node3 = createRecordPathNode( 3 );

        graph.setStartNode( fork );
        graph.addNode( node2 );
        graph.addNode( node3 );
        graph.addTransition( new TransitionImpl( "1_2", fork, node2 ) );
        graph.addTransition( new TransitionImpl( "1_3", fork, node3 ) );
        return graph;
    }

    /**
     * <pre>
     *     +-(condition_1)-[2]
     * [XOR]
     *     +-(condition_1)-[3]
     * </pre>
     */
    public Graph exclusivechoice_twoConditionsTrue(){
        GraphImpl graph = new GraphImpl( "exclusivechoice_twoConditionsTrue", 1 );

        XorFork fork = new XorFork( 1 );
        fork.addCondition( createAttributeEqualsCondition( VALUE1 ), "1_2" );
        fork.addCondition( createAttributeEqualsCondition( VALUE1 ), "1_3" );
        Node node2 = createRecordPathNode( 2 );
        Node node3 = createRecordPathNode( 3 );

        graph.setStartNode( fork );
        graph.addNode( node2 );
        graph.addNode( node3 );
        graph.addTransition( new TransitionImpl( "1_2", fork, node2 ) );
        graph.addTransition( new TransitionImpl( "1_3", fork, node3 ) );
        return graph;
    }

    /**
     * <pre>
     * [XOR]-(default)-[2]--[XOR]
     * </pre>
     */
    public Graph simplemerge_one(){
        GraphImpl graph = new GraphImpl( "simplemerge_one", 1 );

        XorFork fork = new XorFork( 1 );
        fork.addCondition( null, "1_2" );
        Node node2 = createRecordPathNode( 2 );
        Node join = new XorJoin( -1 );

        graph.setStartNode( fork );
        graph.addNode( node2 );
        graph.addNode( join );
        graph.addTransition( new TransitionImpl( "1_2", fork, node2 ) );
        graph.addTransition( new TransitionImpl( node2, join ) );
        return graph;
    }

    /**
     * <pre>
     * [XOR]-(default)-[2]--[XOR]--[4]
     * </pre>
     */
    public Graph simplemerge_one_post(){
        GraphImpl graph = new GraphImpl( "simplemerge_one_post", 1 );

        XorFork fork = new XorFork( 1 );
        fork.addCondition( null, "1_2" );
        Node node2 = createRecordPathNode( 2 );
        Node join = new XorJoin( -1 );
        Node node4 = createRecordPathNode( 4 );

        graph.setStartNode( fork );
        graph.addNode( node2 );
        graph.addNode( join );
        graph.addNode( node4 );
        graph.addTransition( new TransitionImpl( "1_2", fork, node2 ) );
        graph.addTransition( new TransitionImpl( node2, join ) );
        graph.addTransition( new TransitionImpl( join, node4 ) );
        return graph;
    }

    /**
     * <pre>
     *     +-(condition_1)-[2]--+
     * [XOR]                    [XOR]--[5]
     *     +---(default)---[3]--+
     * </pre>
     */
    public Graph simplemerge_two(){
        GraphImpl graph = new GraphImpl( "simplemerge_two", 1 );

        XorFork fork = new XorFork( 1 );
        fork.addCondition( new ExpressionLanguageCondition( "conditionAttribute == 'value1'" ), "1_2" );
        fork.addCondition( null, "1_3" );
        Node node2 = createRecordPathNode( 2 );
        Node node3 = createRecordPathNode( 3 );
        Node join = new XorJoin( -1 );

        graph.setStartNode( fork );
        graph.addNode( node2 );
        graph.addNode( node3 );
        graph.addNode( join );
        graph.addTransition( new TransitionImpl( "1_2", fork, node2 ) );
        graph.addTransition( new TransitionImpl( "1_3", fork, node3 ) );
        graph.addTransition( new TransitionImpl( node2, join ) );
        graph.addTransition( new TransitionImpl( node3, join ) );
        return graph;
    }

    /**
     * <pre>
     *     +-(condition_1)-[2]--+
     * [XOR]                    [XOR]--[5]
     *     +---(default)---[3]--+
     * </pre>
     */
    public Graph simplemerge_two_post(){
        GraphImpl graph = new GraphImpl( "simplemerge_two_post", 1 );

        XorFork fork = new XorFork( 1 );
        fork.addCondition( new ExpressionLanguageCondition( "conditionAttribute == 'value1'" ), "1_2" );
        fork.addCondition( null, "1_3" );
        Node node2 = createRecordPathNode( 2 );
        Node node3 = createRecordPathNode( 3 );
        Node join = new XorJoin( -1 );
        Node node5 = createRecordPathNode( 5 );

        graph.setStartNode( fork );
        graph.addNode( node2 );
        graph.addNode( node3 );
        graph.addNode( join );
        graph.addNode( node5 );
        graph.addTransition( new TransitionImpl( "1_2", fork, node2 ) );
        graph.addTransition( new TransitionImpl( "1_3", fork, node3 ) );
        graph.addTransition( new TransitionImpl( node2, join ) );
        graph.addTransition( new TransitionImpl( node3, join ) );
        graph.addTransition( new TransitionImpl( join, node5 ) );
        return graph;
    }

    /**
     * <pre>
     * [OR]-(condition_1)-[2]
     * </pre>
     */
    public Graph multiplechoice_one(){
        GraphImpl graph = new GraphImpl( "multiplechoice_one", 1 );

        OrFork fork = new OrFork( 1 );
        fork.addCondition( createAttributeEqualsCondition( VALUE1 ), "1_2" );
        Node node2 = createRecordPathNode( 2 );

        graph.setStartNode( fork );
        graph.addNode( node2 );
        graph.addTransition( new TransitionImpl( "1_2", fork, node2 ) );
        return graph;
    }

    /**
     * <pre>
     *    +-(condition_1)-[2]
     * [OR]
     *    +-(condition_2)-[3]
     * </pre>
     */
    public Graph multiplechoice_two(){
        GraphImpl graph = new GraphImpl( "multiplechoice_two", 1 );

        OrFork fork = new OrFork( 1 );
        fork.addCondition( createAttributeEqualsCondition( VALUE1 ), "1_2" );
        fork.addCondition( createAttributeEqualsCondition( VALUE2 ), "1_3" );
        Node node2 = createRecordPathNode( 2 );
        Node node3 = createRecordPathNode( 3 );

        graph.setStartNode( fork );
        graph.addNode( node2 );
        graph.addNode( node3 );
        graph.addTransition( new TransitionImpl( "1_2", fork, node2 ) );
        graph.addTransition( new TransitionImpl( "1_3", fork, node3 ) );
        return graph;
    }

    /**
     * <pre>
     * [OR]-(default)-[2]
     * </pre>
     */
    public Graph multiplechoice_default(){
        GraphImpl graph = new GraphImpl( "multiplechoice_default", 1 );

        OrFork fork = new OrFork( 1 );
        fork.addCondition( null, "1_2" );
        Node node2 = createRecordPathNode( 2 );

        graph.setStartNode( fork );
        graph.addNode( node2 );
        graph.addTransition( new TransitionImpl( "1_2", fork, node2 ) );
        return graph;
    }

    /**
     * <pre>
     *    +-(condition_1)-[2]
     * [OR]
     *    +---(default)---[3]
     * </pre>
     */
    public Graph multiplechoice_one_default(){
        GraphImpl graph = new GraphImpl( "multiplechoice_one_default", 1 );

        OrFork fork = new OrFork( 1 );
        fork.addCondition( createAttributeEqualsCondition( VALUE1 ), "1_2" );
        fork.addCondition( null, "1_3" );
        Node node2 = createRecordPathNode( 2 );
        Node node3 = createRecordPathNode( 3 );

        graph.setStartNode( fork );
        graph.addNode( node2 );
        graph.addNode( node3 );
        graph.addTransition( new TransitionImpl( "1_2", fork, node2 ) );
        graph.addTransition( new TransitionImpl( "1_3", fork, node3 ) );
        return graph;
    }

    /**
     * <pre>
     *    +-(condition_1)-[2]
     * [OR]
     *    +-(condition_1)-[3]
     * </pre>
     */
    public Graph multiplechoice_twoConditionsTrue(){
        GraphImpl graph = new GraphImpl( "multiplechoice_twoConditionsTrue", 1 );

        OrFork fork = new OrFork( 1 );
        fork.addCondition( createAttributeEqualsCondition( VALUE1 ), "1_2" );
        fork.addCondition( createAttributeEqualsCondition( VALUE1 ), "1_3" );
        Node node2 = createRecordPathNode( 2 );
        Node node3 = createRecordPathNode( 3 );

        graph.setStartNode( fork );
        graph.addNode( node2 );
        graph.addNode( node3 );
        graph.addTransition( new TransitionImpl( "1_2", fork, node2 ) );
        graph.addTransition( new TransitionImpl( "1_3", fork, node3 ) );
        return graph;
    }

    /**
     * <pre>
     * [AND]--[2]--[CD]
     * </pre>
     */
    public Graph cancelling_discriminator_one(){
        GraphImpl graph = new GraphImpl( "cancelling_discriminator_one", 1 );

        Node fork = new AndFork( 1 );
        Node node2 = createRecordPathNode( 2 );
        Node join = new CancellingDiscriminator( -1 );

        graph.setStartNode( fork );
        graph.addNode( node2 );
        graph.addNode( join );
        graph.addTransition( new TransitionImpl( "1_2", fork, node2 ) );
        graph.addTransition( new TransitionImpl( node2, join ) );
        return graph;
    }

    /**
     * <pre>
     * [AND]--[2]--[CD]--[4]
     * </pre>
     */
    public Graph cancelling_discriminator_one_post(){
        GraphImpl graph = new GraphImpl( "cancelling_discriminator_one_post", 1 );

        Node fork = new AndFork( 1 );
        Node node2 = createRecordPathNode( 2 );
        Node join = new CancellingDiscriminator( -1 );
        Node node4 = createRecordPathNode( 4 );

        graph.setStartNode( fork );
        graph.addNode( node2 );
        graph.addNode( join );
        graph.addNode( node4 );
        graph.addTransition( new TransitionImpl( "1_2", fork, node2 ) );
        graph.addTransition( new TransitionImpl( node2, join ) );
        graph.addTransition( new TransitionImpl( join, node4 ) );
        return graph;
    }

    /**
     * <pre>
     *     +--[2]--+
     * [AND]       [CD]
     *     +--[3]--+
     * </pre>
     */
    public Graph cancelling_discriminator_two(){
        GraphImpl graph = new GraphImpl( "cancelling_discriminator_two", 1 );

        Node fork = new AndFork( 1 );
        Node node2 = createRecordPathNode( 2 );
        Node node3 = createRecordPathNode( 3 );
        Node join = new CancellingDiscriminator( -1 );

        graph.setStartNode( fork );
        graph.addNode( node2 );
        graph.addNode( node3 );
        graph.addNode( join );
        graph.addTransition( new TransitionImpl( "1_2", fork, node2 ) );
        graph.addTransition( new TransitionImpl( "1_3", fork, node3 ) );
        graph.addTransition( new TransitionImpl( node2, join ) );
        graph.addTransition( new TransitionImpl( node3, join ) );
        return graph;
    }

    /**
     * <pre>
     *      +-------+
     *  [AND]       [CD]
     *      +--[2]--+
     * </pre>
     */
    public Graph cancelling_discriminator_two_firstBranchEmpty(){
        GraphImpl graph = new GraphImpl( "cancelling_discriminator_two_firstBranchEmpty", 1 );

        Node fork = new AndFork( 1 );
        Node node2 = createRecordPathNode( 2 );
        Node join = new CancellingDiscriminator( -1 );

        graph.setStartNode( fork );
        graph.addNode( node2 );
        graph.addNode( join );
        graph.addTransition( new TransitionImpl( "1_-1", fork, join ) );
        graph.addTransition( new TransitionImpl( "1_2", fork, node2 ) );
        graph.addTransition( new TransitionImpl( node2, join ) );
        return graph;
    }

    /**
     * <pre>
     *      +--[2]--+
     *  [AND]       [CD]
     *      +-------+
     * </pre>
     */
    public Graph cancelling_discriminator_two_secondBranchEmpty(){
        GraphImpl graph = new GraphImpl( "cancelling_discriminator_two_secondBranchEmpty", 1 );

        Node fork = new AndFork( 1 );
        Node node2 = createRecordPathNode( 2 );
        Node join = new CancellingDiscriminator( -1 );

        graph.setStartNode( fork );
        graph.addNode( node2 );
        graph.addNode( join );
        graph.addTransition( new TransitionImpl( "1_2", fork, node2 ) );
        graph.addTransition( new TransitionImpl( "1_-1", fork, join ) );
        graph.addTransition( new TransitionImpl( node2, join ) );
        return graph;
    }

    /**
     * <pre>
     *          +--[3]--+
     * [1]--[AND]       [CD]--[6]
     *          +--[4]--+
     * </pre>
     */
    public Graph cancelling_discriminator_two_pre_post(){
        GraphImpl graph = new GraphImpl( "cancelling_discriminator_two_pre_post", 1 );

        Node node1 = createRecordPathNode( 1 );
        Node fork = new AndFork( 2 );
        Node node3 = createRecordPathNode( 3 );
        Node node4 = createRecordPathNode( 4 );
        Node join = new CancellingDiscriminator( -2 );
        Node node6 = createRecordPathNode( 6 );

        graph.setStartNode( node1 );
        graph.addNode( fork );
        graph.addNode( node3 );
        graph.addNode( node4 );
        graph.addNode( join );
        graph.addNode( node6 );
        graph.addTransition( new TransitionImpl( node1, fork ) );
        graph.addTransition( new TransitionImpl( "2_3", fork, node3 ) );
        graph.addTransition( new TransitionImpl( "2_4", fork, node4 ) );
        graph.addTransition( new TransitionImpl( node3, join ) );
        graph.addTransition( new TransitionImpl( node4, join ) );
        graph.addTransition( new TransitionImpl( join, node6 ) );
        return graph;
    }

    /**
     * <pre>
     *          +-------+
     * [1]--[AND]       [CD]--[5]
     *          +--[3]--+
     * </pre>
     */
    public Graph cancelling_discriminator_two_firstBranchEmpty_pre_post(){
        GraphImpl graph = new GraphImpl( "cancelling_discriminator_two_firstBranchEmpty_pre_post", 1 );

        Node node1 = createRecordPathNode( 1 );
        Node fork = new AndFork( 2 );
        Node node3 = createRecordPathNode( 3 );
        Node join = new CancellingDiscriminator( -2 );
        Node node5 = createRecordPathNode( 5 );

        graph.setStartNode( node1 );
        graph.addNode( fork );
        graph.addNode( node3 );
        graph.addNode( join );
        graph.addNode( node5 );

        graph.addTransition( new TransitionImpl( node1, fork ) );
        graph.addTransition( new TransitionImpl( "2_-2", fork, join ) );
        graph.addTransition( new TransitionImpl( "2_3", fork, node3 ) );
        graph.addTransition( new TransitionImpl( node3, join ) );
        graph.addTransition( new TransitionImpl( join, node5 ) );
        return graph;
    }

    /**
     * <pre>
     *          +--[3]--+
     * [1]--[AND]       [CD]--[6]
     *          +-------+
     * </pre>
     */
    public Graph cancelling_discriminator_two_secondBranchEmpty_pre_post(){
        GraphImpl graph = new GraphImpl( "cancelling_discriminator_two_secondBranchEmpty_pre_post", 1 );

        Node node1 = createRecordPathNode( 1 );
        Node fork = new AndFork( 2 );
        Node node3 = createRecordPathNode( 3 );
        Node join = new CancellingDiscriminator( -2 );
        Node node6 = createRecordPathNode( 6 );

        graph.setStartNode( node1 );
        graph.addNode( fork );
        graph.addNode( node3 );
        graph.addNode( join );
        graph.addNode( node6 );
        graph.addTransition( new TransitionImpl( node1, fork ) );
        graph.addTransition( new TransitionImpl( "2_3", fork, node3 ) );
        graph.addTransition( new TransitionImpl( "2_-2", fork, join ) );
        graph.addTransition( new TransitionImpl( node3, join ) );
        graph.addTransition( new TransitionImpl( join, node6 ) );
        return graph;
    }

    /**
     * <pre>
     *          +--[3]--+              +--[8]--+
     * [1]--[AND]       [CD]--[6]--[AND]       [CD]--[11]
     *          +--[4]--+              +--[9]--+
     * </pre>
     */
    public Graph cancelling_discriminator_twice(){
        GraphImpl graph = new GraphImpl( "cancelling_discriminator_twice", 1 );

        Node node1 = createRecordPathNode( 1 );
        Node fork1 = new AndFork( 2 );
        Node node3 = createRecordPathNode( 3 );
        Node node4 = createRecordPathNode( 4 );
        Node join1 = new CancellingDiscriminator( -2 );
        Node node6 = createRecordPathNode( 6 );
        Node fork2 = new AndFork( 7 );
        Node node8 = createRecordPathNode( 8 );
        Node node9 = createRecordPathNode( 9 );
        Node join2 = new CancellingDiscriminator( -7 );
        Node node11 = createRecordPathNode( 11 );

        graph.setStartNode( node1 );
        graph.addNode( fork1 );
        graph.addNode( node3 );
        graph.addNode( node4 );
        graph.addNode( join1 );
        graph.addNode( node6 );
        graph.addNode( fork2 );
        graph.addNode( node8 );
        graph.addNode( node9 );
        graph.addNode( join2 );
        graph.addNode( node11 );
        graph.addTransition( new TransitionImpl( node1, fork1 ) );
        graph.addTransition( new TransitionImpl( "2_3", fork1, node3 ) );
        graph.addTransition( new TransitionImpl( "2_4", fork1, node4 ) );
        graph.addTransition( new TransitionImpl( node3, join1 ) );
        graph.addTransition( new TransitionImpl( node4, join1 ) );
        graph.addTransition( new TransitionImpl( join1, node6 ) );
        graph.addTransition( new TransitionImpl( node6, fork2 ) );
        graph.addTransition( new TransitionImpl( "7_8", fork2, node8 ) );
        graph.addTransition( new TransitionImpl( "7_9", fork2, node9 ) );
        graph.addTransition( new TransitionImpl( node8, join2 ) );
        graph.addTransition( new TransitionImpl( node9, join2 ) );
        graph.addTransition( new TransitionImpl( join2, node11 ) );
        return graph;
    }

    /**
     * <pre>
     *                      +--[signal(1)]--+
     *          +--[3]--[AND]               [CD]--[8]---+
     *          |           +--[signal(2)]--+           |
     *          |                                       |
     * [1]--[AND]                                       [CD]--[16]
     *          |                                       |                           
     *          |           +--[signal(3)]--+           |
     *          +--[9]--[AND]               [CD]--[14]--+
     *                      +--[signal(4)]--+
     * </pre>
     */
    public Graph cancelling_discriminator_and_nested(){
        GraphImpl graph = new GraphImpl( "cancelling_discriminator_and_nested", 1 );

        Node node1 = createRecordPathNode( 1 );
        Node fork1 = new AndFork( 2 );

        Node node3 = createRecordPathNode( 3 );
        Node fork2 = new AndFork( 4 );
        Node node5 = new CatchSignal( 5, "1" );
        Node node6 = new CatchSignal( 6, "2" );
        Node join1 = new CancellingDiscriminator( -4 );
        Node node8 = createRecordPathNode( 8 );

        Node node9 = createRecordPathNode( 9 );
        Node fork3 = new AndFork( 10 );
        Node node11 = new CatchSignal( 11, "3" );
        Node node12 = new CatchSignal( 12, "4" );
        Node join2 = new CancellingDiscriminator( -10 );
        Node node14 = createRecordPathNode( 14 );

        Node join3 = new CancellingDiscriminator( -2 );
        Node node16 = createRecordPathNode( 16 );

        graph.setStartNode( node1 );
        graph.addNode( fork1 );
        graph.addNode( node3 );
        graph.addNode( fork2 );
        graph.addNode( node5 );
        graph.addNode( node6 );
        graph.addNode( join1 );
        graph.addNode( node8 );
        graph.addNode( node9 );
        graph.addNode( fork3 );
        graph.addNode( node11 );
        graph.addNode( node12 );
        graph.addNode( join2 );
        graph.addNode( node14 );
        graph.addNode( join3 );
        graph.addNode( node16 );

        graph.addTransition( new TransitionImpl( node1, fork1 ) );
        graph.addTransition( new TransitionImpl( "2_3", fork1, node3 ) );
        graph.addTransition( new TransitionImpl( "2_9", fork1, node9 ) );

        graph.addTransition( new TransitionImpl( node3, fork2 ) );
        graph.addTransition( new TransitionImpl( "4_5", fork2, node5 ) );
        graph.addTransition( new TransitionImpl( "4_6", fork2, node6 ) );
        graph.addTransition( new TransitionImpl( node5, join1 ) );
        graph.addTransition( new TransitionImpl( node6, join1 ) );
        graph.addTransition( new TransitionImpl( join1, node8 ) );
        graph.addTransition( new TransitionImpl( node8, join3 ) );

        graph.addTransition( new TransitionImpl( node9, fork3 ) );
        graph.addTransition( new TransitionImpl( "10_11", fork3, node11 ) );
        graph.addTransition( new TransitionImpl( "10_12", fork3, node12 ) );
        graph.addTransition( new TransitionImpl( node11, join2 ) );
        graph.addTransition( new TransitionImpl( node12, join2 ) );
        graph.addTransition( new TransitionImpl( join2, node14 ) );
        graph.addTransition( new TransitionImpl( node14, join3 ) );

        graph.addTransition( new TransitionImpl( join3, node16 ) );

        return graph;
    }

    /**
     * <pre>
     *                     +-(condition_1)-[signal(1)]--+
     *          +--[3]--[OR]                            [CD]--[8]---+
     *          |          +-(condition_2)-[signal(2)]--+           |
     *          |                                                   |
     * [1]--[AND]                                                   [CD]--[16]
     *          |                                                   |                           
     *          |          +-(condition_3)-[signal(3)]--+           |
     *          +--[9]--[OR]                            [CD]--[14]--+
     *                     +-(condition_4)-[signal(4)]--+
     * </pre>
     */
    public Graph cancelling_discriminator_or_nested(){
        GraphImpl graph = new GraphImpl( "cancelling_discriminator_or_nested", 1 );

        Node node1 = createRecordPathNode( 1 );
        Node fork1 = new AndFork( 2 );

        Node node3 = createRecordPathNode( 3 );
        OrFork fork2 = new OrFork( 4 );
        fork2.addCondition( new ExpressionLanguageCondition( "true" ), "4_5" );
        fork2.addCondition( new ExpressionLanguageCondition( "true" ), "4_6" );
        Node node5 = new CatchSignal( 5, "1" );
        Node node6 = new CatchSignal( 6, "2" );
        Node join1 = new CancellingDiscriminator( -4 );
        Node node8 = createRecordPathNode( 8 );

        Node node9 = createRecordPathNode( 9 );
        OrFork fork3 = new OrFork( 10 );
        fork3.addCondition( new ExpressionLanguageCondition( "true" ), "10_11" );
        fork3.addCondition( new ExpressionLanguageCondition( "true" ), "10_12" );
        Node node11 = new CatchSignal( 11, "3" );
        Node node12 = new CatchSignal( 12, "4" );
        Node join2 = new CancellingDiscriminator( -10 );
        Node node14 = createRecordPathNode( 14 );

        Node join3 = new CancellingDiscriminator( -2 );
        Node node16 = createRecordPathNode( 16 );

        graph.setStartNode( node1 );
        graph.addNode( fork1 );
        graph.addNode( node3 );
        graph.addNode( fork2 );
        graph.addNode( node5 );
        graph.addNode( node6 );
        graph.addNode( join1 );
        graph.addNode( node8 );
        graph.addNode( node9 );
        graph.addNode( fork3 );
        graph.addNode( node11 );
        graph.addNode( node12 );
        graph.addNode( join2 );
        graph.addNode( node14 );
        graph.addNode( join3 );
        graph.addNode( node16 );

        graph.addTransition( new TransitionImpl( node1, fork1 ) );
        graph.addTransition( new TransitionImpl( "2_3", fork1, node3 ) );
        graph.addTransition( new TransitionImpl( "2_9", fork1, node9 ) );

        graph.addTransition( new TransitionImpl( node3, fork2 ) );
        graph.addTransition( new TransitionImpl( "4_5", fork2, node5 ) );
        graph.addTransition( new TransitionImpl( "4_6", fork2, node6 ) );
        graph.addTransition( new TransitionImpl( node5, join1 ) );
        graph.addTransition( new TransitionImpl( node6, join1 ) );
        graph.addTransition( new TransitionImpl( join1, node8 ) );
        graph.addTransition( new TransitionImpl( node8, join3 ) );

        graph.addTransition( new TransitionImpl( node9, fork3 ) );
        graph.addTransition( new TransitionImpl( "10_11", fork3, node11 ) );
        graph.addTransition( new TransitionImpl( "10_12", fork3, node12 ) );
        graph.addTransition( new TransitionImpl( node11, join2 ) );
        graph.addTransition( new TransitionImpl( node12, join2 ) );
        graph.addTransition( new TransitionImpl( join2, node14 ) );
        graph.addTransition( new TransitionImpl( node14, join3 ) );

        graph.addTransition( new TransitionImpl( join3, node16 ) );

        return graph;
    }

    /**
     * <pre>
     *  [1]--[XOR]
     *   |     |(ExecutionCountCondition)
     *   +--<--+
     * </pre>
     */
    public Graph loop_one( int executionCount ){
        GraphImpl graph = new GraphImpl( "loop_one", 1 );
        Node node100 = new SetAttributeActivity( 100, "executionCount", 0 );
        XorJoin join = new XorJoin( -2 );
        Node node101 = new SetAttributeActivity( 101, "executionCount", "${executionCount + 1}" );
        Node node1 = createRecordPathNode( 1 );
        XorFork fork = new XorFork( 2 );
        fork.addCondition( new ExpressionLanguageCondition( "executionCount < " + executionCount ), "2_-2" );

        graph.setStartNode( node100 );
        graph.addNode( join );
        graph.addNode( node101 );
        graph.addNode( node1 );
        graph.addNode( fork );
        graph.addTransition( new TransitionImpl( node100, join ) );
        graph.addTransition( new TransitionImpl( join, node101 ) );
        graph.addTransition( new TransitionImpl( node101, node1 ) );
        graph.addTransition( new TransitionImpl( node1, fork ) );
        graph.addTransition( new TransitionImpl( "2_-2", fork, join ) );
        return graph;
    }

    /**
     *  [100]--[XOR(-2)]--[SetAttribute(101)]--[1]--[XOR(2)]--[3]
     *           |                                    |(ExecutionCountCondition)
     *           +-----------------<------------------+
     */
    public Graph loop_one_special( int executionCount ){
        GraphImpl graph = new GraphImpl( "loop_one_special", 1 );
        Node node100 = new SetAttributeActivity( 100, "executionCount", 0 );
        XorJoin join = new XorJoin( -2 );
        Node node101 = new SetAttributeActivity( 101, "executionCount", "${executionCount + 1}" );
        Node node1 = createRecordPathNode( 1 );
        XorFork fork = new XorFork( 2 );
        fork.addCondition( new ExpressionLanguageCondition( "executionCount < " + executionCount ), "2_-2" );
        fork.addCondition( null, "2_3" );
        Node node3 = createRecordPathNode( 3 );

        graph.setStartNode( node100 );
        graph.addNode( join );
        graph.addNode( node101 );
        graph.addNode( node1 );
        graph.addNode( fork );
        graph.addNode( node3 );
        graph.addTransition( new TransitionImpl( node100, join ) );
        graph.addTransition( new TransitionImpl( join, node101 ) );
        graph.addTransition( new TransitionImpl( node101, node1 ) );
        graph.addTransition( new TransitionImpl( node1, fork ) );
        graph.addTransition( new TransitionImpl( "2_-2", fork, join ) );
        graph.addTransition( new TransitionImpl( "2_3", fork, node3 ) );
        return graph;
    }

    /**
     *  [100]--[XOR(-3)]--[SetAttribute(101)]--[1]--[2]--[XOR(3)]
     *           |                                         |(ExecutionCountCondition)
     *           +-----------------<-----------------------+
     */
    public Graph loop_two( int executionCount ){
        GraphImpl graph = new GraphImpl( "loop_two", 1 );

        Node node100 = new SetAttributeActivity( 100, "executionCount", 0 );
        XorJoin join = new XorJoin( -3 );
        Node node101 = new SetAttributeActivity( 101, "executionCount", "${executionCount + 1}" );
        Node node1 = createRecordPathNode( 1 );
        Node node2 = createRecordPathNode( 2 );
        XorFork fork = new XorFork( 3 );
        fork.addCondition( new ExpressionLanguageCondition( "executionCount < " + executionCount ), "3_-3" );

        graph.setStartNode( node100 );
        graph.addNode( join );
        graph.addNode( node101 );
        graph.addNode( node1 );
        graph.addNode( node2 );
        graph.addNode( fork );
        graph.addTransition( new TransitionImpl( node100, join ) );
        graph.addTransition( new TransitionImpl( join, node101 ) );
        graph.addTransition( new TransitionImpl( node101, node1 ) );
        graph.addTransition( new TransitionImpl( node1, node2 ) );
        graph.addTransition( new TransitionImpl( node2, fork ) );
        graph.addTransition( new TransitionImpl( "3_-3", fork, join ) );
        return graph;
    }

    /**
     *                              +--[SetAttribute(101)]--[3]--[XOR(-2)]
     *                              |                              |
     *  [1]--[SetAttribute(100)]--[XOR(2)]-------------<-----------+
     *                              |(ExecutionCountCondition)
     *                              +--[4]
     */
    public Graph loop_while( int executionCount ){
        GraphImpl graph = new GraphImpl( "loop_while", 1 );
        Node node1 = createRecordPathNode( 1 );
        Node node100 = new SetAttributeActivity( 100, "executionCount", 0 );
        XorFork fork = new XorFork( 2 );
        fork.addCondition( new ExpressionLanguageCondition( "executionCount < " + executionCount ), "2_101" );
        fork.addCondition( null, "2_4" );
        Node node101 = new SetAttributeActivity( 101, "executionCount", "${executionCount + 1}" );
        Node node3 = createRecordPathNode( 3 );
        XorJoin join = new XorJoin( -2 );
        Node node4 = createRecordPathNode( 4 );

        graph.setStartNode( node1 );
        graph.addNode( node100 );
        graph.addNode( fork );
        graph.addNode( node101 );
        graph.addNode( node3 );
        graph.addNode( join );
        graph.addNode( node4 );
        graph.addTransition( new TransitionImpl( node1, node100 ) );
        graph.addTransition( new TransitionImpl( node100, fork ) );
        graph.addTransition( new TransitionImpl( "2_101", fork, node101 ) );
        graph.addTransition( new TransitionImpl( node101, node3 ) );
        graph.addTransition( new TransitionImpl( node3, join ) );
        graph.addTransition( new TransitionImpl( "2_4", fork, node4 ) );
        graph.addTransition( new TransitionImpl( join, fork ) );
        return graph;
    }

    /**
     * <pre>
     *          +--[3]--+
     * [1]--[AND]       [AND]--[6]--[XOR]
     *  |       +--[4]--+             |(ExecutionCountCondition)
     *  +--------------<--------------+
     * </pre>
     */
    public Graph loop_andForkJoin_twoThreads_before_after( int executionCount ){
        GraphImpl graph = new GraphImpl(
                "loop_andForkJoin_twoThreads_before_after", 1 );

        Node node100 = new SetAttributeActivity( 100, "executionCount", 0 );
        XorJoin join2 = new XorJoin( -7 );
        Node node101 = new SetAttributeActivity( 101, "executionCount", "${executionCount + 1}" );
        Node node1 = createRecordPathNode( 1 );
        Node fork1 = new AndFork( 2 );
        Node node3 = createRecordPathNode( 3 );
        Node node4 = createRecordPathNode( 4 );
        Node join1 = new AndJoin( -2 );
        Node node6 = createRecordPathNode( 6 );
        XorFork fork2 = new XorFork( 7 );
        fork2.addCondition( new ExpressionLanguageCondition( "executionCount < " + executionCount ), "7_-7" );

        graph.setStartNode( node100 );
        graph.addNode( join2 );
        graph.addNode( node101 );
        graph.addNode( node1 );
        graph.addNode( fork1 );
        graph.addNode( node3 );
        graph.addNode( node4 );
        graph.addNode( join1 );
        graph.addNode( node6 );
        graph.addNode( fork2 );
        graph.addTransition( new TransitionImpl( node100, join2 ) );
        graph.addTransition( new TransitionImpl( join2, node101 ) );
        graph.addTransition( new TransitionImpl( node101, node1 ) );
        graph.addTransition( new TransitionImpl( node1, fork1 ) );
        graph.addTransition( new TransitionImpl( "2_3", fork1, node3 ) );
        graph.addTransition( new TransitionImpl( "2_4", fork1, node4 ) );
        graph.addTransition( new TransitionImpl( node3, join1 ) );
        graph.addTransition( new TransitionImpl( node4, join1 ) );
        graph.addTransition( new TransitionImpl( join1, node6 ) );
        graph.addTransition( new TransitionImpl( node6, fork2 ) );
        graph.addTransition( new TransitionImpl( "7_-7", fork2, join2 ) );
        return graph;
    }

    /**
     * <pre>
     *          +--[3]--+              +--[8]--+
     * [1]--[AND]       [CD]--[6]--[AND]       [CD]--[11]--[XOR]
     *  |       +--[4]--+              +--[9]--+             |(ExecutionCountCondition)
     *  +----------------------<-----------------------------+
     * </pre>
     */
    public Graph loop_cancelling_discriminator_twice( int executionCount ){
        GraphImpl graph = new GraphImpl( "loop_cancelling_discriminator_twice", 1, true );

        Node node100 = new SetAttributeActivity( 100, "executionCount", 0 );
        XorJoin join3 = new XorJoin( -12 );
        Node node101 = new SetAttributeActivity( 101, "executionCount", "${executionCount + 1}" );
        Node node1 = createRecordPathNode( 1 );
        Node fork1 = new AndFork( 2 );
        Node node3 = createRecordPathNode( 3 );
        Node node4 = createRecordPathNode( 4 );
        Node join1 = new CancellingDiscriminator( -2 );
        Node node6 = createRecordPathNode( 6 );
        Node fork2 = new AndFork( 7 );
        Node node8 = createRecordPathNode( 8 );
        Node node9 = createRecordPathNode( 9 );
        Node join2 = new CancellingDiscriminator( -7 );
        Node node11 = createRecordPathNode( 11 );
        XorFork fork3 = new XorFork( 12 );
        fork3.addCondition( new ExpressionLanguageCondition( "executionCount < " + executionCount ), "12_-12" );

        graph.setStartNode( node100 );
        graph.addNode( join3 );
        graph.addNode( node101 );
        graph.addNode( node1 );
        graph.addNode( fork1 );
        graph.addNode( node3 );
        graph.addNode( node4 );
        graph.addNode( join1 );
        graph.addNode( node6 );
        graph.addNode( fork2 );
        graph.addNode( node8 );
        graph.addNode( node9 );
        graph.addNode( join2 );
        graph.addNode( node11 );
        graph.addNode( fork3 );
        graph.addTransition( new TransitionImpl( node100, join3 ) );
        graph.addTransition( new TransitionImpl( join3, node101 ) );
        graph.addTransition( new TransitionImpl( node101, node1 ) );
        graph.addTransition( new TransitionImpl( node1, fork1 ) );
        graph.addTransition( new TransitionImpl( "2_3", fork1, node3 ) );
        graph.addTransition( new TransitionImpl( "2_4", fork1, node4 ) );
        graph.addTransition( new TransitionImpl( node3, join1 ) );
        graph.addTransition( new TransitionImpl( node4, join1 ) );
        graph.addTransition( new TransitionImpl( join1, node6 ) );
        graph.addTransition( new TransitionImpl( node6, fork2 ) );
        graph.addTransition( new TransitionImpl( "7_8", fork2, node8 ) );
        graph.addTransition( new TransitionImpl( "7_9", fork2, node9 ) );
        graph.addTransition( new TransitionImpl( node8, join2 ) );
        graph.addTransition( new TransitionImpl( node9, join2 ) );
        graph.addTransition( new TransitionImpl( join2, node11 ) );
        graph.addTransition( new TransitionImpl( node11, fork3 ) );
        graph.addTransition( new TransitionImpl( "12_-12", fork3, join3 ) );

        return graph;
    }

    /**
     * <pre>
     * [signal(go)]
     * </pre>
     */
    public Graph signal_one(){
        GraphImpl graph = new GraphImpl( "signal_one", 1 );
        Node node1 = new CatchSignal( 1, SIGNAL );
        graph.setStartNode( node1 );
        return graph;
    }

    /**
     * <pre>
     * [signal(go)]
     * </pre>
     */
    public Graph signal_one_special( OutputMapping resultMapping ){
        GraphImpl graph = new GraphImpl( "signal_one_special", 1 );
        Node node1 = new CatchSignal( 1, SIGNAL, resultMapping );
        graph.setStartNode( node1 );
        return graph;
    }

    /**
     * <pre>
     * [1]--[signal(go)]--[3]
     * </pre>
     */
    public Graph signal_one_pre_post(){
        GraphImpl graph = new GraphImpl( "signal_one_pre_post", 1, true );

        Node node1 = createRecordPathNode( 1 );
        Node node2 = new CatchSignal( 2, SIGNAL, new ValueMapping( RESULT_KEY ) );
        Node node3 = createRecordPathNode( 3 );

        graph.setStartNode( node1 );
        graph.addNode( node2 );
        graph.addNode( node3 );
        graph.addTransition( new TransitionImpl( node1, node2 ) );
        graph.addTransition( new TransitionImpl( node2, node3 ) );
        return graph;
    }

    /**
     * <pre>
     * [signal(go)]--[signal(go)]
     * </pre>
     */
    public Graph signal_two(){
        GraphImpl graph = new GraphImpl( "signal_two", 1 );

        Node node1 = new CatchSignal( 1, SIGNAL, new ValueMapping( RESULT_KEY ) );
        Node node2 = new CatchSignal( 2, SIGNAL, new ValueMapping( RESULT_KEY ) );

        graph.setStartNode( node1 );
        graph.addNode( node2 );
        graph.addTransition( new TransitionImpl( node1, node2 ) );
        return graph;
    }

    /**
     * <pre>
     *     +--[signal(go)]--+
     * [AND]                [AND]
     *     +--[signal(go)]--+
     * </pre>
     */
    public Graph signal_andJoin(){
        GraphImpl graph = new GraphImpl( "signal_andJoin", 1 );

        Node fork = new AndFork( 1 );
        Node node2 = new CatchSignal( 2, SIGNAL, new ValueMapping( RESULT_KEY ) );
        Node node3 = new CatchSignal( 3, SIGNAL, new ValueMapping( RESULT_KEY ) );
        Node join = new AndJoin( -1 );

        graph.setStartNode( fork );
        graph.addNode( fork );
        graph.addNode( node2 );
        graph.addNode( node3 );
        graph.addNode( join );
        graph.addTransition( new TransitionImpl( "1_2", fork, node2 ) );
        graph.addTransition( new TransitionImpl( "1_3", fork, node3 ) );
        graph.addTransition( new TransitionImpl( node2, join ) );
        graph.addTransition( new TransitionImpl( node3, join ) );
        return graph;
    }

    /**
     * <pre>
     *     +--[signal(go)]--+
     * [AND]                [AND]
     *     +--[signal(go)]--+
     * </pre>
     */
    public Graph signal_cancellingDiscriminator(){
        GraphImpl graph = new GraphImpl( "signal_cancellingDiscriminator", 1 );

        Node fork = new AndFork( 1 );
        Node node2 = new CatchSignal( 2, SIGNAL, new ValueMapping( RESULT_KEY ) );
        Node node3 = new CatchSignal( 3, SIGNAL, new ValueMapping( RESULT_KEY ) );
        Node join = new CancellingDiscriminator( -1 );

        graph.setStartNode( fork );
        graph.addNode( fork );
        graph.addNode( node2 );
        graph.addNode( node3 );
        graph.addNode( join );
        graph.addTransition( new TransitionImpl( "1_2", fork, node2 ) );
        graph.addTransition( new TransitionImpl( "1_3", fork, node3 ) );
        graph.addTransition( new TransitionImpl( node2, join ) );
        graph.addTransition( new TransitionImpl( node3, join ) );
        return graph;
    }

    /**
     * <pre>
     *          +--[signal(go)]--+
     * [1]--[AND]                [AND]--[6]
     *          +--[signal(go)]--+
     * </pre>
     */
    public Graph signal_parallel_pre_post(){
        GraphImpl graph = new GraphImpl( "signal_parallel_pre_post", 1 );

        Node node1 = createRecordPathNode( 1 );
        Node fork = new AndFork( 2 );
        Node node3 = new CatchSignal( 3, SIGNAL );
        Node node4 = new CatchSignal( 4, SIGNAL );
        Node join = new AndJoin( -2 );
        Node node6 = createRecordPathNode( 6 );

        graph.setStartNode( node1 );
        graph.addNode( fork );
        graph.addNode( node3 );
        graph.addNode( node4 );
        graph.addNode( join );
        graph.addNode( node6 );
        graph.addTransition( new TransitionImpl( node1, fork ) );
        graph.addTransition( new TransitionImpl( "2_3", fork, node3 ) );
        graph.addTransition( new TransitionImpl( "2_4", fork, node4 ) );
        graph.addTransition( new TransitionImpl( node3, join ) );
        graph.addTransition( new TransitionImpl( node4, join ) );
        graph.addTransition( new TransitionImpl( join, node6 ) );
        return graph;
    }

    /**
     * <pre>
     * [timer(60s)]
     * </pre>
     */
    public Graph timer_one(){
        GraphImpl graph = new GraphImpl( "timer_one", 1 );
        Node node1 = new CatchTimer( 1, TIMER_MS );
        graph.setStartNode( node1 );
        return graph;
    }

    /**
     * <pre>
     * [1]--[timer(60s)]--[3]
     * </pre>
     */
    public Graph timer_one_pre_post(){
        GraphImpl graph = new GraphImpl( "timer_one_pre_post", 1, true );

        Node node1 = createRecordPathNode( 1 );
        Node node2 = new CatchTimer( 2, TIMER_MS );
        Node node3 = createRecordPathNode( 3 );

        graph.setStartNode( node1 );
        graph.addNode( node2 );
        graph.addNode( node3 );
        graph.addTransition( new TransitionImpl( node1, node2 ) );
        graph.addTransition( new TransitionImpl( node2, node3 ) );
        return graph;
    }

    /**
     * <pre>
     * [timer(60s)]--[timer(60s)]
     * </pre>
     */
    public Graph timer_two(){
        GraphImpl graph = new GraphImpl( "timer_two", 1 );

        Node node1 = new CatchTimer( 1, TIMER_MS );
        Node node2 = new CatchTimer( 2, TIMER_MS );

        graph.setStartNode( node1 );
        graph.addNode( node2 );
        graph.addTransition( new TransitionImpl( node1, node2 ) );
        return graph;
    }

    /**
     * <pre>
     *          +--[timer(60s)]--+
     * [1]--[AND]                [AND]--[6]
     *          +--[timer(60s)]--+
     * </pre>
     */
    public Graph timer_parallel_pre_post(){
        GraphImpl graph = new GraphImpl( "timer_parallel_pre_post", 1 );

        Node node1 = createRecordPathNode( 1 );
        Node fork = new AndFork( 2 );
        Node node3 = new CatchTimer( 3, TIMER_MS );
        Node node4 = new CatchTimer( 4, TIMER_MS );
        Node join = new AndJoin( -2 );
        Node node6 = createRecordPathNode( 6 );

        graph.setStartNode( node1 );
        graph.addNode( fork );
        graph.addNode( node3 );
        graph.addNode( node4 );
        graph.addNode( join );
        graph.addNode( node6 );
        graph.addTransition( new TransitionImpl( node1, fork ) );
        graph.addTransition( new TransitionImpl( "2_3", fork, node3 ) );
        graph.addTransition( new TransitionImpl( "2_4", fork, node4 ) );
        graph.addTransition( new TransitionImpl( node3, join ) );
        graph.addTransition( new TransitionImpl( node4, join ) );
        graph.addTransition( new TransitionImpl( join, node6 ) );
        return graph;
    }

    /**
     * <pre>
     *          +--[signal(invoice)]--[escalation]--[5]---+
     * [1]--[AND]--[signal(payment)]--[escalation]--[8]---[AND]--[13]
     *          +--[signal(unknown)]--[escalation]--[11]--+
     * </pre>
     */
    public Graph escalation_three(){
        GraphImpl graph = new GraphImpl( "escalation_three", 1 );

        Node node1 = createRecordPathNode( 1 );
        Node fork = new AndFork( 2 );

        Node node3 = new CatchSignal( 3, "invoice" );
        Node node4 = new ThrowEscalation( 4 );
        Node node5 = createRecordPathNode( 5 );

        Node node6 = new CatchSignal( 6, "payment" );
        Node node7 = new ThrowEscalation( 7 );
        Node node8 = createRecordPathNode( 8 );

        Node node9 = new CatchSignal( 9, "unknown" );
        Node node10 = new ThrowEscalation( 10 );
        Node node11 = createRecordPathNode( 11 );

        Node join = new AndJoin( -2 );
        Node node13 = createRecordPathNode( 13 );

        graph.setStartNode( node1 );
        graph.addNode( fork );
        graph.addNode( node3 );
        graph.addNode( node4 );
        graph.addNode( node5 );
        graph.addNode( node6 );
        graph.addNode( node7 );
        graph.addNode( node8 );
        graph.addNode( node9 );
        graph.addNode( node10 );
        graph.addNode( node11 );
        graph.addNode( join );
        graph.addNode( node13 );
        graph.addTransition( new TransitionImpl( node1, fork ) );

        graph.addTransition( new TransitionImpl( "2_3", fork, node3 ) );
        graph.addTransition( new TransitionImpl( node3, node4 ) );
        graph.addTransition( new TransitionImpl( node4, node5 ) );
        graph.addTransition( new TransitionImpl( node5, join ) );

        graph.addTransition( new TransitionImpl( "2_6", fork, node6 ) );
        graph.addTransition( new TransitionImpl( node6, node7 ) );
        graph.addTransition( new TransitionImpl( node7, node8 ) );
        graph.addTransition( new TransitionImpl( node8, join ) );

        graph.addTransition( new TransitionImpl( "2_9", fork, node9 ) );
        graph.addTransition( new TransitionImpl( node9, node10 ) );
        graph.addTransition( new TransitionImpl( node10, node11 ) );
        graph.addTransition( new TransitionImpl( node11, join ) );

        graph.addTransition( new TransitionImpl( join, node13 ) );

        return graph;
    }

    /**
     * <pre>
     * [1]--[objectcall]--[3]
     * </pre>
     */
    public Graph objectcall_one_pre_post( Object target, String method, InputMapping<?>[] argumentsMappings, OutputMapping resultMapping ){
        GraphImpl graph = new GraphImpl( "objectcall_one_pre_post", 1 );

        Node node1 = createRecordPathNode( 1 );
        Node node2 = new ObjectCallActivity( 2, "objectcall", target, method, argumentsMappings, resultMapping );
        Node node3 = createRecordPathNode( 3 );

        graph.setStartNode( node1 );
        graph.addNode( node2 );
        graph.addNode( node3 );
        graph.addTransition( new TransitionImpl( node1, node2 ) );
        graph.addTransition( new TransitionImpl( node2, node3 ) );
        return graph;
    }

    /**
     * <pre>
     * [1]--[beanAsyncCall]--[3]
     * </pre>
     */
    public Graph beanasynccall_one_pre_post(){
        GraphImpl graph = new GraphImpl( "beanasynccall_one_pre_post", 1 );

        Node node1 = createRecordPathNode( 1 );
        Node node2 = new BeanAsyncCallActivity( 2, BEAN, METHOD, new InputMapping<?>[]{ConstantMapping.of( ARGUMENT )}, ValueMapping.of( RESULT_KEY ) );
        Node node3 = createRecordPathNode( 3 );

        graph.setStartNode( node1 );
        graph.addNode( node2 );
        graph.addNode( node3 );
        graph.addTransition( new TransitionImpl( node1, node2 ) );
        graph.addTransition( new TransitionImpl( node2, node3 ) );
        return graph;
    }

    /**
     * <pre>
     * [1]--[beanasynccall]--[3]
     * </pre>
     */
    public Graph beanasynccall_one_pre_post_special( String bean, String method, InputMapping<?>[] argumentsMappings, OutputMapping resultMapping ){
        GraphImpl graph = new GraphImpl( "beanasynccall_one_pre_post_special", 1, true );

        Node node1 = createRecordPathNode( 1 );
        Node node2 = new BeanAsyncCallActivity( 2, bean, method, argumentsMappings, resultMapping );
        Node node3 = createRecordPathNode( 3 );

        graph.setStartNode( node1 );
        graph.addNode( node2 );
        graph.addNode( node3 );
        graph.addTransition( new TransitionImpl( node1, node2 ) );
        graph.addTransition( new TransitionImpl( node2, node3 ) );
        return graph;
    }

    /**
     * <pre>
     * [beanasynccall]--[beanasynccall]
     * </pre>
     */
    public Graph beanasynccall_two(){
        GraphImpl graph = new GraphImpl( "beanasynccall_two", 1 );

        Node node1 = new BeanAsyncCallActivity( 1, BEAN, METHOD, new InputMapping<?>[]{ConstantMapping.of( ARGUMENT )}, ValueMapping.of( RESULT_KEY ) );
        Node node2 = new BeanAsyncCallActivity( 2, BEAN, METHOD, new InputMapping<?>[]{ConstantMapping.of( ARGUMENT )}, ValueMapping.of( RESULT_KEY ) );

        graph.setStartNode( node1 );
        graph.addNode( node2 );
        graph.addTransition( new TransitionImpl( node1, node2 ) );
        return graph;
    }

    /**
     * <pre>
     * [1]--[htask]--[3]
     * </pre>
     */
    public Graph human_task_one_pre_post(){
        GraphImpl graph = new GraphImpl( "human_task_one_pre_post", 1 );

        Node node1 = createRecordPathNode( 1 );

        MapMapping inputMapping = new MapMapping( Collections.singletonMap( ARGUMENT_KEY, (InputMapping<Object>)new ConstantMapping<Object>( ARGUMENT ) ) );
        Node node2 = new HumanTaskActivity( 2, ROLE, USER, inputMapping, new ValueMapping( RESULT_KEY ) );
        Node node3 = createRecordPathNode( 3 );

        graph.setStartNode( node1 );
        graph.addNode( node2 );
        graph.addNode( node3 );
        graph.addTransition( new TransitionImpl( node1, node2 ) );
        graph.addTransition( new TransitionImpl( node2, node3 ) );
        return graph;
    }

    /**
     * <pre>
     * [1]--[htask]--[3]
     * </pre>
     */
    public Graph human_task_one_pre_post_special( InputMapping<String> roleMapping,
                                                  InputMapping<String> userMapping,
                                                  InputMapping<Map<String, Object>> argumentsMapping,
                                                  OutputMapping resultMapping ){
        GraphImpl graph = new GraphImpl( "human_task_one_pre_post_special", 1 );

        Node node1 = createRecordPathNode( 1 );
        Node node2 = new HumanTaskActivity( 2, roleMapping, userMapping, argumentsMapping, resultMapping );
        Node node3 = createRecordPathNode( 3 );

        graph.setStartNode( node1 );
        graph.addNode( node2 );
        graph.addNode( node3 );
        graph.addTransition( new TransitionImpl( node1, node2 ) );
        graph.addTransition( new TransitionImpl( node2, node3 ) );
        return graph;
    }

    /**
     * <pre>
     * [htask]--[htask]
     * </pre>
     */
    public Graph human_task_two(){
        GraphImpl graph = new GraphImpl( "human_task_two", 1 );

        MapMapping inputMapping = new MapMapping( Collections.singletonMap( ARGUMENT_KEY, (InputMapping<Object>)new ConstantMapping<Object>( ARGUMENT ) ) );
        Node node1 = new HumanTaskActivity( 1, ROLE, USER, inputMapping, new ValueMapping( RESULT_KEY ) );
        Node node2 = new HumanTaskActivity( 2, ROLE, USER, inputMapping, new ValueMapping( RESULT_KEY ) );

        graph.setStartNode( node1 );
        graph.addNode( node2 );
        graph.addTransition( new TransitionImpl( node1, node2 ) );
        return graph;
    }

    /**
     * <pre>
     * [validate]--[validate]--[validate]--[validate]--[validate]--[value]
     * </pre>
     */
    public Graph validate_attribute(){
        GraphImpl graph = new GraphImpl( "validate_attribute", 1 );

        Node node1 = new ValidateAttributeActivity( 1, "required1", String.class );
        Node node2 = new ValidateAttributeActivity( 2, "required2", String.class, true );
        Node node3 = new ValidateAttributeActivity( 3, "optional1", String.class, false );
        Node node4 = new ValidateAttributeActivity( 4, "optional2", String.class, false, null );
        Node node5 = new ValidateAttributeActivity( 5, "optional3", String.class, false, "default" );
        Node node6 = new SetAttributeActivity( 6, "attribute", false );

        graph.setStartNode( node1 );
        graph.addNode( node2 );
        graph.addNode( node3 );
        graph.addNode( node4 );
        graph.addNode( node5 );
        graph.addNode( node6 );
        graph.addTransition( new TransitionImpl( node1, node2 ) );
        graph.addTransition( new TransitionImpl( node2, node3 ) );
        graph.addTransition( new TransitionImpl( node3, node4 ) );
        graph.addTransition( new TransitionImpl( node4, node5 ) );
        graph.addTransition( new TransitionImpl( node5, node6 ) );
        return graph;
    }

    public Graph error_no_start_node(){
        GraphImpl graph = new GraphImpl( "error_no_start_node", 1 );

        Node node1 = createRecordPathNode( 1 );
        Node node2 = createRecordPathNode( 2 );

        graph.addNode( node1 );
        graph.addNode( node2 );
        graph.addTransition( new TransitionImpl( node1, node2 ) );
        return graph;
    }

    /**
     * <pre>
     *     +-(condition_1)-[2]
     * [XOR]
     *                     [3]
     * </pre>
     */
    public Graph error_exclusivechoice_two(){
        GraphImpl graph = new GraphImpl( "error_exclusivechoice_two", 1 );

        XorFork fork = new XorFork( 1 );
        fork.addCondition( createAttributeEqualsCondition( VALUE1 ), "1_2" );
        fork.addCondition( createAttributeEqualsCondition( VALUE2 ), "1_3" );
        Node node2 = createRecordPathNode( 2 );
        Node node3 = createRecordPathNode( 3 );

        graph.setStartNode( fork );
        graph.addNode( node2 );
        graph.addNode( node3 );
        graph.addTransition( new TransitionImpl( "1_2", fork, node2 ) );
        return graph;
    }

    /**
     * <pre>
     *          +--[3]--+
     * [1]--[AND]       [CD]--[6]
     *             [4]--+
     * </pre>
     */
    public Graph error_cancelling_discriminator_two_pre_post(){
        GraphImpl graph = new GraphImpl( "error_cancelling_discriminator_two_pre_post", 1 );

        Node node1 = createRecordPathNode( 1 );
        Node fork = new AndFork( 2 );
        Node node3 = createRecordPathNode( 3 );
        Node node4 = createRecordPathNode( 4 );
        Node join = new CancellingDiscriminator( -2 );
        Node node6 = createRecordPathNode( 6 );

        graph.setStartNode( node1 );
        graph.addNode( fork );
        graph.addNode( node3 );
        graph.addNode( node4 );
        graph.addNode( join );
        graph.addNode( node6 );
        graph.addTransition( new TransitionImpl( node1, fork ) );
        graph.addTransition( new TransitionImpl( "2_3", fork, node3 ) );
        graph.addTransition( new TransitionImpl( node3, join ) );
        graph.addTransition( new TransitionImpl( node4, join ) );
        graph.addTransition( new TransitionImpl( join, node6 ) );
        return graph;
    }

    public Graph error_reserved_variable_name(){
        GraphImpl graph = new GraphImpl( "error_no_start_node", 1 );

        Node node1 = new SetAttributeActivity( 1, "NOW", "illegal variable 1" );
        Node node2 = new SetAttributeActivity( 2, "WORKFLOW_INSTANCE_ID", "illegal variable 2" );

        graph.setStartNode( node1 );
        graph.addNode( node2 );
        graph.addTransition( new TransitionImpl( node1, node2 ) );
        return graph;
    }

    /**
     * <pre>
     * [1]--[new_instance]
     * </pre>
     */
    public Graph create_new_instance( String name ){
        GraphImpl graph = new GraphImpl( "create_new_instance", 1 );

        Node node1 = new SetAttributeActivity( 1, "name", "Heli Kopter" );
        MapMapping mapMapping = new MapMapping();
        mapMapping.addEntryMapping( "other", new AttributeMapping<Object>( "name" ) );
        Node node2 = new CreateNewInstanceActivity( 2, name, null, "a", "b", mapMapping );

        graph.setStartNode( node1 );
        graph.addNode( node2 );
        graph.addTransition( new TransitionImpl( node1, node2 ) );
        return graph;
    }

    /**
     * <pre>
     * [1]--[new_instance]
     * </pre>
     */
    public Graph create_new_instance( String name, Integer version, String label1, String label2, MapMapping mapMapping ){
        GraphImpl graph = new GraphImpl( "create_new_instance", 1 );

        Node node1 = createRecordPathNode( 1 );
        Node node2 = new CreateNewInstanceActivity( 2, name, version, label1, label2, mapMapping );

        graph.setStartNode( node1 );
        graph.addNode( node2 );
        graph.addTransition( new TransitionImpl( node1, node2 ) );
        return graph;
    }

    private Node createRecordPathNode( int id ){
        return new ScriptActivity( id, new RecordPathScript( id ) );
    }

    private Condition createAttributeEqualsCondition( Object testValue ){
        return new AttributeEqualsCondition( CONDITION_TEST_ATTRIBUTE, testValue );
    }

}
