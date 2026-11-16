package com.conexa.clarity.ui.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.conexa.clarity.R;
import com.conexa.clarity.data.model.ConsultaChat;

import java.util.List;

public class HistorialChatAdapter
        extends RecyclerView.Adapter<
        HistorialChatAdapter.HistorialViewHolder> {

    public interface OnHistorialClickListener {

        void onHistorialClick(
                ConsultaChat consulta
        );

        void onHistorialLongClick(
                ConsultaChat consulta,
                int posicion
        );
    }

    private final List<ConsultaChat> historial;
    private final OnHistorialClickListener listener;

    public HistorialChatAdapter(
            List<ConsultaChat> historial,
            OnHistorialClickListener listener
    ) {
        this.historial = historial;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HistorialViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View vista =
                LayoutInflater.from(
                        parent.getContext()
                ).inflate(
                        R.layout.item_historial_chat,
                        parent,
                        false
                );

        return new HistorialViewHolder(
                vista
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull HistorialViewHolder holder,
            int position
    ) {

        ConsultaChat elemento =
                historial.get(position);

        holder.tvConsultaHistorial.setText(
                elemento.getConsulta()
        );

        holder.tvRespuestaHistorial.setText(
                elemento.getRespuesta()
        );

        holder.tvFechaHistorial.setText(
                elemento.getFecha()
        );

        holder.itemView.setOnClickListener(
                view -> {

                    int posicionActual =
                            holder.getAdapterPosition();

                    if (posicionActual
                            == RecyclerView.NO_POSITION) {

                        return;
                    }

                    listener.onHistorialClick(
                            historial.get(
                                    posicionActual
                            )
                    );
                }
        );

        holder.itemView.setOnLongClickListener(
                view -> {

                    int posicionActual =
                            holder.getAdapterPosition();

                    if (posicionActual
                            == RecyclerView.NO_POSITION) {

                        return false;
                    }

                    listener.onHistorialLongClick(
                            historial.get(
                                    posicionActual
                            ),
                            posicionActual
                    );

                    return true;
                }
        );
    }

    @Override
    public int getItemCount() {
        return historial.size();
    }

    public void eliminarElemento(
            int posicion
    ) {

        if (posicion < 0
                || posicion >= historial.size()) {

            return;
        }

        historial.remove(
                posicion
        );

        notifyItemRemoved(
                posicion
        );
    }

    static class HistorialViewHolder
            extends RecyclerView.ViewHolder {

        private final TextView tvConsultaHistorial;
        private final TextView tvRespuestaHistorial;
        private final TextView tvFechaHistorial;

        public HistorialViewHolder(
                @NonNull View itemView
        ) {
            super(itemView);

            tvConsultaHistorial =
                    itemView.findViewById(
                            R.id.tvConsultaHistorial
                    );

            tvRespuestaHistorial =
                    itemView.findViewById(
                            R.id.tvRespuestaHistorial
                    );

            tvFechaHistorial =
                    itemView.findViewById(
                            R.id.tvFechaHistorial
                    );
        }
    }
}