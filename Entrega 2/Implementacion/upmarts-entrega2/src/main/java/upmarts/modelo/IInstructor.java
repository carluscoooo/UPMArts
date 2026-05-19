package upmarts.modelo;

public interface IInstructor {
    // Métodos específicos de Instructor
    String getIBAN();
    void setIBAN(String iban);
    
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
    boolean esInstructor();
    boolean esAdministrador();
    boolean esParticipante();
    
    // Métodos de gestión
    boolean darseDeBaja();
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
