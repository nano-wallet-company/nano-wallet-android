package co.nano.nanowallet.bus;

/**
 * Event when error occurs on the websocket
 */

public class SocketError {
    private Throwable error;

    public SocketError(Throwable error) {
        this.error = error;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }
}
