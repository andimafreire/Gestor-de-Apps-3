package com.andima.gestordeapps;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;

import java.util.Objects;

/**
 * Created by Andima on 15/03/2018.
 */

public class DialogCerrarSesion extends DialogFragment {

    private Context context;
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.menu_cerrar_sesion);
        builder.setMessage(R.string.mensaje_dialog_cerrar_sesion);
        builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                /* si la conexión se realiza correctamente se borra el usuario del fichero
                 * de preferencias para que al iniciar la app la próxima vez se necesario
                 * iniciar sesión de nuevo y se cierra la app.*/
                String st = "http://galan.ehu.eus/afreire003/WEB/cerrarSesion.php";
                String param = "email=" + VariablesGlobales.getVariablesGlobales().getEmail();
                Log.d("param", param);
                if(new Conexion(context).conexionConAsyncTask(st, param)) {
                    PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString("USUARIO", null).apply();
                    VariablesGlobales.getVariablesGlobales().setEmail(null);
                    Objects.requireNonNull(getActivity()).finish();
                }
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                onCancel(dialogInterface);
            }
        });
        return builder.create();
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
