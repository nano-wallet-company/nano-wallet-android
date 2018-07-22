package co.nano.nanowallet.ui.RFID;

import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
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
import co.nano.nanowallet.model.RFIDHeaders;
import co.nano.nanowallet.model.RFIDInvoiceData;
import co.nano.nanowallet.model.RFIDViewMessage;
import io.realm.Realm;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RFIDUiInvoice.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RFIDUiInvoice#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RFIDUiInvoice extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    Button btn_pay;
    Button btn_cancel;

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
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment rfid_new_invoice.
     */
    // TODO: Rename and change types and number of parameters
    public static RFIDUiInvoice newInstance(String param1, String param2) {
        RFIDUiInvoice fragment = new RFIDUiInvoice();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    /*
     * https://stackoverflow.com/questions/6495898/findviewbyid-in-fragment
     * "You must clean up these stored references [from onViewCreated] by setting them back to null in onDestroyView() or you will leak the Activity."
     * Note: These are actually from onCreateView but I'll be careful and do it anyway
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        btn_pay.setOnClickListener(null);
        btn_pay = null;
        btn_cancel.setOnClickListener(null);
        btn_cancel = null;
    }
    public void setMainActivity(MainActivity _mainActivity)
    {
        mainActivity = _mainActivity;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_rfid_invoice, container, false);

        btn_cancel = (Button) fragmentView.findViewById(R.id.rfid_invoice_btn_cancel);
        btn_cancel.setEnabled(true);
        btn_cancel.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Message msg = null;
                RFIDViewMessage viewMsg = new RFIDViewMessage(false, RFIDViewMessage.resetUiID, null, false);
                msg = new Message();
                msg.obj = viewMsg;
                mainActivity.handler.handleMessage(msg);
            }
        });

        btn_pay = (Button) fragmentView.findViewById(R.id.rfid_invoice_btn_pay);
        btn_pay.setEnabled(true);
        btn_pay.setOnClickListener(new View.OnClickListener()
        {
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
                else
                {
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

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    public void setRFIDInvoiceData(RFIDViewMessage viewMsg)
    {
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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
