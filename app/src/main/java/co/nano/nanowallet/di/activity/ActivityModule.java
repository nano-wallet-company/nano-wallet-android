package co.nano.nanowallet.di.activity;

import android.content.Context;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import co.nano.nanowallet.model.NanoWallet;
import co.nano.nanowallet.network.AccountService;
import co.nano.nanowallet.network.model.Actions;
import co.nano.nanowallet.network.model.BaseResponse;
import co.nano.nanowallet.network.model.BlockTypes;
import co.nano.nanowallet.network.model.response.AccountCheckResponse;
import co.nano.nanowallet.network.model.response.AccountHistoryResponse;
import co.nano.nanowallet.network.model.response.BlockItem;
import co.nano.nanowallet.network.model.response.BlockResponse;
import co.nano.nanowallet.network.model.response.CurrentPriceResponse;
import co.nano.nanowallet.network.model.response.ErrorResponse;
import co.nano.nanowallet.network.model.response.ProcessResponse;
import co.nano.nanowallet.network.model.response.SubscribeResponse;
import co.nano.nanowallet.network.model.response.TransactionResponse;
import co.nano.nanowallet.network.model.response.WarningResponse;
import co.nano.nanowallet.network.model.response.WorkResponse;
import co.nano.nanowallet.util.ExceptionHandler;
import dagger.Module;
import dagger.Provides;
import io.gsonfire.GsonFireBuilder;

@Module
public class ActivityModule {
    private final Context mContext;
    private NanoWallet mNanoWallet;

    public ActivityModule(Context context) {
        mContext = context;
    }

    @Provides
    Context providesActivityContext() {
        return mContext;
    }

    @Provides
    NanoWallet providesNanoWallet(Context context) {
        if (mNanoWallet == null) {
            mNanoWallet = new NanoWallet(context);
        }
        return mNanoWallet;
    }

    @Provides
    @ActivityScope
    AccountService providesAccountService(Context context) {
        return new AccountService(context);
    }

    @Provides
    @ActivityScope
    Gson providesGson() {
        // configure gson to detect and set proper types
        GsonFireBuilder builder = new GsonFireBuilder()
                .registerPreProcessor(BaseResponse.class, (clazz, src, gson) -> {
                    // figure out the response type based on what fields are in the response
                    if (src.isJsonObject() && src.getAsJsonObject().get("messageType") == null) {
                        if (src.getAsJsonObject().get("uuid") != null ||
                                (src.getAsJsonObject().get("frontier") != null &&
                                        src.getAsJsonObject().get("representative_block") != null) ||
                                (src.getAsJsonObject().get("error") != null &&
                                        src.getAsJsonObject().get("currency") != null)) {
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
                        } else if (src.getAsJsonObject().get("warning") != null) {
                            // warning response
                            src.getAsJsonObject().addProperty("messageType", Actions.WARNING.toString());
                        } else if (src.getAsJsonObject().get("block") != null && src.getAsJsonObject().get("account") != null
                                && src.getAsJsonObject().get("hash") != null) {
                            // block response
                            src.getAsJsonObject().addProperty("messageType", "block");
                        } else if (src.getAsJsonObject().get("ready") != null) {
                            // account check response
                            src.getAsJsonObject().addProperty("messageType", Actions.CHECK.toString());
                        } else if (src.getAsJsonObject().get("hash") != null) {
                            // process block response
                            src.getAsJsonObject().addProperty("messageType", Actions.PROCESS.toString());
                        } else if (src.getAsJsonObject().get("type") != null &&
                                src.getAsJsonObject().get("type").getAsString().equals(BlockTypes.STATE.toString())) {
                            // state block response
                            src.getAsJsonObject().addProperty("messageType", Actions.PROCESS.toString());
                        } else if (src.getAsJsonObject().get("contents") != null) {
                            // get block response
                            ObjectMapper mapper = new ObjectMapper();
                            String content = src.getAsJsonObject().get("contents").getAsString();
                            content = content.replace("\n", "");
                            content = content.replace("\\\"", "\"");
                            src.getAsJsonObject().remove("contents");
                            BlockItem blockItem = new Gson().fromJson(content, BlockItem.class);
                            src.getAsJsonObject().addProperty("type", blockItem.getType());
                            src.getAsJsonObject().addProperty("account", blockItem.getAccount());
                            src.getAsJsonObject().addProperty("previous", blockItem.getPrevious());
                            src.getAsJsonObject().addProperty("representative", blockItem.getRepresentative());
                            src.getAsJsonObject().addProperty("balance", blockItem.getBalance());
                            src.getAsJsonObject().addProperty("link", blockItem.getLink());
                            src.getAsJsonObject().addProperty("link_as_account", blockItem.getLink_as_account());
                            src.getAsJsonObject().addProperty("work", blockItem.getWork());
                            src.getAsJsonObject().addProperty("signature", blockItem.getSignature());
                            src.getAsJsonObject().addProperty("messageType", "contents");
                        }
                    }
                }).registerTypeSelector(BaseResponse.class, readElement -> {
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
                        } else if (kind.equals(Actions.WARNING.toString())) {
                            return WarningResponse.class;
                        } else if (kind.equals(Actions.CHECK.toString())) {
                            return AccountCheckResponse.class;
                        } else if (kind.equals(Actions.PROCESS.toString())) {
                            return ProcessResponse.class;
                        } else if (kind.equals("block")) {
                            return TransactionResponse.class;
                        } else if (kind.equals("contents")) {
                            return BlockResponse.class;
                        } else {
                            return null; // returning null will trigger Gson's default behavior
                        }
                    } else {
                        return null;
                    }
                });

        return builder.createGson();
    }
}
