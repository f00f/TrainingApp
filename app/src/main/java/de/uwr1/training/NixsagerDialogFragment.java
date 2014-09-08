package de.uwr1.training;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

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
        final String player = getName().toString();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(player)
                .setItems(R.array.dialog_items_nixsager, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        switch (which) {
                            case 0:
                                Training.sendZusage(player);
                                break;
                            case 1:
                                Training.sendAbsage(player);
                                break;
                        }
                    }
                })
        ;
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
