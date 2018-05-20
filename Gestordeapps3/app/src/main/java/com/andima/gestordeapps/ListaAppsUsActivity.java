package com.andima.gestordeapps;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;

/**
 * Created by Andima on 29/03/2018.
 */

public class ListaAppsUsActivity extends ListaAppsAbstractActivity{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //se establece el titulo de la aplicación con el email de registro del usuario
        setTitle(getResources().getString(R.string.titulo_lista_apps)+" "
                +VariablesGlobales.getVariablesGlobales().getEmail());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        /*se pone a true la visibilidad de los elementos del menú
         * que deben mostrarse en esta actividad*/
        menu.findItem(R.id.filtroInstaladas).setVisible(true);
        menu.findItem(R.id.filtrar).setVisible(true);
        /*se marca la casilla del elemento del menú con el valor
         * seleccionado(por defecto false(no marcada))*/
        menu.findItem(R.id.filtroInstaladas).setChecked(PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("INCLUIR_INSTALADAS",false));
        menu.findItem(R.id.appsTodas).setVisible(true);
        return result;
    }

    @Override
    protected void onResume() {

        super.onResume();

        //se recarga la lista de apps por si hubiera cambios.
        CargarAppsAsyncTask tarea = new CargarAppsAsyncTask();
        tarea.setIncluirInstaladas(PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean("INCLUIR_INSTALADAS",false));
        tarea.setTodas(false);
        tarea.execute();
    }
}
