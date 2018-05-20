package com.andima.gestordeapps;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Andima on 15/03/2018.
 */

public class AppInfoActivity extends AbstractActivity {

    public static App app;
    private RecyclerView capsRecyclerView;
    private View mProgressView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //si la aplicación sufre un reinicio forzado la app no existirá y esta actividad debe cerrarse
        if(app != null){
            setTitle(app.getNombre());
            setContentView(R.layout.activity_app_info);
            cargarEnlayout();
        }
        else finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //si se ha cerrado sesión o se ha eliminado la cuenta la actividad termina
        if(VariablesGlobales.getVariablesGlobales().getEmail()==null)
            finish();
        // si el tema ha cambiado se relanza la actividad para aplicarlo
        if (VariablesGlobales.getVariablesGlobales().isTemaCambiado()) {
            VariablesGlobales.getVariablesGlobales().setTemaCambiado(false);
            recreate();
        }
        showProgress(true);
        //se recargan las capturas por si hubiera cambios.
        CargarCapturasAsyncTask tarea = new CargarCapturasAsyncTask();
        tarea.execute();
    }

    @Override
    public  boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        /*se pone a true la visibilidad de los elementos del menú
         * que deben mostrarse en esta actividad*/
        if(app.isInstalada()){
            menu.findItem(R.id.compartir_apk).setVisible(true);
            menu.findItem(R.id.guardar_apk).setVisible(true);
        }
        return result;
    }

    private void cargarEnlayout() {
        //si la app está instalada se carga el icono y se visibilizan los botones
        if (app.isInstalada()){
            this.findViewById(R.id.ejecutarInfo).setVisibility(View.VISIBLE);
            this.findViewById(R.id.desinstalarInfo).setVisibility(View.VISIBLE);
            this.findViewById(R.id.ajustesInfo).setVisibility(View.VISIBLE);
            this.findViewById(R.id.borrarInfo).setVisibility(View.VISIBLE);
        } else { // si no, se comprueba que la app pertenezca al usuario para mostrar la opción de borrar
            String st = "http://galan.ehu.eus/afreire003/WEB/comprobarApp.php";
            String param = "email=" + VariablesGlobales.getVariablesGlobales().getEmail() + "&paquete=" +
                    app.getPaquete();

            if(new Conexion(getApplicationContext()).conexionConAsyncTask(st, param)) {
                this.findViewById(R.id.borrarInfo).setVisibility(View.VISIBLE);
            }
        }
        /*si la app es esta app se oculta el botón ejecutar para impedir cargar la
        * pila con mas actividades de esta app*/
        if (app.getPaquete().equals(getPackageName()))
            this.findViewById(R.id.ejecutarInfo).setVisibility(View.INVISIBLE);

        if (app.getIcono() != null)
            ((ImageView) this.findViewById(R.id.iconoInfo)).setImageDrawable(app.getIcono());
        ((TextView) this.findViewById(R.id.nombreInfo)).setText(app.getNombre());
        ((TextView) this.findViewById(R.id.paqueteInfo)).setText(app.getPaquete());

        capsRecyclerView= findViewById(R.id.capturas_recycler);
        mProgressView = findViewById(R.id.capturas_progress);
        mProgressView.bringToFront();
    }
    /*botón de la vista que borra la app de la tabla relación app-usuario y, si esta instalada,
    * la desinstala*/
    public void borrarApp(View view){
        String st = "http://galan.ehu.eus/afreire003/WEB/borrar.php";
        String param = "tabla=AppUsuario&email=" + VariablesGlobales.getVariablesGlobales().getEmail() + "&paquete=" +
                app.getPaquete();
        Log.d("param", param);
        /* si el borrado de la app se realiza correctamente de la base de datos se procede a
        * desinstalar la app*/
        if(new Conexion(getApplicationContext()).conexionConAsyncTask(st, param)) {
            if (app.isInstalada()) desinstalarApp(view);
            finish();
        }
    }

    public void ejecutarApp(View view){
        try {
            startActivity(getPackageManager().getLaunchIntentForPackage(app.getPaquete()));
        } catch (ActivityNotFoundException e) {
            Log.i("ejecutar",e.getMessage());
        }
    }

    public void desinstalarApp(View view){
        startActivity(new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + app.getPaquete())));
        finish();
    }

    public void ajustesApp(View view){
        try {
            /*se abre la app de ajustes por defecto del dispositivo con la información de la
            * app correspondiente*/
            startActivity(new Intent("android.settings.APPLICATION_DETAILS_SETTINGS",
                    Uri.parse("package:" + app.getPaquete())));
        } catch (ActivityNotFoundException e) {
            Log.i("ajustes",e.getMessage());
        }
    }
    public void abrirEnPlayApp(View view){
        startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=" + app.getPaquete())));
    }

    private void cargarRecyclerView(List<Bitmap> pCapturas){
        CapturasRecyclerAdapter elAdapter = new CapturasRecyclerAdapter(pCapturas);
        capsRecyclerView.setAdapter(elAdapter);
        LinearLayoutManager elLayoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL,false);
        capsRecyclerView.setLayoutManager(elLayoutManager);
        elAdapter.setActivity(this);
        showProgress(false);
    }

    public void subirImagen(){
        Intent elIntentGal = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(elIntentGal, 4567);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 4567 && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                new SubirCapturaAsyncTask().execute(bitmap);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), R.string.error_io, Toast.LENGTH_SHORT).show();
            }
        }
    }
    private class CargarCapturasAsyncTask extends AsyncTask<Void, Void, List<Bitmap> > {

        @Override
        protected List<Bitmap> doInBackground(Void... voids) {
            List<Bitmap> capturas = new ArrayList<>();
            capturas.add(BitmapFactory.decodeResource(getResources(), R.drawable.list_add));

            String st = "http://galan.ehu.eus/afreire003/WEB/obtenerCapturas.php";
            String param = "paquete="+app.getPaquete();
            Log.d("param", param);
            Conexion con = new Conexion(getApplicationContext());
            if (con.hayConexion()) {
                //se recuperan todas las capturas de la tabla Capturas que esten relacionadas con la app
                JSONArray resultado = con.obtenerArrayResultado(con.establecerConexion(st, param, false));
                if (resultado != null) {
                    for (int i = 0; i < resultado.size(); i++) {
                        JSONObject jsonCaptura = (JSONObject) resultado.get(i);
                        // se decodifican las capturas recibidas para introducirlas en la lista
                        byte[] image = Base64.decode(jsonCaptura.get("captura").toString(), Base64.DEFAULT);
                        ByteArrayInputStream imageStream = new ByteArrayInputStream(image);
                        Bitmap laimg = BitmapFactory.decodeStream(imageStream);
                        capturas.add(laimg);
                    }
                }
            }
            return capturas;
        }

        @Override
        protected void onPostExecute(List<Bitmap> pCapturas) {
            cargarRecyclerView(pCapturas);
        }
    }

    private class SubirCapturaAsyncTask extends AsyncTask<Bitmap, Void, Boolean>{

        @Override
        protected Boolean doInBackground(Bitmap... bitmaps) {
            String st = "http://galan.ehu.eus/afreire003/WEB/subirCaptura.php";
            Conexion con =  new Conexion(getApplicationContext());
            if(con.hayConexion()) {
                // se codifica la captura
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                bitmaps[0].compress(Bitmap.CompressFormat.JPEG, 50, output);
                byte[] byteArray = output.toByteArray();
                String imageStr = Base64.encodeToString(byteArray, Base64.DEFAULT);
                // se preparan los parámetros
                HashMap<String, String> detail = new HashMap<>();
                detail.put("paquete", AppInfoActivity.app.getPaquete());
                detail.put("captura", imageStr);
                String params = con.hashMapToUrl(detail);
                // se realiza la subida
                return con.establecerConexion(st, params, true) != null;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean correcto) {
            if(correcto) recreate();
        }
    }
}
