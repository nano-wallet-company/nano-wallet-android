package co.nano.nanowallet.ui.webview;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import co.nano.nanowallet.R;
import co.nano.nanowallet.bus.AcceptAgreement;
import co.nano.nanowallet.bus.RxBus;
import co.nano.nanowallet.databinding.FragmentWebviewAgreementBinding;
import co.nano.nanowallet.ui.common.BaseDialogFragment;
import co.nano.nanowallet.ui.common.KeyboardUtil;

/**
 * Webview
 */
public class WebViewAgreementDialogFragment extends BaseDialogFragment {
    public static final String TAG = WebViewAgreementDialogFragment.class.getSimpleName();

    private String mUrl;
    private String mTitle;
    private String mId;

    private FragmentWebviewAgreementBinding binding;

    private static final String ARG_URL = "argUrl";
    private static final String ARG_TITLE = "argTitle";
    private static final String ARG_ID = "argID";

    private final WebChromeClient mWebChromeClient = new WebChromeClient() {
        public void onProgressChanged(WebView view, int progress) {
            if (progress < 100 && binding.dialogAppBarProgress.getVisibility() == ProgressBar.GONE) {
                binding.dialogAppBarProgress.setVisibility(ProgressBar.VISIBLE);
                binding.dialogAppBarProgress.setIndeterminate(false);
            }

            binding.dialogAppBarProgress.setProgress(progress);
            if (progress == 100) {
                binding.dialogAppBarProgress.setVisibility(ProgressBar.GONE);
                binding.webviewAgreementSwiperefresh.setRefreshing(false);
            }
        }
    };

    private SwipeRefreshLayout.OnRefreshListener mSwipeRefreshListener = () -> binding.webviewAgreementWebview.reload();

    // Required empty public constructor
    public WebViewAgreementDialogFragment() {
    }

    public static WebViewAgreementDialogFragment newInstance(String url, String title, String id) {
        Bundle args = new Bundle();
        args.putString(ARG_URL, url);
        args.putString(ARG_TITLE, title);
        args.putString(ARG_ID, id);
        WebViewAgreementDialogFragment fragment = new WebViewAgreementDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mUrl = getArguments().getString(ARG_URL);
            mTitle = getArguments().getString(ARG_TITLE);
            mId = getArguments().getString(ARG_ID);
        }

        setStyle(STYLE_NO_FRAME, R.style.AppTheme_Modal_Window);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_webview_agreement, container, false);
        view = binding.getRoot();

        setStatusBarWhite(view);

        binding.webviewAgreementSwiperefresh.setOnRefreshListener(mSwipeRefreshListener);
        binding.setHandlers(new ClickHandlers());

        // set the listener for Navigation
        if (binding.dialogAppbar != null) {
            final WebViewAgreementDialogFragment window = this;
            binding.dialogAppbar.setTitle(mTitle);
            binding.dialogAppbar.setNavigationOnClickListener(v1 -> {
                KeyboardUtil.hideKeyboard(getActivity());
                window.dismiss();
            });

            binding.dialogAppBarProgress.setIndeterminate(true);
        }

        binding.webviewAgreementWebview.setWebViewClient(new WebViewClient() {});
        binding.webviewAgreementWebview.setWebChromeClient(mWebChromeClient);
        binding.webviewAgreementWebview.setInitialScale(1);
        binding.webviewAgreementWebview.getSettings().setDomStorageEnabled(true);
        binding.webviewAgreementWebview.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        binding.webviewAgreementWebview.getSettings().setLoadWithOverviewMode(true);
        binding.webviewAgreementWebview.getSettings().setUseWideViewPort(true);

        binding.webviewAgreementWebview.loadUrl(mUrl);

        binding.webviewAgreementWebview.setOnScrollChangedCallback((l, t, oldl, oldt) -> {
            int height = (int) Math.floor(binding.webviewAgreementWebview.getContentHeight() * binding.webviewAgreementWebview.getScale());
            int webViewHeight = binding.webviewAgreementWebview.getMeasuredHeight();
            if(binding.webviewAgreementWebview.getScrollY() + webViewHeight >= (height - 10)){
                binding.webviewAgreementAcceptButton.setEnabled(true);
            }
        });

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.webviewAgreementWebview.onPause();
        binding.webviewAgreementWebview.pauseTimers();
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.webviewAgreementWebview.resumeTimers();
        binding.webviewAgreementWebview.onResume();
    }


    @Override
    public void onDestroy() {
        binding.webviewAgreementWebview.destroy();
        super.onDestroy();
    }

    public class ClickHandlers {
        public void onClickAccept(View view) {
            RxBus.get().post(new AcceptAgreement(mId));
            KeyboardUtil.hideKeyboard(getActivity());
            dismiss();
        }
    }
}
