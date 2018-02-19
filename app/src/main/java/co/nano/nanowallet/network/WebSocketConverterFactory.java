package co.nano.nanowallet.network;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.navin.flintstones.rxwebsocket.WebSocketConverter;

import java.lang.reflect.Type;

import co.nano.nanowallet.network.converters.GsonRequestConvertor;
import co.nano.nanowallet.network.converters.GsonResponseConvertor;
import co.nano.nanowallet.network.model.Actions;
import co.nano.nanowallet.network.model.BaseNetworkModel;
import co.nano.nanowallet.network.model.response.AccountCheckResponse;
import co.nano.nanowallet.network.model.response.AccountHistoryResponse;
import co.nano.nanowallet.network.model.response.TransactionResponse;
import co.nano.nanowallet.network.model.response.CurrentPriceResponse;
import co.nano.nanowallet.network.model.response.ErrorResponse;
import co.nano.nanowallet.network.model.response.ProcessResponse;
import co.nano.nanowallet.network.model.response.SubscribeResponse;
import co.nano.nanowallet.network.model.response.WorkResponse;
import io.gsonfire.GsonFireBuilder;

public final class WebSocketConverterFactory extends WebSocketConverter.Factory {
    public static WebSocketConverterFactory create() {
        // configure gson to detect and set proper types
        GsonFireBuilder builder = new GsonFireBuilder()
                .registerPreProcessor(BaseNetworkModel.class, (clazz, src, gson) -> {
                    // figure out the response type based on what fields are in the response
                    if (src.isJsonObject() && src.getAsJsonObject().get("messageType") == null) {
                        if (src.getAsJsonObject().get("frontier") != null) {
                            // subscribe response
                            src.getAsJsonObject().addProperty("messageType", Actions.SUBSCRIBE.toString());
                        } else if (src.getAsJsonObject().get("history") != null) {
                            // history response
                            src.getAsJsonObject().addProperty("messageType", Actions.HISTORY.toString());
                            // if history is an empty string, make it an array instead
                            if (!src.getAsJsonObject().get("history").isJsonArray()) {
                                src.getAsJsonObject().add("history", new JsonArray());
                            }
                        } else if (src.getAsJsonObject().get("currency") != null) {
                            // current price
                            src.getAsJsonObject().addProperty("messageType", Actions.PRICE.toString());
                        } else if (src.getAsJsonObject().get("work") != null) {
                            // work response
                            src.getAsJsonObject().addProperty("messageType", Actions.WORK.toString());
                        } else if (src.getAsJsonObject().get("error") != null) {
                            // error response
                            src.getAsJsonObject().addProperty("messageType", Actions.ERROR.toString());
                        } else if (src.getAsJsonObject().get("block") != null && src.getAsJsonObject().get("account") != null
                                && src.getAsJsonObject().get("hash") != null) {
                            // block response
                            src.getAsJsonObject().addProperty("messageType", "block");
                        } else if (src.getAsJsonObject().get("ready") != null) {
                            // account check response
                            src.getAsJsonObject().addProperty("messageType", Actions.CHECK.toString());
                        } else if (src.getAsJsonObject().get("hash") != null) {
                            // account check response
                            src.getAsJsonObject().addProperty("messageType", Actions.PROCESS.toString());
                        }
                    }
                }).registerTypeSelector(BaseNetworkModel.class, readElement -> {
                    // return proper type based on the message type that was set
                    if (readElement.isJsonObject() && readElement.getAsJsonObject().get("messageType") != null) {
                        String kind = readElement.getAsJsonObject().get("messageType").getAsString();
                        if (kind.equals(Actions.SUBSCRIBE.toString())) {
                            return SubscribeResponse.class;
                        } else if (kind.equals(Actions.HISTORY.toString())) {
                            return AccountHistoryResponse.class;
                        } else if (kind.equals(Actions.PRICE.toString())) {
                            return CurrentPriceResponse.class;
                        } else if (kind.equals(Actions.WORK.toString())) {
                            return WorkResponse.class;
                        } else if (kind.equals(Actions.ERROR.toString())) {
                            return ErrorResponse.class;
                        } else if (kind.equals(Actions.CHECK.toString())) {
                            return AccountCheckResponse.class;
                        } else if (kind.equals(Actions.PROCESS.toString())) {
                            return ProcessResponse.class;
                        } else if (kind.equals("block")) {
                            return TransactionResponse.class;
                        } else {
                            return null; // returning null will trigger Gson's default behavior
                        }
                    } else {
                        return null;
                    }
                });

        return create(builder.createGson());
    }

    public static WebSocketConverterFactory create(Gson gson) {
        return new WebSocketConverterFactory(gson);
    }

    private final Gson gson;

    private WebSocketConverterFactory(Gson gson) {
        if (gson == null) throw new NullPointerException("gson == null");
        this.gson = gson;
    }

    @Override
    public WebSocketConverter<String, ?> responseBodyConverter(Type type) {
        TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
        return new GsonResponseConvertor(gson, adapter);
    }

    @Override
    public WebSocketConverter<?, String> requestBodyConverter(Type type) {
        TypeAdapter<?> adapter = gson.getAdapter(TypeToken.get(type));
        return new GsonRequestConvertor<>(gson, adapter);
    }
}