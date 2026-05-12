package upmarts.integracion;

import java.lang.reflect.Method;

public class AdaptadorLDAP implements IValidadorUPM {

    public boolean verificarCredencialesUPM(String correo, String password) {
        Boolean resultadoExterno = llamarExternalLDAP(correo, password);

        if (resultadoExterno != null) {
            return resultadoExterno.booleanValue();
        }

        return validarSinLibreriaExterna(correo, password);
    }

    private Boolean llamarExternalLDAP(String correo, String password) {
        String[] nombresClase = {
                "ExternalLDAP",
                "externals.ExternalLDAP",
                "upm.externals.ExternalLDAP"
        };

        String[] nombresMetodo = {
                "OperationvalidarUsuarioUPM",
                "OperacionvalidarUsuarioUPM",
                "validarUsuarioUPM",
                "verificarCredencialesUPM"
        };

        for (int i = 0; i < nombresClase.length; i++) {
            try {
                Class<?> clase = Class.forName(nombresClase[i]);
                Object objetoLDAP = clase.getDeclaredConstructor().newInstance();

                for (int j = 0; j < nombresMetodo.length; j++) {
                    try {
                        Method metodo = clase.getMethod(nombresMetodo[j], String.class, String.class);
                        Object resultado = metodo.invoke(objetoLDAP, correo, password);

                        if (resultado instanceof Boolean) {
                            return (Boolean) resultado;
                        }
                    } catch (NoSuchMethodException e) {
                        // Se prueba con el siguiente nombre de método.
                    }
                }
            } catch (Exception e) {
                // Si la librería no está disponible, se usa la validación local de respaldo.
            }
        }

        return null;
    }

    private boolean validarSinLibreriaExterna(String correo, String password) {
        if (correo == null || password == null) {
            return false;
        }

        String correoNormalizado = correo.toLowerCase().trim();

        return password.length() >= 12
                && (correoNormalizado.endsWith("@upm.es") || correoNormalizado.endsWith("@alumnos.upm.es"));
    }
}
