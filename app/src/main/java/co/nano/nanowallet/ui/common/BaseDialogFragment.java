package co.nano.nanowallet.ui.common;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.app.DialogFragment;
import android.view.View;

import co.nano.nanowallet.R;
import co.nano.nanowallet.broadcastreceiver.ClipboardAlarmReceiver;

/**
 * Created by szeidner on 12/01/2018.
 */

public class BaseDialogFragment extends DialogFragment {

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
