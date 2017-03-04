package cz.uhk.machacek.UI;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import cz.uhk.machacek.Model.DiaryObject;
import cz.uhk.machacek.R;
import cz.uhk.machacek.library.ResourceHelper;

/**
 * @author Jan Macháček
 *         Created on 26.11.2016.
 */
public class DiaryObjectAdapter extends ArrayAdapter<DiaryObject> {

    private Context context;
    private final ArrayList<DiaryObject> values;


    public DiaryObjectAdapter(Context context, ArrayList<DiaryObject> values) {
        super(context, R.layout.diary_adapter_item, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater infater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View row = infater.inflate(R.layout.diary_adapter_item, parent, false);

        SparseArray<String> typeMap = ResourceHelper
                .getObjectTypes(getContext());

        TextView name = (TextView) row.findViewById(R.id.diary_from);
        TextView length = (TextView) row.findViewById(R.id.diary_length);
        TextView timeFrom = (TextView) row.findViewById(R.id.diary_time_from);
        TextView log = (TextView) row.findViewById(R.id.content);
        TextView weather = (TextView) row.findViewById(R.id.weatherList);
        TextView guid = (TextView) row.findViewById(R.id.diary_guid);
        TextView sync_ok = (TextView) row.findViewById(R.id.diarys_sync_ok) ;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


        try {
            Date date = dateFormat.parse(values.get(position).getFrom());

            SimpleDateFormat time = new SimpleDateFormat("HH:mm");
            time.format(date).toString();

            java.text.DateFormat df = android.text.format.DateFormat.getLongDateFormat(getContext());


            Date startDate = dateFormat.parse(values.get(position).getFrom());
            Date endDate = dateFormat.parse(values.get(position).getTo());

            long difference = endDate.getTime() - startDate.getTime();
            int hours = (int) (difference / (1000 * 60 * 60));
            int min = (int) (difference - (1000 * 60 * 60 * hours)) / (1000 * 60);


            name.setText(df.format(date));
            timeFrom.setText(time.format(date).toString());
            length.setText(String.format("%02d:%02d", hours, min));
            log.setText(values.get(position).getLog());
            weather.setText(values.get(position).getWeather());
            guid.setText(values.get(position).getGuid());
            sync_ok.setText(Integer.toString(values.get(position).getSyncOk()));





        } catch (ParseException e) {
            e.printStackTrace();
        }
        return row;
    }
}
