package co.nano.nanowallet.ui.pin;

import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import javax.inject.Inject;

import co.nano.nanowallet.R;
import co.nano.nanowallet.analytics.AnalyticsEvents;
import co.nano.nanowallet.analytics.AnalyticsService;
import co.nano.nanowallet.bus.CreatePin;
import co.nano.nanowallet.bus.RxBus;
import co.nano.nanowallet.databinding.FragmentCreatePinBinding;
import co.nano.nanowallet.ui.common.ActivityWithComponent;
import co.nano.nanowallet.ui.common.BaseDialogFragment;

/**
 * Settings main screen
 */
public class CreatePinDialogFragment extends BaseDialogFragment {
    private FragmentCreatePinBinding binding;
    public static String TAG = CreatePinDialogFragment.class.getSimpleName();
    private static final int PIN_LENGTH = 4;
    private String firstPin = null;

    @Inject
    AnalyticsService analyticsService;

    /**
     * Create new instance of the dialog fragment (handy pattern if any data needs to be passed to it)
     *
     * @return ReceiveDialogFragment instance
     */
    public static CreatePinDialogFragment newInstance() {
        Bundle args = new Bundle();
        CreatePinDialogFragment fragment = new CreatePinDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, R.style.AppTheme_Modal_Window);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // init dependency injection
        if (getActivity() instanceof ActivityWithComponent) {
            ((ActivityWithComponent) getActivity()).getActivityComponent().inject(this);
        }

        // inflate the view
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_create_pin, container, false);
        view = binding.getRoot();

        // show keyboard by default
        binding.pinEntry.requestFocus();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }

        binding.setHandlers(new ClickHandlers());
        binding.pinEntry.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String pin = v.getText().toString();
                if (pin.length() != PIN_LENGTH) {
                    return false;
                }
                if (firstPin == null) {
                    // first pin submitted, so move to confirm screen
                    firstPin = pin;
                    binding.pinTitle.setText(R.string.pin_confirm_title);
                    v.setText("");
                } else if (firstPin.equals(pin)) {
                    // pins matched, so set this as the pin
                    RxBus.get().post(new CreatePin(pin));
                    // close keyboard
                    InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                    if (inputMethodManager != null && getActivity().getCurrentFocus() != null) {
                        inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                    }

                    dismiss();
                } else {
                    // pins did not match
                    binding.pinTitle.setText(R.string.pin_confirm_error);
                }

                return true;
            }
            return false;
        });


        setStatusBarWhite(view);

        return view;
    }

    public class ClickHandlers {
        public void onClickClose(View view) {
            InputMethodManager input = (InputMethodManager) binding.pinEntry.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (input != null && input.isActive()) {
                input.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            }
            dismiss();
        }
    }
}
