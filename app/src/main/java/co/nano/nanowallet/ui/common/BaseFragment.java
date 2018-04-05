package co.nano.nanowallet.ui.common;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import co.nano.nanowallet.R;
import co.nano.nanowallet.analytics.AnalyticsService;
import co.nano.nanowallet.broadcastreceiver.ClipboardAlarmReceiver;
import co.nano.nanowallet.model.Credentials;
import co.nano.nanowallet.ui.pin.CreatePinDialogFragment;
import co.nano.nanowallet.ui.pin.PinDialogFragment;
import co.nano.nanowallet.ui.scan.ScanActivity;
import co.nano.nanowallet.util.ExceptionHandler;
import io.realm.Realm;

/**
 * Helper methods used by all fragments
 */

public class BaseFragment extends Fragment {
    private static final int ZXING_CAMERA_PERMISSION = 1;
    protected static final int SCAN_RESULT = 2;

    private String scanActivityTitle;
    private boolean isSeedScanner;
    protected View view;

    /**
     * Set status bar color to dark blue
     */
    protected void setStatusBarBlue() {
        setStatusBarColor(R.color.very_dark_blue);
    }

    /**
     * Set status bar color to white
     *
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
     *
     * @param drawable Drawable reference
     */
    protected void setTitleDrawable(int drawable) {
        if (getActivity() instanceof WindowControl) {
            ((WindowControl) getActivity()).setTitleDrawable(drawable);
        }
    }

    /**
     * Enable or disable back button
     *
     * @param enabled Is enabled or not
     */
    protected void setBackEnabled(boolean enabled) {
        if (getActivity() instanceof WindowControl) {
            ((WindowControl) getActivity()).setBackEnabled(enabled);
        }
    }

    /**
     * Go back action
     */
    protected void goBack() {
        if (getActivity() instanceof WindowControl) {
            try {
                ((WindowControl) getActivity()).getFragmentUtility().pop();
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case ZXING_CAMERA_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    Intent intent = new Intent(getActivity(), ScanActivity.class);
                    intent.putExtra(ScanActivity.EXTRA_TITLE, scanActivityTitle);
                    intent.putExtra(ScanActivity.EXTRA_IS_SEED, isSeedScanner);
                    startActivityForResult(intent, SCAN_RESULT);
                }
            }
        }
    }

    /**
     * Start the scanner activity
     *
     * @param title Title that should be displayed above the viewfinder
     */
    protected void startScanActivity(String title, boolean isSeedScanner) {
        this.scanActivityTitle = title;
        this.isSeedScanner = isSeedScanner;
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // check first to see if camera permission has been granted
            requestPermissions(new String[]{Manifest.permission.CAMERA}, ZXING_CAMERA_PERMISSION);
        } else {
            Intent intent = new Intent(getActivity(), ScanActivity.class);
            intent.putExtra(ScanActivity.EXTRA_TITLE, this.scanActivityTitle);
            intent.putExtra(ScanActivity.EXTRA_IS_SEED, this.isSeedScanner);
            startActivityForResult(intent, SCAN_RESULT);
        }
    }

    /**
     * Set alarm for 2 minutes to clear the clipboard
     */
    protected void setClearClipboardAlarm() {
        // create pending intent
        AlarmManager alarmMgr = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getContext(), ClipboardAlarmReceiver.class);
        intent.setAction("co.nano.nanowallet.alarm");
        PendingIntent alarmIntent = PendingIntent.getBroadcast(getContext(), 0, intent, 0);

        // set a two minute alarm to start the pending intent
        if (alarmMgr != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                alarmMgr.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 120 * 1000, alarmIntent);
            } else {
                alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 120 * 1000, alarmIntent);
            }
        }
    }

    protected void showCreatePinScreen() {
        if (getActivity() instanceof WindowControl) {
            CreatePinDialogFragment dialog = CreatePinDialogFragment.newInstance();
            dialog.show(((WindowControl) getActivity()).getFragmentUtility().getFragmentManager(),
                    CreatePinDialogFragment.TAG);

            // make sure that dialog is not null
            ((WindowControl) getActivity()).getFragmentUtility().getFragmentManager().executePendingTransactions();

            if (dialog.getDialog() != null) {
                dialog.getDialog().setOnDismissListener(dialogInterface -> {
                    KeyboardUtil.hideKeyboard(getActivity());
                });
            }
        }
    }

    protected void showPinScreen(String subtitle) {
        if (getActivity() instanceof WindowControl) {
            PinDialogFragment dialog = PinDialogFragment.newInstance(subtitle);
            dialog.show(((WindowControl) getActivity()).getFragmentUtility().getFragmentManager(),
                    PinDialogFragment.TAG);

            // make sure that dialog is not null
            ((WindowControl) getActivity()).getFragmentUtility().getFragmentManager().executePendingTransactions();

            // reset status bar to blue when dialog is closed
            if (dialog.getDialog() != null) {
                dialog.getDialog().setOnDismissListener(dialogInterface -> setStatusBarBlue());
            }
        }
    }

    /**
     * Create a link from the text on the view
     *
     * @param v    TextView
     * @param text id of text to add to the field
     */
    protected void createLink(TextView v, int text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            v.setText(Html.fromHtml(getString(text), Html.FROM_HTML_MODE_LEGACY));
        } else {
            v.setText(Html.fromHtml(getString(text)));
        }
        v.setTransformationMethod(new LinkTransformationMethod());
        v.setMovementMethod(LinkMovementMethod.getInstance());
    }


    /**
     * Show opt-in alert for analytics
     *
     * @param analyticsService Instance of analytics service
     */
    protected void showAnalyticsOptIn(AnalyticsService analyticsService, Realm realm) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(getContext());
        }

        builder.setTitle(R.string.analytics_optin_alert_title)
                .setMessage(R.string.analytics_optin_alert_message)
                .setPositiveButton(R.string.analytics_optin_alert_confirm_cta, (dialog, which) -> {
                    realm.beginTransaction();
                    Credentials credentials = realm.where(Credentials.class).findFirst();
                    if (credentials != null) {
                        credentials.setHasAgreedToTracking(true);
                        credentials.setHasAnsweredAnalyticsTracking(true);
                    }
                    realm.commitTransaction();
                })
                .setNegativeButton(R.string.analytics_optin_alert_cancel_cta, (dialog, which) -> {
                    realm.beginTransaction();
                    Credentials credentials = realm.where(Credentials.class).findFirst();
                    if (credentials != null) {
                        credentials.setHasAgreedToTracking(false);
                        credentials.setHasAnsweredAnalyticsTracking(true);
                    }
                    realm.commitTransaction();
                    analyticsService.stop();
                })
                .show();
    }
}
