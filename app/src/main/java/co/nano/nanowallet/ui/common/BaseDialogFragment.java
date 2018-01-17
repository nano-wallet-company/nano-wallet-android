package co.nano.nanowallet.ui.common;

import android.support.v4.app.DialogFragment;
import android.view.View;

import co.nano.nanowallet.R;

/**
 * Created by szeidner on 12/01/2018.
 */

public class BaseDialogFragment extends DialogFragment {

    /**
     * Set status bar color to dark blue
     */
    protected void setStatusBarBlue() {
        setStatusBarColor(R.color.very_dark_blue);
    }

    /**
     * Set status bar color to white
     * @param view an active view
     */
    protected void setStatusBarWhite(View view) {
        setStatusBarColor(R.color.bright_white);
        setIconsDark(view);
    }

    private void setStatusBarColor(int color) {
        if (getActivity() instanceof FragmentControl) {
            ((FragmentControl) getActivity()).setStatusBarColor(color);
        }
    }

    private void setIconsDark(View view) {
        if (getActivity() instanceof FragmentControl) {
            ((FragmentControl) getActivity()).setDarkIcons(view);
        }
    }

}
