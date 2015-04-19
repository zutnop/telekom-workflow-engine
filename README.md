# Introduction
This document gives an overview of the Telekom workflow engine. For more details, please read the wiki pages: https://github.com/zutnop/telekom-workflow-engine/wiki.

# Workflow engine 
The telekom-workflow-engine is a custom built embeddable technology that provides a runtime environment for long-lived business processes.
The main features, and the meaning of life, of the engine are: 
* execute and manage long-lived automatic business processes (workflows)
* provide support for executing existing service layer methods (business logic) in those processes
* provide support for human interaction in those processes (manual human tasks)
* listen to and react on signals/events coming from integrated external systems
* guarantee robust error handling, transaction management, scalability and transparency
* provide automated testing support for your workflows

# Why yet another workflow engine?
The available open-source offerings were mostly based on drag and drop visual design, lots of XML files and the BPMN standard, which is great if you are building demos with less than 10 elements in the workflow or writing Gartner recommendations, but it quickly becomes a huge PITA if you are actually implementing and running complex real-world working software on those platforms and integrating this with your existing services layer. 

The previous 10 years we were using BEA/Oracle Weblogic Integration platform versions 7.x, 8.x, 9.x (end-of-life, no support, black box, mystical problems, running instances dissapearing etc., stuck with Java 5 and other ancient technologies, workflow development is awfully slow and complex) and the last 3 years we were trying to find a new platform for our workflows. After having a brief look at Activiti, Bonita Open Solution and JBoss jBPM, we decided to try the jBPM 5.4. After several proof-of-concepts on the jBPM and after adding/changing/enhancing/implementing/identifying all the missing parts (clustering, timers, WS-HumanTasks, java tasks, admin console, REST interface) it became clear that this was NOT the way forward. The last drop to the glass came when we actually tried to implement a few of our simpler workflows, the developer experience was nowhere near good enough to be able to commit to this for the next 10 years (it was buggy, slow, complex, managed to break the definition in a way that required to start from the scratch because there was no way to undo or fix it in he IDE, etc.)!

It was decided to build a lightweight embeddable workflow engine that meets our requirements and plays nicely together with our technology stack to achieve:
* at least 10% time saving when developing applications which work based on automated workflows (the workflow definition implementation part wins way more than 10%, but there is a lot of other stuff you need to do when building the whole application (data layer, business services, web appliction, analysis, testing, user education etc.) so the 10%+ was estimated for the total win)
* robustness (no black box)
* performance (platform is designed to do only what we need it to do)
* low lifetime costs (no licence fees, open tehncologies which can easily be updated)

# Technical vision
### Engine overview
The engine implementation can be divided into three main parts:
* core - provides the runtime environment for workflow execution (based on graph oriented programming) together with all the supporting services (clustering, persistence, error handling etc.)
* API - interface (DSL) for writing your workflow definitions and plugins, more info in "Workflow implementations" paragraph
* web - web console, REST services and JMX interface for monitoring and interacting with the running engine

An empty engine itself does not provide any value for the end users. To do something useful with the engine, you need to write your workflow definitions (via using the API). The engine implementation and the workflow definitions are to be kept in separate repositories to provide a clear distinction between the platform and the actual workflow business logic code. The engine, together with those workflow definitions, packaged as a *.war archive, will be deployed to Tomcat web server(s). When the web server is started, the engine spools up, reads the previous state from its DB and continues to execute the ongoing instances. The engine will keep executing the started workflow instances, persisting the new state when a wait state is reached.

### Error handling
The execution of the long-lived workflows is a delicate process (must survive technical problems) and thus a lot of effort has gone into making the workflow engine very robust and bulletproof. The workflows are executed as transactions from one wait state to the next wait state. If the next wait state is successfully reached, then the new state is persisted and the transaction is commited. If an exception occurs, the transaction is rolled back, the execution is stored and the workflow will go into a frozen state, waiting for a human decision. After correcting the problem, the workflow can be continued from the last good wait state.
This achieves:
* good protection against infrastructure problems, e.g.:
  * database problems
  * webservice problems
  * external application errors
* low impact of development errors - a bug, which causes a NullpointerException, freezes the instance; bug is fixed and released into production; workflow instance can be continued from the last wait state

### Performance
The engine is built to provide high throughput and supports scalability in both dimensions:
* vertical - faster CPU, more memory, increase the engine thread pool size to support more concurrency
* horizontal - clustering and its dynamic management based on the Hazelcast framework; adding and removing cluster nodes does not distract the engine's work; the self-healing mechanism finds the dead nodes and redistributes it's pending work

### Monitoring/management
The workflow engine publishes a web console, REST services and JMX interface for monitoring and interacting with the running engine. These services provide the following functionality:
* overview of engine health and current work
* list of workflow instances and human tasks
* tools for managing the execution errors (cancel, unfreeze, etc)
* tools for interacting with human tasks

### Testing
The workflow engine is fully covered with unit and integration tests. And the workflow definitions can also be easily tested with JUnit tests for rapid development and debuging.

### Workflow implementations
To implement an automatic workflow, you need to implement the workflow process/rules in Java using the telekom-workflow-api interfaces. Each workflow is described in its own Java class as one or more steps.

Workflow definition example:
```java
factory
    .start()
    
    // validate input
    .validateInputVariable( 0, "customerId", String.class)
    
    // load data into environment
    .variable( "customerBalance" ).call( 1, "customerService", "getCustomerBalance", "${customerId}" )
    // calculate the suspend time after the warning message has been sent
    .variable( "suspendTime" ).call( 2, "customerService", "getSuspendTimeAfterWarning" )

    .whileDo( 3, "customerBalance < 0 && suspendTime.time > System.currentTimeMillis()" )
        .split( 4 )
            .branch()
                // wait until suspendTime
                .waitUntilDate( 6, "${suspendTime}" )
            .branch()
                // and at the same time monitor balance changes
                .waitSignal( 7, "PAYMENT" )
        .joinFirst()
        .variable( "customerBalance" ).call( 8, "customerService", "getCustomerBalance", "${customerId}" )
    .whileDo()

    // only continue with the proceedings if the customer balance is still negative
    .if_( 9, "customerBalance < 0" )
        // create a suspend order
        .variable("suspendOrderId").callAsync( 10, "customerService", "suspendCustomer", "${customerId}" )
        
        // wait until the order is beeing processed
        .doWhile()
            .waitTimer( 11, "1000" )
            .variable( "suspendOrderStatus" ).call( 12, "customerService", "getOrderStatus", "${suspendOrderId}" )
        .doWhile( 13, "suspendOrderStatus == 'PROCESSING'" )
        
        // if the order fails, create a manual task
        .if_( 14, "suspendOrderStatus != 'COMPLETED'" )
            .humanTask( 15, "ROLE_CUSTOMER_SUPPORT", null ).withAttribute( "customerId", "${customerId}" ).withAttribute( "taskType", "MANUAL_SUSPEND" ).done()
        .endIf()
    
        // find out the next step 
        .variable( "nextStep" ).call( 16, "exampleStepSelector", "findNextStep", "${customerId}", "02" )
        //  and start it, passing along the customerId attribute
        .createInstance( 17, "${nextStep}", null, "${customerId}", null ).withAttribute( "customerId", "${customerId}" ).done()
    .endIf()

    .end();
```
