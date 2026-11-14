package com.conexa.clarity.ui.tareas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.conexa.clarity.R;
import com.conexa.clarity.data.local.TareaDao;
import com.conexa.clarity.data.model.Tarea;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TareasAdapter extends RecyclerView.Adapter<TareasAdapter.TareaViewHolder> {
    public interface OnTareaClickListener {
        void onTareaClick(Tarea tarea);

        void onTareaCheckChange(Tarea tarea, boolean isChecked);
    }

    private List<Tarea> listaTareas = new ArrayList<>();
    private final TareaDao tareaDao;
    private final OnTareaClickListener listener;

    public TareasAdapter(TareaDao tareaDao, OnTareaClickListener listener) {
        this.tareaDao = tareaDao;
        this.listener = listener;
    }

    public void setTareas(List<Tarea> nuevasTareas) {
        this.listaTareas = nuevasTareas != null ? nuevasTareas : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TareaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tarea, parent, false);
        return new TareaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TareaViewHolder holder, int position) {
        Tarea tarea = listaTareas.get(position);
        holder.bind(tarea);
    }

    @Override
    public int getItemCount() {
        return listaTareas.size();
    }

    class TareaViewHolder extends RecyclerView.ViewHolder {
        private final CheckBox cbCompletada;
        private final TextView tvTitulo;
        private final TextView tvDescripcion;
        private final TextView tvPrioridad;
        private final TextView tvFechaVencimiento;
        private final View layoutProgreso;
        private final TextView tvProgresoTexto;
        private final ProgressBar pbSubtareas;

        private final TextView tvToggleMicroPasos;
        private final LinearLayout contenedorMicroPasos;

        public TareaViewHolder(@NonNull View itemView) {
            super(itemView);
            cbCompletada = itemView.findViewById(R.id.cbCompletada);
            tvTitulo = itemView.findViewById(R.id.tvTituloTarea);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcionTarea);
            tvPrioridad = itemView.findViewById(R.id.tvPrioridadBadge);
            tvFechaVencimiento = itemView.findViewById(R.id.tvFechaVencimiento);
            layoutProgreso = itemView.findViewById(R.id.layoutProgresoSubtareas);
            tvProgresoTexto = itemView.findViewById(R.id.tvProgresoTexto);
            pbSubtareas = itemView.findViewById(R.id.pbSubtareas);

            tvToggleMicroPasos = itemView.findViewById(R.id.tvToggleMicroPasos);
            contenedorMicroPasos = itemView.findViewById(R.id.contenedorMicroPasos);
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
            if (tarea.getFechaVencimiento() != null && !tarea.getFechaVencimiento().trim().isEmpty()) {
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
            // Barra de progreso segun micro-pasos completado y desplegable
            int totalSubtareas = tareaDao.contarSubtareas(tarea.getIdTarea());
            if (totalSubtareas > 0) {
                layoutProgreso.setVisibility(View.VISIBLE);
                actualizarProgresoSubtareas(tarea);
                // Estado inicial del desplegable
                contenedorMicroPasos.setVisibility(View.GONE);
                tvToggleMicroPasos.setText("Ver micro-pasos ▼");

                tvToggleMicroPasos.setOnClickListener(v -> {
                    if (contenedorMicroPasos.getVisibility() == View.VISIBLE) {
                        contenedorMicroPasos.setVisibility(View.GONE);
                        tvToggleMicroPasos.setText("Ver micro-pasos ▼");
                    } else {
                        contenedorMicroPasos.setVisibility(View.VISIBLE);
                        tvToggleMicroPasos.setText("Ocultar micro-pasos ▲");
                        cargarMicroPasosDinamicos(tarea);
                    }
                });
            } else {
                layoutProgreso.setVisibility(View.GONE);
                contenedorMicroPasos.setVisibility(View.GONE);
            }
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTareaClick(tarea);
                }
            });
        }



            private void configurarPrioridad (String prioridad){
                if (prioridad == null) prioridad = Tarea.PRIORIDAD_MEDIA;
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


            private void actualizarProgresoSubtareas(Tarea tareaPadre) {
                int total = tareaDao.contarSubtareas(tareaPadre.getIdTarea());
                int completadas = tareaDao.contarSubtareasCompletadas(tareaPadre.getIdTarea());
                int porcentaje = total > 0 ? (int) (((float) completadas / total) * 100) : 0;

                pbSubtareas.setProgress(porcentaje);
                tvProgresoTexto.setText("Micro-pasos: " + completadas + "/" + total + " (" + porcentaje + "%)");
            }

            private void cargarMicroPasosDinamicos(Tarea tareaPadre) {
                contenedorMicroPasos.removeAllViews();
                List<Tarea> subList = tareaDao.obtenerSubtareas(tareaPadre.getIdTarea());

                for (Tarea sub : subList) {
                    CheckBox cb = new CheckBox(itemView.getContext());
                    cb.setText(sub.getTitulo());
                    cb.setTextSize(13);
                    cb.setTextColor(Color.parseColor("#4A4A4A"));
                    cb.setChecked(sub.esCompletada());

                    if (sub.esCompletada()) {
                        cb.setPaintFlags(cb.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    }

                    cb.setOnCheckedChangeListener((btn, isChecked) -> {
                        String fecha = isChecked
                                ? new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date())
                                : null;
                        String nuevoEstado = isChecked ? Tarea.ESTADO_COMPLETADA : Tarea.ESTADO_PENDIENTE;

                        tareaDao.cambiarEstado(sub.getIdTarea(), nuevoEstado, fecha);
                        sub.setEstado(nuevoEstado);

                        if (isChecked) {
                            cb.setPaintFlags(cb.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        } else {
                            cb.setPaintFlags(cb.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                        }

                        actualizarProgresoSubtareas(tareaPadre);

                        // Si todos los micro-pasos se completaron, marcar la tarea principal
                        int total = tareaDao.contarSubtareas(tareaPadre.getIdTarea());
                        int completadas = tareaDao.contarSubtareasCompletadas(tareaPadre.getIdTarea());
                        if (total > 0 && completadas == total && !cbCompletada.isChecked()) {
                            cbCompletada.setChecked(true);
                        }
                    });

                    contenedorMicroPasos.addView(cb);
                }
            }

            private void aplicarEfectoTachado ( boolean completada){
                if (completada) {
                    tvTitulo.setPaintFlags(tvTitulo.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    tvTitulo.setAlpha(0.5f);
                } else {
                    tvTitulo.setPaintFlags(tvTitulo.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    tvTitulo.setAlpha(1.0f);
                }
            }
        }
    }
