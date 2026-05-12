package upmarts.modelo;

import java.util.ArrayList;
import java.util.List;

public abstract class Participante extends UsuarioConDNI {

    private String tarjetaCredito;
    private List<PreferenciaArtistica> preferenciasArtisticas;

    public Participante(String nombreUsuario, String nombreCompleto, String correoElectronico,
                        String password, String dni, String tarjetaCredito,
                        List<PreferenciaArtistica> preferenciasArtisticas) {
        super(nombreUsuario, nombreCompleto, correoElectronico, password, dni);
        this.tarjetaCredito = tarjetaCredito;
        setPreferenciasArtisticas(preferenciasArtisticas);
    }

    public String getTarjetaCredito() {
        return tarjetaCredito;
    }

    public void setTarjetaCredito(String tarjetaCredito) {
        this.tarjetaCredito = tarjetaCredito;
    }

    public List<PreferenciaArtistica> getPreferenciasArtisticas() {
        return new ArrayList<PreferenciaArtistica>(preferenciasArtisticas);
    }

    public void setPreferenciasArtisticas(List<PreferenciaArtistica> preferenciasArtisticas) {
        this.preferenciasArtisticas = new ArrayList<PreferenciaArtistica>();

        if (preferenciasArtisticas != null) {
            for (PreferenciaArtistica preferencia : preferenciasArtisticas) {
                if (preferencia != null && preferencia.getDisciplina() != null) {
                    this.preferenciasArtisticas.add(preferencia);
                }
            }
        }
    }

    public boolean darseDeBaja() {
        return true;
    }
}
