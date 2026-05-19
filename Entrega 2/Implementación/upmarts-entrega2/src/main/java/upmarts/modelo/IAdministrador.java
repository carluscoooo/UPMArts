package upmarts.modelo;

public interface IAdministrador {
    // Métodos específicos de Administrador
    String getTelefonoAdministrador();
    void setTelefonoAdministrador(String telefono);
    
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
    boolean esAdministrador();
    boolean esInstructor();
    boolean esParticipante();
    
    // Métodos de gestión
    boolean puedeDarseDeBaja();
    String getCodigoTipoPersistencia();
    String getInformacionExtra();
    double obtenerDescuento();
    
    // Métodos de datos especiales
    String getDatoEspecifico();
    String getEtiquetaDatoEspecifico();
    boolean validarDatoEspecifico(String dato);
    void actualizarDatoEspecifico(String dato);
    
    // Método de persistencia
    String getTipoRegistro();
}
