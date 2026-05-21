package upmarts.modelo;

public abstract class Usuario {

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

    public abstract RolUsuario getRol();
}
