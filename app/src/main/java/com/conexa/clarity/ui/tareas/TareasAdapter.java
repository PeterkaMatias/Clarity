package com.conexa.clarity.ui.tareas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.conexa.clarity.R;
import com.conexa.clarity.data.local.TareaDao;
import com.conexa.clarity.data.model.Tarea;

import java.util.ArrayList;
import java.util.List;

public class TareasAdapter extends RecyclerView.Adapter<TareasAdapter.TareaViewHolder> {
    public interface OnTareaClickListener{
        void onTareaClick(Tarea tarea);
        void onTareaCheckChange(Tarea tarea, boolean isChecked);
    }
    private List<Tarea> listaTareas = new ArrayList<>();
    private final TareaDao tareaDao;
    private final OnTareaClickListener listener;

    public TareasAdapter(TareaDao tareaDao, OnTareaClickListener listener){
        this.tareaDao = tareaDao;
        this.listener = listener;
    }
    public void setTareas(List<Tarea> nuevasTareas){
        this.listaTareas = nuevasTareas != null ? nuevasTareas : new ArrayList<>();
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public TareaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tarea, parent, false);
        return new TareaViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull TareaViewHolder holder, int position){
        Tarea tarea = listaTareas.get(position);
        holder.bind(tarea);
    }
    @Override
    public int getItemCount(){
        return listaTareas.size();
    }
    class TareaViewHolder extends RecyclerView.ViewHolder{
        private final CheckBox cbCompletada;
        private final TextView tvTitulo;
        private final TextView tvDescripcion;
        private final TextView tvPrioridad;
        private final TextView tvFechaVencimiento;
        private final View layoutProgreso;
        private final TextView tvProgresoTexto;
        private final ProgressBar pbSubtareas;

        public TareaViewHolder(@NonNull View itemView){
            super(itemView);
            cbCompletada = itemView.findViewById(R.id.cbCompletada);
            tvTitulo = itemView.findViewById(R.id.tvTituloTarea);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionTarea);
            tvPrioridad = itemView.findViewById(R.id.tvPrioridadBadge);
            tvFechaVencimiento = itemView.findViewById(R.id.tvFechaVencimiento);
            layoutProgreso = itemView.findViewById(R.id.layoutProgresoSubtareas);
            tvProgresoTexto = itemView.findViewById(R.id.tvProgresoTexto);
            pbSubtareas = itemView.findViewById(R.id.pbSubtareas);
        }
        public void bind(Tarea tarea) {
            tvTitulo.setText(tarea.getTitulo());
            // Descripcion
            if (tarea.getDescripcion() != null && !tarea.getDescripcion().trim().isEmpty()) {
                tvDescripcion.setVisibility(View.VISIBLE);
                tvDescripcion.setText(tarea.getDescripcion());
            } else {
                tvDescripcion.setVisibility(View.GONE);
            }
            // Fecha de vencimiento
            if (tarea.getFechaVencimiento() != null && !tarea.getFechaVencimiento().trim().isEmpty()){
                tvFechaVencimiento.setVisibility(View.VISIBLE);
                tvFechaVencimiento.setText(" Vence: " + tarea.getFechaVencimiento());
            } else {
                tvFechaVencimiento.setVisibility(View.GONE);
            }
            // Badge de prioridad
            configurarPrioridad(tarea.getPrioridad());

            // Checkbox y tachado de texto
            cbCompletada.setOnCheckedChangeListener(null);
            boolean completada = tarea.esCompletada();
            cbCompletada.setChecked(completada);
            aplicarEfectoTachado(completada);

            cbCompletada.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onTareaCheckChange(tarea, isChecked);
                }
            });

            // Barra de progreso segun micro-pasos completado
            int totalSubtareas = tareaDao.contarSubtareas(tarea.getIdTarea());
            if(totalSubtareas > 0){
                layoutProgreso.setVisibility(View.VISIBLE);
                int completadas = tareaDao.contarSubtareasCompletadas(tarea.getIdTarea());
                int porcentaje = (int) (((float) completadas / totalSubtareas) * 100);

                pbSubtareas.setProgress(porcentaje);
                tvProgresoTexto.setText("Micro-pasos: " + completadas + "/" + totalSubtareas + "(" + porcentaje + "%)");
            } else {
                layoutProgreso.setVisibility(View.GONE);
            }
            // Click para editar la tarea
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTareaClick(tarea);
                }
            });
        }

private void configurarPrioridad(String prioridad){
            if(prioridad == null) prioridad = Tarea.PRIORIDAD_MEDIA;
            tvPrioridad.setText(prioridad);
            switch (prioridad) {
                case Tarea.PRIORIDAD_ALTA:
                    tvPrioridad.setBackgroundColor(Color.parseColor("#C94A4A")); //Rojo suave
                    break;
                case Tarea.PRIORIDAD_BAJA:
                    tvPrioridad.setBackgroundColor(Color.parseColor("#7A9D8C")); //verde grisaceo
                    break;
                case Tarea.PRIORIDAD_MEDIA:
                default:
                    tvPrioridad.setBackgroundColor(Color.parseColor("#E09F3E")); //ambar/naranja
                    break;
            }

        }

        private void aplicarEfectoTachado(boolean completada){
            if(completada){
                tvTitulo.setPaintFlags(tvTitulo.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvTitulo.setAlpha(0.5f);
            } else{
                tvTitulo.setPaintFlags(tvTitulo.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                tvTitulo.setAlpha(1.0f);
            }
        }
    }
}
