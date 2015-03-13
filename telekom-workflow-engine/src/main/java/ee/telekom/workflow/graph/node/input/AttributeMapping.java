package ee.telekom.workflow.graph.node.input;

import ee.telekom.workflow.graph.GraphInstance;

/**
 * InputMapping for an attribute's value in the given environment.
 * <p>
 * NB! At the time of writing, this AttributeMapping is actually not usable via the workflow engine API (DSL). ExpressionLanguageMapping is used instead.
 */
public class AttributeMapping<T> implements InputMapping<T>{

    private String attributeName;

    public AttributeMapping( String attributeName ){
        this.attributeName = attributeName;
    }

    public String getAttributeName(){
        return attributeName;
    }

    @Override
    public T evaluate( GraphInstance instance ){
        @SuppressWarnings("unchecked")
        T attribute = (T)instance.getEnvironment().getAttribute( attributeName );
        return attribute;
    }

}
