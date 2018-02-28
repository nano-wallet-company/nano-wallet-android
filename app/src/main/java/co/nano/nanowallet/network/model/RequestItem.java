package co.nano.nanowallet.network.model;

/**
 * Request object for queue
 */

public class RequestItem<T> {
    private boolean isProcessing = false;
    private T request;

    public RequestItem(T request) {
        this.request = request;
    }

    public T getRequest() {
        return request;
    }

    public void setRequest(T request) {
        this.request = request;
    }

    public boolean isProcessing() {
        return isProcessing;
    }

    public void setProcessing(boolean processing) {
        isProcessing = processing;
    }
}
