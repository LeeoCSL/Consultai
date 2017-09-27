package br.com.carregai.carregai2.fragments;

/**
 * Created by renan.boni on 10/08/2017.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.TextView;


import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Calendar;
import java.util.Locale;

import br.com.carregai.carregai2.activity.LoginActivity;


public class DialogWeek extends DialogFragment {

    private TextView[] mDiasSemana;
    private boolean mCheckeds[];

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
        final String[] items = {"Segunda-feira", "Terça-feira","Quarta-feira","Quinta-feira",
                "Sexta-feira","Sábado","Domingo"};


        mDiasSemana = new TextView[items.length];

        mCheckeds = new boolean[items.length];

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());

        for(int i = 0; i < items.length; i++){
            String key = items[i].toLowerCase();
            boolean value = sharedPref.getBoolean(key, false);

            mCheckeds[i] = value;
        }

        AlertDialog.Builder builder =
                new AlertDialog.Builder(getActivity());

        builder.setTitle("Dias de uso")
                .setMultiChoiceItems(items, mCheckeds,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            public void onClick(DialogInterface dialog, int item, boolean isChecked) {
                                mCheckeds[item] = isChecked;
                            }
                        });

        builder.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
                SharedPreferences.Editor editor = sharedPref.edit();
                Bundle bundle = new Bundle();
                for(int i = 0; i < items.length; i++){
                    bundle.putBoolean(items[i].replace("-", "_").toLowerCase(), mCheckeds[i]);
                    editor.putBoolean(items[i].toLowerCase(), mCheckeds[i]);
                    editor.commit();
                }
                bundle.putString("email", sharedPref.getString("emailParam", " "));
                bundle.putString("email_google", sharedPref.getString("emailGoogle", ""));
                bundle.putString("email_facebook", sharedPref.getString("emailFB", ""));
                bundle.putString("sexo", sharedPref.getString("gender", ""));

                mFirebaseAnalytics.logEvent("selecao_dias_semana", bundle);
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return builder.create();
    }
}
