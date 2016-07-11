package com.conestogac.assignment2;



import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
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

    public interface SetDistanceDialogListener {
        void onFinishSetDistanceDialog(int distance);
    }

    public static SetDistanceFragment newInstance(String title) {
        SetDistanceFragment frag = new SetDistanceFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE,android.R.style.Theme_Holo_Light_Dialog);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_set_distance, container);

        getDialog().setTitle(getArguments().getString("title", "No Title"));
        Button dismiss = (Button) rootView.findViewById(R.id.dismiss);

        final EditText edDistance = (EditText) rootView.findViewById(R.id.edDistance);
        edDistance.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dismiss.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SetDistanceDialogListener listener = (SetDistanceDialogListener) getTargetFragment();
                listener.onFinishSetDistanceDialog(Integer.valueOf(edDistance.getText().toString()));
                dismiss();
            }
        });

        return rootView;
    }

}
