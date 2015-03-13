package ee.telekom.workflow.api;

/**
 * The workflow engine is built for running long-lived business processes. The execution logic for these business processes (or process parts) is defined via 
 * workflow definitions. You need to write and deploy a workflow definition for every business process (or multiple definitions, if you want to split the 
 * process into subprocesses/steps) that you want to run on the workflow engine.
 * <p>
 * A workflow definition is a plain Java class which implements the WorkflowDefinition interface and uses the WorkflowFactory DSL API to define the process 
 * execution logic. There is no visual drag and drop process view, there is no BPMN. 
 * <p>
 * In addition to a execution logic description, a workflow definition also has a human readable name and a version number. When a new workflow instance (one 
 * execution of this business process) is created, it starts with the specified (or latest) version number. This workflow instance will live it's entire life 
 * on this version number. If you make changes into the workflow definition that don't break the backward compatibility, then you may keep the same version 
 * number and this workflow instance will start using the new definition after the next deployment. But if you make changes that break the backward 
 * compatibility, you need to increase the version number, and then the instance won't automatically migrate to the new definition. This also means that you 
 * must keep the workflow definition previous versions present for as long as there are running instances on these previous versions (when you increase the 
 * workflow definition version from 1 to 2, you must keep the old java file with version 1 and add a new java file with version 2).
 * <p>
 * The workflow definitions are meant to be kept in your implementation projects (far away from the engine implementation repository, to provide a clear 
 * distinction between the platform and the actual workflow business logic code) together with your service layer beans and plugins/event listeners. 
 * The workflow definitions must be defined as Spring beans in your implementation project's app context (@Component).
 * <p>
 * The workflow engine converts your definition into internal graph format and stores the result in the internal repository. The engine then provides you 
 * with different interfaces (web console, REST services, engine facade) to start a new workflow instance and to interact with existing instances. 
 *
 * @author Erko Hansar
 * @see WorkflowFactory
 */
public interface WorkflowDefinition{

    /**
     * Return a human readable name for this workflow definition.
     * 
     * @return a human readable name for this workflow definition
     */
    String getName();

    /**
     * Return a version number for the current workflow definition. 
     * <p>
     * Used when starting a new workflow instance. This workflow instance will live it's entire life on this version number. If you make changes that don't 
     * break the backward compatibility, then you may keep the same version number and this workflow instance will start using the new definition after the 
     * next deployment. But if you make changes that break the backward compatibility, you need to increase the version number, and then the instance won't 
     * automatically migrate to the new definition.
     * 
     * @return a version number for the current workflow definition
     */
    int getVersion();

    /**
     * This method will be called once per run-time when initializing the workflow definitions for the implementing classes. 
     * <p>
     * Use the given factory like this:
     * <pre>
     * public void configureWorkflowDefinition( WorkflowFactory factory ){
     *     \/* @formatter:off *\/
     *     factory
     *         .start()
     *         .call( 1, "helloWorldService", "sendHelloToCustomer", "${customerId}" )
     *         .end();
     *     \/* @formatter:on *\/
     * }
     * </pre>
     * 
     * @param workflowFactory factory provides the DSL API for constructing the workflow definition and is later used by the engine to convert the 
     */
    void configureWorkflowDefinition( WorkflowFactory factory );

}