package ee.telekom.workflow.core.lock;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import ee.telekom.workflow.TestApplicationContexts;
import ee.telekom.workflow.core.common.WorkflowEngineConfiguration;
import ee.telekom.workflow.core.node.NodeService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = TestApplicationContexts.DEFAULT)
@DirtiesContext
public class LockServiceIT extends TestApplicationContexts{

    @Autowired
    private LockService lockService;
    @Autowired
    private NodeService nodeService;
    @Autowired
    private WorkflowEngineConfiguration config;

    @Test
    public void test(){
        // before acquire
        Assert.assertFalse( lockService.isOwnLock() );
        Assert.assertNull( lockService.getLockOwner() );
        Assert.assertFalse( lockService.releaseOwnLock() );
        Assert.assertFalse( lockService.releaseExpiredLock() );

        // acquire
        Assert.assertTrue( lockService.acquireLock() );
        Assert.assertFalse( lockService.acquireLock() );
        Assert.assertTrue( lockService.isOwnLock() );
        Assert.assertEquals( config.getNodeName(), lockService.getLockOwner() );
        Assert.assertTrue( lockService.refreshOwnLock() );
        Assert.assertFalse( lockService.releaseExpiredLock() );

        // release
        Assert.assertTrue( lockService.releaseOwnLock() );
        Assert.assertFalse( lockService.isOwnLock() );
        Assert.assertNull( lockService.getLockOwner() );
        Assert.assertFalse( lockService.releaseOwnLock() );
        Assert.assertFalse( lockService.releaseExpiredLock() );
    }

    @Before
    public void prepareTest(){
        nodeService.findOrCreateByName( config.getNodeName() );
    }

}
