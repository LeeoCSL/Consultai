package br.com.carregai.carregai2.fragments;


import br.com.carregai.carregai2.MainActivity;
import br.com.carregai.carregai2.R;
import br.com.carregai.carregai2.activity.ComoUsarActivity;
import br.com.carregai.carregai2.activity.LoginActivity;
import br.com.carregai.carregai2.activity.RechargeActivity;
import br.com.carregai.carregai2.adapter.DashboardGridViewAdapter;
import br.com.carregai.carregai2.model.DashboardItem;
import br.com.carregai.carregai2.model.Recarga;
import br.com.carregai.carregai2.service.UpdatingService;
import br.com.carregai.carregai2.utils.Utility;
import butterknife.BindView;
import butterknife.ButterKnife;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.blackcat.currencyedittext.CurrencyEditText;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormatSymbols;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.content.Context.ALARM_SERVICE;
import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by renan.boni on 09/08/2017.
 */

public class ServicesFragment extends Fragment {

    private FirebaseAnalytics mFirebaseAnalytics;

    private GridView mGridView;
    private DashboardGridViewAdapter mAdapter;
    private List<DashboardItem> mItens;

    public static final int GASTO_DIARIO = 0;
    public static final int VALOR_RECARGA = 1;
    public static final int COMPRAI = 2;
    private static final int COMO_USAR = 3;
    private static final int LIMPAR_CAMPOS = 4;
    private static final int VIAGEM_EXTRA = 5;

    public static final int DIAS_DA_SEMANA = 7;

    public static TextView mDisplay;

    private int[] mDrawables = {R.drawable.comum, R.drawable.integracao_360, R.drawable.estudante, R.drawable.integracao_estudante};

    private int currentIndex = 0;

    public ServicesFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());

        getItensList();
        View view = inflater.inflate(R.layout.service_layout, container, false);
        mDisplay = (TextView) view.findViewById(R.id.tv_display);
        mGridView = (GridView) view.findViewById(R.id.dashboard_gridview);
        mGridView.setAdapter(new DashboardGridViewAdapter(view.getContext(), 1, mItens));
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case GASTO_DIARIO:
                        valorDiario();
                        break;
                    case VALOR_RECARGA:
                        valorRecarga();
                        break;
                    case COMPRAI:
                        comprai();
                        break;

                    case COMO_USAR:
                        comoFunciona();
                        break;

                    case LIMPAR_CAMPOS:
                        limparCampos();
                        break;
                    case VIAGEM_EXTRA:
                        viagemExtra();
                        break;
                    case 6:
                        setTimer();
                        break;
                    case DIAS_DA_SEMANA:
                        selectDays();
                        break;
                    case 8:
                        changeView(view);
                        break;

                }
            }
        });
        return view;
    }


    private void setTimer(){

        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final Calendar calendar = Calendar.getInstance();

        final int hour = sp.getInt("notification_hour", 0);
        final int min = sp.getInt("notification_minute", 0);

        calendar.set(Calendar.YEAR, Calendar.MONTH, Calendar.DATE, hour, min);

        TimePickerDialog dialog = new TimePickerDialog(getActivity(), 0, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                SharedPreferences.Editor editor = sp.edit();

                editor.putInt("notification_hour", hourOfDay);
                editor.putInt("notification_minute", minute);

                editor.commit();
            }
        }, hour, min, true);



        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        String hora =  String.valueOf(sharedPref.getInt("notification_hour", 0));


        Bundle bundle = new Bundle();
        bundle.putString("hora_alarme", String.valueOf(sharedPref.getInt("notification_hour", 0)) + ":"+ String.valueOf(sharedPref.getInt("notification_minute", 0)));
        bundle.putString("email", sharedPref.getString("emailParam", " "));
        bundle.putString("email_google", sharedPref.getString("emailGoogle", ""));
        bundle.putString("nome",sharedPref.getString("nome", ""));
        //TODO fb idade sexo tel
        bundle.putString("email_facebook", sharedPref.getString("emailFB", ""));

        mFirebaseAnalytics.logEvent("configurar_notificacao", bundle);
        dialog.show();
    }

    private void changeView(View v) {

        ImageView imageView = (ImageView) v.findViewById(R.id.grid_item_image);

        if(currentIndex > mDrawables.length - 1){
            currentIndex = 0;
        }

        imageView.setImageResource(mDrawables[currentIndex]);
        currentIndex++;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateViews();
    }

    private void getItensList() {
        mItens = new ArrayList<>();

        mItens.add(new DashboardItem(R.drawable.cifrao, "Gasto diário"));
        mItens.add(new DashboardItem(R.drawable.moeda, "Valor recarga"));
        mItens.add(new DashboardItem(R.drawable.cifrao2, "ComprAí"));


        mItens.add(new DashboardItem(R.drawable.interrogacao, "Como usar"));
        mItens.add(new DashboardItem(R.drawable.ic_vassoura, "Limpar dados"));
        mItens.add(new DashboardItem(R.drawable.bus, "Viagem extra"));
        mItens.add(new DashboardItem(R.drawable.cino, "Alarme"));
        mItens.add(new DashboardItem(R.drawable.calendario, "Dias de uso"));

        mItens.add(new DashboardItem(R.drawable.comum, "Tarifação"));
    }

    private void updateViews() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mDisplay.setText(Utility.formatValue(sp.getFloat("saldo_atual", 0)));
    }

    public void valorDiario() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Valor Diário");

        final CurrencyEditText input = new CurrencyEditText(getActivity(), null);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final float saldo = sp.getFloat("valor_diario", 0);

        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Digite o valor diário...");
        input.setText(Utility.formatValue(saldo));

        builder.setView(input);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                float db = Utility.stringToFloat(input.getText().toString());

                storeValue("valor_diario", db);

                Utility.makeText(getActivity(), "O seu gasto diário foi salvo.");
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                Bundle bundle = new Bundle();
                bundle.putFloat("valor_diario", sharedPref.getFloat("valor_diario", 0) );
                bundle.putString("email", sharedPref.getString("emailParam", " "));
                bundle.putString("email_google", sharedPref.getString("emailGoogle", ""));
                bundle.putString("link_fb", LoginActivity.linkFB);
                bundle.putString("nome",sharedPref.getString("nome", ""));
                //TODO fb idade sexo tel
                bundle.putString("email_facebook", sharedPref.getString("emailFB", ""));

                mFirebaseAnalytics.logEvent("valor_diario", bundle);

            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void valorRecarga() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Valor Recarga");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final float saldo = sp.getFloat("valor_recarga", 0);

        final CurrencyEditText input = new CurrencyEditText(getActivity(), null);
        input.setText(Utility.formatValue(saldo));
        builder.setView(input);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                float valorRecarga = Utility.stringToFloat(input.getText().toString());
                float saldoFinal = saldo + valorRecarga;

                mDisplay.setText(Utility.formatValue(saldoFinal));

                storeValue("saldo_atual", saldoFinal);
                storeValue("valor_recarga", valorRecarga);

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy-HH:mm:ss");
                Date data = new Date();

                Calendar cal = Calendar.getInstance();
                cal.setTime(data);
                Date dataAtual = cal.getTime();

                String dataCompleta = dateFormat.format(dataAtual);

                String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(userID).child("recargas");

                HashMap<String, String> values = new HashMap<String, String>();
                values.put("recarga_horario", dataCompleta);
                values.put("recarga_valor", String.valueOf(valorRecarga));

                ref.push().setValue(values);

                Utility.makeText(getActivity(), "Sua recarga foi atualizada.");
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                Bundle bundle = new Bundle();
                bundle.putFloat("valor_recarga",valorRecarga );
                bundle.putString("email", sharedPref.getString("emailParam", " "));
                bundle.putString("email_google", sharedPref.getString("emailGoogle", ""));
                bundle.putString("nome",sharedPref.getString("nome", ""));
                //TODO fb idade sexo tel
                bundle.putString("email_facebook", sharedPref.getString("emailFB", ""));


                mFirebaseAnalytics.logEvent("valor_recarga", bundle);
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void selectDays() {
        FragmentManager fragmentManager = getFragmentManager();
        DialogWeek dialogWeek = new DialogWeek();
        dialogWeek.show(fragmentManager, "dias_semana");


    }

    private void storeValue(String key, float value) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putFloat(key, value);

        editor.commit();
    }

    private void comoFunciona() {
        Calendar calendar = Calendar.getInstance();
        String currentDay = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());

        String dia = new DateFormatSymbols().getWeekdays()[calendar.get(Calendar.DAY_OF_WEEK)];
        Format formatter = new SimpleDateFormat("EEEE");
        String s = formatter.format(new Date());


        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        Bundle bundle = new Bundle();
        bundle.putString("email", sharedPref.getString("emailParam", " "));
        bundle.putString("email_google", sharedPref.getString("emailGoogle", ""));
        bundle.putString("nome",sharedPref.getString("nome", ""));
        //TODO fb idade sexo tel
        bundle.putString("email_facebook", sharedPref.getString("emailFB", ""));

        mFirebaseAnalytics.logEvent("como_funciona", bundle);




        startActivity(new Intent(getActivity(), ComoUsarActivity.class));
    }

    public void comprai(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Bundle bundle = new Bundle();


        bundle.putString("email", sharedPref.getString("emailParam", " "));
        bundle.putString("email_google", sharedPref.getString("emailGoogle", ""));
        bundle.putString("nome",sharedPref.getString("nome", ""));
        //TODO fb idade sexo tel
        bundle.putString("email_facebook", sharedPref.getString("emailFB", ""));
        mFirebaseAnalytics.logEvent("clique_botao_comprai", bundle);
        startActivity(new Intent(getActivity(), RechargeActivity.class));

    }
    public void viagemExtra() {


        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        float value = sharedPref.getFloat("viagem_extra", 0);
        Log.d("SF", Float.toString(value));
        if (value == 0.0) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Configure o valor da viagem extra nas configurações")
                    .setPositiveButton("Entendi", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            builder.setNegativeButton("", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create();
            builder.show();


        } else {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            float saldoAtual = sp.getFloat("saldo_atual", 0);

            if (saldoAtual - value < 0) {
                Utility.makeText(getActivity(), "Atualize o valor de sua recarga antes de calcular a viagem extra.");
            } else if (saldoAtual - value >= 0) {
                saldoAtual -= value;
                SharedPreferences.Editor editor = sp.edit();
                editor.putFloat("saldo_atual", saldoAtual);
                editor.commit();
                mDisplay.setText("R$ " + String.format("%.2f", saldoAtual));
                Utility.makeText(getActivity(), "Seu saldo foi atualizado. [Viagem Extra: R$ " + value + " ]");

                Bundle bundle = new Bundle();
                bundle.putString("email", sharedPref.getString("emailParam", " "));
                bundle.putString("email_google", sharedPref.getString("emailGoogle", ""));
                bundle.putString("nome",sharedPref.getString("nome", ""));
                //TODO fb idade sexo tel

                bundle.putString("email_facebook", sharedPref.getString("emailFB", ""));

                mFirebaseAnalytics.logEvent("viagem_extra", bundle);
            }
        }
    }

    private void limparCampos() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());



        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Limpar campos");
        builder.setMessage("Você tem certeza que deseja limpar os campos?");
        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

                SharedPreferences.Editor editor = sharedPref.edit();

                String value = "R$ 0,00";

                editor.putFloat("valor_diario", 0);
                editor.putFloat("valor_recarga", 0);
                editor.putFloat("viagem_extra", 0);
                editor.putFloat("saldo_atual", 0);

                mDisplay.setText(value);

                final String[] items = {"Segunda-feira", "Terça-feira", "Quarta-feira", "Quinta-feira",
                        "Sexta-feira", "Sábado", "Domingo"};


                for (int i = 0; i < items.length; i++) {
                    String key = items[i].toLowerCase();
                    editor.putBoolean(key, false);
                }

                editor.commit();

                Utility.makeText(getActivity(), "Seus dados foram limpos");

                Bundle bundle = new Bundle();
                    bundle.putString("email", sharedPref.getString("emailParam", " "));
                    bundle.putString("email_google", sharedPref.getString("emailGoogle", ""));
                    bundle.putString("nome",sharedPref.getString("nome", ""));
                //TODO fb idade sexo tel
                    bundle.putString("email_facebook", sharedPref.getString("emailFB", ""));

                mFirebaseAnalytics.logEvent("limpar_campos", bundle);


            }
        });
        builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());



        builder.show();
    }
}
