package ee.telekom.workflow.graph.node.gateway.condition;

import org.apache.commons.lang3.ObjectUtils;

import ee.telekom.workflow.graph.GraphInstance;

public class AttributeEqualsCondition implements Condition{

    private String attributeName;
    private Object testValue;

    public AttributeEqualsCondition( String attributeName, Object testValue ){
        this.attributeName = attributeName;
        this.testValue = testValue;
    }

    public String getAttributeName(){
        return attributeName;
    }

    public Object getTestValue(){
        return testValue;
    }

    @Override
    public boolean evaluate( GraphInstance instance ){
        return ObjectUtils.equals( instance.getEnvironment().getAttribute( attributeName ), testValue );
    }

}