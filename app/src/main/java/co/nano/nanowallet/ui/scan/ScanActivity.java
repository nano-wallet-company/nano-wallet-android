package co.nano.nanowallet.ui.scan;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.Result;

import co.nano.nanowallet.R;
import co.nano.nanowallet.model.Credentials;
import me.dm7.barcodescanner.core.IViewFinder;
import me.dm7.barcodescanner.zxing.ZXingScannerView;


public class ScanActivity extends BaseScannerActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView mScannerView;
    public static final String QR_CODE_RESULT = "QRCodeResult";
    public static final String EXTRA_TITLE = "ScanActivityTitle";
    public static final String EXTRA_IS_SEED = "ScanActivityIsSeed";
    
    private boolean isSeedOnlyScan = false;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_scan);
        setupToolbar();

        ViewGroup contentFrame = findViewById(R.id.scan_content_frame);
        mScannerView = new ZXingScannerView(this) {
            @Override
            protected IViewFinder createViewFinderView(Context context) {
                return new NanoViewFinderView(context);
            }
        };
        contentFrame.addView(mScannerView);

        // get title
        String title = getIntent().getStringExtra(EXTRA_TITLE);
        if (title != null) {
            TextView instructions = findViewById(R.id.scan_instruction_label);
            if (instructions != null) {
                instructions.setText(title);
            }
        }
        
        // get seed only scan setting
        isSeedOnlyScan = getIntent().getBooleanExtra(EXTRA_IS_SEED, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        Bundle conData = new Bundle();
        if (!isSeedOnlyScan || Credentials.isValidSeed(rawResult.getText())) {
            conData.putString(QR_CODE_RESULT, isSeedOnlyScan ? rawResult.getText().toUpperCase() : rawResult.getText());
            Intent intent = new Intent();
            intent.putExtras(conData);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            Toast.makeText(this, R.string.scan_seed_error, Toast.LENGTH_SHORT).show();
            Handler handler = new Handler();
            handler.postDelayed(() -> mScannerView.resumeCameraPreview(ScanActivity.this), 2000);
        }
    }
}
