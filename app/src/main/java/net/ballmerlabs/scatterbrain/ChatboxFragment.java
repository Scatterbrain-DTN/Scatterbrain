package net.ballmerlabs.scatterbrain;

import android.app.Activity;
import android.bluetooth.BluetoothClass;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import net.ballmerlabs.scatterbrain.network.BLE.BlockDataPacket;
import net.ballmerlabs.scatterbrain.network.DeviceProfile;
import net.ballmerlabs.scatterbrain.network.GlobalNet;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChatboxFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChatboxFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatboxFragment extends Fragment {
    private EditText MsgBox;
    private ListView messageTimeline;
    private ArrayAdapter<String> Messages;
    private Button sendButton;
    private GlobalNet globnet;
    private DeviceProfile profile;
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    public static ChatboxFragment newInstance(int sectionNumber) {
        ChatboxFragment fragment = new ChatboxFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public ChatboxFragment() {
        // Required empty public constructor
    }

    /* copies over network object from main activity */
    public void registerNetwork(GlobalNet globnet) {
        this.globnet = globnet;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);


        messageTimeline = (ListView) rootView.findViewById(R.id.timeline);

        MsgBox = (EditText) rootView.findViewById(R.id.editText);
        Messages = new ArrayAdapter<String>(this.getActivity(),android.R.layout.simple_list_item_1);
        messageTimeline.setAdapter(Messages);



        //messagebox handeling
        sendButton = (Button) rootView.findViewById(R.id.send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateList();
            }
        });


        return rootView;
    }

    public void setDeviceProfile(DeviceProfile me) {
        this.profile = me;
    }

    //adds a message to the list and clears the input field
    private void updateList() {
        Messages.add(MsgBox.getText().toString());
        BlockDataPacket bd = new BlockDataPacket(MsgBox.getText().toString().getBytes(), true,profile);
        globnet.sendBlePacket(bd);
        MsgBox.setText("");

    }

    public void addMessage(String message) {
        Messages.add(message);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
      //  ((MainActivity) activity).onSectionAttached(
        //        getArguments().getInt(ARG_SECTION_NUMBER));
    }



    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
