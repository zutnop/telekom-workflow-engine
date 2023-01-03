package ee.telekom.workflow.api;

/**
 * Enum indicating if work item is automatically retried on recovery.
 */
public enum AutoRecovery {
    ENABLED, DISABLED;

    public static AutoRecovery of(boolean autoRecovery){
        return autoRecovery ? ENABLED : DISABLED;
    }

    public static AutoRecovery getDefault(){
        return ENABLED;
    }

    public boolean asBoolean(){
        return this == ENABLED;
    }
}
