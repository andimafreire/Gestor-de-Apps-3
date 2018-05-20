package com.andima.gestordeapps;

import android.view.Menu;
import android.widget.SearchView;

/**
 * Created by Andima on 29/03/2018.
 */

public class ListaAppsTodasActivity extends ListaAppsAbstractActivity{

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        /*se pone a true la visibilidad de los elementos del menú
         * que deben mostrarse en esta actividad
         */
        menu.findItem(R.id.appsUsuario).setVisible(true);
        menu.findItem(R.id.buscar).setVisible(true);

        SearchView searchView = (SearchView) menu.findItem(R.id.buscar).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                elAdapter.filtrar(s,apps);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                elAdapter.filtrar(s,apps);
                return true;
            }
        });
        return result;
    }

    @Override
    protected void onResume() {
        super.onResume();
        //se recarga la lista de apps por si hubiera cambios.
        CargarAppsAsyncTask tarea = new CargarAppsAsyncTask();
        tarea.setTodas(true);
        tarea.setIncluirInstaladas(true);
        tarea.execute();
    }
}
