package upmarts.modelo;

import java.util.List;

public interface IMiembroUPM {
    // Metodos especificos de MiembroUPM
    String getRolUPM();
    void setRolUPM(String rolUPM);

    // Metodos heredados de ParticipanteExterno
    String getTarjetaCredito();
    void setTarjetaCredito(String tarjeta);

    List<PreferenciaArtistica> getPreferenciasArtisticas();
    void setPreferenciasArtisticas(List<PreferenciaArtistica> preferenciasArtisticas);

    // Metodos heredados de UsuarioConDNI
    String getDNI();
    void setDNI(String dni);

    // Metodos heredados de Usuario
    String getNombreUsuario();
    void setNombreUsuario(String nombreUsuario);

    String getNombreCompleto();
    void setNombreCompleto(String nombreCompleto);

    String getCorreoElectronico();
    void setCorreoElectronico(String correoElectronico);

    String getContrasena();
    void setContrasena(String password);

    // Metodos de rol y sistema
    String getRolSistema();
    boolean esParticipante();
    boolean esAdministrador();
    boolean esInstructor();

    // Metodos de gestion
    boolean darseDeBaja();
    boolean puedeDarseDeBaja();
    String getCodigoTipoPersistencia();
    String getTipoRegistro();
    String getInformacionExtra();
    double obtenerDescuento();

    // Metodos de datos especiales
    String getDatoEspecifico();
    String getEtiquetaDatoEspecifico();
    boolean validarDatoEspecifico(String dato);
    void actualizarDatoEspecifico(String dato);
}
