package upmarts.modelo;

public interface IAdministrador {
    // Metodos especificos de Administrador
    String getTelefonoAdministrador();
    void setTelefonoAdministrador(String telefono);

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
    boolean esAdministrador();
    boolean esInstructor();
    boolean esParticipante();

    // Metodos de gestion
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
