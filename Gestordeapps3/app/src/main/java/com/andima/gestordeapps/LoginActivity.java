package com.andima.gestordeapps;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import org.json.simple.JSONObject;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AbstractActivity {

    private UserLoginTask mAuthTask = null;
    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        /*si existe un usuario en el fichero de preferencias la sesión
        aun esta activa y se lanza la siguiente actividad cerrando ésta*/
        if (PreferenceManager.getDefaultSharedPreferences(this).getString("USUARIO",null)!=null){
            VariablesGlobales.getVariablesGlobales().setEmail
                    (PreferenceManager.getDefaultSharedPreferences(this).getString("USUARIO",null));
            startActivity(new Intent(this, ListaAppsUsActivity.class));
            finish();
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle(R.string.login_titulo);
        mEmailView = findViewById(R.id.email);

        mPasswordView = findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    @Override
    public void onResume(){
        super.onResume();
        //si el tema ha cambiado se actualiza
        if (VariablesGlobales.getVariablesGlobales().isTemaCambiado()){
            VariablesGlobales.getVariablesGlobales().setTemaCambiado(false);
            recreate();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        //se oculta la opción del menu cerrar sesión y eliminar cuenta
        menu.findItem(R.id.eliminarCuenta).setVisible(false);
        menu.findItem(R.id.cerrarSesion).setVisible(false);
        return result;
    }
    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */

    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }
    //se comprueba la valided del email
    private boolean isEmailValid(String email) {
        return email.contains("@") && email.length()<40;
    }
    //se comprueba la valided de la contraseña
    private boolean isPasswordValid(String password) {
        return password.length() > 3 && password.length() < 30;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }
    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    @SuppressLint("StaticFieldLeak")
    private class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private Boolean incorrecto;
        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
            incorrecto = false;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String st = "http://galan.ehu.eus/afreire003/WEB/login.php";
            Conexion con =  new Conexion(getApplicationContext());
            if (con.hayConexion()) {
                String param = "email=" + mEmail + "&password=" + mPassword + "&tokenfcm=" +
                        PreferenceManager.getDefaultSharedPreferences(getBaseContext()).
                                getString("TOKENFCM", "");
                Log.d("param", param);
                JSONObject resultado = con.obtenerResultado(con.establecerConexion(st, param, false));
                if (resultado != null) {
                    switch (resultado.get("result").toString()) {
                        case "logeado":
                            return true;
                        case "registrado":
                            NotificationManager nm = (NotificationManager) getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
                            NotificationCompat.Builder nb = new NotificationCompat.Builder(getApplicationContext(), "1")
                                    .setSmallIcon(android.R.drawable.ic_menu_info_details)
                                    .setContentTitle(getString(R.string.titulo_notif_registro))
                                    .setContentText(getString(R.string.texto_notif_registro))
                                    .setVibrate(new long[]{50})
                                    .setAutoCancel(true)
                                    //se muestra durante 1 minuto y luego se cierra sola pero solo a partir de android O
                                    .setTimeoutAfter(60000);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                NotificationChannel nc = new NotificationChannel("1",
                                        getString(R.string.nombre_canal1),
                                        NotificationManager.IMPORTANCE_DEFAULT);
                                // Configurar el canal de comunicación
                                if (nm != null) {
                                    nm.createNotificationChannel(nc);
                                }
                            }
                            if (nm != null) {
                                nm.notify(1, nb.build());
                            }
                            return true;
                        case "incorrecto":
                            incorrecto = true;
                            return false;
                    }
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);
            /*si el login es correcto o se ha registrado se introduce el email en el fichero
            * de preferencias para no tener que volver a iniciar sesión hasta que el usuario
            * lo decida y se inicia la siguiente actividad */
            if (success) {
                PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putString("USUARIO", mEmail).apply();
                VariablesGlobales.getVariablesGlobales().setEmail(mEmail);
                startActivity(new Intent(getBaseContext(), ListaAppsUsActivity.class));
                finish();
            }else{
                if(incorrecto){
                    mPasswordView.setError(getString(R.string.error_incorrect_password));
                    mPasswordView.requestFocus();
                }
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

