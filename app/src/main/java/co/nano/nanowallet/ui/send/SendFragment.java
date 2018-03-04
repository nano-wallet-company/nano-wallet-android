package co.nano.nanowallet.ui.send;

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

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.github.ajalt.reprint.core.AuthenticationFailureReason;
import com.github.ajalt.reprint.core.Reprint;
import com.hwangjr.rxbus.annotation.Subscribe;

import java.math.BigInteger;
import java.text.NumberFormat;

import javax.inject.Inject;

import co.nano.nanowallet.R;
import co.nano.nanowallet.bus.RxBus;
import co.nano.nanowallet.bus.SendInvalidAmount;
import co.nano.nanowallet.databinding.FragmentSendBinding;
import co.nano.nanowallet.model.Address;
import co.nano.nanowallet.model.AvailableCurrency;
import co.nano.nanowallet.model.NanoWallet;
import co.nano.nanowallet.network.AccountService;
import co.nano.nanowallet.network.model.response.ErrorResponse;
import co.nano.nanowallet.network.model.response.ProcessResponse;
import co.nano.nanowallet.ui.common.ActivityWithComponent;
import co.nano.nanowallet.ui.common.BaseFragment;
import co.nano.nanowallet.ui.common.UIUtil;
import co.nano.nanowallet.ui.scan.ScanActivity;
import co.nano.nanowallet.util.NumberUtil;
import co.nano.nanowallet.util.SharedPreferencesUtil;

import static android.app.Activity.RESULT_OK;

/**
 * Send Screen
 */
public class SendFragment extends BaseFragment {
    private FragmentSendBinding binding;
    public static String TAG = SendFragment.class.getSimpleName();
    private boolean localCurrencyActive = false;
    private AlertDialog fingerprintDialog;

    @Inject
    NanoWallet wallet;

    @Inject
    AccountService accountService;

    @Inject
    SharedPreferencesUtil sharedPreferencesUtil;

    @BindingAdapter("layout_constraintGuide_percent")
    public static void setLayoutConstraintGuidePercent(Guideline guideline, float percent) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) guideline.getLayoutParams();
        params.guidePercent = percent;
        guideline.setLayoutParams(params);
    }

    /**
     * Create new instance of the fragment (handy pattern if any data needs to be passed to it)
     *
     * @return
     */
    public static SendFragment newInstance() {
        Bundle args = new Bundle();
        SendFragment fragment = new SendFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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
                Answers.getInstance().logCustom(new CustomEvent("Address Scan Camera View Used"));
                startScanActivity(getString(R.string.scan_send_instruction_label), false);
                return true;
        }

        return false;
    }

    public void hideSoftKeyboard() {
        //Hides the SoftKeyboard
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null && getActivity().getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
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
        Answers.getInstance().logCustom(new CustomEvent("Send VC Viewed"));

        // init dependency injection
        if (getActivity() instanceof ActivityWithComponent) {
            ((ActivityWithComponent) getActivity()).getActivityComponent().inject(this);
        }

        // subscribe to bus
        RxBus.get().register(this);

        // change keyboard mode
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        // inflate the view
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_send, container, false);
        View view = binding.getRoot();
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

        binding.sendAddress.setOnFocusChangeListener((view12, hasFocus) -> {
            binding.setShowAmount(!hasFocus);
        });

        binding.sendAddress.setBackgroundResource(binding.sendAddress.getText().length() > 0 ? R.drawable.bg_seed_input_active : R.drawable.bg_seed_input);
        UIUtil.colorizeSpannable(binding.sendAddress.getText(), getContext());

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
     * @param sendInvalidAmount
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
     * @param errorResponse
     */
    @Subscribe
    public void receiveServiceError(ErrorResponse errorResponse) {
        // show alert with a message to the user letting them know the amount they entered
        showError(R.string.send_error_alert_title, errorResponse.getError());
    }

    /**
     * Received a successful send response so go back
     *
     * @param processResponse Process Response
     */
    @Subscribe
    public void receiveProcessResponse(ProcessResponse processResponse) {
        accountService.requestUpdate();
        Answers.getInstance().logCustom(new CustomEvent("Send Nano Finished"));
        goBack();
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
     * @param v
     * @param hasFocus
     * @param isLocalCurrency
     */
    private void toggleFieldFocus(EditText v, boolean hasFocus, boolean isLocalCurrency) {
        localCurrencyActive = isLocalCurrency;

        v.setTextSize(TypedValue.COMPLEX_UNIT_SP, hasFocus ? 20f : 16f);
        binding.sendAmountNanoSymbol.setTextSize(TypedValue.COMPLEX_UNIT_SP, hasFocus && isLocalCurrency ? 16f : 14f);
        binding.sendAmountNanoSymbol.setAlpha(hasFocus && !isLocalCurrency ? 1.0f : 0.5f);

        // clear amounts
        wallet.clearSendAmounts();
        binding.setWallet(wallet);
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
        } else if (value.equals(getString(R.string.send_keyboard_decimal))) {
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
        BigInteger sendAmount = NumberUtil.getAmountAsRawBigInteger(wallet.getSendNanoAmount());
        BigInteger balance = wallet.getAccountBalanceNanoRaw().toBigInteger().subtract(sendAmount);

        accountService.requestSend(wallet.getFrontierBlock(), destination, balance);
        Answers.getInstance().logCustom(new CustomEvent("Send Nano Began"));
    }

    public class ClickHandlers {
        /**
         * Listener for styling updates when text changes
         *
         * @param s
         * @param start
         * @param before
         * @param count
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

        public void onClickConfirm(View view) {
            binding.setShowAmount(true);
            setShortAddress();
            hideSoftKeyboard();
            binding.sendAmountNano.requestFocus();
        }

        public void onClickSend(View view) {
            if (!validateRequest()) {
                return;
            }

            if (Reprint.isHardwarePresent() && Reprint.hasFingerprintRegistered()) {
                // show fingerprint dialog
                LayoutInflater factory = LayoutInflater.from(getContext());
                final View viewFingerprint = factory.inflate(R.layout.view_fingerprint, null);
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
            } else {
                // no fingerprint hardware present
                executeSend();
            }
        }

        public void onClickMax(View view) {
            Answers.getInstance().logCustom(new CustomEvent("Send: Max Amount Used"));
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
        builder.setNegativeButton(negativeText,
                (dialog, which) -> {
                    // negative button logic
                });

        fingerprintDialog = builder.create();
        // display dialog
        fingerprintDialog.show();
    }

    private void showFingerprintSuccess(View view) {
        TextView textView = view.findViewById(R.id.fingerprint_textview);
        textView.setText(getString(R.string.send_fingerprint_success));
        textView.setTextColor(ContextCompat.getColor(getContext(), R.color.dark_sky_blue));
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

    private void showFingerprintError(AuthenticationFailureReason reason, CharSequence message, View view) {
        TextView textView = view.findViewById(R.id.fingerprint_textview);
        textView.setText(message.toString());
        textView.setTextColor(ContextCompat.getColor(getContext(), R.color.error));
        textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_fingerprint_error, 0, 0, 0);
    }
}
