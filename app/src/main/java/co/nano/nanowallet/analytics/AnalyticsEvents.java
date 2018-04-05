package co.nano.nanowallet.analytics;

public class AnalyticsEvents {
    public static final String BAD_SEED_VIEWED = "Alert for bad seed viewed";
    public static final String BAD_WALLET_SEED_PASTED = "Bad Wallet Seed Pasted";
    public static final String CREATE_WORK_FAILED = "Create Work Failed";
    public static final String CREATE_WORK_FAILED_FOR_OPEN_BLOCK = "Create Work For Open Block Failed";
    public static final String ENDPOINT_UNWRAP_FAILED = "Endpoint Unwrap Failed";
    public static final String ERROR_PARSING_QR_CODE = "Error Parsing QR Code";
    public static final String ERROR_DECODING_CMCBTC_PRICE_DATA = "Error decoding CoinMarketCap BTC price data";
    public static final String ERROR_DECODING_CMCNANO_PRICE_DATA = "Error decoding CoinMarketCap Nano price data";
    public static final String ERROR_GETTING_CMCBTC_PRICE_DATA = "Error getting CoinMarketCap BTC price data";
    public static final String ERROR_GETTING_CMCNANO_PRICE_DATA = "Error getting CoinMarketCap Nano price data";
    public static final String ERROR_GETTING_EXCHANGE_PRICE_DATA = "Error getting exchange price data";
    public static final String ERROR_GENERATING_WORK_FOR_SENDING = "Error Generating Work for Sending";
    public static final String ERROR_UNWRAPPING_LOCAL_CURRENCY_TEXT = "Error unwrapping Local Currency text";
    public static final String LOCAL_CURRENCY_SELECTED = "Local Currency Selected";
    public static final String MISSING_CREDENTIALS = "App crashed due to missing Credentials";
    public static final String NANO_ADDRESS_COPIED = "Nano Address Copied";
    public static final String SEED_COPY_FAILED = "Seed Copy Failed";
    public static final String SEED_COPIED = "Seed Copied";
    public static final String SEED_CONFIRMATON_CONTINUE_BUTTON_PRESSED = "Seed Confirmation Continue Button Pressed";
    public static final String SEND_ADDRESS_FETCH_FAILED = "Send VC Address Fetch Failed";
    public static final String SEND_AUTH_ERROR = "Error with Send Authentication";
    public static final String SEND_BEGAN = "Send Nano Began";
    public static final String SEND_FINISHED = "Send Nano Finished";
    public static final String SEND_MAX_AMOUNT_USED = "Send: Max Amount Used";
    public static final String SEND_WORK_UNWRAP_FAILED = "Send VC Send Nano Work Unwrap Failed";
    public static final String SHARE_DIALOGUE_VIEWED = "Share Dialogue Viewed";
    public static final String SOCKET_CLOSED_HOME = "Socket Closed in HomeVM";
    public static final String SOCKET_ERROR_HOME = "socket Error in HomeVM";
    public static final String LOG_OUT = "User Logged Out";

    // Legal
    public static final String DISCLAIMER_AGREEMENT_TOGGLED = "Mobile Disclaimer Agreement Toggled";
    public static final String EULA_AGREEMENT_TOGGLED = "Mobile EULA Agreement Toggled";
    public static final String PRIVACY_POLICY_AGREEMENT_TOGGLED = "Mobile Privacy Policy Agreement Toggled";

    public static final String DISCLAIMER_VIEWED = "Mobile Disclaimer Viewed";
    public static final String EULA_VIEWED = "Mobile EULA Viewed";
    public static final String PRIVACY_POLICY_VIEWED = "Mobile Privacy Policy Viewed";

    // LocalCurrencyPair
    public static final String ERROR_FORMATTING_LOCAL_CURRENCY_STRING_TO_DOUBLE = "Error formatting Local Currency String to Double";
    public static final String ERROR_IN_LOCAL_CURRENCY_DECODE_FUNCTION = "Error in LocalCurrencyPair.decode function";
    public static final String UNABLE_TO_GET_LOCAL_CURRENCY_PAIR_BTC_PRICE = "Local Currency Pair Error: unable to get Bitcoin price";

    public static final String ERROR_FORMATTING_NANO_STRING_TO_DOUBLE = "Error formatting Nano Price Pair String to Double";
    public static final String ERROR_IN_NANO_DECODE_FUNCTION = "Error in NanoPricePair.decode function";
    public static final String UNABLE_TO_GET_NANO_PRICE = "Nano Price Pair Error: unable to get price";

    // VCs Viewed
    public static final String ADDRESS_SCAN_CAMERA_VIEWED = "Address Scan Camera View Viewed";
    public static final String HOME_VIEWED = "Home VC Viewed";
    public static final String EASTER_EGG_VIEWED = "Easter Egg Viewed";
    public static final String LEGAL_VIEWED = "Legal VC Viewed";
    public static final String RECEIVE_VIEWED = "Receive VC Viewed";
    public static final String SETTINGS_VIEWED = "Settings VC Viewed";
    public static final String SEED_SCAN_CAMERA_VIEWED = "Seed Scan Camera View Viewed";
    public static final String SEND_VIEWED = "Send VC Viewed";
    public static final String SEED_CONFIRMATION_VIEWED = "Seed Confirmation VC Viewed";
    public static final String WELCOME_VIEWED = "Welcome VC Viewed";
    public static final String CREATE_PIN_VIEWED = "Create PIN VC Viewed";
    public static final String ENTER_PIN_VIEWED = "Enter PIN VC Viewed";
}
