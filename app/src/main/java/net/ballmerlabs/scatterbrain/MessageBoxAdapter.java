package net.ballmerlabs.scatterbrain;

import android.content.ContentResolver;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Adapter for the control of the slightly less horrible message list view.
 */
public class MessageBoxAdapter extends BaseAdapter {
    public Context context;
    public ArrayList<DispMessage> data;
    private static LayoutInflater inflater = null;

    public MessageBoxAdapter(Context context) {
        this.context = context;
        this.data = new ArrayList<>();
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if(vi == null)
            vi = inflater.inflate(R.layout.messagerow,parent);
        TextView text = (TextView) vi.findViewById(R.id.messagebody);
        TextView header =(TextView) vi.findViewById(R.id.messageheader);
        text.setText(data.get(position).body);
        header.setText(data.get(position).header);
        return vi;
    }
}
