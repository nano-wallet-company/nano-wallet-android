package co.nano.nanowallet.util;

import timber.log.Timber;

public class ExceptionHandler {
    public static void handle(Throwable t) {
        if (t != null) {
            // log to crashlytics
            //Crashlytics.logException(t);

            // Log to console
            Timber.e(t.getMessage());
        }
    }
}