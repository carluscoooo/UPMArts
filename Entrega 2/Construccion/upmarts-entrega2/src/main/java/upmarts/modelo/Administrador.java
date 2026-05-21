package upmarts.modelo;

public class Administrador extends Usuario {

    private String telefonoAdministrador;

    public Administrador(String nombreUsuario, String nombreCompleto, String correoElectronico,
                         String password, String telefono) {
        super(nombreUsuario, nombreCompleto, correoElectronico, password);
        this.telefonoAdministrador = telefono;
    }

    public String getTelefonoAdministrador() {
        return telefonoAdministrador;
    }

    public void setTelefonoAdministrador(String telefonoAdministrador) {
        this.telefonoAdministrador = telefonoAdministrador;
    }

    @Override
    public RolUsuario getRol() {
        return RolUsuario.ADMINISTRADOR;
    }
}
