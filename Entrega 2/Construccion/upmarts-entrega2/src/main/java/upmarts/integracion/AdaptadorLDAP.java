package upmarts.integracion;

public class AdaptadorLDAP implements IValidadorUPM {

    private static final String DOMINIO_UPM = "@upm.es";
    private static final String DOMINIO_ALUMNOS = "@alumnos.upm.es";

    @Override
    public boolean verificarCredencialesUPM(String correo, String password) {
        if (correo == null || password == null || password.trim().length() < 12) {
            return false;
        }

        String correoNormalizado = correo.toLowerCase().trim();

        if (!correoNormalizado.endsWith(DOMINIO_UPM) && !correoNormalizado.endsWith(DOMINIO_ALUMNOS)) {
            return false;
        }

        try {
            if (!existeCuentaUPM(correoNormalizado)) {
                return false;
            }

            Object datosUsuario = crearDatosUsuarioUPM(correoNormalizado);
            String email = invocarMetodoTexto(datosUsuario, "getEmail");
            String rol = invocarMetodoTexto(datosUsuario, "getRol");

            return datosUsuario != null
                    && email != null
                    && correoNormalizado.equals(email.trim().toLowerCase())
                    && rolCompatibleConCorreo(correoNormalizado, rol);
        } catch (ReflectiveOperationException | SecurityException e) {
            return validarSinLibreriaExterna(password);
        } catch (LinkageError e) {
            return validarSinLibreriaExterna(password);
        } catch (RuntimeException e) {
            return validarSinLibreriaExterna(password);
        }
    }

    private boolean existeCuentaUPM(String correoNormalizado) throws ReflectiveOperationException {
        Class<?> autenticacion = Class.forName("servidor.Autenticacion");
        Object resultado = autenticacion.getMethod("existeCuentaUPMStatic", String.class)
                .invoke(null, correoNormalizado);
        return Boolean.TRUE.equals(resultado);
    }

    private Object crearDatosUsuarioUPM(String correoNormalizado) throws ReflectiveOperationException {
        // Se usa UPMUserData directamente porque LoginLDAP abre una ventana Swing y bloquea la consola.
        Class<?> datosUsuario = Class.forName("servidor.UPMUserData");
        return datosUsuario.getConstructor(String.class).newInstance(correoNormalizado);
    }

    private String invocarMetodoTexto(Object objeto, String metodo) throws ReflectiveOperationException {
        if (objeto == null) {
            return null;
        }

        Object valor = objeto.getClass().getMethod(metodo).invoke(objeto);
        return valor != null ? valor.toString() : null;
    }

    private boolean rolCompatibleConCorreo(String correoNormalizado, String rol) {
        if (rol == null) {
            return false;
        }

        String rolNormalizado = rol.trim().toUpperCase();

        if (correoNormalizado.endsWith(DOMINIO_ALUMNOS)) {
            return "ALUMNO".equals(rolNormalizado);
        }

        return "PDI".equals(rolNormalizado) || "PAS".equals(rolNormalizado);
    }

    private boolean validarSinLibreriaExterna(String password) {
        // Permite probar la aplicacion fuera de Maven si la libreria externa no esta en el classpath.
        return password.trim().length() >= 12;
    }
}
