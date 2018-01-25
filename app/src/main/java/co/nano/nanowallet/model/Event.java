package co.nano.nanowallet.model;

/**
 * Created by Mica Busch on 1/25/2018.
 */

public class Event {
    private final String message;

    public Event(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
