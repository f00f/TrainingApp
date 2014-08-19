package de.uwr1.training;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class VersionFragment extends Fragment {


    public VersionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View my_view = inflater.inflate(R.layout.fragment_version, container, false);

        ((TextView)my_view.findViewById(R.id.version))
            .setText(getString(R.string.app_name) + " " + getString(R.string.lbl_version) + " " + Config.getVersion());

        return my_view;
    }
}
