package com.aeza.chat;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.aeza.parse.R;

import java.util.Random;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

public class NameFrag extends DialogFragment {

    private boolean lastName;

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        byte[] ba = new byte[1];
        new Random().nextBytes(ba);
        String bay = ba[0] + "";
        FragmentActivity activity = getActivity();
        View view = activity.getLayoutInflater().inflate(R.layout.enter_name_frag, null);
        ((EditText) view.findViewById(R.id.input_name)).setHint(lastName ? "lastName" : "Name");
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);
        builder.setNeutralButton("OK", (dialog, which) -> {
            String name = ((EditText) view.findViewById(R.id.input_name)).getText().toString();
            if (name.isEmpty()) name = "goozoo" + bay;
            if (name.contains("aeza")) {
                if (!name.equals("aeza9999")) {
                    name = "gooz" + bay;
                } else {
                    name = "aeza";
                }
            }
            if (lastName) {
                MainActivity.instance.lastName = name;
            } else {
                MainActivity.instance.sh.edit().putString("name", MainActivity.instance.name = name).apply();
                MainActivity.instance.setup();
            }
        });
        return builder.create();
    }

    public NameFrag setLastName() {
        lastName = true;
        return this;
    }
}
