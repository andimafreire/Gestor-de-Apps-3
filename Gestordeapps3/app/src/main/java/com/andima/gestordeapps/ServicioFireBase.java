package com.andima.gestordeapps;

import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Andima on 21/04/2018.
 */

public class ServicioFireBase extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("token", "Refreshed token: " + refreshedToken);
        /* se guarda el token en el fichero de preferencias para asignárselo al usuario que
         * inicie sesión en la base de datos*/
        PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putString("TOKENFCM", refreshedToken).apply();
    }
}
