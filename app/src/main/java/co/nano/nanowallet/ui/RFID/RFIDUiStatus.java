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

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RFIDUiStatus.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RFIDUiStatus#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RFIDUiStatus extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    Button btn_return;
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

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment rfid_result.
     */
    // TODO: Rename and change types and number of parameters
    public static RFIDUiStatus newInstance(String param1, String param2) {
        RFIDUiStatus fragment = new RFIDUiStatus();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
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
        btn_return.setOnClickListener(null);
        btn_return = null;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View fragmentView = inflater.inflate(R.layout.fragment_rfid_status, container, false);
        TextView view_headline = (TextView)fragmentView.findViewById(R.id.rfid_status_textView_result_headline);
        TextView view_details = (TextView)fragmentView.findViewById(R.id.rfid_status_textView_result_details);

        btn_return = (Button) fragmentView.findViewById(R.id.rfid_status_btn_return);
        btn_return.setEnabled(true);
        btn_return.setOnClickListener(new View.OnClickListener()
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

        view_headline.setText(headlineText);
        view_details.setText(detailsText);

        return fragmentView;
    }

    // TODO: Rename method, update argument and hook method into UI event
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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
