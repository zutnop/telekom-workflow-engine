package ee.telekom.workflow.graph.node.input;

import java.util.Date;

import ee.telekom.workflow.graph.GraphInstance;

public class DueDateMapping implements InputMapping<Date>{

    private InputMapping<Long> delayMillisMapping;

    public DueDateMapping( long delayMillis ){
        this( ConstantMapping.of( delayMillis ) );
    }

    public DueDateMapping( InputMapping<Long> delayMillisMapping ){
        this.delayMillisMapping = delayMillisMapping;
    }

    public InputMapping<Long> getDelayMillisMapping(){
        return delayMillisMapping;
    }

    @Override
    public Date evaluate( GraphInstance instance ){
        return new Date( System.currentTimeMillis() + delayMillisMapping.evaluate( instance ) );
    }

}
