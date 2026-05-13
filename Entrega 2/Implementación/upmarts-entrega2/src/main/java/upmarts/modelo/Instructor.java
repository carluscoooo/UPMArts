package upmarts.modelo;

public class Instructor extends UsuarioConDNI implements IInstructor {

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
    public boolean darseDeBaja() {
        return true;
    }

    @Override
    public String getRolSistema() {
        return "INSTRUCTOR";
    }

    @Override
    public boolean esInstructor() {
        return true;
    }

    @Override
    public String getCodigoTipoPersistencia() {
        return PERSISTENCIA_INSTRUCTOR;
    }

    @Override
    protected String getPersistenciaAdicional() {
        StringBuilder extras = new StringBuilder();
        extras.append(";").append(limpiarParaPersistencia(getDNI()));
        extras.append(";").append(limpiarParaPersistencia(getIBAN()));
        return extras.toString();
    }

    @Override
    public String getInformacionExtra() {
        return super.getInformacionExtra() + "\n   IBAN: " + getIBAN();
    }
}
