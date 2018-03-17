package co.nano.nanowallet.bus;

/**
 * Bus event object when pin complete event occurs
 */

public class PinComplete {
    private String pin;

    public PinComplete() {
    }

    public PinComplete(String pin) {
        this.pin = pin;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }
}
