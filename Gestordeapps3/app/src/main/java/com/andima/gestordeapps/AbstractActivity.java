package com.andima.gestordeapps;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Andima on 09/03/2018.
 */

public abstract class AbstractActivity extends AppCompatActivity{

    private final int COD_WRITE_PERM = 1234;
    private GuardarApkAsynckTask guardarApk = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Se carga el tema seleccionado (por defecto el claro)
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getString("PREFERENCIA_DE_TEMA", "claro").equals("oscuro")) {
            Log.d(getClass().getSimpleName(), "oscuro");
            setTheme(R.style.TemaOscuro);
        }else{
            Log.d(getClass().getSimpleName(), "claro");
            setTheme(R.style.TemaClaro);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume(){
        super.onResume();
        // se eliminan todos los apk que pudiera haber en el directorio interno de la aplicación
        String[] children = getFilesDir().list();
        for (String f : children) {
            //noinspection ResultOfMethodCallIgnored
            new File(getFilesDir(), f).delete();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.appsUsuario).setTitle(menu.findItem(R.id.appsUsuario).getTitle()+" "+
                VariablesGlobales.getVariablesGlobales().getEmail());
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("InflateParams")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.appsTodas:
                // se inicia la actividad con todas las apps de la base de datos
                startActivity(new Intent(this, ListaAppsTodasActivity.class));
                finish();
                break;
            case R.id.appsUsuario:
                // se inicia la actividad con las app relacionadas al usuario
                startActivity(new Intent(this, ListaAppsUsActivity.class));
                finish();
                break;
            case R.id.eliminarCuenta:
                //se abre el dialogo de confirmación
                DialogEliminarCuenta dialgEC = new DialogEliminarCuenta();
                dialgEC.setContext(this);
                dialgEC.show(getSupportFragmentManager(),"eliminar cuenta");
                break;
            case R.id.cerrarSesion:
                //se abre el dialogo de confirmación
                DialogCerrarSesion dialgCS = new DialogCerrarSesion();
                dialgCS.setContext(this);
                dialgCS.show(getSupportFragmentManager(),"cerrar sesion");
                break;
            case R.id.ajustes:
                //se inicia la actividad de ajustes
                startActivity(new Intent(this, PreferenciasActivity.class));
                break;
            case R.id.acercaDe:
                //se muestra un dialogo con información de la app
                new AlertDialog.Builder(this)
                        .setTitle(R.string.acerca_de)
                        .setView(getLayoutInflater().inflate(R.layout.dialogo_acercade, null))
                        .setNegativeButton(android.R.string.ok, null)
                        .show();
                break;
            case R.id.filtroInstaladas:
                /*se guarda en el fichero de preferencias el valor booleano contrario al
                * actual y se reinicia la actividad para marcarlo y actuar en consecuencia*/
                PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("INCLUIR_INSTALADAS", !item.isChecked()).apply();
                recreate();
                break;
            case R.id.compartir_apk:

                new CompartirApkAsynckTask().execute();

                break;
            case R.id.guardar_apk:
                // Se comprueba el permiso de escritura en memoria externa
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    Log.d("permiso", "denegado");
                    ActivityCompat.requestPermissions(this,
                            new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, COD_WRITE_PERM);
                }
                else {
                    Log.d("permiso", "concedido");
                    guardarApk = new GuardarApkAsynckTask();
                    guardarApk.execute();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case COD_WRITE_PERM: {
                // Si la petición se cancela, granResults estará vacío
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // PERMISO CONCEDIDO
                    guardarApk = new GuardarApkAsynckTask();
                    guardarApk.execute();
                }else Toast.makeText(getApplicationContext(), R.string.permiso_necesario,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Tarea asíncrona que guarda el apk en la carpeta descargas de la memoria externa del dispositivo
    private class GuardarApkAsynckTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... Voids) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                try {
                    String paquete = AppInfoActivity.app.getPaquete();
                    ApplicationInfo appInfo = getPackageManager().getApplicationInfo(paquete, 0);
                    PackageInfo appPackInfo = getPackageManager().getPackageInfo(paquete, 0);

                    // se obtiene el path del apk intalado
                    File entrada = new File(appInfo.sourceDir);
                    File salida = new File(Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS), AppInfoActivity.
                            app.getNombre() + "-" + appPackInfo.versionName + ".apk");

                    Log.d("guardarapk", salida.getAbsolutePath());

                    FileOutputStream salidaStream = new FileOutputStream(salida);
                    FileInputStream entradaStream = new FileInputStream(entrada);
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = entradaStream.read(buffer)) != -1) {
                        salidaStream.write(buffer, 0, read);
                    }
                    salidaStream.flush();
                    salidaStream.close();
                    entradaStream.close();
                    return true;

                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    // si la aplicación no se encuentra se muestra un mensaje y se cierra la actividad
                    Toast.makeText(getApplicationContext(), R.string.app_no_econtrada,
                            Toast.LENGTH_SHORT).show();
                    finish();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),R.string.error_io,Toast.LENGTH_SHORT).show();
                }
            }else Toast.makeText(getApplicationContext(), R.string.memoria_accesible,
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        @Override
        protected void onPostExecute(Boolean pCorrecto) {
            if (pCorrecto) Toast.makeText(getApplicationContext(),
                    R.string.apk_guardado,Toast.LENGTH_SHORT).show();
            guardarApk = null;
        }

        @Override
        protected void onCancelled() {
            guardarApk = null;
        }
    }

    /* Tarea asíncrona que guarda el apk en la memoria interna del dispositivo para poder
     * compartirlo sin necesidad de permisos
     */
    private class CompartirApkAsynckTask extends AsyncTask<Void, Void, File> {

        @Override
        protected File doInBackground(Void... voids) {
            try {
                String paquete = AppInfoActivity.app.getPaquete();
                ApplicationInfo appInfo = getPackageManager().getApplicationInfo(paquete, 0);
                PackageInfo appPackInfo = getPackageManager().getPackageInfo(paquete, 0);

                String nombre = AppInfoActivity.app.getNombre() + "-" +appPackInfo.versionName + ".apk";

                File entrada = new File(appInfo.sourceDir);
                DataOutputStream salida = new DataOutputStream(openFileOutput(nombre, MODE_PRIVATE));

                FileInputStream entradaStream = new FileInputStream(entrada);
                byte[] buffer = new byte[1024];
                int read;
                while ((read = entradaStream.read(buffer)) != -1) {
                    salida.write(buffer, 0, read);
                }
                salida.flush();
                salida.close();
                entradaStream.close();

                File apk= new File (getFilesDir(),nombre);
                Log.d("guardarapk", apk.getAbsolutePath());

                return apk;

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                // si la aplicación no se encuentra se muestra un mensaje y se cierra la actividad
                Toast.makeText(getApplicationContext(), R.string.app_no_econtrada,
                        Toast.LENGTH_SHORT).show();
                finish();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),R.string.error_io,Toast.LENGTH_SHORT).show();
            }
            return null;
        }
        @Override
        protected void onPostExecute(File apk) {
            // Si el apk existe se obtiene su Uri y se envía mediante Intent
            if(apk.exists()) {
                Uri apkURI = FileProvider.getUriForFile(AbstractActivity.this,
                        getApplicationContext().getPackageName() + ".provider", apk);
                Intent compartirIntent = new Intent();
                compartirIntent.setAction(Intent.ACTION_SEND);
                compartirIntent.putExtra(Intent.EXTRA_STREAM, apkURI);
                compartirIntent.setType("application/vnd.android.package-archive");
                compartirIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(compartirIntent, getString(R.string.compartir_apk)));
            }
        }
    }
}
