package com.andima.gestordeapps;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.AsyncTask;

public class TrabajoCargarApps extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        new CargarAppsAsyncTask(params).execute();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    private class CargarAppsAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private JobParameters params;

        private CargarAppsAsyncTask(JobParameters pParams){
            params = pParams;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            new CargarApps(TrabajoCargarApps.this).cargarApps(false,false);
            return false;
        }

        @Override
        protected void onPostExecute(Boolean pRepetir) {
            jobFinished(params, pRepetir);
        }
    }
}
