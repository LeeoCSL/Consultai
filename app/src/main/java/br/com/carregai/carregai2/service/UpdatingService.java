package br.com.carregai.carregai2.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import br.com.carregai.carregai2.R;
import br.com.carregai.carregai2.activity.LoginActivity;
import br.com.carregai.carregai2.utils.NotificationUtils;

/**
 * Created by renan.boni on 16/08/2017.
 */

public class UpdatingService extends IntentService {

    public UpdatingService() {
        super("UpdatingService");
    }

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());

        Calendar calendar = Calendar.getInstance();
        String currentDay = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplication());

        switch (currentDay){
            case "segunda":
                currentDay = "segunda-feira";
                break;
            case "terça":
                currentDay = "terça-feira";
                break;
            case "quarta":
                currentDay = "quarta-feira";
                break;
            case "quinta":
                currentDay = "quinta-feira";
                break;
            case "sexta":
                currentDay = "sexta-feira";
                break;
        }

        if(sharedPref.getBoolean(currentDay, false)){

            float saldoAtual = sharedPref.getFloat("saldo_atual", 0);
            float valorDiario = sharedPref.getFloat("valor_diario", 0);

            if(saldoAtual - valorDiario > 0){

                Log.i("Saldo_atual", ""+saldoAtual);
                Log.i("Valor_diario", ""+valorDiario);

                saldoAtual -= valorDiario;
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putFloat("saldo_atual", saldoAtual);
                editor.commit();

                Bundle bundle = new Bundle();

                bundle.putString("email", sharedPref.getString("emailParam", " "));
                bundle.putString("email_google", sharedPref.getString("emailGoogle", ""));
                bundle.putString("nome",sharedPref.getString("nome", ""));
                //TODO fb idade sexo tel
                bundle.putString("email_facebook", sharedPref.getString("emailFB", ""));
                bundle.putFloat("valor_debitado", valorDiario);
                bundle.putString("sexo", sharedPref.getString("gender", ""));

                mFirebaseAnalytics.logEvent("debito_diario", bundle);

            }
            if(saldoAtual < 10){
                int hour = sharedPref.getInt("notification_hour", 22);
                int min = sharedPref.getInt("notification_minute", 45);


                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, hour);
                cal.set(Calendar.MINUTE, min);
                cal.set(Calendar.SECOND, 00);


                Intent myIntent = new Intent(this, NotificationService.class);

                PendingIntent pendingIntent = pendingIntent = PendingIntent.getService(this, 0,
                        myIntent, 0);

                AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
                alarmManager.setRepeating(
                        AlarmManager.RTC_WAKEUP,
                        cal.getTimeInMillis(),
                        AlarmManager.INTERVAL_HALF_DAY,
                        pendingIntent);
            }
        }
    }
}
