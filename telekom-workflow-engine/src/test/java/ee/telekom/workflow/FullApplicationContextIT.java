package ee.telekom.workflow;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = TestApplicationContexts.FULL)
@DirtiesContext
public class FullApplicationContextIT{

    @Test
    public void test(){
    }

}
