package co.nano.nanowallet.model;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

public class RawAmount {
    public static final BigDecimal NANO_MULTIPLIER = BigDecimal.valueOf(1, -24);

    public static final TypeAdapter<RawAmount> TYPE_ADAPTER = new TypeAdapter<RawAmount>() {
        @Override
        public void write(JsonWriter out, RawAmount value) throws IOException {
            out.value(value.getRawAmount().toString());
        }

        @Override
        public RawAmount read(JsonReader in) throws IOException {
            return fromString(in.nextString());
        }
    };

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
}
