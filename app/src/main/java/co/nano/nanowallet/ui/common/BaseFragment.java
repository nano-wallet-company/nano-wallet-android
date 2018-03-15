package co.nano.nanowallet.ui.common;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.View;

import co.nano.nanowallet.R;
import co.nano.nanowallet.broadcastreceiver.ClipboardAlarmReceiver;
import co.nano.nanowallet.ui.scan.ScanActivity;
import co.nano.nanowallet.util.ExceptionHandler;

/**
 * Helper methods used by all fragments
 */

public class BaseFragment extends Fragment {
    private static final int ZXING_CAMERA_PERMISSION = 1;
    protected static final int SCAN_RESULT = 2;

    private String scanActivityTitle;
    private boolean isSeedScanner;

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
}
