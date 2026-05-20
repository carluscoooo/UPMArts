package upmarts.modelo;

public interface IInstructor {
    // Metodos especificos de Instructor
    String getIBAN();
    void setIBAN(String iban);

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
    boolean esInstructor();
    boolean esAdministrador();
    boolean esParticipante();

    // Metodos de gestion
    boolean darseDeBaja();
    boolean puedeDarseDeBaja();
    String getCodigoTipoPersistencia();
    String getInformacionExtra();
    double obtenerDescuento();

    // Metodos de datos especiales
    String getDatoEspecifico();
    String getEtiquetaDatoEspecifico();
    boolean validarDatoEspecifico(String dato);
    void actualizarDatoEspecifico(String dato);

    // Metodo de persistencia
    String getTipoRegistro();
}
