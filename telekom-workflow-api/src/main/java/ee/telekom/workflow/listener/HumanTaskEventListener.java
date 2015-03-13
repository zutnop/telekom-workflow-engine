package ee.telekom.workflow.listener;

/**
 * Provides a listener that is notified when human task lifecycle events occur.
 * 
 * @author Christian Klock
 */
public interface HumanTaskEventListener{

    /**
     * Called after a human task was created.
     * 
     * @param event
     *            information on the created human task
     */
    void onCreated( HumanTaskEvent event );

    /**
     * Called after a human task was completed.
     * 
     * @param event
     *            information on the created human task
     * @param humanTaskResult
     *            the result that was submitted when completing the human task 
     */
    void onCompleted( HumanTaskEvent event, Object humanTaskResult );

    /**
     * Called after a human task was cancelled.
     * 
     * @param event
     *            information on the created human task
     */
    void onCancelled( HumanTaskEvent event );

}
