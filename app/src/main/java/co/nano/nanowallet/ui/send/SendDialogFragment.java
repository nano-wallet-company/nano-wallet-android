package co.nano.nanowallet.ui.send;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.nano.nanowallet.R;
import co.nano.nanowallet.databinding.FragmentSendBinding;
import co.nano.nanowallet.ui.common.BaseDialogFragment;
import co.nano.nanowallet.ui.common.UIUtil;

/**
 * Settings main screen
 */
public class SendDialogFragment extends BaseDialogFragment {
    private FragmentSendBinding binding;
    public static String TAG = SendDialogFragment.class.getSimpleName();
    private String address;
    private static final int QRCODE_SIZE = 200;

    /**
     * Create new instance of the dialog fragment (handy pattern if any data needs to be passed to it)
     *
     * @return
     */
    public static SendDialogFragment newInstance() {
        Bundle args = new Bundle();
        SendDialogFragment fragment = new SendDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, R.style.AppTheme_Modal_Window);

        // TODO: The receive address should be passed in or generated somewhere
        address = "xrb_3gntuoguehi9d1mnhnar6ojx7jseeerwj5hesb4b4jga7oybbdbqyzap7ijg";
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // inflate the view
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_send, container, false);
        View view = binding.getRoot();
        binding.setHandlers(new ClickHandlers());

        setStatusBarWhite(view);

        return view;
    }


    public class ClickHandlers {
        public void onClickClose(View view) {
            dismiss();
        }

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

        }

        public void onClickDecimal(View view) {

        }

        public void onClickDelete(View view) {

        }

        public void onClickZero(View view) {

        }

        public void onClickOne(View view) {

        }

        public void onClickTwo(View view) {

        }

        public void onClickThree(View view) {

        }

        public void onClickFour(View view) {

        }

        public void onClickFive(View view) {

        }

        public void onClickSix(View view) {

        }

        public void onClickSeven(View view) {

        }

        public void onClickEight(View view) {

        }

        public void onClickNine(View view) {

        }

    }
}
