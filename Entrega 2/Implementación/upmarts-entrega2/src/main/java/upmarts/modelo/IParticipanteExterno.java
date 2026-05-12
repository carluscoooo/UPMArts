package upmarts.modelo;

import java.util.List;

public interface IParticipanteExterno {
    String getTarjetaCredito();
    void setTarjetaCredito(String tarjeta);
    List<PreferenciaArtistica> getPreferenciasArtisticas();
    boolean darseDeBaja();
}
