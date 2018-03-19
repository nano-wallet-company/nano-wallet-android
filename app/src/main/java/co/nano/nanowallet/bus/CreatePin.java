package co.nano.nanowallet.bus;

/**
 * Bus event object when pin complete event occurs
 */

public class CreatePin {
    private String pin;

    public CreatePin() {
    }

    public CreatePin(String pin) {
        this.pin = pin;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }
}
