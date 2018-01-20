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
import android.widget.Button;

import co.nano.nanowallet.R;
import co.nano.nanowallet.databinding.FragmentSendBinding;
import co.nano.nanowallet.ui.common.BaseFragment;
import co.nano.nanowallet.ui.common.UIUtil;

/**
 * Settings main screen
 */
public class SendFragment extends BaseFragment {
    private FragmentSendBinding binding;
    public static String TAG = SendFragment.class.getSimpleName();
    private String nanoValue = "";

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

        return view;
    }

    private void updateAmount(CharSequence value) {
        if (value.equals(getString(R.string.send_keyboard_delete))) {
            // delete last character
            if (nanoValue.length() > 0) {
                nanoValue = nanoValue.substring(0, nanoValue.length() - 1);
            }
        } else if (value.equals(getString(R.string.send_keyboard_decimal))) {
            // decimal point
            if (!nanoValue.contains(value)) {
                nanoValue = nanoValue + value;
            }
        } else {
            // digits
            nanoValue = nanoValue + value;
        }
        binding.setNanoValue(nanoValue);
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
