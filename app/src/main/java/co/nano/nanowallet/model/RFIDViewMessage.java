package co.nano.nanowallet.model;

public class RFIDViewMessage
{
    public static final int resetUiID = -122334455;
    private boolean isShowInvoice=false;
    private int nextViewId = -1;
    private Object[] params = null;
    private boolean isCredentialsUpdate=false;

    public RFIDViewMessage(boolean _isShowInvoice, int _nextViewId, Object[] _params, boolean _isCredentialsUpdate)
    {
        isShowInvoice = _isShowInvoice;
        nextViewId = _nextViewId;
        params = _params;
        isCredentialsUpdate = _isCredentialsUpdate;
    }

    public int getNextViewId()
    {
        return nextViewId;
    }

    public Object[] getParams()
    {
        return params;
    }

    public boolean getIsCredentialsUpdate()
    {
        return isCredentialsUpdate;
    }

    public boolean getIsShowInvoice()
    {
        return isShowInvoice;
    }
}
