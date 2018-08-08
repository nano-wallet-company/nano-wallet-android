package co.nano.nanowallet.ui.RFID;

import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.math.BigInteger;

import javax.inject.Inject;

import co.nano.nanowallet.MainActivity;
import co.nano.nanowallet.NanoUtil;
import co.nano.nanowallet.R;
import co.nano.nanowallet.model.Credentials;
import co.nano.nanowallet.model.RFIDCardService;
import co.nano.nanowallet.model.RFIDInvoiceData;
import co.nano.nanowallet.model.RFIDViewMessage;
import io.realm.Realm;

public class RFIDUiInvoice extends Fragment {
    Button btnPay;
    Button btnCancel;

    MainActivity mainActivity;

    @Inject
    Realm realm;

    private OnFragmentInteractionListener mListener;

    String storeName = null;
    String spendAmountNano = null;
    String localCurr = null;
    String localCurrAmountFormatted = null;
    String exchangeRate = null;

    public RFIDUiInvoice() {
    }

    public static RFIDUiInvoice newInstance(String param1, String param2) {
        RFIDUiInvoice fragment = new RFIDUiInvoice();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /*
     * https://stackoverflow.com/questions/6495898/findviewbyid-in-fragment
     * "You must clean up these stored references [from onViewCreated] by setting them back to null in onDestroyView() or you will leak the Activity."
     * Note: These are actually from onCreateView but I'll be careful and do it anyway
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        btnPay.setOnClickListener(null);
        btnPay = null;
        btnCancel.setOnClickListener(null);
        btnCancel = null;
    }

    public void setMainActivity(MainActivity _mainActivity) {
        mainActivity = _mainActivity;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_rfid_invoice, container, false);

        btnCancel = (Button) fragmentView.findViewById(R.id.rfid_invoice_btn_cancel);
        btnCancel.setEnabled(true);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Message msg = null;
                RFIDViewMessage viewMsg = new RFIDViewMessage(false, RFIDViewMessage.RESETUIID, null, false);
                msg = new Message();
                msg.obj = viewMsg;
                mainActivity.handler.handleMessage(msg);
            }
        });

        btnPay = (Button) fragmentView.findViewById(R.id.rfid_invoice_btn_pay);
        btnPay.setEnabled(true);
        btnPay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                RFIDViewMessage viewMsg = null;
                Message msg = null;
                String[] params = null;

                if(RFIDCardService.invoice.getRawBalanceAfter().compareTo(new BigInteger("0"))>=0) {
                    Credentials credentials = mainActivity.getCredentials();
                    String privateKey = credentials.getPrivateKey();
                    String address = credentials.getAddressString();
                    String balanceFormattedHex = leftPad(radix(RFIDCardService.invoice.getRawBalanceAfter()),32);

                    String stateHash = NanoUtil.computeStateHash(NanoUtil.addressToPublic(address),
                            RFIDCardService.invoice.getPreviousBlock(),
                            NanoUtil.addressToPublic(RFIDCardService.invoice.getAppRepAddress()),
                            balanceFormattedHex,
                            NanoUtil.addressToPublic(RFIDCardService.invoice.getPosAddress()));

                    String signature = NanoUtil.sign(privateKey, stateHash);
                    RFIDCardService.invoice.setSignatureForRFIDPos(NanoUtil.hexStringToByteArray(signature));
                    RFIDCardService.invoice.setPaymentDecisionStatus(RFIDInvoiceData.PAYMENT_DECISION_PAY);

                    params = new String[2];
                    params[0] = "Payment prepared";
                    params[1] = "Move your phone over the reader to pay.";
                    viewMsg = new RFIDViewMessage(false, R.id.fragment_rfid_status, params, false);
                    msg = new Message();
                    msg.obj = viewMsg;
                    mainActivity.handler.handleMessage(msg);
                }
                else {
                    params = new String[2];
                    params[0] = "Insufficient funds";
                    params[1] = "You do not have enough Nano to pay.";
                    viewMsg = new RFIDViewMessage(false,R.id.fragment_rfid_status, params, false);
                    msg = new Message();
                    msg.obj = viewMsg;
                    mainActivity.handler.handleMessage(msg);
                }
            }
        });

        TextView view_storeName_val = (TextView)fragmentView.findViewById(R.id.rfid_invoice_textView_store_name_value);
        TextView view_spendAmountNano_val = (TextView)fragmentView.findViewById(R.id.rfid_invoice_textView_amount_nano_value);
        TextView view_localCurr = (TextView)fragmentView.findViewById(R.id.rfid_invoice_textView_amount_local_currency);
        TextView view_localCurr_val = (TextView)fragmentView.findViewById(R.id.rfid_invoice_textView_amount_local_currency_value);
        TextView view_exchangeRate_val = (TextView)fragmentView.findViewById(R.id.rfid_invoice_textView_exchange_rate_value);

        view_storeName_val.setText(storeName);
        view_spendAmountNano_val.setText(spendAmountNano);
        view_localCurr.setText("Amount "+localCurr);
        view_localCurr_val.setText(localCurrAmountFormatted+" "+localCurr);
        view_exchangeRate_val.setText(exchangeRate+" "+localCurr);

        return fragmentView;
    }

    String radix(BigInteger value) {
        return leftPad(value.toString(16).toUpperCase(), 32);
    }

    String leftPad(String str, int size) {
        if (str.length() >= size) {
            return str;
        }

        StringBuilder builder = new StringBuilder();
        while (str.length() + builder.length() < size) {
            builder.append("0");
        }
        return builder.append(str).toString();
    }

    public void setRFIDInvoiceData(RFIDViewMessage viewMsg) {
        Object[] params = viewMsg.getParams();
        storeName = (String)params[0];
        spendAmountNano = (String)params[1];
        localCurr = (String)params[2];
        localCurrAmountFormatted = (String)params[3];
        exchangeRate = (String)params[4];
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
