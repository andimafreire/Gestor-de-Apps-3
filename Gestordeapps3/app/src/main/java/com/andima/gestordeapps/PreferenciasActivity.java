package com.andima.gestordeapps;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.view.Menu;


/**
 * Created by Andima on 12/03/2018.
 */

public class PreferenciasActivity extends AbstractActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.ajustes);
        PreferenciasFragment pf = new PreferenciasFragment();
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, pf)
                .commit();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.ajustes).setVisible(false);
        if(VariablesGlobales.getVariablesGlobales().getEmail()==null){
            menu.findItem(R.id.cerrarSesion).setVisible(false);
            menu.findItem(R.id.eliminarCuenta).setVisible(false);
        }
        return result;
    }

    public static class PreferenciasFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferencias);
        }
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            switch (key) {
                /*si la preferencia es tema se asigna el booleano a la variable y reinicia la
                * actividad para aplicar el tema correspondiente*/
                case "PREFERENCIA_DE_TEMA":
                    VariablesGlobales.getVariablesGlobales().setTemaCambiado(true);
                    if (prefs.getString("PREFERENCIA_DE_TEMA", "claro").equals("oscuro")) {
                        Log.d("temas", "oscuro");
                    } else {Log.d("temas", "claro");}
                    getActivity().recreate();
                    break;
                default:
                    break;
            }
        }
        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }
    }
}
