package com.conexa.clarity.ui.tareas;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.conexa.clarity.R;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
public class MicroPasosPreliminarAdapter extends RecyclerView.Adapter<MicroPasosPreliminarAdapter.PasoViewHolder> {

    private final List<String> pasos = new ArrayList<>();
    public void setPasos(List<String> nuevosPasos) {
        this.pasos.clear();
        if (nuevosPasos != null){
            this.pasos.addAll(nuevosPasos);
        }
        notifyDataSetChanged();
    }

    public List<String> getPasos() {
        return pasos;
    }

    public void agregarPaso(String texto) {
        pasos.add(texto);
        notifyItemInserted(pasos.size() - 1);
    }

    @NonNull
    @Override
    public PasoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_micropaso_preliminar, parent, false);
        return new PasoViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull PasoViewHolder holder, int position){
        holder.bind(position);
    }
    @Override
    public int getItemCount() {
        return pasos.size();

    }

    class PasoViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvNumero;
        private final TextInputEditText etTexto;
        private final ImageButton btnEliminar;

        public PasoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumero = itemView.findViewById(R.id.tvNumeroPaso);
            etTexto = itemView.findViewById(R.id.etTextoMicroPaso);
            btnEliminar = itemView.findViewById(R.id.btnEliminarPaso);

        }

        public void bind(int position) {
            tvNumero.setText(String.valueOf(position + 1));
            etTexto.setText(pasos.get(position));

            // Actualzar la lista en memoria cuando el usuario edita el texto
            etTexto.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override

                public void afterTextChanged(Editable s) {
                    int posActual = getAdapterPosition();
                    if (posActual != RecyclerView.NO_POSITION && posActual < pasos.size()) {
                        pasos.set(posActual, s.toString());
                    }
                }

            });

            //Borrar el paso
            btnEliminar.setOnClickListener(V -> {
                int posActual = getAdapterPosition();
                if (posActual != RecyclerView.NO_POSITION && posActual < pasos.size()) {
                    pasos.remove(posActual);
                    notifyItemRemoved(posActual);
                    notifyItemRangeChanged(posActual, pasos.size());
                }

            });
        }
    }
}
