package co.nano.nanowallet.ui.receive;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.sumimakito.awesomeqr.AwesomeQRCode;

import co.nano.nanowallet.R;
import co.nano.nanowallet.alarm.ClipboardAlarmReceiver;
import co.nano.nanowallet.databinding.FragmentReceiveBinding;
import co.nano.nanowallet.ui.common.BaseDialogFragment;
import co.nano.nanowallet.ui.common.UIUtil;

/**
 * Settings main screen
 */
public class ReceiveDialogFragment extends BaseDialogFragment {
    private FragmentReceiveBinding binding;
    public static String TAG = ReceiveDialogFragment.class.getSimpleName();
    private String address;
    private static final int QRCODE_SIZE = 200;

    /**
     * Create new instance of the dialog fragment (handy pattern if any data needs to be passed to it)
     *
     * @return
     */
    public static ReceiveDialogFragment newInstance() {
        Bundle args = new Bundle();
        ReceiveDialogFragment fragment = new ReceiveDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, R.style.AppTheme_Modal_Window);

        // TODO: The receive address should be passed in or generated somewhere
        address = "xrb_3gntuoguehi9d1mnhnar6ojx7jseeerwj5hesb4b4jga7oybbdbqyzap7ijg";
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // inflate the view
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_receive, container, false);
        View view = binding.getRoot();
        binding.setHandlers(new ClickHandlers());

        // colorize address text
        binding.receiveAddress.setText(UIUtil.getColorizedSpannable(address, getContext()));

        // generate QR code
        new AwesomeQRCode.Renderer()
                .contents(address)
                .size((int) UIUtil.convertDpToPixel(240, getContext()))
                .margin((int) UIUtil.convertDpToPixel(20, getContext()))
                .dotScale(0.55f)
                .background(BitmapFactory.decodeResource(getResources(), R.drawable.qrbackground))
                .renderAsync(new AwesomeQRCode.Callback() {
                    @Override
                    public void onRendered(AwesomeQRCode.Renderer renderer, final Bitmap bitmap) {
                        getActivity().runOnUiThread(() -> binding.receiveBarcode.setImageBitmap(bitmap));
                    }

                    @Override
                    public void onError(AwesomeQRCode.Renderer renderer, Exception e) {
                        e.printStackTrace();
                    }
                });

        setStatusBarWhite(view);

        return view;
    }


    public class ClickHandlers {
        public void onClickClose(View view) {
            dismiss();
        }

        public void onClickShare(View view) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, address);
            shareIntent.putExtra(Intent.EXTRA_STREAM, UIUtil.viewToByteArray(binding.receiveBarcode));
            shareIntent.setType("image/png");
            startActivity(Intent.createChooser(shareIntent, getString(R.string.receive_share_title)));
        }

        public void onClickCopy(View view) {
            // copy address to clipboard
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText(ClipboardAlarmReceiver.CLIPBOARD_NAME, address);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
            }

            Snackbar snackbar = Snackbar.make(view, Html.fromHtml(getString(R.string.receive_copy_message)), Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(R.string.receive_copy_done, view1 -> {
            });
            snackbar.show();

            setClearClipboardAlarm();
        }
    }
}
