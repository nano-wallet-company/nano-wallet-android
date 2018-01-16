package co.nano.nanowallet.ui.receive;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import co.nano.nanowallet.R;
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
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(address, BarcodeFormat.QR_CODE,
                    (int) UIUtil.convertDpToPixel(200, getContext()), (int) UIUtil.convertDpToPixel(200, getContext()));
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            binding.receiveBarcode.setImageBitmap(bmp);
        } catch (WriterException e) {
            Log.e(TAG, e.getMessage());
        }

        setStatusBarWhite(view);

        return view;
    }


    public class ClickHandlers {
        public void onClickClose(View view) {
            dismiss();
        }

        public void onClickCopy(View view) {
            // copy address to clipboard
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("address", address);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
            }

            Snackbar snackbar = Snackbar.make(view, Html.fromHtml(getString(R.string.receive_copy_message)), Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction(R.string.receive_copy_done, view1 -> {
            });
            snackbar.show();
        }
    }
}
