package br.com.carregai.carregai2.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import br.com.carregai.carregai2.MainActivity;
import br.com.carregai.carregai2.R;
import br.com.carregai.carregai2.model.User;
import br.com.carregai.carregai2.utils.Utility;
import butterknife.BindView;
import butterknife.ButterKnife;

public class RegisterActivity extends AppCompatActivity {


    private FirebaseAuth mAuth;

    @BindView(R.id.input_login_register)
    TextInputLayout mEmail;

    @BindView(R.id.input_password_register)
    TextInputLayout mPassword;

    @BindView(R.id.input_name_register)
    TextInputLayout mName;

    private ProgressDialog mDialog;

    private FirebaseAnalytics mFirebaseAnalytics;

    SharedPreferences sharedPref;

    private String userName, userEmail, userPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ButterKnife.bind(this);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        mAuth = FirebaseAuth.getInstance();

        mDialog = new ProgressDialog(this);

        mEmail = (TextInputLayout)findViewById(R.id.input_login_register);
        mPassword = (TextInputLayout)findViewById(R.id.input_password_register);
    }

    public void handlerLogin(View v){
        finish();
    }

    public void handlerRegister(View v){
        validateDataFromEditText();
    }

    private void validateDataFromEditText(){
        userName = mName.getEditText().getText().toString().trim();
        userEmail = mEmail.getEditText().getText().toString().trim();
        userPassword = mPassword.getEditText().getText().toString().trim();

        if(TextUtils.isEmpty(userName)){
            mName.setError("O campo nome está vazio.");
            return;
        }

        if(!Utility.isEmailValid(userEmail)){
            mEmail.setError("Email inválido.");
            return;
        }

        if(userPassword.length() < 6){
            mPassword.setError("Sua senha deve ter no mínimo 6 caracteres.");
            return;
        }
        createUser(userEmail, userPassword);

    }

    private void createUser(String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(userID);

                        UserProfileChangeRequest profUpdate = new UserProfileChangeRequest.Builder()
                                .setDisplayName(authResult.getUser().getDisplayName())
                                .setPhotoUri(Uri.parse("https://api.adorable.io/avatars/285/"
                                        + authResult.getUser().getDisplayName() + ".png"))
                                .build();

                        authResult.getUser().updateProfile(profUpdate);

                        User user = new User();
                        user.setEmail(userEmail);
                        user.setName(userName);

                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("emailParam",userEmail);
                        editor.putString("nome", userName);
                        editor.commit();

                        ref.setValue(user);

                        Bundle bundle  = new Bundle();
                        bundle.putString("email", sharedPref.getString("emailParam", " "));
                        bundle.putString("nome",sharedPref.getString("nome", ""));
                        mFirebaseAnalytics.logEvent("cadastro_ok", bundle);

                        Intent intent = new Intent(RegisterActivity.this, Main3Activity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this,  "Algum erro ocorreu. Tente novamente.", Toast.LENGTH_LONG).show();
            }
        });
    }

}
