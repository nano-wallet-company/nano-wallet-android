package co.nano.nanowallet.ui.common;

import android.view.View;

/**
 * Created by szeidner on 10/01/2018.
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
