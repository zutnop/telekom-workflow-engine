package ee.telekom.workflow.graph.core;

import org.junit.Assert;
import org.junit.Test;

import ee.telekom.workflow.graph.AbstractGraphTest;
import ee.telekom.workflow.graph.Graph;
import ee.telekom.workflow.graph.GraphFactory;
import ee.telekom.workflow.graph.GraphRepository;

public class GraphRepositoryTest extends AbstractGraphTest{

    @Test
    public void test_add_get(){
        GraphRepository repo = new GraphRepositoryImpl();
        Graph graph = GraphFactory.INSTANCE.sequence_one();
        repo.addGraph( graph );
        Assert.assertSame( graph, repo.getGraph( graph.getName(), graph.getVersion() ) );
        Assert.assertSame( graph, repo.getGraph( graph.getName(), null ) );
    }

}
