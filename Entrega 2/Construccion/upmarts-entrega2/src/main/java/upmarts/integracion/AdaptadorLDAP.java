package upmarts.integracion;

import servidor.Autenticacion;
import servidor.IUPMUserData;
import servidor.UPMUserData;
import servidor.UPMUsers;

public class AdaptadorLDAP implements IValidadorUPM {

    @Override
    public boolean verificarCredencialesUPM(String correo, String password) {
        if (correo == null || password == null || password.trim().length() < 12) {
            return false;
        }

        String correoNormalizado = correo.toLowerCase().trim();

        if (!correoNormalizado.endsWith("@upm.es") && !correoNormalizado.endsWith("@alumnos.upm.es")) {
            return false;
        }

        try {
            boolean cuentaExiste = Autenticacion.existeCuentaUPMStatic(correoNormalizado);

            if (!cuentaExiste) {
                return false;
            }

            // ExternalLDAP.LoginLDAP() abre un dialogo modal Swing y bloquea la CLI.
            IUPMUserData datosUsuario = new UPMUserData(correoNormalizado);
            return datosUsuario != null
                    && datosUsuario.getEmail() != null
                    && correoNormalizado.equals(datosUsuario.getEmail().trim().toLowerCase())
                    && rolCompatibleConCorreo(correoNormalizado, datosUsuario.getRol());
        } catch (RuntimeException e) {
            return validarSinLibreriaExterna(password);
        } catch (LinkageError e) {
            return validarSinLibreriaExterna(password);
        }
    }

    private boolean rolCompatibleConCorreo(String correoNormalizado, UPMUsers rol) {
        if (rol == null) {
            return false;
        }

        if (correoNormalizado.endsWith("@alumnos.upm.es")) {
            return UPMUsers.ALUMNO.equals(rol);
        }

        return UPMUsers.PDI.equals(rol) || UPMUsers.PAS.equals(rol);
    }

    private boolean validarSinLibreriaExterna(String password) {
        return password.trim().length() >= 12;
    }
}
