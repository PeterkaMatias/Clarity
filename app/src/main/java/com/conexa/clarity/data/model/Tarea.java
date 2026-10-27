package com.conexa.clarity.data.model;

import java.io.Serializable;


public class Tarea implements Serializable {
    public static final String ESTADO_PENDIENTE = "PENDIENTE";
    public static final String ESTADO_EN_PROGRESO = "EN_PROGRESO";
    public static final String ESTADO_COMPLETADA = "COMPLETADA";
    public static final String PRIORIDAD_ALTA = "ALTA";
    public static final String PRIORIDAD_MEDIA = "MEDIA";
    public static final String PRIORIDAD_BAJA = "BAJA";
    private int idTarea;
    private int idUsuario;
    private String titulo;
    private String descripcion;
    private String estado;
    private String prioridad;
    private String fechaCreacion;
    private String fechaFinalizado;
    private String fechaVencimiento;
    private int idTareaPadre; // 0 = tarea principal, >0 = micro-paso
    public Tarea() {}
    public Tarea(int idUsuario, String titulo, String descripcion,
                 String estado, String prioridad,
                 String fechaCreacion, String fechaFinalizado, String fechaVencimiento,
                 int idTareaPadre) {
        this.idUsuario   = idUsuario;
        this.titulo      = titulo;
        this.descripcion = descripcion;
        this.estado      = estado;
        this.prioridad   = prioridad;
        this.fechaCreacion = fechaCreacion;
        this.fechaFinalizado = fechaFinalizado;
        this.fechaVencimiento = fechaVencimiento;
        this.idTareaPadre = idTareaPadre;
    }
    //Getters
    public int getIdTarea() { return idTarea; }
    public int getIdUsuario() { return idUsuario; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }

    public String getEstado() { return estado; }
    public String getPrioridad() { return prioridad; }
    public String getFechaCreacion() { return fechaCreacion; }
    public String getFechaFinalizado() { return fechaFinalizado; }
    public String getFechaVencimiento() { return fechaVencimiento; }
    public int getIdTareaPadre() { return idTareaPadre; }

    //Setters
    public void setIdTarea(int idTarea) { this.idTarea = idTarea; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setPrioridad(String prioridad) { this.prioridad = prioridad; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public void setFechaFinalizado(String fechaFinalizado) { this.fechaFinalizado = fechaFinalizado; }
    public void setFechaVencimiento(String fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    public void setIdTareaPadre(int idTareaPadre) { this.idTareaPadre = idTareaPadre; }

    // Helpers de conveniencia
    public boolean esMicroPaso() { return idTareaPadre > 0; }
    public boolean esCompletada() { return ESTADO_COMPLETADA.equals(estado); }

    // ===== Compatibilidad con el Dashboard puente (MainActivity / TareasHomeAdapter) =====

    public int getId() { return idTarea; }

    public boolean isCompletada() { return esCompletada(); }

    public void setCompletada(boolean completada) {
        this.estado = completada ? ESTADO_COMPLETADA : ESTADO_PENDIENTE;
    }

    /** Los micro-pasos son los que genera el desglose con IA */
    public boolean isCreadaPorIA() { return esMicroPaso(); }

}