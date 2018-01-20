package co.nano.nanowallet.ui.send;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import co.nano.nanowallet.R;
import co.nano.nanowallet.databinding.FragmentSendBinding;
import co.nano.nanowallet.ui.common.BaseFragment;
import co.nano.nanowallet.ui.common.UIUtil;

/**
 * Settings main screen
 */
public class SendDialogFragment extends BaseFragment {
    private FragmentSendBinding binding;
    public static String TAG = SendDialogFragment.class.getSimpleName();
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
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_send, menu);
        super.onCreateOptionsMenu(menu,inflater);
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
        setBackEndabled(true);
        setTitle(getString(R.string.send_title));
        setTitleDrawable(R.drawable.ic_send);

        return view;
    }


    public class ClickHandlers {
        public void onClickClose(View view) {
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
