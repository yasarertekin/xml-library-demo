package nl.koop.xmldemo.dto;

public class TransformResult {
    private String library;
    private String output;
    private long tijdMs;
    private long geheugenKb;
    private boolean success;
    private String error;

    public TransformResult(String library) {
        this.library = library;
        this.success = false;
    }

    // Getters and Setters
    public String getLibrary() {
        return library;
    }

    public void setLibrary(String library) {
        this.library = library;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public long getTijdMs() {
        return tijdMs;
    }

    public void setTijdMs(long tijdMs) {
        this.tijdMs = tijdMs;
    }

    public long getGeheugenKb() {
        return geheugenKb;
    }

    public void setGeheugenKb(long geheugenKb) {
        this.geheugenKb = geheugenKb;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
        this.success = false;
    }
}