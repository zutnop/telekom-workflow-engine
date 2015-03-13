package ee.telekom.workflow.web.rest.model;

import java.io.Serializable;

import ee.telekom.workflow.graph.WorkItemStatus;

public class HumanTaskModel implements Serializable{

    private static final long serialVersionUID = 1L;
    private long refNum;
    private long woinRefNum;

    private int tokenId;
    private WorkItemStatus status;

    private String role;
    private String user;

    private String arguments;
    private String result;

    public HumanTaskModel(){
    }

    public HumanTaskModel( long refNum, long woinRefNum, int tokenId, WorkItemStatus status, String role, String userName, String arguments, String result ){
        this.refNum = refNum;
        this.woinRefNum = woinRefNum;
        this.tokenId = tokenId;
        this.status = status;
        this.role = role;
        this.user = userName;
        this.arguments = arguments;
        this.result = result;
    }

    public long getRefNum(){
        return refNum;
    }

    public long getWoinRefNum(){
        return woinRefNum;
    }

    public int getTokenId(){
        return tokenId;
    }

    public WorkItemStatus getStatus(){
        return status;
    }

    public String getRole(){
        return role;
    }

    public String getUser(){
        return user;
    }

    public String getArguments(){
        return arguments;
    }

    public String getResult(){
        return result;
    }

}
