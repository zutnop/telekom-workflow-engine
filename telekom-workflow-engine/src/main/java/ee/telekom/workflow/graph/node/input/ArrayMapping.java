package ee.telekom.workflow.graph.node.input;

import ee.telekom.workflow.graph.GraphInstance;

/**
 * InputMapping that evaluates to an array. The class is mainly intended to created argument maps for tasks and method call activities.
 */
public class ArrayMapping implements InputMapping<Object[]>{

    private InputMapping<?>[] elementMappings;

    public ArrayMapping( InputMapping<?>[] elementMappings ){
        this.elementMappings = elementMappings;
    }

    public InputMapping<?>[] getElementMappings(){
        return elementMappings;
    }

    @Override
    public Object[] evaluate( GraphInstance instance ){
        if( elementMappings == null || elementMappings.length == 0 ){
            return null;
        }
        Object[] array = new Object[elementMappings.length];
        for( int i = 0; i < elementMappings.length; i++ ){
            array[i] = elementMappings[i].evaluate( instance );
        }
        return array;
    }

}