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
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.InputType;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import co.nano.nanowallet.NanoUtil;
import co.nano.nanowallet.R;
import co.nano.nanowallet.analytics.AnalyticsService;
import co.nano.nanowallet.broadcastreceiver.ClipboardAlarmReceiver;
import co.nano.nanowallet.bus.Logout;
import co.nano.nanowallet.bus.RxBus;
import co.nano.nanowallet.bus.SeedCreatedWithAnotherWallet;
import co.nano.nanowallet.model.Credentials;
import co.nano.nanowallet.ui.pin.CreatePinDialogFragment;
import co.nano.nanowallet.ui.pin.PinDialogFragment;
import co.nano.nanowallet.ui.scan.ScanActivity;
import co.nano.nanowallet.ui.send.SendFragment;
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

    protected void showSeedUpdateAlert() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(getContext());
        }

        builder.setTitle(R.string.seed_update_alert_title)
                .setMessage(R.string.seed_update_alert_message)
                .setPositiveButton(R.string.seed_update_alert_confirm_cta, (dialog, which) -> {
                    dialog.dismiss();
                    String newSeed = NanoUtil.generateSeed();
                    String newSeedDisplay = newSeed.replaceAll("(.{4})", "$1 ");
                    String address = NanoUtil.publicToAddress(NanoUtil.privateToPublic(NanoUtil.seedToPrivate(newSeed)));
                    AlertDialog addressDialog = builder.setTitle(R.string.seed_update_address_alert_title)
                            .setMessage(getString(R.string.seed_update_address_alert_message, newSeedDisplay, address))
                            .setPositiveButton(null, null)
                            .setNegativeButton(R.string.seed_update_address_alert_neutral_cta, null)
                            .setCancelable(false)
                            .create();

                    addressDialog.setOnShowListener(dialog1 -> {
                        Button copy = ((AlertDialog) dialog1).getButton(AlertDialog.BUTTON_NEGATIVE);

                        copy.setOnClickListener(view -> {
                            addressDialog.dismiss();

                            // create seed verify alert
                            final TextInputLayout layout = new TextInputLayout(view.getContext());
                            layout.setPadding((int) UIUtil.convertDpToPixel(20f, view.getContext()), 0, (int) UIUtil.convertDpToPixel(20f, view.getContext()), 0);
                            final TextInputEditText input = new TextInputEditText(view.getContext());
                            layout.addView(input);

                            input.setInputType(InputType.TYPE_CLASS_TEXT);
                            input.setSingleLine(false);
                            input.setAllCaps(true);
                            AlertDialog seedVerifyBuilder = new AlertDialog.Builder(getContext())
                                    .setTitle(R.string.seed_update_verify_title)
                                    .setView(layout)
                                    .setPositiveButton(R.string.seed_update_verify_confirm, null)
                                    .setNegativeButton(R.string.seed_update_verify_cancel, (dialog22, which12) -> dialog22.cancel())
                                    .create();

                            seedVerifyBuilder.setOnShowListener(dialog32 -> {
                                Button ok2 = ((AlertDialog) dialog32).getButton(AlertDialog.BUTTON_POSITIVE);
                                ok2.setOnClickListener(view3 -> {
                                            if (input.getText().toString().toLowerCase().equals(newSeed.toLowerCase())) {
                                                if (getActivity() instanceof WindowControl) {
                                                    // navigate to send screen
                                                    dialog32.dismiss();
                                                    ((WindowControl) getActivity()).getFragmentUtility().add(
                                                            SendFragment.newInstance(newSeed),
                                                            FragmentUtility.Animation.ENTER_LEFT_EXIT_RIGHT,
                                                            FragmentUtility.Animation.ENTER_RIGHT_EXIT_LEFT,
                                                            SendFragment.TAG
                                                    );
                                                }
                                            } else {
                                                layout.setError(getString(R.string.seed_update_verify_error));
                                            }
                                        }
                                );

                            });
                            seedVerifyBuilder.show();
                        });
                    });
                    addressDialog.show();
                })
                .setNegativeButton(R.string.seed_update_alert_cancel_cta, (dialog, which) -> {
                    RxBus.get().post(new SeedCreatedWithAnotherWallet());
                })
                .show();
    }

    /**
     * Show opt-in alert for analytics
     */
    protected void showSeedReminderAlert(String seed) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(getContext());
        }
        String address = NanoUtil.publicToAddress(NanoUtil.privateToPublic(NanoUtil.seedToPrivate(seed)));
        String newSeedDisplay = seed.replaceAll("(.{4})", "$1 ");
        AlertDialog reminderDialog = builder.setTitle(R.string.seed_reminder_alert_title)
                .setMessage(getString(R.string.seed_reminder_alert_message, newSeedDisplay, address))
                .setPositiveButton(R.string.seed_reminder_alert_confirm_cta, null)
                .setNegativeButton(R.string.seed_reminder_alert_neutral_cta, null)
                .create();

        reminderDialog.setOnShowListener(dialog1 -> {
            Button copy = ((AlertDialog) dialog1).getButton(AlertDialog.BUTTON_NEGATIVE);
            Button ok = ((AlertDialog) dialog1).getButton(AlertDialog.BUTTON_POSITIVE);

            copy.setOnClickListener(view -> {
                dialog1.dismiss();
            });

            ok.setOnClickListener(view2 -> {
                if (getActivity() instanceof WindowControl) {
                    reminderDialog.dismiss();
                    RxBus.get().post(new Logout());
                }
            });
        });
        reminderDialog.show();
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
                        credentials.setHasAnsweredAnalyticsQuestion(true);
                    }
                    realm.commitTransaction();
                    analyticsService.start();
                })
                .setNegativeButton(R.string.analytics_optin_alert_cancel_cta, (dialog, which) -> {
                    realm.beginTransaction();
                    Credentials credentials = realm.where(Credentials.class).findFirst();
                    if (credentials != null) {
                        credentials.setHasAgreedToTracking(false);
                        credentials.setHasAnsweredAnalyticsQuestion(true);
                    }
                    realm.commitTransaction();
                    analyticsService.stop();
                })
                .show();
    }
}
