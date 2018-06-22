package co.nano.nanowallet.ui.send;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Guideline;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.github.ajalt.reprint.core.AuthenticationFailureReason;
import com.github.ajalt.reprint.core.Reprint;
import com.hwangjr.rxbus.annotation.Subscribe;

import java.math.BigInteger;
import java.text.NumberFormat;
import java.util.HashMap;

import javax.inject.Inject;

import co.nano.nanowallet.NanoUtil;
import co.nano.nanowallet.R;
import co.nano.nanowallet.analytics.AnalyticsEvents;
import co.nano.nanowallet.analytics.AnalyticsService;
import co.nano.nanowallet.bus.CreatePin;
import co.nano.nanowallet.bus.HideOverlay;
import co.nano.nanowallet.bus.PinComplete;
import co.nano.nanowallet.bus.RxBus;
import co.nano.nanowallet.bus.SendInvalidAmount;
import co.nano.nanowallet.bus.ShowOverlay;
import co.nano.nanowallet.databinding.FragmentSendBinding;
import co.nano.nanowallet.model.Address;
import co.nano.nanowallet.model.AvailableCurrency;
import co.nano.nanowallet.model.Credentials;
import co.nano.nanowallet.model.NanoWallet;
import co.nano.nanowallet.network.AccountService;
import co.nano.nanowallet.network.model.response.ErrorResponse;
import co.nano.nanowallet.network.model.response.ProcessResponse;
import co.nano.nanowallet.ui.common.ActivityWithComponent;
import co.nano.nanowallet.ui.common.BaseFragment;
import co.nano.nanowallet.ui.common.KeyboardUtil;
import co.nano.nanowallet.ui.common.UIUtil;
import co.nano.nanowallet.ui.scan.ScanActivity;
import co.nano.nanowallet.util.NumberUtil;
import co.nano.nanowallet.util.SharedPreferencesUtil;
import io.realm.Realm;

import static android.app.Activity.RESULT_OK;

/**
 * Send Screen
 */
public class SendFragment extends BaseFragment {
    private FragmentSendBinding binding;
    public static String TAG = SendFragment.class.getSimpleName();
    private boolean localCurrencyActive = false;
    private AlertDialog fingerprintDialog;
    private static final String ARG_NEW_SEED = "argNewSeed";
    private String newSeed;

    @Inject
    NanoWallet wallet;

    @Inject
    AccountService accountService;

    @Inject
    SharedPreferencesUtil sharedPreferencesUtil;

    @Inject
    Realm realm;

    @Inject
    AnalyticsService analyticsService;

    @BindingAdapter("layout_constraintGuide_percent")
    public static void setLayoutConstraintGuidePercent(Guideline guideline, float percent) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) guideline.getLayoutParams();
        params.guidePercent = percent;
        guideline.setLayoutParams(params);
    }

    /**
     * Create new instance of the fragment (handy pattern if any data needs to be passed to it)
     *
     * @return New instance of SendFragment
     */
    public static SendFragment newInstance() {
        Bundle args = new Bundle();
        SendFragment fragment = new SendFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Create new instance of the fragment (handy pattern if any data needs to be passed to it)
     *
     * @return New instance of SendFragment
     */
    public static SendFragment newInstance(String newSeed) {
        Bundle args = new Bundle();
        args.putString(ARG_NEW_SEED, newSeed);
        SendFragment fragment = new SendFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments() != null) {
            newSeed = getArguments().getString(ARG_NEW_SEED);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_send, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.send_camera:
                analyticsService.track(AnalyticsEvents.ADDRESS_SCAN_CAMERA_VIEWED);
                startScanActivity(getString(R.string.scan_send_instruction_label), false);
                return true;
        }

        return false;
    }

    public void showSoftKeyboard() {
        //Shows the SoftKeyboard
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && getActivity().getCurrentFocus() != null) {
            inputMethodManager.showSoftInput(binding.sendAddress, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // init dependency injection
        if (getActivity() instanceof ActivityWithComponent) {
            ((ActivityWithComponent) getActivity()).getActivityComponent().inject(this);
        }

        analyticsService.track(AnalyticsEvents.SEND_VIEWED);

        // subscribe to bus
        RxBus.get().register(this);

        // change keyboard mode
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        KeyboardUtil.hideKeyboard(getActivity());

        // inflate the view
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_send, container, false);
        view = binding.getRoot();
        binding.setHandlers(new ClickHandlers());

        setStatusBarBlue();
        setBackEnabled(true);
        setTitle(getString(R.string.send_title));
        setTitleDrawable(R.drawable.ic_send);

        // hide keyboard for edittext fields
        binding.sendAmountNano.setInputType(InputType.TYPE_NULL);
        binding.sendAmountLocalcurrency.setInputType(InputType.TYPE_NULL);

        // set active and inactive states for edittext fields
        binding.sendAmountNano.setOnFocusChangeListener((view1, b) -> toggleFieldFocus((EditText) view1, b, false));
        binding.sendAmountLocalcurrency.setOnFocusChangeListener((view1, b) -> toggleFieldFocus((EditText) view1, b, true));
        binding.sendAmountLocalcurrency.setHint(NumberFormat.getCurrencyInstance(getLocalCurrency().getLocale()).format(0));
        binding.setShowAmount(true);

        binding.sendAddress.setOnFocusChangeListener((view12, hasFocus) -> binding.setShowAmount(!hasFocus));

        binding.sendAddress.setBackgroundResource(binding.sendAddress.getText().length() > 0 ? R.drawable.bg_seed_input_active : R.drawable.bg_seed_input);
        UIUtil.colorizeSpannable(binding.sendAddress.getText(), getContext());


        // updates to handle seed conversion 1.0.2
        if (newSeed != null) {
            String address = NanoUtil.publicToAddress(NanoUtil.privateToPublic(NanoUtil.seedToPrivate(newSeed)));
            binding.sendAddress.setText(address);
            setShortAddress();
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // unregister from bus
        RxBus.get().unregister(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check to make sure we are responding to camera result
        if (requestCode == SCAN_RESULT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Bundle res = data.getExtras();
                if (res != null) {
                    // parse address
                    Address address = new Address(res.getString(ScanActivity.QR_CODE_RESULT));

                    // set to scanned value
                    if (address.getAddress() != null) {
                        binding.sendAddress.setText(address.getAddress());
                    }

                    if (address.getAmount() != null) {
                        wallet.setSendNanoAmount(address.getAmount());
                        binding.setWallet(wallet);
                    }

                    setShortAddress();
                }
            }
        }
    }


    public AvailableCurrency getLocalCurrency() {
        return sharedPreferencesUtil.getLocalCurrency();
    }

    /**
     * Event that occurs if an amount entered is invalid
     *
     * @param sendInvalidAmount Send Invalid Amount event
     */
    @Subscribe
    public void receiveInvalidAmount(SendInvalidAmount sendInvalidAmount) {
        // reset amount to max in wallet
        wallet.setSendNanoAmount(wallet.getLongerAccountBalanceNano());
        binding.setWallet(wallet);

        // show alert with a message to the user letting them know the amount they entered
        showError(R.string.send_amount_too_large_alert_title, R.string.send_amount_too_large_alert_message);
    }

    /**
     * Catch errors from the service
     *
     * @param errorResponse Error Resposne event
     */
    @Subscribe
    public void receiveServiceError(ErrorResponse errorResponse) {
        RxBus.get().post(new HideOverlay());
        // show alert with a message to the user letting them know the amount they entered
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(getContext());
        }
        builder.setMessage(R.string.send_error_alert_message)
                .setPositiveButton(R.string.send_amount_too_large_alert_cta, (dialog, which) -> {
                    goBack();
                })
                .show();
    }

    /**
     * Received a successful send response so go back
     *
     * @param processResponse Process Response
     */
    @Subscribe
    public void receiveProcessResponse(ProcessResponse processResponse) {
        RxBus.get().post(new HideOverlay());
        accountService.requestUpdate();
        analyticsService.track(AnalyticsEvents.SEND_FINISHED);

        // updates to handle seed conversion 1.0.2
        if (newSeed != null) {
            realm.beginTransaction();
            Credentials credentials = realm.where(Credentials.class).findFirst();
            if (credentials != null) {
                credentials.setHasSentToNewSeed(true);
                credentials.setNewlyGeneratedSeed(newSeed);
            }
            realm.commitTransaction();
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Light_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(getContext());
            }

            builder.setTitle(R.string.seed_update_send_completed_alert_title)
                    .setMessage(R.string.seed_update_send_completed_alert_message)
                    .setPositiveButton(R.string.seed_update_send_completed_alert_confirm, (dialog, which) -> goBack())
                    .show();
        } else {
            goBack();
        }
    }

    /**
     * Pin entered correctly
     * @param pinComplete PinComplete object
     */
    @Subscribe
    public void receivePinComplete(PinComplete pinComplete) {
        executeSend();
    }

    @Subscribe
    public void receiveCreatePin(CreatePin pinComplete) {
        realm.beginTransaction();
        Credentials credentials = realm.where(Credentials.class).findFirst();
        if (credentials != null) {
            credentials.setPin(pinComplete.getPin());
        }
        realm.commitTransaction();
        executeSend();
    }

    private boolean validateRequest() {
        // check for valid address
        Address destination = new Address(binding.sendAddress.getText().toString());
        if (!destination.isValidAddress()) {
            showError(R.string.send_error_alert_title, R.string.send_error_alert_message);
            return false;
        }

        // check that amount being sent is less than or equal to account balance
        if (wallet.getSendNanoAmount().isEmpty()) {
            return false;
        }
        BigInteger balance = NumberUtil.getAmountAsRawBigInteger(wallet.getSendNanoAmount());
        if (balance.compareTo(wallet.getAccountBalanceNanoRaw().toBigInteger()) > 0) {
            showError(R.string.send_error_alert_title, R.string.send_error_alert_message);
            return false;
        }

        // check that we have a frontier block
        if (wallet.getFrontierBlock() == null) {
            showError(R.string.send_error_alert_title, R.string.send_error_alert_message);
            return false;
        }

        return true;
    }

    private void enableSendIfPossible() {
        boolean enableSend = true;

        // check for valid address
        Address destination = new Address(binding.sendAddress.getText().toString());
        if (!destination.isValidAddress()) {
            enableSend = false;
        }

        // check that amount being sent is less than or equal to account balance
        if (wallet.getSendNanoAmount().isEmpty()) {
            enableSend = false;
        }
        BigInteger balance = NumberUtil.getAmountAsRawBigInteger(wallet.getSendNanoAmount());
        if (balance.compareTo(new BigInteger("0")) <= 0 || balance.compareTo(wallet.getAccountBalanceNanoRaw().toBigInteger()) > 0) {
            enableSend = false;
        }


        // check that we have a frontier block
        if (wallet.getFrontierBlock() == null) {
            enableSend = false;
        }

        binding.sendSendButton.setEnabled(enableSend);
    }

    private void showError(int title, int message) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(getContext());
        }
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.send_amount_too_large_alert_cta, (dialog, which) -> {
                })
                .show();
    }

    private void showError(int title, String message) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(getContext());
        }
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.send_amount_too_large_alert_cta, (dialog, which) -> {
                })
                .show();
    }

    /**
     * Helper to set focus size and color on fields
     *
     * @param v               EditText view
     * @param hasFocus        Does view have focus currently?
     * @param isLocalCurrency Is this view the local currency view?
     */
    private void toggleFieldFocus(EditText v, boolean hasFocus, boolean isLocalCurrency) {
        localCurrencyActive = isLocalCurrency;

        v.setTextSize(TypedValue.COMPLEX_UNIT_SP, hasFocus ? 20f : 16f);
        binding.sendAmountNanoSymbol.setAlpha(hasFocus && !isLocalCurrency ? 1.0f : 0.5f);

        // clear amounts
        wallet.clearSendAmounts();
        binding.setWallet(wallet);

        // set local currency decimal separator if local currency is active, otherwise . for nano
        binding.sendKeyboardDecimal.setText(localCurrencyActive ? wallet.getDecimalSeparator() : ".");
    }

    /**
     * Update amount strings based on input processed
     *
     * @param value String value of character pressed
     */
    private void updateAmount(CharSequence value) {
        if (value.equals(getString(R.string.send_keyboard_delete))) {
            // delete last character
            if (localCurrencyActive) {
                if (wallet.getLocalCurrencyAmount().length() > 0) {
                    wallet.setLocalCurrencyAmount(wallet.getLocalCurrencyAmount().substring(0, wallet.getLocalCurrencyAmount().length() - 1));
                }
            } else {
                if (wallet.getSendNanoAmount().length() > 0) {
                    wallet.setSendNanoAmount(wallet.getSendNanoAmount().substring(0, wallet.getSendNanoAmount().length() - 1));
                }
            }
        } else if ((!localCurrencyActive && value.equals(getString(R.string.send_keyboard_decimal))) || (localCurrencyActive && value.equals(wallet.getDecimalSeparator()))) {
            // decimal point
            if (localCurrencyActive) {
                if (!wallet.getLocalCurrencyAmount().contains(value)) {
                    wallet.setLocalCurrencyAmount(wallet.getLocalCurrencyAmount() + value);
                }
            } else {
                if (!wallet.getSendNanoAmount().contains(value)) {
                    wallet.setSendNanoAmount(wallet.getSendNanoAmount() + value);
                }
            }
        } else {
            // digits
            if (localCurrencyActive) {
                wallet.setLocalCurrencyAmount(wallet.getLocalCurrencyAmount() + value);
            } else {
                wallet.setSendNanoAmount(wallet.getSendNanoAmount() + value);
            }
        }
        binding.setWallet(wallet);
        enableSendIfPossible();
    }

    private void setShortAddress() {
        // set short address if appropriate
        Address address = new Address(binding.sendAddress.getText().toString());
        if (address.isValidAddress()) {
            binding.sendAddressDisplay.setText(address.getColorizedShortSpannable());
            binding.sendAddressDisplay.setBackgroundResource(binding.sendAddressDisplay.length() > 0 ? R.drawable.bg_seed_input_active : R.drawable.bg_seed_input);
        } else {
            binding.sendAddressDisplay.setText("");
            binding.sendAddressDisplay.setBackgroundResource(binding.sendAddressDisplay.length() > 0 ? R.drawable.bg_seed_input_active : R.drawable.bg_seed_input);
        }
        enableSendIfPossible();
    }

    private void executeSend() {
        Address destination = new Address(binding.sendAddress.getText().toString());
        if (destination.isValidAddress()) {
            RxBus.get().post(new ShowOverlay());
            BigInteger sendAmount = NumberUtil.getAmountAsRawBigInteger(wallet.getSendNanoAmount());

            accountService.requestSend(wallet.getFrontierBlock(), destination, sendAmount);
            analyticsService.track(AnalyticsEvents.SEND_BEGAN);
        } else {
            showError(R.string.send_error_alert_title, R.string.send_error_alert_message);
        }
    }



    public class ClickHandlers {
        /**
         * Listener for styling updates when text changes
         *
         * @param s      Character sequence
         * @param start  Starting character
         * @param before Character that came before
         * @param count  Total character count
         */
        public void onAddressTextChanged(CharSequence s, int start, int before, int count) {
            // set background to active or not
            binding.sendAddress.setBackgroundResource(s.length() > 0 ? R.drawable.bg_seed_input_active : R.drawable.bg_seed_input);

            // colorize input string
            UIUtil.colorizeSpannable(binding.sendAddress.getText(), getContext());
        }

        public void onAddressDisplayClicked(View view) {
            binding.setShowAmount(false);
            binding.sendAddress.setSelection(binding.sendAddress.getText().length());
            showSoftKeyboard();
        }

        public void onClickNanoContainer(View view) {
            binding.sendAmountNano.requestFocus();
        }

        public void onClickConfirm(View view) {
            binding.setShowAmount(true);
            setShortAddress();
            KeyboardUtil.hideKeyboard(getActivity());
            binding.sendAmountNano.requestFocus();
        }

        public void onClickSend(View view) {
            if (!validateRequest()) {
                return;
            }

            Credentials credentials = realm.where(Credentials.class).findFirst();

            if (Reprint.isHardwarePresent() && Reprint.hasFingerprintRegistered()) {
                // show fingerprint dialog
                LayoutInflater factory = LayoutInflater.from(getContext());
                @SuppressLint("InflateParams") final View viewFingerprint = factory.inflate(R.layout.view_fingerprint, null);
                showFingerprintDialog(viewFingerprint);
                com.github.ajalt.reprint.rxjava2.RxReprint.authenticate()
                        .subscribe(result -> {
                            switch (result.status) {
                                case SUCCESS:
                                    showFingerprintSuccess(viewFingerprint);
                                    break;
                                case NONFATAL_FAILURE:
                                    showFingerprintError(result.failureReason, result.errorMessage, viewFingerprint);
                                    break;
                                case FATAL_FAILURE:
                                    showFingerprintError(result.failureReason, result.errorMessage, viewFingerprint);
                                    break;
                            }
                        });
            } else if (credentials != null && credentials.getPin() != null) {
                showPinScreen(getString(R.string.send_pin_description, wallet.getSendNanoAmount()));
            } else if (credentials != null && credentials.getPin() == null) {
                showCreatePinScreen();
            }
        }

        public void onClickMax(View view) {
            analyticsService.track(AnalyticsEvents.SEND_MAX_AMOUNT_USED);
            wallet.setSendNanoAmount(wallet.getLongerAccountBalanceNano());
            binding.setWallet(wallet);
            enableSendIfPossible();
        }

        public void onClickNumKeyboard(View view) {
            updateAmount(((Button) view).getText());
        }

    }

    private void showFingerprintDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.send_fingerprint_title));
        builder.setMessage(getString(R.string.send_fingerprint_description,
                !wallet.getSendNanoAmountFormatted().isEmpty() ? wallet.getSendNanoAmountFormatted() : "0"));
        builder.setView(view);
        String negativeText = getString(android.R.string.cancel);
        builder.setNegativeButton(negativeText, (dialog, which) -> Reprint.cancelAuthentication());

        fingerprintDialog = builder.create();
        fingerprintDialog.setCanceledOnTouchOutside(false);
        // display dialog
        fingerprintDialog.show();
    }

    private void showFingerprintSuccess(View view) {
        if (isAdded()) {
            TextView textView = view.findViewById(R.id.fingerprint_textview);
            textView.setText(getString(R.string.send_fingerprint_success));
            if (getContext() != null) {
                textView.setTextColor(ContextCompat.getColor(getContext(), R.color.dark_sky_blue));
            }
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fingerprint_success, 0, 0, 0);
            executeSend();

            // close dialog after 1 second
            final Handler handler = new Handler();
            final Runnable runnable = () -> {
                if (fingerprintDialog != null && fingerprintDialog.isShowing()) {
                    fingerprintDialog.dismiss();
                }
            };
            handler.postDelayed(runnable, 500);
        }
    }

    private void showFingerprintError(AuthenticationFailureReason reason, CharSequence message, View view) {
        if (isAdded()) {
            final HashMap<String, String> customData = new HashMap<>();
            customData.put("description", reason.name());
            analyticsService.track(AnalyticsEvents.SEND_AUTH_ERROR, customData);
            TextView textView = view.findViewById(R.id.fingerprint_textview);
            textView.setText(message.toString());
            if (getContext() != null) {
                textView.setTextColor(ContextCompat.getColor(getContext(), R.color.error));
            }
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fingerprint_error, 0, 0, 0);
        }
    }
}
