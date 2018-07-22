package co.nano.nanowallet.model;

public class  RFIDHeaders
{
    public static final byte INCOMING_INVOICE_START           = 0x01;
    public static final byte INCOMING_INVOICE_FOLLOWUP        = 0x02;
    public static final byte SIGNED_PACKET_REQUEST            = 0x03;
    public static final byte ACCOUNT_REQUEST                  = 0x04;
    public static final byte FINAL_RESULT_HEADER              = 0x06;
    public static final byte SIGNED_PACKET_RECEIVED_OK        = 0x13;
}
