package upmarts.modelo;

import java.util.List;

public class PersonalUPM extends MiembroUPM {

    private int antiguedad;

    public PersonalUPM(String nombreUsuario, String nombreCompleto, String correoElectronico,
                       String password, String dni, String tarjeta, int antiguedad,
                       List<PreferenciaArtistica> preferenciasArtisticas) {
        super(nombreUsuario, nombreCompleto, correoElectronico, password, dni, tarjeta,
                "PERSONAL", preferenciasArtisticas);
        this.antiguedad = antiguedad;
    }

    public int getAntiguedad() {
        return antiguedad;
    }

    public void setAntiguedad(int antiguedad) {
        this.antiguedad = antiguedad;
    }

    @Override
    public String getRolSistema() {
        return "PERSONAL UPM";
    }

    @Override
    public String getCodigoTipoPersistencia() {
        return PERSISTENCIA_PERSONAL_UPM;
    }

    @Override
    public String getTipoRegistro() {
        return "PERSONAL_UPM";
    }

    @Override
    public String getInformacionExtra() {
        return super.getInformacionExtra() + "\n   Antigüedad: " + getAntiguedad() + " años";
    }

    @Override
    public String getDatoEspecifico() {
        return String.valueOf(getAntiguedad());
    }

    @Override
    public String getEtiquetaDatoEspecifico() {
        return "Antigüedad";
    }

    @Override
    public boolean validarDatoEspecifico(String dato) {
        if (dato == null || dato.trim().isEmpty()) {
            return false;
        }

        try {
            int valor = Integer.parseInt(dato.trim());
            return valor >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void actualizarDatoEspecifico(String dato) {
        try {
            setAntiguedad(Integer.parseInt(dato.trim()));
        } catch (Exception e) {
            setAntiguedad(0);
        }
    }

    @Override
    public double obtenerDescuento() {
        double descuento = 0.25 + antiguedad * 0.03;
        return descuento > 0.5 ? 0.5 : descuento;
    }

    @Override
    public String getPersistenciaAdicional() {
        StringBuilder extras = new StringBuilder();
        extras.append(";").append(limpiarParaPersistencia(getDNI()));
        extras.append(";").append(limpiarParaPersistencia(getTarjetaCredito()));
        extras.append(";").append(getAntiguedad());
        extras.append(";").append(limpiarParaPersistencia(convertirPreferenciasATexto()));
        return extras.toString();
    }
}
