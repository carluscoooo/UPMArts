package upmarts.modelo;

import java.util.List;

public class ParticipanteExterno extends Participante implements IParticipanteExterno {

    public ParticipanteExterno(String nombreUsuario, String nombreCompleto, String correoElectronico,
                               String password, String dni, String tarjeta,
                               List<PreferenciaArtistica> preferenciasArtisticas) {
        super(nombreUsuario, nombreCompleto, correoElectronico, password, dni, tarjeta, preferenciasArtisticas);
    }

    @Override
    public String getRolSistema() {
        return "PARTICIPANTE EXTERNO";
    }

    @Override
    public String getCodigoTipoPersistencia() {
        return PERSISTENCIA_EXTERNO;
    }

    @Override
    public String getTipoRegistro() {
        return "EXTERNO";
    }
}
