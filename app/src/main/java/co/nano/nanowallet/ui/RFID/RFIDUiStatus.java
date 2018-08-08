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

import co.nano.nanowallet.MainActivity;
import co.nano.nanowallet.R;
import co.nano.nanowallet.model.RFIDViewMessage;

public class RFIDUiStatus extends Fragment {

    Button btnReturn;
    MainActivity mainActivity;

    String headlineText = null;
    String detailsText = null;

    public void setRFIDStatusData(RFIDViewMessage viewMsg)
    {
        Object[] params = viewMsg.getParams();
        headlineText = (String)params[0];
        detailsText = (String)params[1];
    }

    public void setMainActivity(MainActivity _mainActivity)
    {
        mainActivity = _mainActivity;
    }

    private OnFragmentInteractionListener mListener;

    public RFIDUiStatus() {
        // Required empty public constructor
    }

    public static RFIDUiStatus newInstance(String param1, String param2) {
        RFIDUiStatus fragment = new RFIDUiStatus();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /*
     * https://stackoverflow.com/questions/6495898/findviewbyid-in-fragment
     * "You must clean up these stored references [from onViewCreated] by setting them back to null in onDestroyView() or you will leak the Activity."
     * Note: These are actually from onCreateView but I'll be careful and do it anyway
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        btnReturn.setOnClickListener(null);
        btnReturn = null;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_rfid_status, container, false);
        TextView view_headline = (TextView)fragmentView.findViewById(R.id.rfid_status_textView_result_headline);
        TextView view_details = (TextView)fragmentView.findViewById(R.id.rfid_status_textView_result_details);

        btnReturn = (Button) fragmentView.findViewById(R.id.rfid_status_btn_return);
        btnReturn.setEnabled(true);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Message msg = null;
                RFIDViewMessage viewMsg = new RFIDViewMessage(false, RFIDViewMessage.RESETUIID, null, false);
                msg = new Message();
                msg.obj = viewMsg;
                mainActivity.handler.handleMessage(msg);
            }
        });

        view_headline.setText(headlineText);
        view_details.setText(detailsText);
        return fragmentView;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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
