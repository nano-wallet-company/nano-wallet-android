package co.nano.nanowallet.bus;

/**
 * Show a webview
 */

public class OpenWebView {
    private String url;
    private String title;

    public OpenWebView(String url) {
        this.url = url;
    }

    public OpenWebView(String url, String title) {
        this.url = url;
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
