package co.nano.nanowallet.ui.common;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;

import co.nano.nanowallet.R;

/**
 * UI Utility Functions
 */

public class UIUtil {
    /**
     * Colorize a string in the following manner:
     * First 9 characters are blue
     * Last 5 characters are orange
     *
     * @param s       Spannable
     * @param context Context
     */
    public static void colorizeSpannable(Spannable s, Context context) {
        if (context == null) {
            return;
        }
        if (s.length() > 0) {
            s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.dark_sky_blue)), 0, s.length() > 8 ? 9 : s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (s.length() > 59) {
                s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.burnt_yellow)), 59, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    /**
     * Colorize a string in the following manner:
     * First 9 characters are blue
     * Last 5 characters are orange
     *
     * @param s       Spannable
     * @param context Context
     */
    public static void colorizeSeed(Spannable s, Context context) {
        if (context == null) {
            return;
        }
        s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.bright_white)), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.dark_sky_blue)), 0, s.length() > 4 ? 5 : s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (s.length() > 59) {
            s.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.burnt_yellow)), 59, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    /**
     * Colorize a string in the following manner:
     * First 9 characters are blue
     * Last 5 characters are orange
     *
     * @param s       String
     * @param context Context
     * @return Spannable string
     */
    public static Spannable getColorizedSpannable(String s, Context context) {
        Spannable sp = new SpannableString(s);
        colorizeSpannable(sp, context);
        return sp;
    }


    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px      A value in px (pixels) unit. Which we need to convert into dp
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}
