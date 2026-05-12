package upmarts.modelo;

import java.util.List;

public abstract class MiembroUPM extends Participante implements IMiembroUPM {

    private String rolUPM;

    public MiembroUPM(String nombreUsuario, String nombreCompleto, String correoElectronico,
                      String password, String dni, String tarjeta, String rolUPM,
                      List<PreferenciaArtistica> preferenciasArtisticas) {
        super(nombreUsuario, nombreCompleto, correoElectronico, password, dni, tarjeta, preferenciasArtisticas);
        this.rolUPM = rolUPM;
    }

    public String getRolUPM() {
        return rolUPM;
    }

    public void setRolUPM(String rolUPM) {
        this.rolUPM = rolUPM;
    }
}
