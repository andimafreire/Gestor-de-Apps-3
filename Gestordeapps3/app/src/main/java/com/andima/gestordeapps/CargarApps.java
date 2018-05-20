package com.andima.gestordeapps;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CargarApps {

    private Context context;

    public CargarApps(Context pContext) {
        context = pContext;
    }

    public List<App> cargarApps(boolean todas, boolean incluirInstaladas){
        Map<String, App> appsAIntrod = new HashMap<>();
        List<App> appsFinal = new ArrayList<>();
        PackageManager pm = context.getPackageManager();

        //se recorren todas las apps del dispositivo
        for (PackageInfo infoApp : pm.getInstalledPackages(0)) {
            /*si la app tiene información, está habilitada y no es del sistema creamos la
             * instancia de App correspondiente*/
            if (null != infoApp.applicationInfo && infoApp.applicationInfo.enabled
                    && (infoApp.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0){
                String paquete = infoApp.packageName;
                String nombre = pm.getApplicationLabel(infoApp.applicationInfo).toString();
                Drawable icono = pm.getApplicationIcon(infoApp.applicationInfo);
                App app = new App(paquete, nombre, icono, true);
                /*se carga la app en el HashMap definido y si esta activada la opción
                 * incluir instaladas se añaden a la lista final*/
                if(incluirInstaladas)
                    appsFinal.add(app);
                appsAIntrod.put(app.getPaquete(),app);
                Log.d("instalada", app.getPaquete());
            }
        }
        Conexion con = new Conexion(context.getApplicationContext());

        String st = "http://galan.ehu.eus/afreire003/WEB/obtenerApps.php";
        if(con.hayConexion()) {

            String param = "";
            // si se envia el el campo email se cargan las apps de ese usuario, si no, se cargan todas
            if (!todas) param = "email=" + VariablesGlobales.getVariablesGlobales().getEmail();

            //se realiza la coxexión y se obtiene el resultado inmediatamente después
            JSONArray resultado = con.obtenerArrayResultado(con.establecerConexion(st, param, false));

            if (resultado != null) {
                for (int i = 0; i < resultado.size(); i++) {
                    JSONObject jsonApp = (JSONObject) resultado.get(i);
                    /*si no esta instalada se añade a la lista final, de lo contrario se elimina del
                     * HashMap para acabar obteniendo una lista de apps que no están en la base de datos*/
                    if (!appsAIntrod.containsKey(jsonApp.get("paquete").toString())) {
                        Log.d("noInstalada", jsonApp.get("paquete").toString() + " " + jsonApp.get("nombre").toString());
                        Drawable icono = null;
                        try {
                            // se decodifica el icono recuperado de la base de datos
                            byte[] image = Base64.decode(jsonApp.get("icono").toString(), Base64.DEFAULT);
                            ByteArrayInputStream imageStream = new ByteArrayInputStream(image);
                            icono = new BitmapDrawable(context.getResources(), BitmapFactory.decodeStream(imageStream));
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                            Toast.makeText(context.getApplicationContext(),
                                    "No ha sido posible recuperar el icono de "
                                            + jsonApp.get("nombre").toString(), Toast.LENGTH_SHORT).show();
                        }

                        App app = new App(jsonApp.get("paquete").toString(),
                                jsonApp.get("nombre").toString(), icono, false);
                        appsFinal.add(app);
                    } else appsAIntrod.remove(jsonApp.get("paquete").toString());
                }
            }

            /*si la lista de apps instaladas que no están en la base de datos no esta vacía
             * se ejecuta el método que introduce todas las apps en la base de datos */
            if (!appsAIntrod.isEmpty())
                introducirEnBD(appsAIntrod.values());
        }
        return appsFinal;
    }

    private void introducirEnBD(Collection<App> appsAIntrod) {
        //por cada app de la lista
        for(App app : appsAIntrod) {
            String imageStr = "";
            try {
                // se codifica el icono de la app instalada para añadirlo a la base de datos
                Bitmap icon = ((BitmapDrawable) app.getIcono()).getBitmap();
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                icon.compress(Bitmap.CompressFormat.PNG, 100, output);
                byte[] byteArray = output.toByteArray();
                imageStr = Base64.encodeToString(byteArray, Base64.DEFAULT);
            }catch (ClassCastException e){
                e.printStackTrace();
            }
            // se preparan los parámetros y se realiza la subida
            HashMap<String,String> detail = new HashMap<>();
            detail.put("email", VariablesGlobales.getVariablesGlobales().getEmail());
            detail.put("paquete", app.getPaquete());
            detail.put("nombre", app.getNombre());
            detail.put("icono", imageStr);
            Conexion con = new Conexion( context.getApplicationContext());
            String param= con.hashMapToUrl(detail);
            String st = "http://galan.ehu.eus/afreire003/WEB/subirApp.php";
            con.establecerConexion(st, param, true);
        }
    }
}
