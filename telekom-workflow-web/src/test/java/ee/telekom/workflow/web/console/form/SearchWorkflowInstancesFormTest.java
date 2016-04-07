package ee.telekom.workflow.web.console.form;

import ee.telekom.workflow.facade.model.WorkflowInstanceFacadeStatus;
import org.junit.*;

import java.util.ArrayList;

public class SearchWorkflowInstancesFormTest {

  @Test
  public void shouldRecognizeMissingSearchCriteria() {
    SearchWorkflowInstancesForm form = new SearchWorkflowInstancesForm();
    form.setId(new ArrayList<String>());
    form.setLabel1(new ArrayList<String>());
    form.setLabel2(new ArrayList<String>());
    form.setStatus(new ArrayList<WorkflowInstanceFacadeStatus>());
    form.setWorkflowName(new ArrayList<String>());
    form.setLength(20);
    form.setStart(0);
    Assert.assertFalse(form.hasSearchCriteria());
  }
}
