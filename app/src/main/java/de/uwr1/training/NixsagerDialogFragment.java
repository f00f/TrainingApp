package de.uwr1.training;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Created by f00f on 01.09.2014.
 */
public class NixsagerDialogFragment extends DialogFragment {
    /* The activity that creates an instance of this dialog fragment must
 * implement this interface in order to receive event callbacks.
 * Each method passes the DialogFragment in case the host needs to query it. */
    public interface NixsagerDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
        public void onDialogNeutralClick(DialogFragment dialog);
    }

    // Use this instance of the interface to deliver action events
    NixsagerDialogListener mListener;
    CharSequence mName;

    // PUBLIC METHODS

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("An-/Abmelden")
                .setMessage("Willst Du " + getName() + " an- oder abmelden?")
                        // Add the buttons
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        // Send the negative button event back to the host activity
                        mListener.onDialogNegativeClick(NixsagerDialogFragment.this);
                    }
                })
                .setNeutralButton(R.string.nixdialog_ab, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked No button
                        // Send the neutral button event back to the host activity
                        mListener.onDialogNeutralClick(NixsagerDialogFragment.this);
                    }
                })
                .setPositiveButton(R.string.nixdialog_an, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked Yes button
                        // Send the positive button event back to the host activity
                        mListener.onDialogPositiveClick(NixsagerDialogFragment.this);
                    }
                })
        ;
        // TODO: maybe use a list, or a custom layout, instead of buttons?
        return builder.create();
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NixsagerDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    // PRIVATE METHODS

    private CharSequence getName() {
        return getArguments().getCharSequence("name", "");
    }
}
