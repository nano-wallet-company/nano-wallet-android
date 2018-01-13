package co.nano.nanowallet.ui.common;

import android.view.View;

/**
 * Created by szeidner on 10/01/2018.
 */

public interface FragmentControl {
    FragmentUtility getFragmentUtility();
    void setStatusBarColor(int color);
    void setDarkIcons(View view);
}
