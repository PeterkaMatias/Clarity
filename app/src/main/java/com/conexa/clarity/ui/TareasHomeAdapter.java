package com.conexa.clarity.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.conexa.clarity.R;
import com.conexa.clarity.data.model.Tarea;

import java.util.List;

public class TareasHomeAdapter extends RecyclerView.Adapter<TareasHomeAdapter.ViewHolder> {

    public interface OnTareaCheckedListener {
        void onTareaChecked(Tarea tarea, boolean isChecked, int position);
    }

    private List<Tarea> lista;
    private final OnTareaCheckedListener listener;

    public TareasHomeAdapter(List<Tarea> lista, OnTareaCheckedListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    public void actualizarTareas(List<Tarea> nuevaLista) {
        this.lista = nuevaLista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tarea_home, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tarea tarea = lista.get(position);
        holder.tvTitulo.setText(tarea.getTitulo());

        // Si fue sugerida/desglosada por IA mostramos un detalle visual
        if (tarea.isCreadaPorIA()) {
            holder.tvPrioridad.setText("Asistida por IA");
        } else {
            holder.tvPrioridad.setText("Tarea cotidiana");
        }

        holder.cbCompletada.setOnCheckedChangeListener(null);
        holder.cbCompletada.setChecked(tarea.isCompletada());

        holder.cbCompletada.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onTareaChecked(tarea, isChecked, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return lista != null ? lista.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbCompletada;
        TextView tvTitulo, tvPrioridad;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cbCompletada = itemView.findViewById(R.id.cbTareaCompletada);
            tvTitulo = itemView.findViewById(R.id.tvTituloTarea);
            tvPrioridad = itemView.findViewById(R.id.tvPrioridadTarea);
        }
    }
}