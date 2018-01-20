package co.nano.nanowallet.ui.send;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import co.nano.nanowallet.R;
import co.nano.nanowallet.databinding.FragmentSendBinding;
import co.nano.nanowallet.model.SendAmount;
import co.nano.nanowallet.ui.common.BaseFragment;
import co.nano.nanowallet.ui.common.UIUtil;

/**
 * Send Screen
 */
public class SendFragment extends BaseFragment {
    private FragmentSendBinding binding;
    public static String TAG = SendFragment.class.getSimpleName();
    private SendAmount sendAmount = new SendAmount();
    private boolean localCurrencyActive = false;

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
                // TODO: Load camera
                return true;
        }

        return false;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

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

        return view;
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
        binding.sendAmountLocalcurrencySymbol.setTextSize(TypedValue.COMPLEX_UNIT_SP, hasFocus && isLocalCurrency ? 16f : 14f);
        binding.sendAmountLocalcurrencySymbol.setTextColor(hasFocus && isLocalCurrency ?
                ContextCompat.getColor(getContext(), R.color.bright_white) : ContextCompat.getColor(getContext(), R.color.semitranslucent_white));

        // clear amounts
        // TODO: Should this be cleared every time?
        sendAmount = new SendAmount();
        binding.setSendAmount(sendAmount);
    }

    /**
     * Update amount strings based on input processed
     * @param value String value of character pressed
     */
    private void updateAmount(CharSequence value) {
        if (value.equals(getString(R.string.send_keyboard_delete))) {
            // delete last character
            if (localCurrencyActive) {
                if (sendAmount.getLocalCurrencyAmount().length() > 0) {
                    sendAmount.setLocalCurrencyAmount(sendAmount.getLocalCurrencyAmount().substring(0, sendAmount.getLocalCurrencyAmount().length() - 1));
                }
            } else {
                if (sendAmount.getNanoAmount().length() > 0) {
                    sendAmount.setNanoAmount(sendAmount.getNanoAmount().substring(0, sendAmount.getNanoAmount().length() - 1));
                }
            }
        } else if (value.equals(getString(R.string.send_keyboard_decimal))) {
            // decimal point
            if (localCurrencyActive) {
                if (!sendAmount.getLocalCurrencyAmount().contains(value)) {
                    sendAmount.setLocalCurrencyAmount(sendAmount.getLocalCurrencyAmount() + value);
                }
            } else {
                if (!sendAmount.getNanoAmount().contains(value)) {
                    sendAmount.setNanoAmount(sendAmount.getNanoAmount() + value);
                }
            }
        } else {
            // digits
            if (localCurrencyActive) {
                sendAmount.setLocalCurrencyAmount(sendAmount.getLocalCurrencyAmount() + value);
            } else {
                sendAmount.setNanoAmount(sendAmount.getNanoAmount() + value);
            }
        }
        binding.setSendAmount(sendAmount);
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

        public void onClickSend(View view) {
            // TODO: Send money
            goBack();
        }

        public void onClickMax(View view) {
            // TODO: Seed with max from wallet
        }

        public void onClickNumKeyboard(View view) {
            updateAmount(((Button) view).getText());
        }

    }
}
