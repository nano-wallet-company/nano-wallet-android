package co.nano.nanowallet.ui.common;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;

import co.nano.nanowallet.R;
import co.nano.nanowallet.broadcastreceiver.ClipboardAlarmReceiver;
import co.nano.nanowallet.util.ExceptionHandler;

/**
 * Base class for dialog fragments
 */

public class BaseDialogFragment extends DialogFragment {

    @Override
    public void show(FragmentManager manager, String tag) {

        try {
            FragmentTransaction ft = manager.beginTransaction();
            ft.add(this, tag);
            ft.commit();
        } catch (IllegalStateException e) {
            ExceptionHandler.handle(e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        getDialog().setOnKeyListener((dialog, keyCode, event) -> {

            if ((keyCode == KeyEvent.KEYCODE_BACK)) {
                //This is the filter
                if (event.getAction() != KeyEvent.ACTION_DOWN)
                    return true;
                else {
                    dismiss();
                    return true; // pretend we've processed it
                }
            } else
                return false; // pass on to be processed as normal
        });
    }

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
