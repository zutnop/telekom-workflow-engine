package ee.telekom.workflow.graph;

/**
 * A bean resolve resolves a bean object based on a bean name. A bean resolver may be used by some node types. 
 */
public interface BeanResolver{

    Object getBean( String name );

}
