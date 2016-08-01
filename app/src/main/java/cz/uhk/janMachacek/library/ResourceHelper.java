package cz.uhk.janMachacek.library;
import cz.uhk.janMachacek.R;
import android.content.Context;
import android.content.res.Resources;
import android.util.SparseArray;

/**
 * Helper pro pøevod hodnot z resource na objekt tøídy SparseArray
 * 
 * @author Jan Macháèek
 *
 */
public class ResourceHelper {
	
	public static SparseArray<String> getObjectTypes(Context context)
	{
		Resources r = context.getResources();
		String[] typeNames = r.getStringArray(R.array.astro_object_names);
		String[] typeValues = r.getStringArray(R.array.astro_object_values);
		SparseArray<String> typeMap = new SparseArray<String>();
		
		for (int i = 0; i < typeNames.length; i++) {
		    typeMap.put(Integer.parseInt(typeValues[i]), typeNames[i]);
		}
		
		return typeMap;
	}
}
