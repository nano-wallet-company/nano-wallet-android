package co.nano.nanowallet.bus;

/**
 * Bus event object when pin change event occurs
 */

public class PinChange {
    private int pinLength;
    private String intermediatePin;

    public PinChange() {
    }

    public PinChange(int pinLength, String intermediatePin) {
        this.pinLength = pinLength;
        this.intermediatePin = intermediatePin;
    }

    public int getPinLength() {
        return pinLength;
    }

    public void setPinLength(int pinLength) {
        this.pinLength = pinLength;
    }

    public String getIntermediatePin() {
        return intermediatePin;
    }

    public void setIntermediatePin(String intermediatePin) {
        this.intermediatePin = intermediatePin;
    }
}
