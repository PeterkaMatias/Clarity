package com.conexa.clarity.ui.logros;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.conexa.clarity.MainActivity;
import com.conexa.clarity.R;
import com.conexa.clarity.data.local.TareaDao;
import com.conexa.clarity.data.local.TemporizadorDao;
import com.conexa.clarity.ui.chat.ChatActivity;
import com.conexa.clarity.ui.resources.RecursosActivity;
import com.conexa.clarity.ui.timer.FocusTimerActivity;
import com.conexa.clarity.util.SesionUsuario;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class LogrosyProgreso extends AppCompatActivity {
    private BottomNavigationView bottomNav;
    private MaterialButton botonDia;
    private MaterialButton botonSemana;
    private MaterialButton botonMes;
    private MaterialButton botonAnio;
    private TextView tvTareasRealizadas;
    private TextView tvMinutosRealizados;
    private TareaDao tareaDao;
    private TemporizadorDao temporizadorDao;
    private int idUsuarioActual;
    private LinearLayout contenedorGrafico;
    private TextView tvSubtituloActividad;
    private TextView tvRachaActual;
    private TextView tvComparacionTareas;
    private TextView tvComparacionMinutos;
    private TextView tvComparacionRacha;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progresoylogros);

        idUsuarioActual = SesionUsuario.obtenerId(this);

        bottomNav = findViewById(R.id.bottomNavigationView);
        botonDia = findViewById(R.id.botonDia);
        botonMes = findViewById(R.id.botonMes);
        botonSemana = findViewById(R.id.botonSemana);
        botonAnio = findViewById(R.id.botonAnio);
        contenedorGrafico = findViewById(R.id.contenedorGrafico);
        tvSubtituloActividad = findViewById(R.id.tvSubtituloActividad);
        tvTareasRealizadas = findViewById(R.id.tvTareasRealizadas);
        tvMinutosRealizados = findViewById(R.id.tvMinutosRealizados);
        tvRachaActual = findViewById(R.id.tvRachaActual);
        tvComparacionTareas = findViewById(R.id.tvComparacionTareas);
        tvComparacionMinutos = findViewById(R.id.tvComparacionMinutos);
        tvComparacionRacha = findViewById(R.id.tvComparacionRacha);

        tareaDao = new TareaDao(this);
        temporizadorDao = new TemporizadorDao(this);

        botonDia.setOnClickListener(v -> seleccionarPeriodo(botonDia,"DIA"));
        botonSemana.setOnClickListener(v -> seleccionarPeriodo(botonSemana,"SEMANA"));
        botonMes.setOnClickListener(v -> seleccionarPeriodo(botonMes,"MES"));
        botonAnio.setOnClickListener(v -> seleccionarPeriodo(botonAnio, "ANIO"));

        seleccionarPeriodo(botonDia, "DIA");
        configurarNavegacion();
    }
    private void actualizarBotonSeleccionado(MaterialButton seleccionado) {

        MaterialButton[] botones = {
                botonDia, botonSemana, botonMes, botonAnio
        };

        for (MaterialButton boton : botones) {

            boolean estaSeleccionado = boton == seleccionado;

            int colorFondo = estaSeleccionado
                    ? R.color.verde_principal
                    : R.color.verde_secundario;

            int colorTexto = estaSeleccionado
                    ? R.color.blanco_superficie
                    : R.color.gris_texto;

            boton.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(
                            ContextCompat.getColor(this, colorFondo)
                    )
            );

            boton.setTextColor(ContextCompat.getColor(this, colorTexto));
        }
    }
    private void seleccionarPeriodo(MaterialButton seleccionado, String periodo){
        actualizarBotonSeleccionado(seleccionado);
        actualizarEstadisticas(periodo);
    }
    private static class DatosGrafico {
        List<String> etiquetas = new ArrayList<>();
        List<Integer> tareas = new ArrayList<>();
        List<Integer> minutos = new ArrayList<>();
    }
    private void actualizarGrafico(
            String periodo,
            Calendar inicio,
            Map<String, Integer> tareasPorFecha,
            Map<String, Integer> minutosPorFecha,
            Map<String, Integer> tareasPorHora,
            Map<String, Integer> minutosPorHora) {

        DatosGrafico datos = obtenerDatosGrafico(
                periodo, inicio,
                tareasPorFecha, minutosPorFecha,
                tareasPorHora, minutosPorHora
        );

        crearGrafico(datos);
    }
    private void crearGrafico(DatosGrafico datos) {

        int maxTareas = 1;
        int maxMinutos = 1;

        for (int valor : datos.tareas) {
            maxTareas = Math.max(maxTareas, valor);
        }

        for (int valor : datos.minutos) {
            maxMinutos = Math.max(maxMinutos, valor);
        }

        contenedorGrafico.removeAllViews();
        contenedorGrafico.setWeightSum(datos.etiquetas.size());

        for (int i = 0; i < datos.etiquetas.size(); i++) {

            LinearLayout grupo = new LinearLayout(this);
            grupo.setOrientation(LinearLayout.VERTICAL);
            grupo.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);

            LinearLayout barras = new LinearLayout(this);
            barras.setOrientation(LinearLayout.HORIZONTAL);
            barras.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);

            barras.addView(crearBarra(
                    datos.minutos.get(i), maxMinutos, R.color.verde_principal));

            barras.addView(crearBarra(
                    datos.tareas.get(i), maxTareas, R.color.verde_secundario));

            grupo.addView(barras);

            TextView etiqueta = new TextView(this);
            etiqueta.setText(datos.etiquetas.get(i));
            etiqueta.setTextSize(10);
            etiqueta.setTextColor(
                    ContextCompat.getColor(this, R.color.gris_texto));
            etiqueta.setGravity(Gravity.CENTER);

            grupo.addView(etiqueta);

            LinearLayout.LayoutParams parametros =
                    new LinearLayout.LayoutParams(0, dp(180), 1);

            contenedorGrafico.addView(grupo, parametros);
        }
    }
    private DatosGrafico obtenerDatosGrafico(
            String periodo,
            Calendar inicio,
            Map<String, Integer> tareasPorFecha,
            Map<String, Integer> minutosPorFecha,
            Map<String, Integer> tareasPorHora,
            Map<String, Integer> minutosPorHora) {

        DatosGrafico datos = new DatosGrafico();

        SimpleDateFormat formatoFecha =
                new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        switch (periodo) {

            case "DIA":

                for (int bloque = 0; bloque < 6; bloque++) {
                    int horaInicio = bloque * 4;
                    int totalTareas = 0;
                    int totalMinutos = 0;

                    for (int hora = horaInicio; hora < horaInicio + 4; hora++) {
                        String clave = String.format(Locale.getDefault(), "%02d", hora);

                        totalTareas += obtenerValor(tareasPorHora, clave);
                        totalMinutos += obtenerValor(minutosPorHora, clave);
                    }

                    datos.etiquetas.add(String.format(
                            Locale.getDefault(), "%02d h", horaInicio));

                    datos.tareas.add(totalTareas);
                    datos.minutos.add(totalMinutos);
                }

                tvSubtituloActividad.setText("Actividad por bloques de 4 horas");
                break;

            case "SEMANA":

                for (int i = 0; i < 7; i++) {
                    Calendar dia = (Calendar) inicio.clone();
                    dia.add(Calendar.DAY_OF_MONTH, i);

                    String fecha = formatoFecha.format(dia.getTime());

                    datos.etiquetas.add(new SimpleDateFormat(
                            "EEE", new Locale("es", "AR"))
                            .format(dia.getTime()));

                    datos.tareas.add(obtenerValor(tareasPorFecha, fecha));
                    datos.minutos.add(obtenerValor(minutosPorFecha, fecha));
                }

                tvSubtituloActividad.setText("Tu ritmo, día a día");
                break;

            case "MES":

                int ultimoDia = inicio.getActualMaximum(Calendar.DAY_OF_MONTH);

                for (int desde = 1; desde <= ultimoDia; desde += 7) {
                    int hasta = Math.min(desde + 6, ultimoDia);
                    int totalTareas = 0;
                    int totalMinutos = 0;

                    for (int diaNumero = desde; diaNumero <= hasta; diaNumero++) {
                        Calendar dia = (Calendar) inicio.clone();
                        dia.set(Calendar.DAY_OF_MONTH, diaNumero);

                        String fecha = formatoFecha.format(dia.getTime());

                        totalTareas += obtenerValor(tareasPorFecha, fecha);
                        totalMinutos += obtenerValor(minutosPorFecha, fecha);
                    }

                    datos.etiquetas.add(desde + "-" + hasta);
                    datos.tareas.add(totalTareas);
                    datos.minutos.add(totalMinutos);
                }

                tvSubtituloActividad.setText("Actividad por semana del mes");
                break;

            case "ANIO":

                int[] tareasMes = new int[12];
                int[] minutosMes = new int[12];

                for (Map.Entry<String, Integer> entrada : tareasPorFecha.entrySet()) {
                    int mes = Integer.parseInt(entrada.getKey().substring(5, 7)) - 1;
                    tareasMes[mes] += entrada.getValue();
                }

                for (Map.Entry<String, Integer> entrada : minutosPorFecha.entrySet()) {
                    int mes = Integer.parseInt(entrada.getKey().substring(5, 7)) - 1;
                    minutosMes[mes] += entrada.getValue();
                }

                String[] nombresMeses = {
                        "Ene", "Feb", "Mar", "Abr", "May", "Jun",
                        "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"
                };

                for (int i = 0; i < 12; i++) {
                    datos.etiquetas.add(nombresMeses[i]);
                    datos.tareas.add(tareasMes[i]);
                    datos.minutos.add(minutosMes[i]);
                }

                tvSubtituloActividad.setText("Tu actividad durante el año");
                break;
        }

        return datos;
    }
    private int obtenerValor(Map<String, Integer> datos, String clave) {
        return datos.getOrDefault(clave, 0);
    }
    private Calendar[] obtenerRangoPeriodo(String periodo) {

        Calendar inicio = Calendar.getInstance();
        Calendar fin = Calendar.getInstance();

        switch (periodo) {
            case "DIA":
                break;

            case "SEMANA":
                inicio.setFirstDayOfWeek(Calendar.MONDAY);
                inicio.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                break;

            case "MES":
                inicio.set(Calendar.DAY_OF_MONTH, 1);
                break;

            case "ANIO":
                inicio.set(Calendar.DAY_OF_YEAR, 1);
                break;

            default:
                throw new IllegalArgumentException("Período no válido: " + periodo);
        }

        // Establecemos el inicio del período a las 00:00.
        inicio.set(Calendar.HOUR_OF_DAY, 0);
        inicio.set(Calendar.MINUTE, 0);
        inicio.set(Calendar.SECOND, 0);
        inicio.set(Calendar.MILLISECOND, 0);

        // El final es el comienzo del siguiente período.
        fin.setTimeInMillis(inicio.getTimeInMillis());

        switch (periodo) {
            case "DIA":
                fin.add(Calendar.DAY_OF_MONTH, 1);
                break;

            case "SEMANA":
                fin.add(Calendar.WEEK_OF_YEAR, 1);
                break;

            case "MES":
                fin.add(Calendar.MONTH, 1);
                break;

            case "ANIO":
                fin.add(Calendar.YEAR, 1);
                break;
        }

        return new Calendar[]{inicio, fin};
    }
    private void actualizarEstadisticas(String periodo) {

        Calendar[] rango = obtenerRangoPeriodo(periodo);

        Calendar inicio = rango[0];
        Calendar fin = rango[1];

        SimpleDateFormat formato =
                new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        String fechaDesde = formato.format(inicio.getTime());
        String fechaHasta = formato.format(fin.getTime());

        int tareas = tareaDao.filtrarTareaPorPeriodo(
                idUsuarioActual, fechaDesde, fechaHasta);

        int minutos = temporizadorDao.sumarMinutosPorPeriodo(
                idUsuarioActual, fechaDesde, fechaHasta);

        tvTareasRealizadas.setText(String.valueOf(tareas));
        tvMinutosRealizados.setText(String.valueOf(minutos));

        actualizarComparaciones(periodo, inicio, fin);
        actualizarRacha();

        Map<String, Integer> tareasPorFecha =
                tareaDao.contarTareasPorFechas(
                        idUsuarioActual, fechaDesde, fechaHasta);

        Map<String, Integer> minutosPorFecha =
                temporizadorDao.sumarMinutosPorFecha(
                        idUsuarioActual, fechaDesde, fechaHasta);

        Map<String, Integer> tareasPorHora = new HashMap<>();
        Map<String, Integer> minutosPorHora = new HashMap<>();

        if (periodo.equals("DIA")) {

            tareasPorHora = tareaDao.contarTareasPorHora(
                    idUsuarioActual, fechaDesde, fechaHasta);

            minutosPorHora = temporizadorDao.sumarMinutosPorHora(
                    idUsuarioActual, fechaDesde, fechaHasta);
        }

        actualizarGrafico(
                periodo,
                inicio,
                tareasPorFecha,
                minutosPorFecha,
                tareasPorHora,
                minutosPorHora
        );
    }

    private int dp(int valor) {
        return (int) (valor * getResources().getDisplayMetrics().density + 0.5f);
    }
    private void actualizarComparaciones(
            String periodo, Calendar inicio, Calendar fin) {

        Calendar inicioAnterior = (Calendar) inicio.clone();
        Calendar finAnterior = (Calendar) fin.clone();

        switch (periodo) {
            case "DIA":
                inicioAnterior.add(Calendar.DAY_OF_MONTH, -1);
                finAnterior.add(Calendar.DAY_OF_MONTH, -1);
                break;

            case "SEMANA":
                inicioAnterior.add(Calendar.WEEK_OF_YEAR, -1);
                finAnterior.add(Calendar.WEEK_OF_YEAR, -1);
                break;

            case "MES":
                inicioAnterior.add(Calendar.MONTH, -1);
                finAnterior.add(Calendar.MONTH, -1);
                break;

            case "ANIO":
                inicioAnterior.add(Calendar.YEAR, -1);
                finAnterior.add(Calendar.YEAR, -1);
                break;
        }

        SimpleDateFormat formato =
                new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        String desde = formato.format(inicioAnterior.getTime());
        String hasta = formato.format(finAnterior.getTime());

        int tareasAnteriores = tareaDao.filtrarTareaPorPeriodo(
                idUsuarioActual, desde, hasta);

        int minutosAnteriores = temporizadorDao.sumarMinutosPorPeriodo(
                idUsuarioActual, desde, hasta);

        int tareasActuales = Integer.parseInt(
                tvTareasRealizadas.getText().toString());

        int minutosActuales = Integer.parseInt(
                tvMinutosRealizados.getText().toString());

        String referencia;

        switch (periodo) {
            case "DIA":
                referencia = "día anterior";
                break;
            case "SEMANA":
                referencia = "semana anterior";
                break;
            case "MES":
                referencia = "mes anterior";
                break;
            default:
                referencia = "año anterior";
                break;
        }

        tvComparacionTareas.setText(
                obtenerTextoComparacion(tareasActuales, tareasAnteriores, referencia));

        tvComparacionMinutos.setText(
                obtenerTextoComparacion(minutosActuales, minutosAnteriores, referencia));
    }

    private String obtenerTextoComparacion(
            int actual, int anterior, String referencia) {

        int diferencia = actual - anterior;

        if (diferencia > 0) {
            return "+" + diferencia + " vs. " + referencia;
        }

        if (diferencia < 0) {
            return diferencia + " vs. " + referencia;
        }

        return "Sin cambios vs. " + referencia;
    }

    @SuppressLint("SetTextI18n")
    private void actualizarRacha() {

        List<String> fechas = tareaDao.obtenerFechasCompletadas(idUsuarioActual);

        DateTimeFormatter formato = DateTimeFormatter.ISO_LOCAL_DATE;

        List<LocalDate> diasCompletados = new ArrayList<>();

        for (String fecha : fechas) {
            diasCompletados.add(LocalDate.parse(fecha, formato));
        }

        LocalDate hoy = LocalDate.now();
        LocalDate ayer = hoy.minusDays(1);

        int rachaActual = 0;

        if (!diasCompletados.isEmpty()) {

            LocalDate ultimoDia = diasCompletados.get(diasCompletados.size() - 1);

            if (ultimoDia.equals(hoy) || ultimoDia.equals(ayer)) {

                LocalDate diaBuscado = ultimoDia;

                for (int i = diasCompletados.size() - 1; i >= 0; i--) {

                    LocalDate fecha = diasCompletados.get(i);

                    if (fecha.equals(diaBuscado)) {
                        rachaActual++;
                        diaBuscado = diaBuscado.minusDays(1);
                    } else if (fecha.isBefore(diaBuscado)) {
                        break;
                    }
                }
            }
        }

        int mejorRacha = 0;
        int rachaTemporal = 0;
        LocalDate diaAnterior = null;

        for (LocalDate fecha : diasCompletados) {

            if (diaAnterior != null && fecha.equals(diaAnterior.plusDays(1))) {
                rachaTemporal++;
            } else {
                rachaTemporal = 1;
            }

            mejorRacha = Math.max(mejorRacha, rachaTemporal);
            diaAnterior = fecha;
        }

        tvRachaActual.setText(String.valueOf(rachaActual));

        tvComparacionRacha.setText(
                "Mejor racha: " + mejorRacha +
                        (mejorRacha == 1 ? " día" : " días")
        );
    }
    private View crearBarra(int valor, int maximo, int color) {

        LinearLayout contenedor = new LinearLayout(this);
        contenedor.setOrientation(LinearLayout.VERTICAL);
        contenedor.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);

        int alturaMaxima = 160;
        int altura = maximo > 0
                ? Math.max(4, (valor * alturaMaxima) / maximo)
                : 4;

        View barra = new View(this);
        barra.setBackgroundColor(ContextCompat.getColor(this, color));

        LinearLayout.LayoutParams parametros =
                new LinearLayout.LayoutParams(
                        dp(12),
                        dp(altura)
                );

        contenedor.addView(barra, parametros);

        return contenedor;
    }
    private void configurarNavegacion() {
        if (bottomNav == null) {
            return;
        }

        bottomNav.setSelectedItemId(R.id.nav_enfoque);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_enfoque) {
                abrirPantalla(FocusTimerActivity.class);
                return true;
            }

            if (itemId == R.id.nav_inicio) {
                abrirPantalla(MainActivity.class);
                return true;
            }

            if (itemId == R.id.nav_chat) {
                abrirPantalla(ChatActivity.class);
                return true;
            }

            if (itemId == R.id.nav_recursos) {
                abrirPantalla(RecursosActivity.class);
                return true;
            }

            return false;
        });
    }
    private void abrirPantalla(Class<?> pantallaDestino) {
        Intent intent = new Intent(this, pantallaDestino);
        intent.putExtra("id_usuario", idUsuarioActual);

        startActivity(intent);

        overridePendingTransition(
                android.R.anim.fade_in,
                android.R.anim.fade_out
        );

        finish();
    }

}
