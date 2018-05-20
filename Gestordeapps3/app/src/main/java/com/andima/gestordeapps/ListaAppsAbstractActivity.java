package com.andima.gestordeapps;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.Collections;
import java.util.List;

/**
 * Created by Andima on 14/03/2018.
 */

public abstract class ListaAppsAbstractActivity extends AbstractActivity{

    protected List<App> apps;
    private RecyclerView listaApps;
    private View mProgressView;
    protected ListaAppsRecyclerAdapter elAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lista_apps);
        listaApps = findViewById(R.id.lista);

        /*si la aplicación sufre un reinicio forzado reabre la actividad sin pasar por la actividad
        * principal*/
        if (VariablesGlobales.getVariablesGlobales().getEmail()==null) {
            VariablesGlobales.getVariablesGlobales().setEmail(PreferenceManager
                    .getDefaultSharedPreferences(this).getString("USUARIO", null));
        }
        mProgressView = findViewById(R.id.lista_progress);
        mProgressView.bringToFront();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //si se ha cerrado sesión o se ha eliminado la cuenta la actividad termina
        if(VariablesGlobales.getVariablesGlobales().getEmail()==null)
            finish();
        /* Se comprueba si el tema ha sido cambiado y
         * se relanza la actividad para aplicarlo*/
        Boolean esOscuro = getWindow().getStatusBarColor() == getResources().getColor(R.color.statusBarColor);
        if((esOscuro && PreferenceManager.getDefaultSharedPreferences(this).
                getString("PREFERENCIA_DE_TEMA", "claro").equals("claro"))||
                (!esOscuro && PreferenceManager.getDefaultSharedPreferences(this).
                        getString("PREFERENCIA_DE_TEMA", "claro").equals("oscuro"))){
            recreate();
            //si esta Actividad está abierta AppInfoActivity no se encuentra en la pila
            VariablesGlobales.getVariablesGlobales().setTemaCambiado(false);
        }
        showProgress(true);

    }

    private void cargarRecyclerView(){
        elAdapter = new ListaAppsRecyclerAdapter(apps);
        listaApps.setAdapter(elAdapter);
        LinearLayoutManager elLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        listaApps.setLayoutManager(elLayoutManager);
        elAdapter.setActivity(this);
        showProgress(false);
    }

    //carga la lista de apps procesada en CargarAppsAsyncTask
    public void cargarApps(List<App> pApps){
        apps = pApps;
        cargarRecyclerView();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    protected class CargarAppsAsyncTask extends AsyncTask<String, Void, List<App>> {

        private boolean incluirInstaladas;
        private boolean todas;


        @Override
        /*si la lista de apps procesadas esta vacía se carga el layout correspondiente
         * de lo contrario se ordena por nombre (el compareTo de la clase App se ha definido así)
         * y se llama al método cargarApps de la actividad*/
        protected void onPostExecute(List<App> pApps) {
            if(!pApps.isEmpty()){
                Collections.sort(pApps);
                ListaAppsAbstractActivity.this.cargarApps(pApps);
            }else{
                ListaAppsAbstractActivity.this.setContentView(R.layout.lista_apps_vacia);
            }
        }
        @Override
        protected List<App> doInBackground(String... strings) {
            return new CargarApps(ListaAppsAbstractActivity.this).cargarApps(todas,incluirInstaladas);
        }

        @Override
        protected void onCancelled() {
            ListaAppsAbstractActivity.this.showProgress(false);
        }

        public void setIncluirInstaladas(boolean p){
            incluirInstaladas = p;
        }

        public void setTodas(boolean b) {
            todas = b;
        }
    }
}