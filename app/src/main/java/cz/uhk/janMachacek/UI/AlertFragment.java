package cz.uhk.janMachacek.UI;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.Toast;

import cz.uhk.janMachacek.R;

/**
 * Created by jan on 22.7.2016.
 */

public class AlertFragment extends DialogFragment {


    private String login, password;

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public interface NoticeDialogListener {
        public void onDialogPositiveClick(AlertFragment dialog);

        public void onDialogNegativeClick(DialogFragment dialog);
    }

    NoticeDialogListener mListener;

    public static AlertFragment newInstance() {
        return new AlertFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (NoticeDialogListener) activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setMessage("sssssssssddddddddd");
        builder.setTitle("Přihlašovací údaje");


        builder.setView(inflater.inflate(R.layout.dialog_signin, null))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditText loginText = (EditText) getDialog().findViewById(R.id.login);
                        EditText passwordText = (EditText) getDialog().findViewById(R.id.password);

                        String login = loginText.getText().toString();
                        String password = passwordText.getText().toString();


                        Toast.makeText(getActivity(),
                                "FRagment :" + login ,
                                Toast.LENGTH_LONG).show();

                        AlertFragment.this.login = login;
                        AlertFragment.this.password = password;

                        mListener.onDialogPositiveClick(AlertFragment.this);
                    }
                });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mListener.onDialogNegativeClick(AlertFragment.this);
            }
        });
        return builder.create();
    }
}
