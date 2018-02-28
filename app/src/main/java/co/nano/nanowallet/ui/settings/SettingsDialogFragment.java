package co.nano.nanowallet.ui.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.databinding.BindingAdapter;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import co.nano.nanowallet.R;
import co.nano.nanowallet.bus.Logout;
import co.nano.nanowallet.bus.RxBus;
import co.nano.nanowallet.databinding.FragmentSettingsBinding;
import co.nano.nanowallet.model.AvailableCurrency;
import co.nano.nanowallet.model.Credentials;
import co.nano.nanowallet.model.StringWithTag;
import co.nano.nanowallet.network.AccountService;
import co.nano.nanowallet.ui.common.ActivityWithComponent;
import co.nano.nanowallet.ui.common.BaseDialogFragment;
import co.nano.nanowallet.ui.common.WindowControl;
import co.nano.nanowallet.util.SharedPreferencesUtil;
import io.realm.Realm;

/**
 * Settings main screen
 */
public class SettingsDialogFragment extends BaseDialogFragment {
    private FragmentSettingsBinding binding;
    public static String TAG = SettingsDialogFragment.class.getSimpleName();
    private Boolean showCurrency = false;

    @Inject
    SharedPreferencesUtil sharedPreferencesUtil;
    @Inject
    Realm realm;
    @Inject
    AccountService accountService;

    @BindingAdapter("android:layout_marginTop")
    public static void setTopMargin(View view, float topMargin) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin, Math.round(topMargin),
                layoutParams.rightMargin, layoutParams.bottomMargin);
        view.setLayoutParams(layoutParams);
    }

    /**
     * Create new instance of the dialog fragment (handy pattern if any data needs to be passed to it)
     *
     * @return
     */
    public static SettingsDialogFragment newInstance() {
        Bundle args = new Bundle();
        SettingsDialogFragment fragment = new SettingsDialogFragment();
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
        Answers.getInstance().logCustom(new CustomEvent("Settings VC Viewed"));

        // inject
        if (getActivity() instanceof ActivityWithComponent) {
            ((ActivityWithComponent) getActivity()).getActivityComponent().inject(this);
        }

        // inflate the view
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_settings, container, false);
        View view = binding.getRoot();
        binding.setHandlers(new ClickHandlers());
        binding.setShowCurrency(showCurrency);

        setStatusBarWhite(view);

        // set up spinner
        List<StringWithTag> availableCurrencies = getAllCurrencies();
        ArrayAdapter<StringWithTag> spinnerArrayAdapter = new ArrayAdapter<>(getContext(),
                R.layout.view_textview_spinner,
                availableCurrencies
        );

        spinnerArrayAdapter.setDropDownViewResource(R.layout.view_textview_spinner);
        binding.settingsLocalCurrencySpinner.setAdapter(spinnerArrayAdapter);
        binding.settingsLocalCurrencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // save local currency to shared preferences
                StringWithTag swt = (StringWithTag) adapterView.getItemAtPosition(i);
                AvailableCurrency key = (AvailableCurrency) swt.tag;
                if (key != null) {
                    sharedPreferencesUtil.setLocalCurrency(key);
                    Answers.getInstance().logCustom(new CustomEvent("Local Currency Selected").putCustomAttribute("currency", key.toString()));
                    // update currency amounts
                    accountService.requestSubscribe();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // set selected item with value saved in shared preferences
        binding.settingsLocalCurrencySpinner.setSelection(getIndexOf(sharedPreferencesUtil.getLocalCurrency(), availableCurrencies));

        // set the listener for Navigation
        Toolbar toolbar = view.findViewById(R.id.dialog_appbar);
        if (toolbar != null) {
            final SettingsDialogFragment window = this;
            TextView title = view.findViewById(R.id.dialog_toolbar_title);
            title.setText(R.string.settings_title);
            toolbar.setNavigationOnClickListener(v1 -> window.dismiss());
        }

        return view;
    }

    /**
     * Get list of all of the available currencies
     *
     * @return
     */
    private List<StringWithTag> getAllCurrencies() {
        List<StringWithTag> itemList = new ArrayList<>();
        for (AvailableCurrency currency : AvailableCurrency.values()) {
            itemList.add(new StringWithTag(currency.getFullDisplayName(), currency));
        }
        return itemList;
    }

    /**
     * Get list of all of the available currencies
     *
     * @return
     */
    private int getIndexOf(AvailableCurrency currency, List<StringWithTag> availableCurrencies) {
        int i = 0;
        for (StringWithTag availableCurrency : availableCurrencies) {
            if (availableCurrency.tag.equals(currency)) {
                return i;
            }
            i++;
        }
        return 0;
    }

    public class ClickHandlers {
        public void onClickLocalCurrency(View view) {
            showCurrency = !showCurrency;
            binding.setShowCurrency(showCurrency);
        }

        public void onClickShowSeed(View view) {
            // show the copy seed dialog
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Light_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(getContext());
            }

            Credentials credentials = realm.where(Credentials.class).findFirst();

            builder.setTitle(R.string.settings_seed_alert_title)
                    .setMessage(R.string.settings_seed_alert_message)
                    .setPositiveButton(R.string.settings_seed_alert_confirm_cta, (dialog, which) -> {
                        Answers.getInstance().logCustom(new CustomEvent("Seed Copied"));
                        // copy seed to clipboard
                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        android.content.ClipData clip = android.content.ClipData.newPlainText("seed", credentials.getSeed());
                        if (clipboard != null) {
                            clipboard.setPrimaryClip(clip);
                        }

                        setClearClipboardAlarm();
                    })
                    .setNegativeButton(R.string.settings_seed_alert_cancel_cta, (dialog, which) -> {
                        // do nothing which dismisses the dialog
                    })
                    .setIcon(R.drawable.ic_warning)
                    .show();
        }

        public void onClickLogOut(View view) {
            if (getActivity() instanceof WindowControl) {

                // show the logout are-you-sure dialog
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Light_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(getContext());
                }
                builder.setTitle(R.string.settings_logout_alert_title)
                        .setMessage(R.string.settings_logout_alert_message)
                        .setPositiveButton(R.string.settings_logout_alert_confirm_cta, (dialog, which) -> {
                            RxBus.get().post(new Logout());
                            dismiss();
                        })
                        .setNegativeButton(R.string.settings_logout_alert_cancel_cta, (dialog, which) -> {
                            // do nothing which dismisses the dialog
                        })
                        .show();
            }
        }
    }
}
