package upmarts.modelo;

public abstract class UsuarioConDNI extends Usuario {

    private String dni;

    public UsuarioConDNI(String nombreUsuario, String nombreCompleto, String correoElectronico,
                         String password, String dni) {
        super(nombreUsuario, nombreCompleto, correoElectronico, password);
        this.dni = dni;
    }

    public String getDNI() {
        return dni;
    }

    public void setDNI(String dni) {
        this.dni = dni;
    }

    @Override
    public String getInformacionExtra() {
        return "   DNI: " + getDNI();
    }
}
