package de.uwr1.training;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Created by f00f on 01.09.2014.
 */
public class NixsagerDialogFragment extends DialogFragment {
    final int IDX_ZUSAGE = 0;
    final int IDX_ABSAGE = 1;
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
                            case IDX_ZUSAGE:
                                Training.sendZusage(player);
                                break;
                            case IDX_ABSAGE:
                                Training.sendAbsage(player);
                                break;
                        }
                    }
                })
        ;
        return builder.create();
    }

    // PRIVATE METHODS

    private CharSequence getName() {
        CharSequence name = getArguments().getCharSequence("name");
        return null != name ? name : "";
    }
}
