package co.nano.nanowallet.network.model;

import com.google.gson.annotations.SerializedName;

/**
 * Includes methods for serializing and deserializing
 */

public class BaseNetworkModel {
    @SerializedName("messageType")
    private String messageType;

    public BaseNetworkModel() {
    }

    public BaseNetworkModel(String messageType) {
        this.messageType = messageType;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
}
