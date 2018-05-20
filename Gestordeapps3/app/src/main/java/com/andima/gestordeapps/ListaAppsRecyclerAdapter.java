package com.andima.gestordeapps;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ListaAppsRecyclerAdapter extends RecyclerView.Adapter<ListaAppsRecyclerAdapter.ListaAppsViewHolder> {

    private List<App> listaApps;
    private ListaAppsAbstractActivity activity;

    public ListaAppsRecyclerAdapter(List<App> PListaApps){
        listaApps = PListaApps;
    }

    @NonNull
    @Override
    public ListaAppsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fila_lista_app_cardview,
                parent, false);
        return new ListaAppsViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull ListaAppsViewHolder holder, int i) {
        holder.nombre.setText(listaApps.get(i).getNombre());
        // si la app contiene un drawable se carga, si no, se carga uno por defecto
        if(listaApps.get(i).getIcono() != null)
            holder.icono.setImageDrawable(listaApps.get(i).getIcono());
        else {
            holder.icono.setImageDrawable(activity.getResources().getDrawable(R.mipmap.ic_launcher_round));
        }
        if (listaApps.get(i).isInstalada()){
            holder.instalada.setText(R.string.app_instalada);
        }else {
            holder.instalada.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return listaApps.size();
    }


    public void filtrar(String s, List<App> apps){
        /* Se vacía la lista del adaptador y de introducen las apps que contengan el String
         * a filtrar sin diferenciar entre mayúsculas y minúsculas
         */
        listaApps = new ArrayList<>();
        for (App app : apps){
            if(app.getNombre().toLowerCase().contains(s.toLowerCase())) listaApps.add(app);
        }
        notifyDataSetChanged();
    }

    public void setActivity(ListaAppsAbstractActivity pA){
        activity = pA;
    }

    public class ListaAppsViewHolder extends RecyclerView.ViewHolder {

        private ImageView icono;
        private TextView nombre;
        private TextView instalada;

        public ListaAppsViewHolder(View itemView) {
            super(itemView);

            nombre= itemView.findViewById(R.id.nombre);
            icono = itemView.findViewById(R.id.icono);
            instalada= itemView.findViewById(R.id.instalada);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try{
                        /*se guarda la app seleccionada de la lista en una variable final de la
                         * actividad que posteriormente se lanza*/
                        App app = listaApps.get(getAdapterPosition());
                        AppInfoActivity.app = app;
                        Log.d("seleccionada", app.getNombre());
                        activity.startActivity(new Intent(activity.getBaseContext(), AppInfoActivity.class));
                    }catch (Exception e){
                        Log.d("seleccionada", e.getMessage());
                        activity.recreate();
                    }
                }
            });
        }
    }
}


