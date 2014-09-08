package de.uwr1.training;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by f00f on 29.08.2014.
 */
public class NixSagerAdapter extends ArrayAdapter<String> {
    int resource;

    public NixSagerAdapter(Context _context, int _resource,
                           String[] _items) {
        super(_context, _resource, _items);
        resource = _resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout nixSagerView;

        String name = getItem(position);

        if (convertView == null) {
            nixSagerView = new LinearLayout(getContext());
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
                    inflater);
            vi.inflate(resource, nixSagerView, true);
        } else {
            nixSagerView = (LinearLayout) convertView;
        }
        TextView itemView = (TextView) nixSagerView.findViewById(R.id.nixsager_list_item_name);
        itemView.setText(name);
        return nixSagerView;
    }
}
