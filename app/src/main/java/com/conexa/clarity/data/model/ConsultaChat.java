package com.conexa.clarity.data.model;

public class ConsultaChat {

    private final long idConsulta;
    private final int idUsuario;
    private final String consulta;
    private final String respuesta;
    private final Integer feedback;
    private final String fecha;

    public ConsultaChat(
            long idConsulta,
            int idUsuario,
            String consulta,
            String respuesta,
            Integer feedback,
            String fecha
    ) {
        this.idConsulta = idConsulta;
        this.idUsuario = idUsuario;
        this.consulta = consulta;
        this.respuesta = respuesta;
        this.feedback = feedback;
        this.fecha = fecha;
    }

    public long getIdConsulta() {
        return idConsulta;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public String getConsulta() {
        return consulta;
    }

    public String getRespuesta() {
        return respuesta;
    }

    public Integer getFeedback() {
        return feedback;
    }

    public String getFecha() {
        return fecha;
    }
}
