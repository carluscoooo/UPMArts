package upmarts.modelo;

import upmarts.validacion.ValidadorDatosUsuario;

public abstract class Usuario {

    public static final String PERSISTENCIA_ADMINISTRADOR = "ADMINISTRADOR";
    public static final String PERSISTENCIA_INSTRUCTOR = "INSTRUCTOR";
    public static final String PERSISTENCIA_EXTERNO = "EXTERNO";
    public static final String PERSISTENCIA_ESTUDIANTE_UPM = "ESTUDIANTE_UPM";
    public static final String PERSISTENCIA_PERSONAL_UPM = "PERSONAL_UPM";

    private String nombreUsuario;
    private String nombreCompleto;
    private String correoElectronico;
    private String password;

    public Usuario(String nombreUsuario, String nombreCompleto, String correoElectronico, String password) {
        this.nombreUsuario = nombreUsuario;
        this.nombreCompleto = nombreCompleto;
        this.correoElectronico = correoElectronico;
        this.password = password;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    public String getContrasena() {
        return password;
    }

    public void setContrasena(String password) {
        this.password = password;
    }

    public static boolean validarNick(String nick) {
        return ValidadorDatosUsuario.validarNick(nick);
    }

    public static boolean validarPassword(String password) {
        return ValidadorDatosUsuario.validarPassword(password);
    }

    public static String cifrarPassword(String password) {
        return ValidadorDatosUsuario.cifrarPassword(password);
    }

    public abstract String getRolSistema();

    public String getCodigoTipoPersistencia() {
        return "DESCONOCIDO";
    }

    public String convertirAlineaPersistencia() {
        StringBuilder linea = new StringBuilder();
        linea.append(getCodigoTipoPersistencia()).append(";");
        linea.append(limpiarParaPersistencia(getNombreUsuario())).append(";");
        linea.append(limpiarParaPersistencia(getNombreCompleto())).append(";");
        linea.append(limpiarParaPersistencia(getCorreoElectronico())).append(";");
        linea.append(limpiarParaPersistencia(getContrasena()));
        linea.append(getPersistenciaAdicional());
        return linea.toString();
    }

    protected String getPersistenciaAdicional() {
        return "";
    }

    protected String limpiarParaPersistencia(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace(";", ",").trim();
    }

    public boolean esAdministrador() {
        return false;
    }

    public boolean esInstructor() {
        return false;
    }

    public boolean esParticipante() {
        return false;
    }

    public boolean puedeDarseDeBaja() {
        return true;
    }

    public double obtenerDescuento() {
        return 0.0;
    }

    public String getInformacionExtra() {
        return "";
    }

    public String getDatoEspecifico() {
        return "";
    }

    public String getEtiquetaDatoEspecifico() {
        return "";
    }

    public boolean validarDatoEspecifico(String dato) {
        return true;
    }

    public void actualizarDatoEspecifico(String dato) {
        // Implementado en subclases que tienen datos específicos
    }

    public String getTipoRegistro() {
        return "DESCONOCIDO";
    }

    @Override
    public String toString() {
        return getRolSistema() + " - " + nombreUsuario + " - " + nombreCompleto + " - " + correoElectronico;
    }
}
