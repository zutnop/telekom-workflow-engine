package ee.telekom.workflow.api;

/**
 * Same methods as in GraphFactory, but builds the graph via WorkflowFactory DSL
 * 
 * @author Erko Hansar
 */
public class DslFactory{

    public static WorkflowFactoryImpl createWorkFlowFactory( String name, int version ){
        return new WorkflowFactoryImpl( name, version );
    }

    /**
     * <pre>
     * [1]
     * </pre>
     */
    public static WorkflowFactory sequence_one(){
        WorkflowFactory factory = createWorkFlowFactory( "sequence_one", 1 );

        /* @formatter:off */
        factory
            .start()
            .variable( "path" ).value( 1, "1" )
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     * [1]--[2]
     * </pre>
     */
    public static WorkflowFactory sequence_two(){
        WorkflowFactory factory = createWorkFlowFactory( "sequence_two", 1 );

        /* @formatter:off */
        factory
            .start()
            .variable( "path" ).value( 1, "1" )
            .variable( "path" ).value( 2, "2" )
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     * [1]--[2]--[3]
     * </pre>
     */
    public static WorkflowFactory sequence_three(){
        WorkflowFactory factory = createWorkFlowFactory( "sequence_three", 1 );

        /* @formatter:off */
        factory
            .start()
            .variable( "path" ).value( 1, "1" )
            .variable( "path" ).value( 2, "2" )
            .variable( "path" ).value( 3, "3" )
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     * [AND]--[2]
     * </pre>
     */
    public static WorkflowFactory split_one(){
        throw new UnsupportedOperationException( "incomplete graph blocks are not supported in DSL" );
    }

    /**
     * <pre>
     *     +--[2]
     * [AND]
     *     +--[3]
     * </pre>
     */
    public static WorkflowFactory split_two(){
        throw new UnsupportedOperationException( "incomplete graph blocks are not supported in DSL" );
    }

    /**
     * <pre>
     *     +--[2]
     * [AND]--[3]
     *     +--[4]
     * </pre>
     */
    public static WorkflowFactory split_three(){
        throw new UnsupportedOperationException( "incomplete graph blocks are not supported in DSL" );
    }

    /**
     * <pre>
     *          +--[3]
     * [1]--[AND]
     *          +--[4]
     * </pre>
     */
    public static WorkflowFactory split_two_pre(){
        throw new UnsupportedOperationException( "incomplete graph blocks are not supported in DSL" );
    }

    /**
     * <pre>
     * [AND]--[2]--[AND]
     * </pre>
     */
    public static WorkflowFactory synchronzation_one(){
        WorkflowFactory factory = createWorkFlowFactory( "synchronzation_one", 1 );

        /* @formatter:off */
        factory
            .start()
            
            .split(1)
                .branch()
                    .variable( "path" ).value( 2, "2" )
            .joinAll()
            
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     *     +--[2]--+
     * [AND]       [AND]
     *     +--[3]--+
     * </pre>
     */
    public static WorkflowFactory synchronzation_two(){
        WorkflowFactory factory = createWorkFlowFactory( "synchronzation_two", 1 );

        /* @formatter:off */
        factory
            .start()
            
            .split(1)
                .branch()
                    .variable( "path" ).value( 2, "2" )
                .branch()
                    .variable( "path" ).value( 3, "3" )
            .joinAll()
            
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     *     +--[2]--+
     * [AND]--[3]--[AND]
     *     +--[4]--+
     * </pre>
     */
    public static WorkflowFactory synchronzation_three(){
        WorkflowFactory factory = createWorkFlowFactory( "synchronzation_three", 1 );

        /* @formatter:off */
        factory
            .start()
            
            .split(1)
                .branch()
                    .variable( "path" ).value( 2, "2" )
                .branch()
                    .variable( "path" ).value( 3, "3" )
                .branch()
                    .variable( "path" ).value( 4, "4" )
            .joinAll()
            
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     *      +-------+
     *  [AND]       [AND]
     *      +--[2]--+
     * </pre>
     */
    public static WorkflowFactory synchronzation_two_firstBranchEmpty(){
        WorkflowFactory factory = createWorkFlowFactory( "synchronzation_two_firstBranchEmpty", 1 );

        /* @formatter:off */
        factory
            .start()
            
            .split(1)
                .branch()
                .branch()
                    .variable( "path" ).value( 2, "2" )
            .joinAll()
            
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     *      +--[2]--+
     *  [AND]       [AND]
     *      +-------+
     * </pre>
     */
    public static WorkflowFactory synchronzation_two_secondBranchEmpty(){
        WorkflowFactory factory = createWorkFlowFactory( "synchronzation_two_secondBranchEmpty", 1 );

        /* @formatter:off */
        factory
            .start()
            
            .split(1)
                .branch()
                    .variable( "path" ).value( 2, "2" )
                .branch()
            .joinAll()
            
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     *      +--+
     *  [AND]  [AND]
     *      +--+
     * </pre>
     */
    public static WorkflowFactory synchronzation_two_bothBranchesEmpty(){
        throw new UnsupportedOperationException( "DSL optimizes empty brances if there are other existing branches already in place, so there will be only "
                + "max 1 empty branch (or none, if a non-empty branch exists). So this can't be compared against GraphFactory tests." );
    }

    /**
     * <pre>
     *          +--[3]--+
     * [1]--[AND]       [AND]--[6]
     *          +--[4]--+
     * </pre>
     */
    public static WorkflowFactory synchronzation_two_pre_post(){
        WorkflowFactory factory = createWorkFlowFactory( "synchronzation_two_pre_post", 1 );

        /* @formatter:off */
        factory
            .start()
            
            .variable( "path" ).value( 1, "1" )
            .split(2)
                .branch()
                    .variable( "path" ).value( 3, "3" )
                .branch()
                    .variable( "path" ).value( 4, "4" )
            .joinAll()
            .variable( "path" ).value( 6, "6" )
            
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     *          +-------+
     * [1]--[AND]       [AND]--[6]
     *          +--[3]--+
     * </pre>
     */
    public static WorkflowFactory synchronization_firstBranchEmpty_pre_post(){
        WorkflowFactory factory = createWorkFlowFactory( "synchronization_firstBranchEmpty_pre_post", 1 );

        /* @formatter:off */
        factory
            .start()
            
            .variable( "path" ).value( 1, "1" )
            .split(2)
                .branch()
                .branch()
                    .variable( "path" ).value( 3, "3" )
            .joinAll()
            .variable( "path" ).value( 6, "6" )
            
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     *          +--[3]--+
     * [1]--[AND]       [AND]--[6]
     *          +-------+
     * </pre>
     */
    public static WorkflowFactory synchronization_secondBranchEmpty_pre_post(){
        WorkflowFactory factory = createWorkFlowFactory( "synchronization_secondBranchEmpty_pre_post", 1 );

        /* @formatter:off */
        factory
            .start()
            
            .variable( "path" ).value( 1, "1" )
            .split(2)
                .branch()
                    .variable( "path" ).value( 3, "3" )
                .branch()
            .joinAll()
            .variable( "path" ).value( 6, "6" )
            
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     *          +--+
     * [1]--[AND]  [AND]--[4]
     *          +--+
     * </pre>
     */
    public static WorkflowFactory synchronization_bothBranchesEmpty_pre_post(){
        throw new UnsupportedOperationException( "DSL optimizes empty brances if there are other existing branches already in place, so there will be only "
                + "max 1 empty branch (or none, if a non-empty branch exists). So this can't be compared against GraphFactory tests." );
    }

    /**
     * <pre>
     * [XOR]-(condition_1)-[2]
     * </pre>
     */
    public static WorkflowFactory exclusivechoice_one(){
        throw new UnsupportedOperationException( "incomplete graph blocks are not supported in DSL" );
    }

    /**
     * <pre>
     *     +-(condition_1)-[2]
     * [XOR]
     *     +-(condition_2)-[3]
     * </pre>
     */
    public static WorkflowFactory exclusivechoice_two(){
        throw new UnsupportedOperationException( "incomplete graph blocks are not supported in DSL" );
    }

    /**
     * <pre>
     *     +-(${condition_1})-[2]
     * [XOR]
     *     +-(${condition_2})-[3]
     * </pre>
     */
    public static WorkflowFactory exclusivechoice_two_expressionlanguage(){
        throw new UnsupportedOperationException( "incomplete graph blocks are not supported in DSL" );
    }

    /**
     * <pre>
     * [XOR]-(default)-[2]
     * </pre>
     */
    public static WorkflowFactory exclusivechoice_default(){
        throw new UnsupportedOperationException( "incomplete graph blocks are not supported in DSL" );
    }

    /**
     * <pre>
     *     +-(condition_1)-[2]
     * [XOR]
     *     +---(default)---[3]
     * </pre>
     */
    public static WorkflowFactory exclusivechoice_one_default(){
        throw new UnsupportedOperationException( "incomplete graph blocks are not supported in DSL" );
    }

    /**
     * <pre>
     *     +-(condition_1)-[2]
     * [XOR]
     *     +-(condition_1)-[3]
     * </pre>
     */
    public static WorkflowFactory exclusivechoice_twoConditionsTrue(){
        throw new UnsupportedOperationException( "incomplete graph blocks are not supported in DSL" );
    }

    /**
     * <pre>
     * [XOR]-(default)-[2]--[XOR]
     * </pre>
     */
    public static WorkflowFactory simplemerge_one(){
        throw new UnsupportedOperationException( "DSL does not support ELSE block without an IF block" );
    }

    /**
     * <pre>
     * [XOR]-(default)-[2]--[XOR]--[4]
     * </pre>
     */
    public static WorkflowFactory simplemerge_one_post(){
        throw new UnsupportedOperationException( "DSL does not support ELSE block without an IF block" );
    }

    /**
     * <pre>
     *     +-(condition_1)-[2]--+
     * [XOR]                    [XOR]
     *     +---(default)---[3]--+
     * </pre>
     */
    public static WorkflowFactory simplemerge_two(){
        WorkflowFactory factory = createWorkFlowFactory( "simplemerge_two", 1 );

        /* @formatter:off */
        factory
            .start()
            
            .if_(1, "conditionAttribute == 'value1'")
                .variable( "path" ).value( 2, "2" )
            .else_()
                .variable( "path" ).value( 3, "3" )
            .endIf()
            
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     *     +-(condition_1)-[2]--+
     * [XOR]                    [XOR]--[5]
     *     +---(default)---[3]--+
     * </pre>
     */
    public static WorkflowFactory simplemerge_two_post(){
        WorkflowFactory factory = createWorkFlowFactory( "simplemerge_two_post", 1 );

        /* @formatter:off */
        factory
            .start()
            
            .if_(1, "conditionAttribute == 'value1'")
                .variable( "path" ).value( 2, "2" )
            .else_()
                .variable( "path" ).value( 3, "3" )
            .endIf()
            .variable( "path" ).value( 5, "5" )
            
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     * [OR]-(condition_1)-[2]
     * </pre>
     */
    public static WorkflowFactory multiplechoice_one(){
        throw new UnsupportedOperationException( "incomplete graph blocks are not supported in DSL" );
    }

    /**
     * <pre>
     *    +-(condition_1)-[2]
     * [OR]
     *    +-(condition_2)-[3]
     * </pre>
     */
    public static WorkflowFactory multiplechoice_two(){
        throw new UnsupportedOperationException( "incomplete graph blocks are not supported in DSL" );
    }

    /**
     * <pre>
     * [OR]-(default)-[2]
     * </pre>
     */
    public static WorkflowFactory multiplechoice_default(){
        throw new UnsupportedOperationException( "incomplete graph blocks are not supported in DSL" );
    }

    /**
     * <pre>
     *    +-(condition_1)-[2]
     * [OR]
     *    +---(default)---[3]
     * </pre>
     */
    public static WorkflowFactory multiplechoice_one_default(){
        throw new UnsupportedOperationException( "incomplete graph blocks are not supported in DSL" );
    }

    /**
     * <pre>
     *    +-(condition_1)-[2]
     * [OR]
     *    +-(condition_1)-[3]
     * </pre>
     */
    public static WorkflowFactory multiplechoice_twoConditionsTrue(){
        throw new UnsupportedOperationException( "incomplete graph blocks are not supported in DSL" );
    }

    /**
     * <pre>
     * [AND]--[2]--[CD]
     * </pre>
     */
    public static WorkflowFactory cancelling_discriminator_one(){
        WorkflowFactory factory = createWorkFlowFactory( "cancelling_discriminator_one", 1 );

        /* @formatter:off */
        factory
            .start()
            
            .split(1)
                .branch()
                    .variable( "path" ).value( 2, "2" )
            .joinFirst()
            
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     * [AND]--[2]--[CD]--[4]
     * </pre>
     */
    public static WorkflowFactory cancelling_discriminator_one_post(){
        WorkflowFactory factory = createWorkFlowFactory( "cancelling_discriminator_one_post", 1 );

        /* @formatter:off */
        factory
            .start()
            
            .split(1)
                .branch()
                    .variable( "path" ).value( 2, "2" )
            .joinFirst()
            .variable( "path" ).value( 4, "2" )
            
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     *     +--[2]--+
     * [AND]       [CD]
     *     +--[3]--+
     * </pre>
     */
    public static WorkflowFactory cancelling_discriminator_two(){
        WorkflowFactory factory = createWorkFlowFactory( "cancelling_discriminator_two", 1 );

        /* @formatter:off */
        factory
            .start()
            
            .split(1)
                .branch()
                    .variable( "path" ).value( 2, "2" )
                .branch()
                    .variable( "path" ).value( 3, "3" )
            .joinFirst()
            
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     *      +-------+
     *  [AND]       [CD]
     *      +--[2]--+
     * </pre>
     */
    public static WorkflowFactory cancelling_discriminator_two_firstBranchEmpty(){
        WorkflowFactory factory = createWorkFlowFactory( "cancelling_discriminator_two_firstBranchEmpty", 1 );

        /* @formatter:off */
        factory
            .start()
            
            .split(1)
                .branch()
                .branch()
                    .variable( "path" ).value( 2, "2" )
            .joinFirst()
            
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     *      +--[2]--+
     *  [AND]       [CD]
     *      +-------+
     * </pre>
     */
    public static WorkflowFactory cancelling_discriminator_two_secondBranchEmpty(){
        WorkflowFactory factory = createWorkFlowFactory( "cancelling_discriminator_two_secondBranchEmpty", 1 );

        /* @formatter:off */
        factory
            .start()
            
            .split(1)
                .branch()
                    .variable( "path" ).value( 2, "2" )
                .branch()
            .joinFirst()
            
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     *          +--[3]--+
     * [1]--[AND]       [CD]--[6]
     *          +--[4]--+
     * </pre>
     */
    public static WorkflowFactory cancelling_discriminator_two_pre_post(){
        WorkflowFactory factory = createWorkFlowFactory( "cancelling_discriminator_two_pre_post", 1 );

        /* @formatter:off */
        factory
            .start()
            
            .variable( "path" ).value( 1, "1" )
            .split(2)
                .branch()
                    .variable( "path" ).value( 3, "3" )
                .branch()
                    .variable( "path" ).value( 4, "4" )
            .joinFirst()
            .variable( "path" ).value( 6, "6" )
            
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     *          +-------+
     * [1]--[AND]       [CD]--[5]
     *          +--[3]--+
     * </pre>
     */
    public static WorkflowFactory cancelling_discriminator_two_firstBranchEmpty_pre_post(){
        WorkflowFactory factory = createWorkFlowFactory( "cancelling_discriminator_two_firstBranchEmpty_pre_post", 1 );

        /* @formatter:off */
        factory
            .start()
            
            .variable( "path" ).value( 1, "1" )
            .split(2)
                .branch()
                .branch()
                    .variable( "path" ).value( 3, "3" )
            .joinFirst()
            .variable( "path" ).value( 5, "5" )
            
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     *          +--[3]--+
     * [1]--[AND]       [CD]--[6]
     *          +-------+
     * </pre>
     */
    public static WorkflowFactory cancelling_discriminator_two_secondBranchEmpty_pre_post(){
        WorkflowFactory factory = createWorkFlowFactory( "cancelling_discriminator_two_secondBranchEmpty_pre_post", 1 );

        /* @formatter:off */
        factory
            .start()
            
            .variable( "path" ).value( 1, "1" )
            .split(2)
                .branch()
                    .variable( "path" ).value( 3, "3" )
                .branch()
            .joinFirst()
            .variable( "path" ).value( 6, "6" )
            
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     *          +--[3]--+              +--[8]--+
     * [1]--[AND]       [CD]--[6]--[AND]       [CD]--[11]
     *          +--[4]--+              +--[9]--+
     * </pre>
     */
    public static WorkflowFactory cancelling_discriminator_twice(){
        WorkflowFactory factory = createWorkFlowFactory( "cancelling_discriminator_twice", 1 );

        /* @formatter:off */
        factory
            .start()
            
            .variable( "path" ).value( 1, "1" )
            .split(2)
                .branch()
                    .variable( "path" ).value( 3, "3" )
                .branch()
                    .variable( "path" ).value( 4, "4" )
            .joinFirst()
            .variable( "path" ).value( 6, "6" )
            .split(7)
                .branch()
                    .variable( "path" ).value( 8, "8" )
                .branch()
                    .variable( "path" ).value( 9, "9" )
            .joinFirst()
            .variable( "path" ).value( 11, "11" )
            
            .end();
        /* @formatter:on */

        return factory;
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
    public static WorkflowFactory cancelling_discriminator_and_nested(){
        WorkflowFactory factory = createWorkFlowFactory( "cancelling_discriminator_and_nested", 1 );

        /* @formatter:off */
        factory
            .start()
            
            .variable( "path" ).value( 1, "1" )
            .split(2)
                .branch()
                    .variable( "path" ).value( 3, "3" )
                    .split(4)
                        .branch()
                            .waitSignal(5, "1")
                        .branch()
                            .waitSignal(6, "2")
                    .joinFirst()
                    .variable( "path" ).value( 8, "8" )
                .branch()
                    .variable( "path" ).value( 9, "9" )
                    .split(10)
                        .branch()
                            .waitSignal(11, "3")
                        .branch()
                            .waitSignal(12, "4")
                    .joinFirst()
                    .variable( "path" ).value( 14, "14" )
            .joinFirst()
            .variable( "path" ).value( 16, "16" )
    
            .end();
        /* @formatter:on */

        return factory;
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
    public static WorkflowFactory cancelling_discriminator_or_nested(){
        WorkflowFactory factory = createWorkFlowFactory( "cancelling_discriminator_or_nested", 1 );

        /* @formatter:off */
        factory
            .start()
            
            .variable( "path" ).value( 1, "1" )
            .split(2)
                .branch()
                    .variable( "path" ).value( 3, "3" )
                    .split(4)
                        .branch("true")
                            .waitSignal(5, "1")
                        .branch("true")
                            .waitSignal(6, "2")
                    .joinFirst()
                    .variable( "path" ).value( 8, "8" )
                .branch()
                    .variable( "path" ).value( 9, "9" )
                    .split(10)
                        .branch("true")
                            .waitSignal(11, "3")
                        .branch("true")
                            .waitSignal(12, "4")
                    .joinFirst()
                    .variable( "path" ).value( 14, "14" )
            .joinFirst()
            .variable( "path" ).value( 16, "16" )

            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     *  [1]--[XOR]
     *   |     |(ExecutionCountCondition)
     *   +--<--+
     * </pre>
     */
    public static WorkflowFactory loop_one( int executionCount ){
        WorkflowFactory factory = createWorkFlowFactory( "loop_one", 1 );

        /* @formatter:off */
        factory
            .start()
            
            .variable( "executionCount" ).value( 100, 0 )
            .doWhile()
                .variable( "executionCount" ).value( 101, "${executionCount + 1}" )
                .variable( "path" ).value( 1, "1" )
            .doWhile(2, "executionCount < " + executionCount)
            
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     *  [1]--[XOR]--[3]
     *   |     |(ExecutionCountCondition)
     *   +--<--+
     * </pre>
     */
    public static WorkflowFactory loop_one_special( int executionCount ){
        WorkflowFactory factory = createWorkFlowFactory( "loop_one_special", 1 );

        /* @formatter:off */
        factory
            .start()
            
            .variable( "executionCount" ).value( 100, 0 )
            .doWhile()
                .variable( "executionCount" ).value( 101, "${executionCount + 1}" )
                .variable( "path" ).value( 1, "1" )
            .doWhile(2, "executionCount < " + executionCount)
            .variable( "path" ).value( 3, "3" )
            
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     * [1]--[2]--[XOR]
     *   |         |(ExecutionCountCondition)
     *   +----<----+
     * </pre>
     */
    public static WorkflowFactory loop_two( int executionCount ){
        WorkflowFactory factory = createWorkFlowFactory( "loop_two", 1 );

        /* @formatter:off */
        factory
            .start()
            
            .variable( "executionCount" ).value( 100, 0 )
            .doWhile()
                .variable( "executionCount" ).value( 101, "${executionCount + 1}" )
                .variable( "path" ).value( 1, "1" )
                .variable( "path" ).value( 2, "2" )
            .doWhile(3, "executionCount < " + executionCount)
            
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     *        +------------------------------------------[4]
     *        |
     * [1]--[XOR]--(ExecutionCountCondition)--[3]
     *        |                                |
     *        +--------------<-----------------+
     * </pre>
     */
    public static WorkflowFactory loop_while( int executionCount ){
        WorkflowFactory factory = createWorkFlowFactory( "loop_while", 1 );

        /* @formatter:off */
        factory
            .start()
            
            .variable( "path" ).value( 1, "1" )
            .variable( "executionCount" ).value( 100, 0 )
            .whileDo( 2, "executionCount < " + executionCount )
                .variable( "executionCount" ).value( 101, "${executionCount + 1}" )
                .variable( "path" ).value( 3, "3" )
            .whileDo()
            .variable( "path" ).value( 4, "4" )
            
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     *          +--[3]--+
     * [1]--[AND]       [AND]--[6]--[XOR]
     *  |       +--[4]--+             |(ExecutionCountCondition)
     *  +--------------<--------------+
     * </pre>
     */
    public static WorkflowFactory loop_andForkJoin_twoThreads_before_after( int executionCount ){
        WorkflowFactory factory = createWorkFlowFactory( "loop_andForkJoin_twoThreads_before_after", 1 );

        /* @formatter:off */
        factory
            .start()
            
            .variable( "executionCount" ).value( 100, 0 )
            .doWhile()
                .variable( "executionCount" ).value( 101, "${executionCount + 1}" )
                .variable( "path" ).value( 1, "1" )
                .split(2)
                    .branch()
                        .variable( "path" ).value( 3, "3" )
                    .branch()
                        .variable( "path" ).value( 4, "4" )
                .joinAll()
                .variable( "path" ).value( 6, "6" )
            .doWhile(7, "executionCount < " + executionCount)
            
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     *          +--[3]--+              +--[8]--+
     * [1]--[AND]       [CD]--[6]--[AND]       [CD]--[11]--[XOR]
     *  |       +--[4]--+              +--[9]--+             |(ExecutionCountCondition)
     *  +----------------------<-----------------------------+
     * </pre>
     */
    public static WorkflowFactory loop_cancelling_discriminator_twice( int executionCount ){
        WorkflowFactory factory = createWorkFlowFactory( "loop_cancelling_discriminator_twice", 1 );

        /* @formatter:off */
        factory
            .start()
            
            .variable( "executionCount" ).value( 100, 0 )
            .doWhile()
                .variable( "executionCount" ).value( 101, "${executionCount + 1}" )
                .variable( "path" ).value( 1, "1" )
                .split(2)
                    .branch()
                        .variable( "path" ).value( 3, "3" )
                    .branch()
                        .variable( "path" ).value( 4, "4" )
                .joinFirst()
                .variable( "path" ).value( 6, "6" )
                .split(7)
                    .branch()
                        .variable( "path" ).value( 8, "8" )
                    .branch()
                        .variable( "path" ).value( 9, "9" )
                .joinFirst()
                .variable( "path" ).value( 11, "11" )
            .doWhile(12, "executionCount < " + executionCount)
            
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     * [signal(go)]
     * </pre>
     */
    public static WorkflowFactory signal_one(){
        WorkflowFactory factory = createWorkFlowFactory( "signal_one", 1 );

        /* @formatter:off */
        factory
            .start()
            
            .waitSignal(1, "go")
            
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     * [1]--[signal(go)]--[3]
     * </pre>
     */
    public static WorkflowFactory signal_one_pre_post(){
        WorkflowFactory factory = createWorkFlowFactory( "signal_one_pre_post", 1 );

        /* @formatter:off */
        factory
            .start()
            
            .variable( "path" ).value( 1, "1" )
            .variable( "result_key" ).waitSignal(2, "go")
            .variable( "path" ).value( 3, "3" )
            
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     * [signal(go)]--[signal(go)]
     * </pre>
     */
    public static WorkflowFactory signal_two(){
        WorkflowFactory factory = createWorkFlowFactory( "signal_two", 1 );

        /* @formatter:off */
        factory
            .start()
            
            .variable( "result_key" ).waitSignal(1, "go")
            .variable( "result_key" ).waitSignal(2, "go")
            
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     *     +--[signal(go)]--+
     * [AND]                [AND]
     *     +--[signal(go)]--+
     * </pre>
     */
    public static WorkflowFactory signal_andJoin(){
        WorkflowFactory factory = createWorkFlowFactory( "signal_andJoin", 1 );

        /* @formatter:off */
        factory
            .start()
            
            .split(1)
                .branch()
                    .variable( "result_key" ).waitSignal(2, "go")
                .branch()
                    .variable( "result_key" ).waitSignal(3, "go")
            .joinAll()
            
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     *     +--[signal(go)]--+
     * [AND]                [AND]
     *     +--[signal(go)]--+
     * </pre>
     */
    public static WorkflowFactory signal_cancellingDiscriminator(){
        WorkflowFactory factory = createWorkFlowFactory( "signal_cancellingDiscriminator", 1 );

        /* @formatter:off */
        factory
            .start()
            
            .split(1)
                .branch()
                    .variable( "result_key" ).waitSignal(2, "go")
                .branch()
                    .variable( "result_key" ).waitSignal(3, "go")
            .joinFirst()
            
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     *          +--[signal(go)]--+
     * [1]--[AND]                [AND]--[6]
     *          +--[signal(go)]--+
     * </pre>
     */
    public static WorkflowFactory signal_parallel_pre_post(){
        WorkflowFactory factory = createWorkFlowFactory( "signal_parallel_pre_post", 1 );

        /* @formatter:off */
        factory
            .start()
            
            .variable( "path" ).value( 1, "1" )
            .split(2)
                .branch()
                    .waitSignal(3, "go")
                .branch()
                    .waitSignal(4, "go")
            .joinAll()
            .variable( "path" ).value( 6, "6" )
            
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     * [timer(60s)]
     * </pre>
     */
    public static WorkflowFactory timer_one(){
        WorkflowFactory factory = createWorkFlowFactory( "timer_one", 1 );

        /* @formatter:off */
        factory
            .start()
            
            .waitTimer(1, "60000")
            
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     * [1]--[timer(60s)]--[3]
     * </pre>
     */
    public static WorkflowFactory timer_one_pre_post(){
        WorkflowFactory factory = createWorkFlowFactory( "timer_one_pre_post", 1 );

        /* @formatter:off */
        factory
            .start()
        
            .variable( "path" ).value( 1, "1" )
            .waitTimer(2, "60000")
            .variable( "path" ).value( 3, "3" )
        
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     * [timer(60s)]--[timer(60s)]
     * </pre>
     */
    public static WorkflowFactory timer_two(){
        WorkflowFactory factory = createWorkFlowFactory( "timer_two", 1 );

        /* @formatter:off */
        factory
            .start()
            
            .waitTimer(1, "60000")
            .waitTimer(2, "60000")
            
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     *          +--[timer(60s)]--+
     * [1]--[AND]                [AND]--[6]
     *          +--[timer(60s)]--+
     * </pre>
     */
    public static WorkflowFactory timer_parallel_pre_post(){
        WorkflowFactory factory = createWorkFlowFactory( "timer_parallel_pre_post", 1 );

        /* @formatter:off */
        factory
            .start()
            
            .variable( "path" ).value( 1, "1" )
            .split(2)
                .branch()
                    .waitTimer(3, "60000")
                .branch()
                    .waitTimer(4, "60000")
            .joinAll()
            .variable( "path" ).value( 6, "6" )
            
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     *          +--[signal(invoice)]--[escalation]--[5]---+
     * [1]--[AND]--[signal(payment)]--[escalation]--[8]---[AND]--[13]
     *          +--[signal(unknown)]--[escalation]--[11]--+
     * </pre>
     */
    public static WorkflowFactory escalation_three(){
        WorkflowFactory factory = createWorkFlowFactory( "escalation_three", 1 );

        /* @formatter:off */
        factory
            .start()
            
            .variable( "path" ).value( 1, "1" )
            .split(2)
                .branch()
                    .waitSignal(3, "invoice")
                    .escalate(4)
                    .variable( "path" ).value( 5, "5" )                 
                .branch()
                    .waitSignal(6, "payment")
                    .escalate(7)
                    .variable( "path" ).value( 8, "8" )
                .branch()
                    .waitSignal(9, "unknown")
                    .escalate(10)
                    .variable( "path" ).value( 11, "11" )
            .joinAll()
            .variable( "path" ).value( 13, "13" )
            
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     * [1]--[objectcall]--[3]
     * </pre>
     */
    public static WorkflowFactory objectcall_one_pre_post(){
        throw new UnsupportedOperationException( "DSL does not support ObjectCallActivity for now" );
    }

    /**
     * <pre>
     * [1]--[beanAsyncCall]--[3]
     * </pre>
     */
    public static WorkflowFactory beanasynccall_one_pre_post(){
        WorkflowFactory factory = createWorkFlowFactory( "beanasynccall_one_pre_post", 1 );

        /* @formatter:off */
        factory
            .start()
        
            .variable( "path" ).value( 1, "1" )
            .variable( "result_key" ).callAsync(2, "bean1", "method1", "argument")
            .variable( "path" ).value( 3, "3" )
        
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     * [beanasynccall]--[beanasynccall]
     * </pre>
     */
    public static WorkflowFactory beanasynccall_two(){
        WorkflowFactory factory = createWorkFlowFactory( "beanasynccall_two", 1 );

        /* @formatter:off */
        factory
            .start()
            
            .variable( "result_key" ).callAsync(1, "bean1", "method1", "argument")
            .variable( "result_key" ).callAsync(2, "bean1", "method1", "argument")
            
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     * [1]--[htask]--[3]
     * </pre>
     */
    public static WorkflowFactory human_task_one_pre_post(){
        WorkflowFactory factory = createWorkFlowFactory( "human_task_one_pre_post", 1 );

        /* @formatter:off */
        factory
            .start()
        
            .variable( "path" ).value( 1, "1" )
            .variable( "result_key" ).humanTask(2,  "role1", "user1").withAttribute( "argument", "argument" ).done()
            .variable( "path" ).value( 3, "3" )
        
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     * [htask]--[htask]
     * </pre>
     */
    public static WorkflowFactory human_task_two(){
        WorkflowFactory factory = createWorkFlowFactory( "human_task_two", 1 );

        /* @formatter:off */
        factory
            .start()
            
            .variable( "result_key" ).humanTask(1,  "role1", "user1").withAttribute( "argument", "argument" ).done()
            .variable( "result_key" ).humanTask(2,  "role1", "user1").withAttribute( "argument", "argument" ).done()
            
            .end();
        /* @formatter:on */

        return factory;
    }

    /**
     * <pre>
     * [validate]--[validate]--[validate]--[validate]--[validate]--[value]
     * </pre>
     */
    public static WorkflowFactory validate_input_variable(){
        WorkflowFactory factory = createWorkFlowFactory( "validate_attribute", 1 );

        /* @formatter:off */
        factory
            .start()
            
            .validateInputVariable( 1, "required1", String.class )
            .validateInputVariable( 2, "required2", String.class, true )
            .validateInputVariable( 3, "optional1", String.class, false )
            .validateInputVariable( 4, "optional2", String.class, false, null )
            .validateInputVariable( 5, "optional3", String.class, false, "default" )
            .variable( "attribute" ).value( 6, false )
            
            .end();
        /* @formatter:on */

        return factory;
    }

}
