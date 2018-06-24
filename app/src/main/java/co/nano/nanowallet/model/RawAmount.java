package co.nano.nanowallet.model;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public class RawAmount {
    public static final BigDecimal NANO_MULTIPLIER = BigDecimal.valueOf(1, -24);

    public static RawAmount fromNanoAmount(BigDecimal nanoAmount) {
        return new RawAmount(nanoAmount.multiply(NANO_MULTIPLIER).toBigIntegerExact());
    }

    public static RawAmount fromString(String rawAmountString) {
        return new RawAmount(new BigInteger(rawAmountString));
    }

    private final BigInteger rawAmount;

    public RawAmount(BigInteger rawAmount) {
        this.rawAmount = rawAmount;
    }

    public BigInteger getRawAmount() {
        return rawAmount;
    }

    public BigDecimal toNanoAmount() {
        return new BigDecimal(getRawAmount()).divide(NANO_MULTIPLIER, RoundingMode.UNNECESSARY);
    }

    public String toRawAmountString() {
        return getRawAmount().toString();
    }

    @Override
    public String toString() {
        return toRawAmountString();
    }
}
