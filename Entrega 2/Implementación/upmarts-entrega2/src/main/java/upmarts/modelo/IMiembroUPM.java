package upmarts.modelo;

import java.util.List;

public interface IMiembroUPM {
    String getRolUPM();
    String getDNI();
    String getTarjetaCredito();
    List<PreferenciaArtistica> getPreferenciasArtisticas();
}
