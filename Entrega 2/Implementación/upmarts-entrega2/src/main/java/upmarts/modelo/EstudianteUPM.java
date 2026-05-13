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

    @Override
    public String getCodigoTipoPersistencia() {
        return PERSISTENCIA_ESTUDIANTE_UPM;
    }

    @Override
    public String getTipoRegistro() {
        return "ALUMNO_UPM";
    }

    @Override
    public String getInformacionExtra() {
        return super.getInformacionExtra() + "\n   Matrícula: " + getNumeroMatricula();
    }

    @Override
    public String getDatoEspecifico() {
        return getNumeroMatricula();
    }

    @Override
    public String getEtiquetaDatoEspecifico() {
        return "Matrícula";
    }

    @Override
    public boolean validarDatoEspecifico(String dato) {
        return dato != null && !dato.trim().isEmpty();
    }

    @Override
    public void actualizarDatoEspecifico(String dato) {
        setNumeroMatricula(dato != null ? dato.trim() : "");
    }

    @Override
    public double obtenerDescuento() {
        return 0.25;
    }

    @Override
    public String getPersistenciaAdicional() {
        StringBuilder extras = new StringBuilder();
        extras.append(";").append(limpiarParaPersistencia(getDNI()));
        extras.append(";").append(limpiarParaPersistencia(getTarjetaCredito()));
        extras.append(";").append(limpiarParaPersistencia(getNumeroMatricula()));
        extras.append(";").append(limpiarParaPersistencia(convertirPreferenciasATexto()));
        return extras.toString();
    }
}
