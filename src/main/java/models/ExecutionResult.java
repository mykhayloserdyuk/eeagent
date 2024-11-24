package models;

public class ExecutionResult {
    private final boolean success;
    // private final String output;
    private final String errors;

    public ExecutionResult(boolean success, String output, String errors) {
        this.success = success;
        // this.output = output;
        this.errors = errors;
    }

    public boolean isSuccess() {
        return success;
    }

    /* public String getOutput() {
        return output;
    } */

    public String getErrors() {
        return errors;
    }
}
