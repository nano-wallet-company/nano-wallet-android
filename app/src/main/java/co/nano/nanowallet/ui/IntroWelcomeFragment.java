package co.nano.nanowallet.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.nano.nanowallet.BuildConfig;
import co.nano.nanowallet.R;

/**
 * The Intro Screen to the app
 */

public class IntroWelcomeFragment extends Fragment {
    @BindView(R.id.intro_welcome_version)
    TextView version;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_intro_welcome, container, false);

        // bind views
        ButterKnife.bind(this, v);

        // bind data to views
        bindData();

        return v;
    }

    private void bindData() {
        version.setText(getString(R.string.version_display, BuildConfig.VERSION_NAME));
    }


}
