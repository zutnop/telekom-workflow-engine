package ee.telekom.workflow.graph.node.output;

import ee.telekom.workflow.graph.Environment;

/**
 * OutputMapping that maps the node execution result on an attribute with the given name.
 */
public class ValueMapping implements OutputMapping{

    private String name;

    public ValueMapping( String name ){
        this.name = name;
    }

    public static ValueMapping of( String name ){
        return new ValueMapping( name );
    }
    
    public String getName() {
        return name;
    }

    @Override
    public void map( Environment environment, Object result ){
        environment.setAttribute( name, result );
    }

}
