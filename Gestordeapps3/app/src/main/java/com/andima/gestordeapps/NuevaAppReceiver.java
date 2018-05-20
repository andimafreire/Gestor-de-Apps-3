package com.andima.gestordeapps;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

public class NuevaAppReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v("receiver", "broadcast recivido");
        /* Si se instala una aplicación nueva y el usuario está logeado se ejecuta un trabajo
         * que carga las apps del usuario en la base de datos aunque no entre en esta aplicación
         */
        if (PreferenceManager.getDefaultSharedPreferences(context)
                .getString("USUARIO", null)!=null) {
            ComponentName componente = new ComponentName(context, TrabajoCargarApps.class);
            JobInfo jobInfo = new JobInfo.Builder(12, componente)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .build();

            JobScheduler jobScheduler = (JobScheduler) context
                    .getSystemService(Context.JOB_SCHEDULER_SERVICE);
            if (jobScheduler != null) {
                jobScheduler.schedule(jobInfo);
            }
        }
    }
}
