package co.nano.nanowallet.network.model.response;

import com.google.gson.annotations.SerializedName;

import co.nano.nanowallet.network.model.BaseResponse;

/**
 * Pushed price data - currently sent every minute to all clients
 */

public class CurrentPriceResponse extends BaseResponse {
    @SerializedName("currency")
    private String currency;

    @SerializedName("price")
    private String price;

    @SerializedName("btc")
    private String btc;

    public CurrentPriceResponse() {
    }

    public CurrentPriceResponse(String currency, String price, String btc) {
        this.currency = currency;
        this.price = price;
        this.btc = btc;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getBtc() {
        // strip all unneeded characters
        return btc != null ? btc.replaceAll("[^\\d.]", "") : null;
    }

    public void setBtc(String btc) {
        this.btc = btc;
    }
}
