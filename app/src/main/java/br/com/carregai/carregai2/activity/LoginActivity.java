package br.com.carregai.carregai2.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.Image;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import br.com.carregai.carregai2.MainActivity;
import br.com.carregai.carregai2.R;
import br.com.carregai.carregai2.model.User;
import br.com.carregai.carregai2.utils.DialogUtils;
import br.com.carregai.carregai2.utils.Utility;
import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {

    public static String emailParam ;
    public static String emailGoogle;
    public static String linkFB;
    public static String nomeFB;
    public static String idFacebook;
    public static String emailFB;
    private FirebaseAnalytics mFirebaseAnalytics;

    @BindView(R.id.input_email)
    EditText mLogin;

    @BindView(R.id.input_password)
    EditText mPassword;

    @BindView(R.id.iv_background)
    ImageView mBackgroundImage;

    private FirebaseAuth mAuth;

    private LoginButton mLoginFacebook;
    private CallbackManager mCallbackManager;

    private SignInButton mGoogleLogin;

    private ImageView mLoginGoogle;

    private GoogleApiClient mGoogleApiClient;

    private static final int RC_SIGN_IN = 1;

    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private String userEmail, userPassword;

    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        mDialog = new ProgressDialog(this);
        ButterKnife.bind(this);

        mBackgroundImage.setBackgroundResource(R.drawable.login_animation);
        mBackgroundImage.post(new Runnable() {
            @Override
            public void run() {
                AnimationDrawable frameAnimation = (AnimationDrawable) mBackgroundImage.getBackground();
                frameAnimation.setEnterFadeDuration(500);
                frameAnimation.setExitFadeDuration(500);
                frameAnimation.start();
            }
        });


        mAuth = FirebaseAuth.getInstance();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() != null){
                    startActivity(new Intent(LoginActivity.this, Main3Activity.class));
                }
            }
        };

        mLoginFacebook = (LoginButton)findViewById(R.id.login_fb);
        mGoogleLogin = (SignInButton)findViewById(R.id.login_google);


        if(mAuth.getCurrentUser() != null){
            Intent intent = new Intent(this, Main3Activity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        // connection failed, should be handled
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        mGoogleLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
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
                        user.setEmail(authResult.getUser().getEmail());
                        user.setName(authResult.getUser().getDisplayName());

                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("emailGoogle",user.getEmail());
                        editor.putString("nome",user.getName());
                        editor.commit();

                        ref.setValue(user);

                        Bundle bundle  = new Bundle();
                        bundle.putString("email_google", user.getEmail());
                        bundle.putString("nome",sharedPref.getString("nome", ""));
                        mFirebaseAnalytics.logEvent("login_google_ok", bundle);

                        Intent intent = new Intent(LoginActivity.this, Main3Activity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                });

    }

    public void forgetPassword(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Email");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        builder.setView(input);

        builder.setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String email = input.getText().toString().trim();

                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("email_reset", email);
                editor.commit();

                if(!TextUtils.isEmpty(email)){
                    DialogUtils.loadingDialog(LoginActivity.this);
                    resetPassword(email);
                    dialog.dismiss();
                }else{

                }
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

    public void resetPassword(String email){
        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        Bundle bundle = new Bundle();
                        bundle.putString("email_reset", sharedPref.getString("email_reset", " "));

                        mFirebaseAnalytics.logEvent("reset_senha_ok", bundle);
                        DialogUtils.hideLoadingDialog();
                        Toast.makeText(LoginActivity.this, "Acabamos de te enviar as instruções de como recuperar sua conta.", Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                DialogUtils.hideLoadingDialog();
                if (e.getClass() == FirebaseAuthInvalidUserException.class) {
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    Bundle bundle = new Bundle();
                    bundle.putString("email_reset", sharedPref.getString("email_reset", " "));

                    mFirebaseAnalytics.logEvent("reset_senha_erro", bundle);
                    Toast.makeText(LoginActivity.this,
                            "Usuário não encontrado", Toast.LENGTH_LONG).show();

                    return;
                }
                if (e.getClass() == FirebaseException.class) {
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    Bundle bundle = new Bundle();
                    bundle.putString("email_reset", sharedPref.getString("email_reset", " "));

                    mFirebaseAnalytics.logEvent("reset_senha_erro", bundle);
                    Toast.makeText(LoginActivity.this,
                            "Email inválido", Toast.LENGTH_LONG).show();
                }
                e.printStackTrace();
            }
        });
    }

    private void validateDateFromEditText(){
        userEmail = mLogin.getText().toString().trim();
        userPassword = mPassword.getText().toString().trim();

        if(!Utility.isEmailValid(userEmail)){
            mLogin.setError("Email inválido.");
            return;
        }
        if(TextUtils.isEmpty(userPassword)){
            mPassword.setError("Senha inválida.");
            return;
        }
        loginWithEmailAndPassword();
    }

    private void loginWithEmailAndPassword() {
        DialogUtils.loadingDialog(this);
        mDialog.setTitle("Entrando");
        mDialog.setMessage("Por favor, espere enquanto fazemos o login");
        mDialog.show();
        mDialog.setCanceledOnTouchOutside(false);
        mAuth.signInWithEmailAndPassword(userEmail, userPassword)
                .addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("emailParam",userEmail);
                        editor.commit();

                        Bundle bundle = new Bundle();
                        bundle.putString("email", sharedPref.getString("emailParam", " "));
                        mFirebaseAnalytics.logEvent("login_email_ok", bundle);

                    }
                }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                DialogUtils.hideLoadingDialog();
                if (e.getClass() == FirebaseAuthUserCollisionException.class) {
                    Utility.makeText(LoginActivity.this,
                            "Email já está sendo usado em uma conta do facebook.");
                    return;
                }
                if (e.getClass() == FirebaseAuthInvalidUserException.class) {
                    Utility.makeText(LoginActivity.this,
                            "Usuário não encontrado.");
                    return;
                }
                if (e.getClass() == FirebaseAuthInvalidCredentialsException.class) {
                    mPassword.setError("Senha inválida");
                    return;
                } else {
                    Utility.makeText(LoginActivity.this,
                            "Erro ao fazer login, tente novamente mais tarde.");
                }
                Bundle bundle = new Bundle();
                bundle.putString("email", userEmail);
                mFirebaseAnalytics.logEvent("login_email_erro", bundle);
                e.printStackTrace();
            }
        });
    }

    public void handlerLogin(View v){
        validateDateFromEditText();
    }

    public void register(View v){
        startActivity(new Intent(this, RegisterActivity.class));
    }

    public void loginWithFacebook(View view) {
        mCallbackManager = CallbackManager.Factory.create();
        mLoginFacebook.setReadPermissions("email", "public_profile");
        mLoginFacebook.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                Profile profile = Profile.getCurrentProfile();

                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                Bundle bundle = new Bundle();
                bundle.putString("nome",sharedPref.getString("nome", ""));
                bundle.putString("email_facebook", sharedPref.getString("emailFB", ""));
                mFirebaseAnalytics.logEvent("login_facebook_ok", bundle);

                handleFacebookAcessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                Bundle bundle = new Bundle();
                bundle.putString("nome",sharedPref.getString("nome", ""));
                bundle.putString("email_facebook", sharedPref.getString("emailFB", ""));
                mFirebaseAnalytics.logEvent("login_facebook_cancelado", bundle);
            }

            @Override
            public void onError(FacebookException error) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                Bundle bundle = new Bundle();
                bundle.putString("nome",sharedPref.getString("nome", ""));
                bundle.putString("email_facebook", sharedPref.getString("emailFB", ""));
                mFirebaseAnalytics.logEvent("login_facebook_erro", bundle);

                Toast.makeText(LoginActivity.this, "Erro: " +error.toString(), Toast.LENGTH_LONG).show();
                error.printStackTrace();
            }
        });
    }

    private void handleFacebookAcessToken(final AccessToken token) {
        final AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

        DialogUtils.loadingDialog(this);

        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        DialogUtils.hideLoadingDialog();

                        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(userID);

                        User user = new User();
                        user.setEmail(authResult.getUser().getEmail());
                        user.setName(authResult.getUser().getDisplayName());
                        user.setImage("https://graph.facebook.com/" +token.getUserId()+"/picture?type=small");

                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("nome",user.getName());
                        editor.putString("emailFB",user.getEmail());



                        editor.commit();

                        Profile currentProfile = Profile.getCurrentProfile();
                        Uri profilePictureUri = currentProfile.getProfilePictureUri(32, 32);

                        FirebaseUser firebaUser = FirebaseAuth.getInstance().getCurrentUser();
                        UserProfileChangeRequest req = new UserProfileChangeRequest.Builder()
                                .setPhotoUri(profilePictureUri).build();


                        firebaUser.updateProfile(req);

                        ref.setValue(user);

                        Intent intent = new Intent(LoginActivity.this, Main3Activity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        DialogUtils.hideLoadingDialog();

                        if (e.getClass() == FirebaseAuthUserCollisionException.class) {
                            Toast.makeText(LoginActivity.this, "Este email está vinculado a uma outra conta com o facebook.", Toast.LENGTH_LONG).show();
                            LoginManager.getInstance().logOut();
                        }
                        if(e.getClass() == FirebaseAuthInvalidUserException.class){
                            Toast.makeText(LoginActivity.this, "Email não cadastrado no nosso sistema", Toast.LENGTH_LONG).show();
                        }
                        if(e.getClass() == FirebaseAuthInvalidCredentialsException.class){
                            Toast.makeText(LoginActivity.this, "Senha inválida", Toast.LENGTH_LONG).show();
                        }
                        else {
                            Toast.makeText(LoginActivity.this,"Erro", Toast.LENGTH_LONG).show();
                        }
                        e.printStackTrace();
                    }
                });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {

            DialogUtils.loadingDialog(this);

            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
                DialogUtils.hideLoadingDialog();
            } else {
                DialogUtils.hideLoadingDialog();
            }
        }else{
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void handlerFakeFacebookLogin(View v){
        if(v.getId() == R.id.login_fb_fake){
            mLoginFacebook.performClick();
        }
    }

    public void handlerFakeGoogleLogin(View v){
        if(v.getId() == R.id.login_google_fake){
            mGoogleLogin.performClick();
        }
    }
}