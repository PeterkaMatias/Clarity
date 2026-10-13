package com.conexa.clarity.ui.resources;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.conexa.clarity.R;
import com.conexa.clarity.data.model.Recurso;

import java.util.List;

public class RecursosAdapter extends RecyclerView.Adapter<RecursosAdapter.ViewHolder> {

    public interface OnRecursoClickListener {
        void onRecursoClick(Recurso recurso);
        void onFavoritoClick(Recurso recurso, int position);
    }

    private List<Recurso> lista;
    private OnRecursoClickListener listener;

    public RecursosAdapter(List<Recurso> lista, OnRecursoClickListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    public void actualizarLista(List<Recurso> nuevaLista) {
        this.lista = nuevaLista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recurso, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recurso item = lista.get(position);
        holder.tvTitulo.setText(item.getTitulo());
        holder.tvResumen.setText(item.getContenido());
        holder.tvCategoria.setText(item.getCategoria().replace("_", " "));
        holder.tvTiempoLectura.setText("⏱ Lectura de " + item.getTiempoLecturaMin() + " min");

        holder.btnFavorito.setImageResource(item.isEsFavorito() ?
                android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);

        holder.itemView.setOnClickListener(v -> listener.onRecursoClick(item));
        holder.btnFavorito.setOnClickListener(v -> listener.onFavoritoClick(item, position));
    }

    @Override
    public int getItemCount() {
        return lista != null ? lista.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitulo, tvResumen, tvCategoria, tvTiempoLectura;
        ImageButton btnFavorito;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvResumen = itemView.findViewById(R.id.tvResumen);
            tvCategoria = itemView.findViewById(R.id.tvCategoria);
            tvTiempoLectura = itemView.findViewById(R.id.tvTiempoLectura);
            btnFavorito = itemView.findViewById(R.id.btnFavorito);
        }
    }
}