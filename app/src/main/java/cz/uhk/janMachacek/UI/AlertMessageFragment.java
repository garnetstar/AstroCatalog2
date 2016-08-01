package cz.uhk.janMachacek.UI;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

/**
 * Created by jan on 1.8.2016.
 */
public class AlertMessageFragment extends DialogFragment {

    private String message;

    public static AlertMessageFragment newInstance() {
        return new AlertMessageFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message);
        builder.setTitle("Error");
        return builder.create();
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
