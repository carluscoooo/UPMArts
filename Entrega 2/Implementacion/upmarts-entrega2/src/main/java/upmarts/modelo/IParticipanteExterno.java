package upmarts.modelo;

import java.util.List;

public interface IParticipanteExterno {
    // Métodos específicos de Participante
    String getTarjetaCredito();
    void setTarjetaCredito(String tarjeta);
    
    List<PreferenciaArtistica> getPreferenciasArtisticas();
    void setPreferenciasArtisticas(List<PreferenciaArtistica> preferenciasArtisticas);
    
    // Métodos heredados de UsuarioConDNI
    String getDNI();
    void setDNI(String dni);
    
    // Métodos heredados de Usuario
    String getNombreUsuario();
    void setNombreUsuario(String nombreUsuario);
    
    String getNombreCompleto();
    void setNombreCompleto(String nombreCompleto);
    
    String getCorreoElectronico();
    void setCorreoElectronico(String correoElectronico);
    
    String getContrasena();
    void setContrasena(String password);
    
    // Métodos de rol y sistema
    String getRolSistema();
    boolean esParticipante();
    boolean esAdministrador();
    boolean esInstructor();
    
    // Métodos de gestión
    boolean darseDeBaja();
    boolean puedeDarseDeBaja();
    String getCodigoTipoPersistencia();
    String getTipoRegistro();
    String getInformacionExtra();
    double obtenerDescuento();
    
    // Métodos de datos especiales
    String getDatoEspecifico();
    String getEtiquetaDatoEspecifico();
    boolean validarDatoEspecifico(String dato);
    void actualizarDatoEspecifico(String dato);
}
