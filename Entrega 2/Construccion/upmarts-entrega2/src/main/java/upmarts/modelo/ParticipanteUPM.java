package upmarts.modelo;

import java.util.List;

public abstract class ParticipanteUPM extends ParticipanteExterno {

    public ParticipanteUPM(String nombreUsuario, String nombreCompleto, String correoElectronico,
                           String password, String dni, String tarjeta,
                           List<PreferenciaArtistica> preferenciasArtisticas) {
        super(nombreUsuario, nombreCompleto, correoElectronico, password, dni, tarjeta, preferenciasArtisticas);
    }
}
