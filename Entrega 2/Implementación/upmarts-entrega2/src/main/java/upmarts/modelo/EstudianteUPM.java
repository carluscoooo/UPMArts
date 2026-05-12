package upmarts.modelo;

import java.util.List;

public class EstudianteUPM extends MiembroUPM {

    private String numeroMatricula;

    public EstudianteUPM(String nombreUsuario, String nombreCompleto, String correoElectronico,
                         String password, String dni, String tarjeta, String numeroMatricula,
                         List<PreferenciaArtistica> preferenciasArtisticas) {
        super(nombreUsuario, nombreCompleto, correoElectronico, password, dni, tarjeta,
                "ESTUDIANTE", preferenciasArtisticas);
        this.numeroMatricula = numeroMatricula;
    }

    public String getNumeroMatricula() {
        return numeroMatricula;
    }

    public void setNumeroMatricula(String numeroMatricula) {
        this.numeroMatricula = numeroMatricula;
    }

    @Override
    public String getRolSistema() {
        return "ESTUDIANTE UPM";
    }
}
