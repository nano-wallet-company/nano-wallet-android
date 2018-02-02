package co.nano.nanowallet.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ClipboardAlarmReceiver extends BroadcastReceiver {

    public static final String CLIPBOARD_NAME = "nano";

    @Override
    public void onReceive(Context context, Intent intent) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText(CLIPBOARD_NAME, "");
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
        }
    }
}