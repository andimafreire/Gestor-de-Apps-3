package com.andima.gestordeapps;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

public class Conexion {

    private Context context;


    public Conexion(Context pContext){
        context = pContext;
    }

    public HttpURLConnection establecerConexion(String pST, String pParam ,boolean pImagen){
        URL targetURL;
        try {
            targetURL = new URL(pST);
            HttpURLConnection urlConnection;
            urlConnection = (HttpURLConnection) targetURL.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            // si los parámetros contienen imágenes se modifica la conexión
            if (pImagen){
                urlConnection.setReadTimeout(15000);
                urlConnection.setConnectTimeout(15000);
                urlConnection.setDoInput(true);
                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(pParam);
                writer.flush();
                writer.close();
                os.close();
            }else{
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                urlConnection.setConnectTimeout(5000);
                urlConnection.setReadTimeout(5000);
                PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
                out.print(pParam);
                out.close();
            }
            Log.d("param", pParam);

            int statusCode = urlConnection.getResponseCode();
            Log.d("codigo", Integer.toString(statusCode));
            if (statusCode == HttpsURLConnection.HTTP_OK){
                return urlConnection;
            } else{
                errorToast(2);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            errorToast(0);
        } catch (ProtocolException e) {
            e.printStackTrace();
            errorToast(4);
        } catch (IOException e) {
            e.printStackTrace();
            errorToast(1);
        }
        return null;
    }

    public JSONArray obtenerArrayResultado(HttpURLConnection httpURLConnection) {
        if (httpURLConnection!=null) {
            InputStream is;
            try {
                is = new BufferedInputStream(httpURLConnection.getInputStream());

                BufferedReader br = new BufferedReader(new
                        InputStreamReader(is, "UTF-8"));
                String line, result = "";
                while ((line = br.readLine()) != null) {
                    result += line;
                }
                is.close();
                if (!result.equals("null")) {
                    JSONParser parser = new JSONParser();
                    return (JSONArray) parser.parse(result);
                }
            } catch (ParseException e) {
                e.printStackTrace();
                errorToast(3);
            } catch (IOException e) {
                e.printStackTrace();
                errorToast(1);
            }
        }
        return null;
    }

    public JSONObject obtenerResultado(HttpURLConnection httpURLConnection) {
        InputStream is;
        try {
            is = new BufferedInputStream(httpURLConnection.getInputStream());

            BufferedReader br = new BufferedReader(new
                    InputStreamReader(is, "UTF-8"));
            String line, result = "";
            while ((line = br.readLine()) != null) {
                result += line;
            }
            is.close();

            JSONParser parser = new JSONParser();
            return (JSONObject) parser.parse(result);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            errorToast(5);
        } catch (IOException e) {
            e.printStackTrace();
            errorToast(1);
        } catch (ParseException e) {
            e.printStackTrace();
            errorToast(3);
        }
        return null;
    }
        public String hashMapToUrl(HashMap<String, String> params){
        try {
            StringBuilder result = new StringBuilder();
            boolean first = true;
            for(Map.Entry<String, String> entry : params.entrySet()){
                if (first)
                    first = false;
                else
                    result.append("&");

                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }
            return result.toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            errorToast(5);
            return null;
        }
    }

    public boolean hayConexion() {
        try {
            final InetAddress address = InetAddress.getByName("galan.ehu.eus");
            return !address.equals("");
        } catch (UnknownHostException e) {
            e.printStackTrace();
            errorToast(2);
        }
        return false;
    }

    private void errorToast(final int i){

        Handler handler =  new Handler(context.getMainLooper());
        handler.post( new Runnable(){
            public void run(){
                CharSequence text = "";
                switch (i){
                    case 0:
                        text =context.getString(R.string.error_malformedurl);
                        break;
                    case 1:
                        text = context.getString(R.string.error_io);
                        break;
                    case 2:
                        text = context.getString(R.string.error_conexion);
                        break;
                    case 3:
                        text = context.getString(R.string.error_json);
                        break;
                    case 4:
                        text =context.getString(R.string.error_metodo);
                        break;
                    case 5:
                        text =context.getString(R.string.error_codificacion);
                        break;
                    case 6:
                        text =context.getString(R.string.error_conexion_interrumpida);
                        break;
                    case 7:
                        text =context.getString(R.string.error_ejecucion);
                        break;
                }
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public Boolean conexionConAsyncTask(String pST, String pParam){
        try {
            return new ConexionAsyncTask().execute(pST, pParam).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            errorToast(6);
        } catch (ExecutionException e) {
            e.printStackTrace();
            errorToast(7);
        }
        return false;
    }

    private class ConexionAsyncTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {
            return hayConexion() && obtenerResultado(establecerConexion(strings[0], strings[1],
                    false)).get("result").toString().equals("correcto");
        }
    }
}
