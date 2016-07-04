package com.conestogac.assignment2;


import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;


/**
 * Get a distanceSetting and save as a sharedpreference
 */
public class SetDistanceFragment extends DialogFragment {


    public SetDistanceFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE,android.R.style.Theme_Holo_Light_Dialog);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_set_distance, container);
        getDialog().setTitle("Set Distance");
        Button dismiss = (Button) rootView.findViewById(R.id.dismiss);
        final EditText edDistance = (EditText) rootView.findViewById(R.id.edDistance);
        edDistance.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dismiss.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Todo save at shared preference
                SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt(getString(R.string.saved_distance), Integer.valueOf(edDistance.getText().toString()));
                editor.commit();
                dismiss();
            }
        });
        return rootView;
    }

}
