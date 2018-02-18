package co.nano.nanowallet.ui.common;

import co.nano.nanowallet.di.activity.ActivityComponent;
import co.nano.nanowallet.di.application.ApplicationComponent;

/**
 * Created by szeidner on 10/01/2018.
 */

public interface ActivityWithComponent {
    ActivityComponent getActivityComponent();
    ApplicationComponent getApplicationComponent();
}
