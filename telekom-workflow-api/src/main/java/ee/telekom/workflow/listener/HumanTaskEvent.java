package ee.telekom.workflow.listener;

import java.util.Map;

/**
 * Provides details on the human task work item associated with the event, the workflow instance's id,
 * the workflow's name and version, the token id, role and user as well as the human task's arguments.
 * 
 * The arguments must be used read-only!
 * 
 * @author Christian Klock
 */
public class HumanTaskEvent{

    private Long woinRefNum;
    private String workflowName;
    private Integer workflowVersion;
    private Integer tokenId;
    private String role;
    private String user;
    private Map<String, Object> arguments;

    public HumanTaskEvent( Long woinRefNum, String workflowName, Integer workflowVersion, Integer tokenId, String role, String user,
            Map<String, Object> arguments ){
        this.woinRefNum = woinRefNum;
        this.workflowName = workflowName;
        this.workflowVersion = workflowVersion;
        this.tokenId = tokenId;
        this.role = role;
        this.user = user;
        this.arguments = arguments;
    }

    public Long getWoinRefNum(){
        return woinRefNum;
    }

    public String getWorkflowName(){
        return workflowName;
    }

    public Integer getWorkflowVersion(){
        return workflowVersion;
    }

    public Integer getTokenId(){
        return tokenId;
    }

    public String getRole(){
        return role;
    }

    public String getUser(){
        return user;
    }

    public Map<String, Object> getArguments(){
        return arguments;
    }

}
