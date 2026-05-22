package upmarts.modelo;

import java.util.List;

public class PersonalUPM extends ParticipanteUPM {

    private int antiguedad;

    public PersonalUPM(String nombreUsuario, String nombreCompleto, String correoElectronico,
                       String password, String dni, String tarjeta, int antiguedad,
                       List<PreferenciaArtistica> preferenciasArtisticas) {
        super(nombreUsuario, nombreCompleto, correoElectronico, password, dni, tarjeta, preferenciasArtisticas);
        this.antiguedad = antiguedad;
    }

    public int getAntiguedad() {
        return antiguedad;
    }

    public void setAntiguedad(int antiguedad) {
        this.antiguedad = antiguedad;
    }

    @Override
    public RolUsuario getRol() {
        return RolUsuario.PERSONAL_UPM;
    }
}
