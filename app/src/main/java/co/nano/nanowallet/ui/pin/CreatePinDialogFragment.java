package co.nano.nanowallet.ui.pin;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.andrognito.pinlockview.PinLockListener;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import javax.inject.Inject;

import co.nano.nanowallet.R;
import co.nano.nanowallet.bus.PinChange;
import co.nano.nanowallet.bus.PinComplete;
import co.nano.nanowallet.bus.RxBus;
import co.nano.nanowallet.databinding.FragmentCreatePinBinding;
import co.nano.nanowallet.ui.common.ActivityWithComponent;
import co.nano.nanowallet.ui.common.BaseDialogFragment;
import io.realm.Realm;
import timber.log.Timber;

/**
 * Settings main screen
 */
public class CreatePinDialogFragment extends BaseDialogFragment {
    private FragmentCreatePinBinding binding;
    public static String TAG = CreatePinDialogFragment.class.getSimpleName();
    private static final int PIN_LENGTH = 4;

    private String firstPin = null;

    private PinLockListener pinLockListener = new PinLockListener() {
        @Override
            public void onComplete(String pin) {
            Timber.d("Pin complete: %s", pin);

            if (firstPin == null) {
                // first pin submitted, so move to confirm screen
                firstPin = pin;
                binding.pinTitle.setText(R.string.pin_confirm_title);
                binding.pinLockView.resetPinLockView();
            } else if (firstPin.equals(pin)) {
                // pins matched, so set this as the pin
                RxBus.get().post(new PinComplete(pin));
                dismiss();
            } else {
                // pins did not match
                Toast.makeText(getContext(), R.string.pin_confirm_error, Toast.LENGTH_SHORT).show();
                firstPin = null;
                binding.pinTitle.setText(R.string.pin_create_title);
                binding.pinLockView.resetPinLockView();
            }
        }

        @Override
        public void onEmpty() {
            Timber.d("Pin empty");
        }

        @Override
        public void onPinChange(int pinLength, String intermediatePin) {
            Timber.d("Pin changed, new length " + pinLength + " with intermediate pin " + intermediatePin);
            RxBus.get().post(new PinChange(pinLength, intermediatePin));
        }
    };

    @Inject
    Realm realm;

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
        Answers.getInstance().logCustom(new CustomEvent("Received VC Viewed"));

        // init dependency injection
        if (getActivity() instanceof ActivityWithComponent) {
            ((ActivityWithComponent) getActivity()).getActivityComponent().inject(this);
        }

        // inflate the view
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_create_pin, container, false);
        View view = binding.getRoot();

        binding.pinLockView.attachIndicatorDots(binding.pinIndicatorDots);
        binding.pinLockView.setPinLockListener(pinLockListener);
        binding.pinLockView.setPinLength(PIN_LENGTH);

        setStatusBarWhite(view);

        return view;
    }
}
