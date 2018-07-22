package co.nano.nanowallet.model;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import java.util.Arrays;

import co.nano.nanowallet.MainActivity;
import co.nano.nanowallet.NanoUtil;
import co.nano.nanowallet.R;

/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * This is a sample APDU Service which demonstrates how to interface with the card emulation support
 * added in Android 4.4, KitKat.
 *
 * <p>This sample replies to any requests sent with the string "Hello World". In real-world
 * situations, you would need to modify this code to implement your desired communication
 * protocol.
 *
 * <p>This sample will be invoked for any terminals selecting AIDs of 0xF11111111, 0xF22222222, or
 * 0xF33333333. See src/main/res/xml/aid_list.xml for more details.
 *
 * <p class="note">Note: This is a low-level interface. Unlike the NdefMessage many developers
 * are familiar with for implementing Android Beam in apps, card emulation only provides a
 * byte-array based communication channel. It is left to developers to implement higher level
 * protocol support as needed.
 */
public class RFIDCardService extends HostApduService {
    String TAG = "CardService";
    public static RFIDInvoiceData invoice = null;
    static MainActivity mainActivity;

    byte[] invoiceArray = null; // incoming invoice as a byte array, is parsed in RFIDInvoiceData
    int invoiceStartIndex = 3; // index 0, 1 and 2 in the incoming byte array are the header and message length. real invoice data starts at index 3.
    int invoiceArrayCurrentIndex=0; // array position for copying the incoming bytes into the invoiceArray
    int invoiceLength=0; // length of the invoice

    byte[] loginByteArray = new byte[] { (byte)0x00, (byte)0xA4, (byte)0x04, (byte)0x00, (byte)0x05, (byte)0xF2,
            (byte)0xA7, (byte)0x31, (byte)0xD8, (byte)0x7C}; // A731D87C

    public static void setMainActivity(MainActivity _mainAct)
    {
        mainActivity = _mainAct;
    }

    /**
     * Called if the connection to the NFC card is lost, in order to let the application know the
     * cause for the disconnection (either a lost link, or another AID being selected by the
     * reader).
     *
     * @param reason Either DEACTIVATION_LINK_LOSS or DEACTIVATION_DESELECTED
     */
    @Override
    public void onDeactivated(int reason) {
        Log.w("Deactivated", "Reason "+reason);

    }

    /**
     * This method will be called when a command APDU has been received from a remote device. A
     * response APDU can be provided directly by returning a byte-array in this method. In general
     * response APDUs must be sent as quickly as possible, given the fact that the user is likely
     * holding his device over an NFC reader when this method is called.
     *
     * <p class="note">If there are multiple services that have registered for the same AIDs in
     * their meta-data entry, you will only get called if the user has explicitly selected your
     * service, either as a default or just for the next tap.
     *
     * <p class="note">This method is running on the main thread of your application. If you
     * cannot return a response APDU immediately, return null and use the {@link
     * #sendResponseApdu(byte[])} method later.
     *
     * //@param commandApdu The APDU that received from the remote device
     * //@param extras A bundle containing extra data. May be null.
     * @return a byte-array containing the response APDU, or null if no response APDU can be sent
     * at this point.
     */

    /*
     * Incoming bytes via nfc are either file selection to communicate with this app,
     * or a message (invoice, payment status request, etc)
     */
    private boolean isMessage(byte[] inByte)
    {
        if(inByte[1]== (byte)0xCA)
            return true;
        else
            return false;
    }

    /*
     * Drop header bytes of message
     */
    private byte[] getMessageBytes(byte[] inByte)
    {
        byte[] newArray = new byte[inByte.length-5];
        for(int i=5; i<inByte.length; i++)
        {
            newArray[i-5] = inByte[i];
        }
        return newArray;
    }

    public static final byte[] intToByteArray(int value) {
        return new byte[] {
                (byte)(value >> 8),
                (byte)value};
    }

    String privateKey = null;
    String address = null;
    String precomputedWork = null; // This is currently possible but not necessary. If this is passed, the rfid pos doesn't have to do pow.

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras)
    {
        byte[] messageBytes = null;
        byte[] returnBytes = new byte[]{(byte) 0x90, (byte) 0x00}; // 90 00 means command successful in the ISO 14443-4 protocol. Is returned if nothing else is returned or the nfc reader acts funny.

        try {

            if (privateKey == null) {
                RFIDViewMessage viewMsg = new RFIDViewMessage(false, -1, null, true);
                Message msg = new Message();
                msg.obj = viewMsg;
                mainActivity.handler.handleMessage(msg);
                Credentials credentials = mainActivity.getCredentials();
                privateKey = credentials.getPrivateKey();
                address = credentials.getAddressString();
                precomputedWork = null;
            }

            if(privateKey==null || address==null)
                mainActivity.displayRFIDErrorMessage();

            if (isMessage(commandApdu)) {
                // Log.w("Beep", "It's a message!");

                messageBytes = getMessageBytes(commandApdu);
                if ((messageBytes != null && messageBytes.length > 0) == false) {
                    return new byte[]{(byte) 0x90, (byte) 0x00};
                }
            }

            if (messageBytes == null || messageBytes.length == 0 || Arrays.equals(commandApdu, loginByteArray))
                return new byte[]{(byte) 0x90, (byte) 0x00};

            if (messageBytes[0] == RFIDHeaders.INCOMING_INVOICE_START) {

                // Log.w(TAG, "...Spotted invoice start...");
                invoiceLength = ((messageBytes[1] & 0xff) << 8) | (messageBytes[2] & 0xff); // bytes in index 1 and 2 tell how long the incoming invoice is
                // Log.w(TAG, "...invoiceLength = " + invoiceLength);
                invoiceArray = new byte[invoiceLength];

                // Copy inv
                invoiceArrayCurrentIndex = 0;
                int bytesToCopyFromThisArray = 255 - invoiceStartIndex;
                if (invoiceLength < bytesToCopyFromThisArray)
                    bytesToCopyFromThisArray = invoiceLength;
                System.arraycopy(messageBytes, invoiceStartIndex, invoiceArray, 0, bytesToCopyFromThisArray);
                invoiceArrayCurrentIndex += bytesToCopyFromThisArray;

                if (invoiceArray.length == invoiceLength) {
                    // If the invoice bytes have all been read, create the invoice
                    createInvoice();
                }

                // Return the message bytes to the pos for confirmation
                returnBytes = intToByteArray(messageBytes.length);
            }

            if (messageBytes.length == 1 && messageBytes[0] == RFIDHeaders.SIGNED_PACKET_RECEIVED_OK) {
                // The box has successfuly received the signature and tells the app so!

                RFIDViewMessage viewMsg = null;
                Message msg = null;
                String[] params = null;

                params = new String[2];
                params[0] = "Success";
                params[1] = "You have authorized the payment.";
                viewMsg = new RFIDViewMessage(false, R.id.fragment_rfid_status, params, false);
                msg = new Message();
                msg.obj = viewMsg;
                mainActivity.handler.handleMessage(msg);
            }

            if (messageBytes[0] == RFIDHeaders.INCOMING_INVOICE_FOLLOWUP) {
                // Invoice followup.. for invoices that are longer than 252 bytes

                //  Log.w(TAG, "...Spotted invoice followup...");
                int remainingBytes = invoiceArray.length - invoiceArrayCurrentIndex;
                System.arraycopy(messageBytes, 1, invoiceArray, invoiceArrayCurrentIndex, messageBytes.length - 1);

                if (invoiceArray.length == invoiceLength) {
                    // Create invoice if all bytes read
                    createInvoice();
                }
                // Return message bytes length as confirmation
                returnBytes = intToByteArray(messageBytes.length);
            }

            // The following happens at the very end of the transaction after the pos has passed the packet on to the node.
            // It may not happen at all if the customer moves his phone away from the reader quickly after authorizing, but most of the time this happens.
            // It displays the outcome of the transaction to the customer. That's either the new balance, or an error message.
            if (messageBytes[0] == RFIDHeaders.FINAL_RESULT_HEADER) {
                Log.w(TAG, "Final result");
                RFIDViewMessage viewMsg = null;
                Message msg = null;
                String[] params = null;

                // Log.w(TAG, "Got final result");
                byte messageLength = messageBytes[1];
                // Log.w(TAG, "messageLength = " + messageLength);
                if (messageLength == messageBytes.length - 2) {
                    byte[] finalResultBytes = new byte[messageBytes.length - 2];
                    System.arraycopy(messageBytes, 2, finalResultBytes, 0, finalResultBytes.length);
                    String finalResult = new String(finalResultBytes);
                    String[] resultArr = finalResult.split(":");
                    switch (resultArr[0]) {
                        case "b":
                            params = new String[2];
                            params[0] = "New balance";
                            params[1] = turnRAWAmountIntoNANO(resultArr[1])+" Nano";
                            viewMsg = new RFIDViewMessage(false, R.id.fragment_rfid_status, params, false);
                            msg = new Message();
                            msg.obj = viewMsg;
                            mainActivity.handler.handleMessage(msg);
                            break;
                        case "e":
                          //  Log.w(TAG, "Its an error.. " + resultArr[1]);
                            params = new String[2];
                            params[0] = "Error";
                            params[1] = resultArr[1];
                            viewMsg = new RFIDViewMessage(false, R.id.fragment_rfid_status, params, false);
                            msg = new Message();
                            msg.obj = viewMsg;
                            //Log.w(TAG, "Its an error, handler==null = " + (mainActivity.handler == null));
                            mainActivity.handler.handleMessage(msg);
                           // Log.w(TAG, "after handler");
                            break;
                    }
                } else {
                    params = new String[2];
                    params[0] = "No reply";
                    params[1] = "Your payment was likely accepted anyway.";
                    viewMsg = new RFIDViewMessage(false, R.id.fragment_rfid_status, params, false);
                    msg = new Message();
                    msg.obj = viewMsg;
                    mainActivity.handler.handleMessage(msg);
                }
            }


            if (messageBytes[0] == RFIDHeaders.SIGNED_PACKET_REQUEST) // is a request for the user's decision where to pay or not
            {
                //  Log.w(TAG, "signed packet request 1");
                if (invoice != null) {
                    // Log.w(TAG, "signed packet request 2");
                    if (invoice.getPaymentDecisionStatus() == RFIDInvoiceData.PAYMENT_DECISION_PAY) {
                        //  Log.w(TAG, "signed packet request 3");
                        byte[] signatureForPos = invoice.getSignatureForRFIDPos();
                        if (signatureForPos != null) {
                            //   Log.w(TAG, "signed packet request 4");
                            returnBytes = signatureForPos;
                        }
                    }
                }
            }

            if (messageBytes[0] == RFIDHeaders.ACCOUNT_REQUEST) // is a request for the user's account so the packet can be crafted on the pos
            {

                //Log.w(TAG, "Account request");
                // Beginning of protocol, reset invoice
                invoice = null;

                // The address is turned into a pubkey, then into a byte array to save bytes
                //Log.w(TAG, "Address = " + address);
                byte[] pubKey = NanoUtil.hexStringToByteArray(NanoUtil.addressToPublic(address));
                byte[] preCompWorkBytes = null;

                // return bytes is 1 byte header + pubkey
                int arrayLength = 1 + pubKey.length;

                // If there is precomputed work, add that to our response
                if (precomputedWork != null && precomputedWork.length() == 16) {
                    arrayLength += 8;
                    preCompWorkBytes = NanoUtil.hexToBytes(precomputedWork);
                }

                returnBytes = new byte[arrayLength];
                returnBytes[0] = RFIDHeaders.ACCOUNT_REQUEST;
                System.arraycopy(pubKey, 0, returnBytes, 1, pubKey.length);

                if (preCompWorkBytes != null) {
                    System.arraycopy(preCompWorkBytes, 0, returnBytes, 1 + pubKey.length, preCompWorkBytes.length);
                }
            }
        }
        catch(Exception ex)
        {
            Log.w(TAG, "Exception", ex);
        }

        if(returnBytes==null)
            returnBytes = new byte[] { (byte)0x90, (byte)0x00 };
        return returnBytes;
    }

    public String turnRAWAmountIntoNANO(String amount)
    {
        if (amount.length() < 30)
        {
            while (amount.length() < 30)
            {
                amount = "0" + amount;
            }
            amount = "0." + amount;
            return amount;
        }
        else
        {
            if(amount.length()==30)
            {
                amount = "0." + amount;
                return amount;
            }
            else if(amount.length()>30)
            {
                int startIndex = amount.length()-30;
                amount = amount.substring(0, startIndex) + "." + amount.substring(startIndex);
                return amount;
            }
        }
        return "Error converting";
    }

    void createInvoice()
    {
        invoice = new RFIDInvoiceData(invoiceArray, mainActivity);
        double amount_local_curr_rounded = Math.round(invoice.getLocalCurrencyAmount()*100.0)/100.0;
        String localCurrAmountFormatted = String.format("%.2f", amount_local_curr_rounded).replace(',', '.');

        Log.w(TAG, "Create RFID Invoice");
        Object[] params = new Object[5];
        params[0] = invoice.getStoreName();
        params[1] = invoice.getSpendAmountNANO();
        params[2] = invoice.getLocalCurrency();
        params[3] = localCurrAmountFormatted;
        params[4] = String.valueOf(invoice.getExchangeRate());

        // Log.w(TAG, "localCurrAmountFormatted = " + localCurrAmountFormatted);
        // Log.w(TAG, "invoice.exchangeRate = " + invoice.getExchangeRate());
        // Log.w(TAG, "invoice.spendAmountNANO = " + invoice.getSpendAmountNANO());

        // show rfid invoice fragment
        RFIDViewMessage viewMsg = new RFIDViewMessage(true, R.id.fragment_rfid_invoice, params, false);
        Message msg = new Message();
        msg.obj = viewMsg;
        mainActivity.handler.handleMessage(msg);
    }
}

