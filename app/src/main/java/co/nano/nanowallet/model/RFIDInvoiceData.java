package co.nano.nanowallet.model;

import android.util.Log;

import java.math.BigDecimal;
import java.math.BigInteger;

import co.nano.nanowallet.MainActivity;
import co.nano.nanowallet.NanoUtil;

public class RFIDInvoiceData
{
    public static final byte PAYMENT_DECISION_PAY             = 0x05;
    public static final byte PAYMENT_DECISION_UNDECIDED       = 0x12;

    private byte paymentDecisionStatus=PAYMENT_DECISION_UNDECIDED;

    // All received from the rfid pos or calculated
    private String storeName=null;
    private String localCurr=null;
    private double localCurrAmount=0;
    private double exchangeRate=0;
    private String posAddress=null;
    private BigInteger rawBalanceBefore=null;
    private BigInteger rawBalanceAfter=null;
    private String spendAmountNANO=null;
    private String previousBlock=null;
    private String appRepAddress =null;

    // This is sent when the customer decides to pay the invoice
    private byte[] signatureForRFIDPos = null;

    public byte getPaymentDecisionStatus() { return paymentDecisionStatus; }
    public String getStoreName() { return storeName; }
    public String getLocalCurrency() { return localCurr; }
    public double getLocalCurrencyAmount() { return localCurrAmount; }
    public double getExchangeRate() { return exchangeRate; }
    public String getPosAddress() { return posAddress; }
    public BigInteger getRawBalanceBefore() { return rawBalanceBefore; }
    public BigInteger getRawBalanceAfter() { return rawBalanceAfter; }
    public String getSpendAmountNANO() { return spendAmountNANO; }
    public String getPreviousBlock() { return previousBlock; }
    public String getAppRepAddress() { return appRepAddress; }
    public byte[] getSignatureForRFIDPos() { return signatureForRFIDPos; }
    public void setSignatureForRFIDPos(byte[] sign) { signatureForRFIDPos = sign; }
    public void setPaymentDecisionStatus(byte status) { paymentDecisionStatus = status; }

    public RFIDInvoiceData(byte[] invoiceData, MainActivity mainActivity)
    {
        try {
            int startRawBalance = ((invoiceData[0] & 0xff) << 8) | (invoiceData[1] & 0xff);
            int startSendAmount = ((invoiceData[2] & 0xff) << 8) | (invoiceData[3] & 0xff);
            int startShopName = ((invoiceData[4] & 0xff) << 8) | (invoiceData[5] & 0xff);
            int startLocalCurr = ((invoiceData[6] & 0xff) << 8) | (invoiceData[7] & 0xff);
            int startExchangeRate = ((invoiceData[8] & 0xff) << 8) | (invoiceData[9] & 0xff);

        /*Log.w("+++startRawBalance", ""+startRawBalance);
        Log.w("+++startSendAmount", ""+startSendAmount);
        Log.w("+++startShopName", ""+startShopName);
        Log.w("+++startLocalCurr", ""+startLocalCurr);
        Log.w("+++startExchangeRate", ""+startExchangeRate);*/

            byte[] pubKeyBoxByteConverted = new byte[32];
            System.arraycopy(invoiceData, 10, pubKeyBoxByteConverted, 0, 32);
            posAddress = NanoUtil.publicToAddress(NanoUtil.bytesToHex(pubKeyBoxByteConverted));
            //Log.w("++boxAddress++", boxAddress);

            byte[] previousBlockByteConverted = new byte[32];
            System.arraycopy(invoiceData, 42, previousBlockByteConverted, 0, 32);
            previousBlock = NanoUtil.bytesToHex(previousBlockByteConverted);
            //Log.w("++previousBlock++", previousBlock);

            byte[] appRepAddressByteConverted = new byte[32];
            System.arraycopy(invoiceData, 74, appRepAddressByteConverted, 0, 32);
            appRepAddress = NanoUtil.publicToAddress(NanoUtil.bytesToHex(appRepAddressByteConverted));
            //Log.w("++appRepAddress++", appRepAddress);

            int rawBalanceLength = startSendAmount - startRawBalance;
            int sendAmountLength = startShopName - startSendAmount;
            int shopNameLength = startLocalCurr - startShopName;
            int localCurrLength = startExchangeRate - startLocalCurr;
            int exchangeRateLength = invoiceData.length - startExchangeRate;

            byte[] storeNameBytes = new byte[shopNameLength];
            System.arraycopy(invoiceData, startShopName, storeNameBytes, 0, shopNameLength);
            storeName = new String(storeNameBytes);
            //Log.w("++storeName++", storeName);

            byte[] localCurrBytes = new byte[localCurrLength];
            System.arraycopy(invoiceData, startLocalCurr, localCurrBytes, 0, localCurrLength);
            localCurr = new String(localCurrBytes);
            //Log.w("++localCurr++", localCurr);

            byte[] initialRawBalanceBytes = new byte[rawBalanceLength];
            System.arraycopy(invoiceData, startRawBalance, initialRawBalanceBytes, 0, rawBalanceLength);

            String rawBalanceTempString = NanoUtil.bytesToHex(initialRawBalanceBytes);
            while (rawBalanceTempString.startsWith("0")) {
                rawBalanceTempString = rawBalanceTempString.substring(1);
            }
            rawBalanceBefore = new BigInteger(rawBalanceTempString);
            // Log.w("++rawBalanceBefore++", "" + rawBalanceBefore);
            byte[] sendAmountBytes = new byte[sendAmountLength];
            System.arraycopy(invoiceData, startSendAmount, sendAmountBytes, 0, sendAmountLength);

            spendAmountNANO = new String(sendAmountBytes, "ASCII");
            // Log.w(spendAmountNANO "+spendAmountNANO);


            byte[] exchangeRateBytes = new byte[exchangeRateLength];
            System.arraycopy(invoiceData, startExchangeRate, exchangeRateBytes, 0, exchangeRateLength);

            exchangeRate = Double.parseDouble(new String(exchangeRateBytes, "ASCII"));
            //Log.w("++exchangeRate++", String.valueOf(exchangeRate));

            localCurrAmount = Double.parseDouble((new BigDecimal(spendAmountNANO).multiply(new BigDecimal(exchangeRate))).toString());

            BigInteger rawSpendAmount = new BigInteger(NanoUtil.turnNANOAmountIntoRAW(String.valueOf(spendAmountNANO)));
            BigInteger oldAmount = rawBalanceBefore;
            rawBalanceAfter = oldAmount.subtract(rawSpendAmount);
        }
        catch(Exception ex)
        {
            Log.w("RFIDInvoiceData", "Exception", ex);
            mainActivity.displayRFIDErrorMessage();
        }
    }
}
