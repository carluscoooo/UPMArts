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

    @Override
    public boolean esParticipante() {
        return true;
    }

    @Override
    public String getInformacionExtra() {
        StringBuilder informacion = new StringBuilder();
        String datosDNI = super.getInformacionExtra();

        if (!datosDNI.isEmpty()) {
            informacion.append(datosDNI).append("\n");
        }

        informacion.append("   Tarjeta: ").append(getTarjetaCredito());
        return informacion.toString();
    }

    @Override
    public boolean puedeDarseDeBaja() {
        return true;
    }

    public boolean darseDeBaja() {
        return true;
    }

    protected String convertirPreferenciasATexto() {
        if (preferenciasArtisticas == null || preferenciasArtisticas.isEmpty()) {
            return "";
        }

        StringBuilder texto = new StringBuilder();

        for (int i = 0; i < preferenciasArtisticas.size(); i++) {
            PreferenciaArtistica preferencia = preferenciasArtisticas.get(i);

            if (preferencia != null && preferencia.getDisciplina() != null) {
                if (texto.length() > 0) {
                    texto.append(",");
                }

                texto.append(preferencia.getDisciplina().name());
                texto.append(":");
                texto.append(preferencia.getNivelExperiencia());
            }
        }

        return texto.toString();
    }

    @Override
    public String getPersistenciaAdicional() {
        StringBuilder extras = new StringBuilder();
        extras.append(";").append(limpiarParaPersistencia(getDNI()));
        extras.append(";").append(limpiarParaPersistencia(getTarjetaCredito()));
        extras.append(";").append(limpiarParaPersistencia(convertirPreferenciasATexto()));
        return extras.toString();
    }

    @Override
    public String getTipoRegistro() {
        return "EXTERNO";
    }

    @Override
    public String getDatoEspecifico() {
        return "";
    }

    @Override
    public String getEtiquetaDatoEspecifico() {
        return "";
    }

    @Override
    public boolean validarDatoEspecifico(String dato) {
        return true;
    }

    @Override
    public void actualizarDatoEspecifico(String dato) {
        // Los participantes externos no tienen dato específico adicional
    }
}
