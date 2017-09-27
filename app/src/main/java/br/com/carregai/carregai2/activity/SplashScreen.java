package br.com.carregai.carregai2.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.analytics.FirebaseAnalytics;

import br.com.carregai.carregai2.R;
import br.com.carregai.carregai2.utils.Utility;
import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.branch.referral.util.LinkProperties;
import com.appsee.Appsee;

public class SplashScreen extends AppCompatActivity {

    private FirebaseAnalytics mFirebaseAnalytics;

    private ImageView mLogoImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Appsee.start("b49552520cbf4838abed3cc2efc938f7");

        Branch branch = Branch.getInstance();
        branch.initSession(new Branch.BranchUniversalReferralInitListener() {
            @Override
            public void onInitFinished(BranchUniversalObject branchUniversalObject, LinkProperties linkProperties, BranchError error) {
                if (error == null) {

                    //variaveis que vão receber os parametros do link
                    String Origem = "organico";
                    String Campanha = "organico";

                    // parametros "channel" e "campaign"
                    if (linkProperties != null) {
                        Campanha = linkProperties.getCampaign();
                        Origem = linkProperties.getChannel();
                    }

//                    eventos firebase com as variaveis
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                    Bundle bundle = new Bundle();
                    bundle.putString("origem", Origem);
                    bundle.putString("campanha", Campanha);
                    bundle.putString("email", sharedPref.getString("emailParam", " "));
                    bundle.putString("email_google", sharedPref.getString("emailGoogle", ""));
                    bundle.putString("nome",sharedPref.getString("nome", ""));
                    //TODO fb idade sexo tel
                    bundle.putString("sexo", sharedPref.getString("gender", ""));
                    bundle.putString("email_facebook", sharedPref.getString("emailFB", ""));
                    Toast.makeText(SplashScreen.this,sharedPref.getString("emailParam", " "), Toast.LENGTH_SHORT).show();
                    mFirebaseAnalytics.logEvent("Tracking", bundle);

                }
                else {
                    Log.i("MyApp", error.getMessage());
                }
            }
        }, this.getIntent().getData(), this);

        mLogoImage = (ImageView)findViewById(R.id.img_logo);

        RotateAnimation rotate = new RotateAnimation(30, 360, Animation.RELATIVE_TO_SELF, 0.5f,  Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(2500);
        mLogoImage.startAnimation(rotate);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                Intent i = new Intent(SplashScreen.this, LoginActivity.class);
                startActivity(i);

                finish();
            }
        }, 2500);
    }
}
