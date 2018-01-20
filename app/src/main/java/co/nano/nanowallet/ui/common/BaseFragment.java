package co.nano.nanowallet.ui.common;

import android.support.v4.app.Fragment;
import android.view.View;

import co.nano.nanowallet.R;

/**
 * Created by szeidner on 12/01/2018.
 */

public class BaseFragment extends Fragment {

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
        if (getActivity() instanceof WindowControl) {
            ((WindowControl) getActivity()).setStatusBarColor(color);
        }
    }

    private void setIconsDark(View view) {
        if (getActivity() instanceof WindowControl) {
            ((WindowControl) getActivity()).setDarkIcons(view);
        }
    }

    /**
     * Hide the app toolbar
     */
    protected void hideToolbar() {
        if (getActivity() instanceof WindowControl) {
            ((WindowControl) getActivity()).setToolbarVisible(false);
        }
    }

    /**
     * Show the app toolbar
     */
    protected void showToolbar() {
        if (getActivity() instanceof WindowControl) {
            ((WindowControl) getActivity()).setToolbarVisible(true);
        }
    }

    /**
     * Set the title of the toolbar
     */
    protected void setTitle(String title) {
        if (getActivity() instanceof WindowControl) {
            ((WindowControl) getActivity()).setTitle(title);
        }
    }

    /**
     * Set drawable on the toolbar
     * @param drawable
     */
    protected void setTitleDrawable(int drawable) {
        if (getActivity() instanceof WindowControl) {
            ((WindowControl) getActivity()).setTitleDrawable(drawable);
        }
    }

    /**
     * Enable or disable back button
     * @param enabled
     */
    protected void setBackEndabled(boolean enabled) {
        if (getActivity() instanceof WindowControl) {
            ((WindowControl) getActivity()).setBackEnabled(enabled);
        }
    }

}
