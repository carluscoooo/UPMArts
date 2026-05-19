package upmarts.modelo;

public class Administrador extends Usuario implements IAdministrador {

    private String telefonoAdministrador;

    public Administrador(String nombreUsuario, String nombreCompleto, String correoElectronico,
                         String password, String telefono) {
        super(nombreUsuario, nombreCompleto, correoElectronico, password);
        this.telefonoAdministrador = telefono;
    }

    @Override
    public String getTelefonoAdministrador() {
        return telefonoAdministrador;
    }

    @Override
    public void setTelefonoAdministrador(String telefono) {
        this.telefonoAdministrador = telefono;
    }

    @Override
    public String getRolSistema() {
        return "ADMINISTRADOR";
    }

    @Override
    public boolean esAdministrador() {
        return true;
    }

    @Override
    public boolean puedeDarseDeBaja() {
        return false;
    }

    @Override
    public String getCodigoTipoPersistencia() {
        return PERSISTENCIA_ADMINISTRADOR;
    }

    @Override
    protected String getPersistenciaAdicional() {
        return ";" + limpiarParaPersistencia(getTelefonoAdministrador());
    }

    @Override
    public String getInformacionExtra() {
        return "   Teléfono: " + getTelefonoAdministrador();
    }
}
