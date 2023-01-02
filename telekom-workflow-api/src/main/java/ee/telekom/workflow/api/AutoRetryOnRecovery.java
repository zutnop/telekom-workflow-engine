package ee.telekom.workflow.api;

/**
 * Enum indicating if work item is automatically retried on recovery.
 */
public enum AutoRetryOnRecovery {
    TRUE, FALSE;

    public static AutoRetryOnRecovery of(boolean autoRetryOnRecovery){
        return autoRetryOnRecovery ? TRUE : FALSE;
    }

    public static AutoRetryOnRecovery getDefault(){
        return TRUE;
    }

    public boolean asBoolean(){
        return this == TRUE;
    }
}
