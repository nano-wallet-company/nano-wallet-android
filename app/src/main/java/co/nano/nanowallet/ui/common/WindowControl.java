package co.nano.nanowallet.ui.common;

import android.view.View;

/**
 * Interface for window control methods
 */

public interface WindowControl {
    FragmentUtility getFragmentUtility();
    void setStatusBarColor(int color);
    void setDarkIcons(View view);
    void setToolbarVisible(boolean visible);
    void setTitle(String title);
    void setTitleDrawable(int drawable);
    void setBackEnabled(boolean enabled);
}
