package ee.telekom.workflow.graph.node.input;

import ee.telekom.workflow.graph.GraphInstance;

/**
 * InputMapping of a constant value that does not depend on the environment.
 */
public class ConstantMapping<T> implements InputMapping<T>{

    private T value;

    public ConstantMapping( T value ){
        this.value = value;
    }

    public static <T> ConstantMapping<T> of( T value ){
        return new ConstantMapping<T>( value );
    }

    public T getValue(){
        return value;
    }

    @Override
    public T evaluate( GraphInstance instance ){
        return value;
    }

}
