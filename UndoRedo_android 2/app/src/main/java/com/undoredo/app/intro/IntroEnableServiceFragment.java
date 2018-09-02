package com.undoredo.app.intro;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.undoredo.app.R;

/**
 * A simple that display a button to Authorize the App accessibility
 * in the Android Settings.
 *
 * Activities that contain this fragment must implement the
 * {@link IntroEnableServiceFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link IntroEnableServiceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class IntroEnableServiceFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private View rootView;
    private Button gotoSettingsButton;

    public IntroEnableServiceFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment IntroEnableServiceFragment.
     */
    public static IntroEnableServiceFragment newInstance() {
        IntroEnableServiceFragment fragment = new IntroEnableServiceFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_intro_enable, container, false);
        gotoSettingsButton = rootView.findViewById(R.id.gotoSettingsButton);
        gotoSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onSettingsButonClick();
            }
        });
        return rootView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
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
        void onSettingsButonClick();
    }
}
