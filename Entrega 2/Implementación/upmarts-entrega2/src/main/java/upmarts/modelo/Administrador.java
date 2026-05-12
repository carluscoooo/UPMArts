package upmarts.modelo;

public class Administrador extends Usuario implements IAdministrador {

    private String telefonoAdministrador;

    public Administrador(String nombreUsuario, String nombreCompleto, String correoElectronico,
                         String password, String telefono) {
        super(nombreUsuario, nombreCompleto, correoElectronico, password);
        this.telefonoAdministrador = telefono;
    }

    public String getTelefonoAdministrador() {
        return telefonoAdministrador;
    }

    public void setTelefonoAdministrador(String telefono) {
        this.telefonoAdministrador = telefono;
    }

    @Override
    public String getRolSistema() {
        return "ADMINISTRADOR";
    }
}
