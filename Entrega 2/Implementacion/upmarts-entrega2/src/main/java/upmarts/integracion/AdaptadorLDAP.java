package upmarts.integracion;

import servidor.Autenticacion;
import servidor.ExternalLDAP;
import servidor.IUPMUserData;
import servidor.UPMUsers;

public class AdaptadorLDAP implements IValidadorUPM {

    @Override
    public boolean verificarCredencialesUPM(String correo, String password) {
        if (correo == null || password == null) {
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

            IUPMUserData datosUsuario = ExternalLDAP.LoginLDAP();
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
        return password.length() >= 12;
    }
}
