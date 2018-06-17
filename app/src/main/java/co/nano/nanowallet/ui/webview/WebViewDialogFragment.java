package co.nano.nanowallet.ui.webview;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import co.nano.nanowallet.R;
import co.nano.nanowallet.databinding.FragmentWebviewBinding;
import co.nano.nanowallet.ui.common.BaseDialogFragment;
import co.nano.nanowallet.ui.common.KeyboardUtil;

/**
 * Webview
 */
public class WebViewDialogFragment extends BaseDialogFragment {
    public static final String TAG = WebViewDialogFragment.class.getSimpleName();

    private String mUrl;
    private String mTitle;

    private FragmentWebviewBinding binding;

    private static final String ARG_URL = "argUrl";
    private static final String ARG_TITLE = "argTitle";

    private final WebChromeClient mWebChromeClient = new WebChromeClient() {
        public void onProgressChanged(WebView view, int progress) {
            if (progress < 100 && binding.dialogAppBarProgress.getVisibility() == ProgressBar.GONE) {
                binding.dialogAppBarProgress.setVisibility(ProgressBar.VISIBLE);
                binding.dialogAppBarProgress.setIndeterminate(false);
            }

            binding.dialogAppBarProgress.setProgress(progress);
            if (progress == 100) {
                binding.dialogAppBarProgress.setVisibility(ProgressBar.GONE);
                binding.webviewSwiperefresh.setRefreshing(false);
            }
        }
    };

    private SwipeRefreshLayout.OnRefreshListener mSwipeRefreshListener = () -> binding.webviewWebview.reload();

    // Required empty public constructor
    public WebViewDialogFragment() {
    }

    public static WebViewDialogFragment newInstance(String url, String title) {
        Bundle args = new Bundle();
        args.putString(ARG_URL, url);
        args.putString(ARG_TITLE, title);
        WebViewDialogFragment fragment = new WebViewDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mUrl = getArguments().getString(ARG_URL);
            mTitle = getArguments().getString(ARG_TITLE);
        }

        setStyle(STYLE_NO_FRAME, R.style.AppTheme_Modal_Window);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_webview, container, false);
        view = binding.getRoot();

        setStatusBarWhite(view);

        binding.webviewSwiperefresh.setOnRefreshListener(mSwipeRefreshListener);

        // set the listener for Navigation
        if (binding.dialogAppbar != null) {
            final WebViewDialogFragment window = this;
            binding.dialogAppbar.setTitle(mTitle);
            binding.dialogAppbar.setNavigationOnClickListener(v1 -> {
                KeyboardUtil.hideKeyboard(getActivity());
                window.dismiss();
            });

            binding.dialogAppBarProgress.setIndeterminate(true);
        }

        binding.webviewWebview.setWebViewClient(new WebViewClient() {});
        binding.webviewWebview.setWebChromeClient(mWebChromeClient);
        binding.webviewWebview.setInitialScale(1);
        binding.webviewWebview.getSettings().setDomStorageEnabled(true);
        binding.webviewWebview.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        binding.webviewWebview.getSettings().setLoadWithOverviewMode(true);
        binding.webviewWebview.getSettings().setUseWideViewPort(true);

        binding.webviewWebview.loadUrl(mUrl);

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.webviewWebview.onPause();
        binding.webviewWebview.pauseTimers();
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.webviewWebview.resumeTimers();
        binding.webviewWebview.onResume();
    }


    @Override
    public void onDestroy() {
        binding.webviewWebview.destroy();
        super.onDestroy();
    }
}
