package co.nano.nanowallet.network.model.request.block;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.gson.annotations.SerializedName;

import co.nano.nanowallet.NanoUtil;
import co.nano.nanowallet.network.model.BlockTypes;

/**
 * Subscribe to websocket server for updates regarding the specified account.
 * First action to take when connecting when app opens or reconnects, IF a wallet already exists
 */
@JsonPropertyOrder({
        "type",
        "source",
        "representative",
        "account",
        "work",
        "signature"
})
public class OpenBlock extends Block {
    @SerializedName("type")
    private String type;

    @SerializedName("source")
    private String source;

    @SerializedName("representative")
    private String representative;

    @SerializedName("account")
    private String account;

    @SerializedName("signature")
    private String signature;

    public OpenBlock() {
        this.type = BlockTypes.OPEN.toString();
    }

    public OpenBlock(String private_key, String source,
                     String representative) {
        this.type = BlockTypes.OPEN.toString();
        this.representative = representative;
        this.account = NanoUtil.publicToAddress(NanoUtil.privateToPublic(private_key));
        this.source = source;
        String hash = NanoUtil.computeOpenHash(source, NanoUtil.addressToPublic(representative), NanoUtil.privateToPublic(private_key));
        this.signature = NanoUtil.sign(private_key, hash);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getRepresentative() {
        return representative;
    }

    public void setRepresentative(String representative) {
        this.representative = representative;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    @Override
    public String toString() {
        return "OpenBlock{" +
                "type='" + type + '\'' +
                ", source='" + source + '\'' +
                ", representative='" + representative + '\'' +
                ", account='" + account + '\'' +
                ", signature='" + signature + '\'' +
                '}';
    }
}
