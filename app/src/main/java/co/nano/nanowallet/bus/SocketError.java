package co.nano.nanowallet.bus;

/**
 * Created by szeidner on 23/02/2018.
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
