package cz.uhk.janMachacek.UI;

import java.util.ArrayList;

import cz.uhk.janMachacek.R;
import cz.uhk.janMachacek.library.AstroObject;
import cz.uhk.janMachacek.library.ResourceHelper;
import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Adaprér pro zobrazení seznamu objektù 
 *  
 * @author Jan Macháèek
 *
 */
public class AstroObjectAdapter extends ArrayAdapter<AstroObject> {

	private Context context;
	private final ArrayList<AstroObject> values;

	public AstroObjectAdapter(Context context, ArrayList<AstroObject> values) {
		super(context, R.layout.object_adapter_item, values);
		this.context = context;
		this.values = values;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		LayoutInflater infater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View row = infater.inflate(R.layout.object_adapter_item, parent, false);

		SparseArray<String> typeMap = ResourceHelper
				.getObjectTypes(getContext());

		TextView name = (TextView) row.findViewById(R.id.object_name);
		TextView constellation = (TextView) row
				.findViewById(R.id.object_constellation);
		TextView type = (TextView) row.findViewById(R.id.object_typ);
		TextView magnitude = (TextView) row.findViewById(R.id.object_magnitude);

		name.setText(values.get(position).getName());
		constellation.setText(values.get(position).getConstellation());
		type.setText(typeMap.get(values.get(position).getType()));
		magnitude.setText(Double.toString(values.get(position).getMagnitude()));

		return row;
	}
}
