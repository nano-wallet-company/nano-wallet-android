package co.nano.nanowallet.network.model;

import com.google.gson.Gson;

/**
 * Includes methods for serializing and deserializing
 */

public abstract class BaseNetworkModel {
    Gson gson;

    public BaseNetworkModel() {
        gson = new Gson();
    }

    public String serialize() {
        return gson.toJson(this);
    }
}
