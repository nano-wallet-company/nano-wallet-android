package co.nano.nanowallet.ui.common;

import co.nano.nanowallet.di.activity.ActivityComponent;
import co.nano.nanowallet.di.application.ApplicationComponent;

/**
 * Interface for Activity with a Component
 */

public interface ActivityWithComponent {
    ActivityComponent getActivityComponent();
    ApplicationComponent getApplicationComponent();
}
