package upmarts.modelo;

public class Instructor extends UsuarioConDNI {

    private String iban;

    public Instructor(String nombreUsuario, String nombreCompleto, String correoElectronico,
                      String password, String dni, String iban) {
        super(nombreUsuario, nombreCompleto, correoElectronico, password, dni);
        this.iban = iban;
    }

    public String getIBAN() {
        return iban;
    }

    public void setIBAN(String iban) {
        this.iban = iban;
    }

    @Override
    public RolUsuario getRol() {
        return RolUsuario.INSTRUCTOR;
    }
}
