package upmarts.modelo;

import java.util.ArrayList;
import java.util.List;

public class ParticipanteExterno extends UsuarioConDNI {

    private String tarjetaCredito;
    private List<PreferenciaArtistica> preferenciasArtisticas;

    public ParticipanteExterno(String nombreUsuario, String nombreCompleto, String correoElectronico,
                               String password, String dni, String tarjeta,
                               List<PreferenciaArtistica> preferenciasArtisticas) {
        super(nombreUsuario, nombreCompleto, correoElectronico, password, dni);
        this.tarjetaCredito = tarjeta;
        setPreferenciasArtisticas(preferenciasArtisticas);
    }

    public String getTarjetaCredito() {
        return tarjetaCredito;
    }

    public void setTarjetaCredito(String tarjetaCredito) {
        this.tarjetaCredito = tarjetaCredito;
    }

    public List<PreferenciaArtistica> getPreferenciasArtisticas() {
        return new ArrayList<>(preferenciasArtisticas);
    }

    public void setPreferenciasArtisticas(List<PreferenciaArtistica> preferenciasArtisticas) {
        this.preferenciasArtisticas = new ArrayList<>();

        if (preferenciasArtisticas != null) {
            for (PreferenciaArtistica preferencia : preferenciasArtisticas) {
                if (preferencia != null && preferencia.getDisciplina() != null) {
                    this.preferenciasArtisticas.add(preferencia);
                }
            }
        }
    }

    @Override
    public RolUsuario getRol() {
        return RolUsuario.PARTICIPANTE_EXTERNO;
    }
}
