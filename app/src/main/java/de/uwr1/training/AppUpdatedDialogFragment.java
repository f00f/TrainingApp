package de.uwr1.training;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;


public class AppUpdatedDialogFragment extends DialogFragment {
    public static final String KEY_OLD_VERSION_ID = "oldVersionId";

    // PUBLIC METHODS

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Activity act = getActivity();
        final int oldVersionId = getArguments().getInt(KEY_OLD_VERSION_ID);
        final String newVersionName = Config.getVersionName(this);
        final String appName = Config.getAppName(act);

        AlertDialog.Builder builder = new AlertDialog.Builder(act);
        builder.setTitle(appName + " wurde auf Version " + newVersionName + " aktualisiert")
                .setMessage("Hey! " + appName + " wurde aktualisiert. Die neue Version enthält diese Verbesserungen:\n\n"
                        + Config.getChangeLogSinceVersion(act, oldVersionId))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Config.setInstalledVersion(act);
                    }
                })
        ;
        return builder.create();
    }
}
