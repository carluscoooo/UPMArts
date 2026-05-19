package upmarts.integracion;

import java.lang.reflect.Method;

public class AdaptadorLDAP implements IValidadorUPM {

    @Override
    public boolean verificarCredencialesUPM(String correo, String password) {
        Boolean resultadoExterno = llamarExternalLDAP(correo, password);

        if (resultadoExterno != null) {
            return resultadoExterno;
        }

        return validarSinLibreriaExterna(correo, password);
    }

    private Boolean llamarExternalLDAP(String correo, String password) {
        if (correo == null || password == null) {
            return null;
        }

        String correoNormalizado = correo.toLowerCase().trim();
        if (!correoNormalizado.endsWith("@upm.es") && !correoNormalizado.endsWith("@alumnos.upm.es")) {
            return false;
        }

        Boolean resultado = intentarAutenticacion(correo);
        if (resultado != null) {
            return resultado;
        }

        resultado = intentarExternalLDAP(correo, password);
        if (resultado != null) {
            return resultado;
        }

        return null;
    }

    private Boolean intentarAutenticacion(String correo) {
        try {
            Class<?> clase = Class.forName("servidor.Autenticacion");

            try {
                Method metodoEstatico = clase.getMethod("existeCuentaUPMStatic", String.class);
                Object resultado = metodoEstatico.invoke(null, correo);
                if (resultado instanceof Boolean) {
                    return (Boolean) resultado;
                }
            } catch (NoSuchMethodException e) {
                // Continúa buscando otro método.
            }

            Object instancia = clase.getDeclaredConstructor().newInstance();
            try {
                Method metodoInstancia = clase.getMethod("existeCuentaUPM", String.class);
                Object resultado = metodoInstancia.invoke(instancia, correo);
                if (resultado instanceof Boolean) {
                    return (Boolean) resultado;
                }
            } catch (NoSuchMethodException e) {
                // Continúa si no existe el método.
            }
        } catch (Exception e) {
            // La librería externa no está disponible o hay un error de reflexión.
        }

        return null;
    }

    private Boolean intentarExternalLDAP(String correo, String password) {
        String[] nombresClase = {
                "servidor.ExternalLDAP",
                "ExternalLDAP",
                "externals.ExternalLDAP",
                "upm.externals.ExternalLDAP"
        };

        String[] nombresMetodo = {
                "verificarCredencialesUPM",
                "validarUsuarioUPM",
                "OperacionvalidarUsuarioUPM",
                "OperationvalidarUsuarioUPM"
        };

        for (String nombreClase : nombresClase) {
            try {
                Class<?> clase = Class.forName(nombreClase);
                Object instancia = clase.getDeclaredConstructor().newInstance();

                for (String nombreMetodo : nombresMetodo) {
                    try {
                        Method metodo = clase.getMethod(nombreMetodo, String.class, String.class);
                        Object resultado = metodo.invoke(instancia, correo, password);

                        if (resultado instanceof Boolean) {
                            return (Boolean) resultado;
                        }
                    } catch (NoSuchMethodException e) {
                        // Continúa con el siguiente nombre de método.
                    }
                }

                try {
                    Method loginMetodo = clase.getMethod("LoginLDAP");
                    Object resultado = loginMetodo.invoke(instancia);
                    if (resultado instanceof Boolean) {
                        return (Boolean) resultado;
                    }
                } catch (NoSuchMethodException e) {
                    // No hay método de login sin argumentos.
                }
            } catch (Exception e) {
                // Si la librería no está disponible o no coincide la clase, se ignora.
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
