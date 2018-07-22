package co.nano.nanowallet.ui.RFID;

/*
 * The RFID thread can't seem to do Log.w or Timber.d, so this can be passed on to the MainActivity's handler instead where it's then shown
 */
public class RFIDUiDebugMessage
{
    private String tag;
    private String message;

    public RFIDUiDebugMessage(String _tag, String _message)
    {
        tag = _tag;
        message = _message;
    }

    public String getTag() { return tag; }
    public String getMessage() { return message; }
}
