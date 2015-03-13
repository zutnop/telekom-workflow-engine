package ee.telekom.workflow.api;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;

import ee.telekom.workflow.graph.Graph;
import ee.telekom.workflow.graph.GraphValidator;
import ee.telekom.workflow.graph.Node;
import ee.telekom.workflow.graph.RecordPathScript;
import ee.telekom.workflow.graph.Transition;
import ee.telekom.workflow.graph.core.GraphValidatorImpl;
import ee.telekom.workflow.graph.node.activity.BeanAsyncCallActivity;
import ee.telekom.workflow.graph.node.activity.BeanCallActivity;
import ee.telekom.workflow.graph.node.activity.HumanTaskActivity;
import ee.telekom.workflow.graph.node.activity.ObjectCallActivity;
import ee.telekom.workflow.graph.node.activity.ScriptActivity;
import ee.telekom.workflow.graph.node.activity.SetAttributeActivity;
import ee.telekom.workflow.graph.node.activity.ValidateAttributeActivity;
import ee.telekom.workflow.graph.node.event.CatchSignal;
import ee.telekom.workflow.graph.node.event.CatchTimer;
import ee.telekom.workflow.graph.node.event.ThrowEscalation;
import ee.telekom.workflow.graph.node.expression.Expression;
import ee.telekom.workflow.graph.node.expression.SimpleMethodCallExpression;
import ee.telekom.workflow.graph.node.gateway.AndFork;
import ee.telekom.workflow.graph.node.gateway.AndJoin;
import ee.telekom.workflow.graph.node.gateway.CancellingDiscriminator;
import ee.telekom.workflow.graph.node.gateway.OrFork;
import ee.telekom.workflow.graph.node.gateway.XorFork;
import ee.telekom.workflow.graph.node.gateway.XorJoin;
import ee.telekom.workflow.graph.node.gateway.condition.AttributeEqualsCondition;
import ee.telekom.workflow.graph.node.gateway.condition.Condition;
import ee.telekom.workflow.graph.node.gateway.condition.ExpressionLanguageCondition;
import ee.telekom.workflow.graph.node.input.ArrayMapping;
import ee.telekom.workflow.graph.node.input.AttributeMapping;
import ee.telekom.workflow.graph.node.input.ConstantMapping;
import ee.telekom.workflow.graph.node.input.DueDateMapping;
import ee.telekom.workflow.graph.node.input.ExpressionLanguageMapping;
import ee.telekom.workflow.graph.node.input.ExpressionMapping;
import ee.telekom.workflow.graph.node.input.InputMapping;
import ee.telekom.workflow.graph.node.input.MapMapping;
import ee.telekom.workflow.graph.node.output.MapEntryMapping;
import ee.telekom.workflow.graph.node.output.OutputMapping;
import ee.telekom.workflow.graph.node.output.ValueMapping;

/**
 * Helper class for API (workflow definition DSL) tests.
 *  
 * @author Erko Hansar
 */
public abstract class AbstractApiTest{

    /**
     * Builds a graph from the given factory and compares it to the given target.
     */
    protected static void assertBuildsTo( WorkflowFactory factory, Graph expected ){
        Graph actual = ((WorkflowFactoryImpl)factory).buildGraph();

        GraphValidator validator = new GraphValidatorImpl();
        List<String> errors = validator.validate( actual );
        Assert.assertEquals( "Graph has validation errors", 0, errors.size() );

        Assert.assertEquals( "Graph names don't match", expected.getName(), actual.getName() );
        Assert.assertEquals( "Graph versions don't match", expected.getVersion(), actual.getVersion() );

        Assert.assertEquals( "Number of graph nodes don't match", expected.getNodes().size(), actual.getNodes().size() );
        Iterator<Node> actualNodes = actual.getNodes().iterator();
        Iterator<Node> expectedNodes = expected.getNodes().iterator();
        while( actualNodes.hasNext() ){
            Node e = expectedNodes.next();
            Node a = actualNodes.next();
            Assert.assertEquals( "Graph node ids don't match " + e + " " + a, e.getId(), a.getId() );
            Assert.assertEquals( "Graph node names don't match " + e + " " + a, e.getName(), a.getName() );

            if( e instanceof ScriptActivity && a instanceof SetAttributeActivity ){
                // special case for comparing DSL graphs against GraphFactory graphs with RecordPathScript
                assertNodeEquals( (ScriptActivity)e, (SetAttributeActivity)a );
                continue;
            }

            Assert.assertEquals( "Graph node classes don't match", e.getClass(), a.getClass() );

            // some of the Node subclasses need special checks to be more useful, like bean and method check for BeanCallActivity, etc.
            if( a instanceof OrFork ){
                assertNodeEquals( (OrFork)e, (OrFork)a );
            }
            else if( a instanceof XorFork ){
                assertNodeEquals( (XorFork)e, (XorFork)a );
            }
            else if( a instanceof BeanAsyncCallActivity ){
                assertNodeEquals( (BeanAsyncCallActivity)e, (BeanAsyncCallActivity)a );
            }
            else if( a instanceof BeanCallActivity ){
                assertNodeEquals( (BeanCallActivity)e, (BeanCallActivity)a );
            }
            else if( a instanceof CatchSignal ){
                assertNodeEquals( (CatchSignal)e, (CatchSignal)a );
            }
            else if( a instanceof CatchTimer ){
                assertNodeEquals( (CatchTimer)e, (CatchTimer)a );
            }
            else if( a instanceof HumanTaskActivity ){
                assertNodeEquals( (HumanTaskActivity)e, (HumanTaskActivity)a );
            }
            else if( a instanceof ObjectCallActivity ){
                assertNodeEquals( (ObjectCallActivity)e, (ObjectCallActivity)a );
            }
            else if( a instanceof ScriptActivity ){
                assertNodeEquals( (ScriptActivity)e, (ScriptActivity)a );
            }
            else if( a instanceof SetAttributeActivity ){
                assertNodeEquals( (SetAttributeActivity)e, (SetAttributeActivity)a );
            }
            else if( a instanceof ValidateAttributeActivity ){
                assertNodeEquals( (ValidateAttributeActivity)e, (ValidateAttributeActivity)a );
            }
            else if( a instanceof AndFork || a instanceof AndJoin || a instanceof CancellingDiscriminator || a instanceof XorJoin
                    || a instanceof ThrowEscalation ){
                // do nothing
            }
            else{
                throw new UnsupportedOperationException( "Please improve the AbstractApiTest class to handle new Node type: " + a.getClass().getCanonicalName() );
            }
        }

        Assert.assertEquals( "Number of graph transitions don't match", expected.getTransitions().size(), actual.getTransitions().size() );
        Iterator<Transition> actualTransitions = actual.getTransitions().iterator();
        Iterator<Transition> expectedTransitions = expected.getTransitions().iterator();
        while( actualTransitions.hasNext() ){
            Transition a = actualTransitions.next();
            Transition e = expectedTransitions.next();
            Assert.assertEquals( "Graph transition names don't match " + e + " " + a, e.getName(), a.getName() );
            Assert.assertEquals( "Graph transition start nodes don't match " + e + " " + a, e.getStartNode().getId(), a.getStartNode().getId() );
            Assert.assertEquals( "Graph transition end nodes don't match " + e + " " + a, e.getEndNode().getId(), a.getEndNode().getId() );
        }
    }

    ///// PRIVATE METHODS /////

    private static void assertNodeEquals( OrFork e, OrFork a ){
        assertConditionEquals( e.getConditions(), a.getConditions() );
    }

    private static void assertNodeEquals( XorFork e, XorFork a ){
        assertConditionEquals( e.getConditions(), a.getConditions() );
    }

    private static void assertNodeEquals( BeanAsyncCallActivity e, BeanAsyncCallActivity a ){
        Assert.assertEquals( "Graph nodes don't match", e.getBean(), a.getBean() );
        Assert.assertEquals( "Graph nodes don't match", e.getMethod(), a.getMethod() );
        assertMappingEquals( e.getArgumentsMapping(), a.getArgumentsMapping() );
        assertMappingEquals( e.getResultMapping(), a.getResultMapping() );
    }

    private static void assertNodeEquals( BeanCallActivity e, BeanCallActivity a ){
        Assert.assertEquals( "Graph nodes don't match", e.getBean(), a.getBean() );
        Assert.assertEquals( "Graph nodes don't match", e.getMethod(), a.getMethod() );
        assertMappingEquals( e.getArgumentsMapping(), a.getArgumentsMapping() );
        assertMappingEquals( e.getResultMapping(), a.getResultMapping() );
    }

    private static void assertNodeEquals( CatchSignal e, CatchSignal a ){
        assertMappingEquals( e.getSignalMapping(), a.getSignalMapping() );
        assertMappingEquals( e.getResultMapping(), a.getResultMapping() );
    }

    private static void assertNodeEquals( CatchTimer e, CatchTimer a ){
        assertMappingEquals( e.getDueDateMapping(), a.getDueDateMapping() );
    }

    private static void assertNodeEquals( HumanTaskActivity e, HumanTaskActivity a ){
        assertMappingEquals( e.getRoleMapping(), a.getRoleMapping() );
        assertMappingEquals( e.getUserMapping(), a.getUserMapping() );
        assertMappingEquals( e.getArgumentsMapping(), a.getArgumentsMapping() );
        assertMappingEquals( e.getResultMapping(), a.getResultMapping() );
    }

    private static void assertNodeEquals( ObjectCallActivity e, ObjectCallActivity a ){
        Assert.assertEquals( "Graph nodes don't match", e.getTarget() != null ? e.getTarget().getClass() : null, a.getTarget() != null ? a.getTarget()
                .getClass() : null );
        Assert.assertEquals( "Graph nodes don't match", e.getMethod(), a.getMethod() );
        assertMappingEquals( e.getArgumentsMapping(), a.getArgumentsMapping() );
        assertMappingEquals( e.getResultMapping(), a.getResultMapping() );
    }

    private static void assertNodeEquals( ScriptActivity e, ScriptActivity a ){
        Assert.assertEquals( "Graph nodes don't match",
                e.getScript() != null ? e.getScript().getClass() : null,
                a.getScript() != null ? a.getScript().getClass() : null );
    }

    private static void assertNodeEquals( SetAttributeActivity e, SetAttributeActivity a ){
        Assert.assertEquals( "Graph nodes don't match", e.getAttribute(), a.getAttribute() );
        Assert.assertEquals( "Graph nodes don't match", e.getValue(), a.getValue() );
    }

    private static void assertNodeEquals( ValidateAttributeActivity e, ValidateAttributeActivity a ){
        Assert.assertEquals( "Graph nodes don't match", e.getAttribute(), a.getAttribute() );
        Assert.assertEquals( "Graph nodes don't match", e.getType(), a.getType() );
        Assert.assertEquals( "Graph nodes don't match", e.isRequired(), a.isRequired() );
        Assert.assertEquals( "Graph nodes don't match", e.getDefaultValue(), a.getDefaultValue() );
    }

    private static void assertNodeEquals( ScriptActivity e, SetAttributeActivity a ){
        // special case for comparing DSL graphs against GraphFactory graphs with RecordPathScript
        Assert.assertTrue( "Graph nodes don't match", a.getAttribute().equals( "path" ) && e.getScript() instanceof RecordPathScript );
    }

    private static void assertConditionEquals( List<Pair<Condition, String>> eConditions, List<Pair<Condition, String>> aConditions ){
        Assert.assertEquals( "Number of conditions does not match", eConditions.size(), aConditions.size() );

        Iterator<Pair<Condition, String>> eIterator = eConditions.iterator();
        Iterator<Pair<Condition, String>> aIterator = aConditions.iterator();
        while( eIterator.hasNext() ){
            Pair<Condition, String> ePair = eIterator.next();
            Pair<Condition, String> aPair = aIterator.next();
            Assert.assertEquals( "Graph nodes don't match", ePair.getRight(), aPair.getRight() );
            Condition e = ePair.getLeft();
            Condition a = aPair.getLeft();
            Assert.assertEquals( "Graph nodes don't match", e != null ? e.getClass() : null, a != null ? a.getClass() : null );
            if( e instanceof AttributeEqualsCondition ){
                Assert.assertEquals( "Graph nodes don't match", ((AttributeEqualsCondition)e).getAttributeName(),
                        ((AttributeEqualsCondition)a).getAttributeName() );
                Assert.assertEquals( "Graph nodes don't match", ((AttributeEqualsCondition)e).getTestValue(), ((AttributeEqualsCondition)a).getTestValue() );
            }
            else if( e instanceof ExpressionLanguageCondition ){
                Assert.assertEquals( "Graph nodes don't match", ((ExpressionLanguageCondition)e).getCondition(),
                        ((ExpressionLanguageCondition)a).getCondition() );
            }
            else if( e != null ){
                throw new UnsupportedOperationException( "Please improve the AbstractApiTest class to handle new Condition type: "
                        + e.getClass().getCanonicalName() );
            }
        }
    }

    private static void assertMappingEquals( InputMapping<?> e, InputMapping<?> a ){
        Assert.assertEquals( "Graph nodes don't match", e != null ? e.getClass() : null, a != null ? a.getClass() : null );
        if( e instanceof ArrayMapping ){
            InputMapping<?>[] eElementMappings = ((ArrayMapping)e).getElementMappings();
            InputMapping<?>[] aElementMappings = ((ArrayMapping)a).getElementMappings();
            for( int i = 0; i < eElementMappings.length; i++ ){
                assertMappingEquals( eElementMappings[i], aElementMappings[i] );
            }
        }
        else if( e instanceof AttributeMapping ){
            Assert.assertEquals( "Graph nodes don't match", ((AttributeMapping<?>)e).getAttributeName(), ((AttributeMapping<?>)a).getAttributeName() );
        }
        else if( e instanceof ConstantMapping ){
            Assert.assertEquals( "Graph nodes don't match", ((ConstantMapping<?>)e).getValue(), ((ConstantMapping<?>)a).getValue() );
        }
        else if( e instanceof ExpressionLanguageMapping ){
            Assert.assertEquals( "Graph nodes don't match", ((ExpressionLanguageMapping<?>)e).getExpression(),
                    ((ExpressionLanguageMapping<?>)a).getExpression() );
        }
        else if( e instanceof ExpressionMapping ){
            Expression<?> eExp = ((ExpressionMapping<?>)e).getExpression();
            Expression<?> aExp = ((ExpressionMapping<?>)a).getExpression();
            Assert.assertEquals( "Graph nodes don't match", eExp != null ? eExp.getClass() : null, aExp != null ? aExp.getClass() : null );
            if( eExp instanceof SimpleMethodCallExpression ){
                Object eExpTarget = ((SimpleMethodCallExpression<?>)eExp).getTarget();
                Object aExpTarget = ((SimpleMethodCallExpression<?>)aExp).getTarget();
                Assert.assertEquals( "Graph nodes don't match", eExpTarget != null ? eExpTarget.getClass() : null, aExpTarget != null ? aExpTarget.getClass()
                        : null );
                String eExpMethod = ((SimpleMethodCallExpression<?>)eExp).getMethodName();
                String aExpMethod = ((SimpleMethodCallExpression<?>)aExp).getMethodName();
                Assert.assertEquals( "Graph nodes don't match", eExpMethod, aExpMethod );
            }
            else if( eExp != null ){
                throw new UnsupportedOperationException( "Please improve the AbstractApiTest class to handle new Expression type: "
                        + e.getClass().getCanonicalName() );
            }
            assertMappingEquals( ((ExpressionMapping<?>)e).getArgumentMapping(), ((ExpressionMapping<?>)a).getArgumentMapping() );
        }
        else if( e instanceof MapMapping ){
            Map<String, InputMapping<Object>> eEntryMappings = ((MapMapping)e).getEntryMappings();
            Map<String, InputMapping<Object>> aEntryMappings = ((MapMapping)a).getEntryMappings();
            for( String key : eEntryMappings.keySet() ){
                InputMapping<Object> eMapping = eEntryMappings.get( key );
                InputMapping<Object> aMapping = aEntryMappings.get( key );
                assertMappingEquals( eMapping, aMapping );
            }
        }
        else if( e instanceof DueDateMapping ){
            DueDateMapping eDueDateMapping = (DueDateMapping)e;
            DueDateMapping aDueDateMapping = (DueDateMapping)a;
            assertMappingEquals( eDueDateMapping.getDelayMillisMapping(), aDueDateMapping.getDelayMillisMapping() );
        }
        else if( e != null ){
            throw new UnsupportedOperationException( "Please improve the AbstractApiTest class to handle new InputMapping type: "
                    + e.getClass().getCanonicalName() );
        }
    }

    private static void assertMappingEquals( OutputMapping e, OutputMapping a ){
        Assert.assertEquals( "Graph nodes don't match", e != null ? e.getClass() : null, a != null ? a.getClass() : null );
        if( e instanceof ValueMapping ){
            Assert.assertEquals( "Graph nodes don't match", ((ValueMapping)e).getName(), ((ValueMapping)a).getName() );
        }
        else if( e instanceof MapEntryMapping ){
            Assert.assertEquals( "Graph nodes don't match", ((MapEntryMapping)e).getMappings(), ((MapEntryMapping)a).getMappings() );
        }
        else if( e != null ){
            throw new UnsupportedOperationException( "Please improve the AbstractApiTest class to handle new OutputMapping type: "
                    + e.getClass().getCanonicalName() );
        }
    }

}
