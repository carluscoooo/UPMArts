package upmarts.validacion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

public final class ValidadorDatosUsuario {

    private static final String NOMBRE_FICHERO_TERMINOS = "terminos_conflictivos.txt";
    private static Set<String> terminosConflictivos;

    private ValidadorDatosUsuario() {
    }

    public static boolean validarNick(String nick) {
        if (nick == null) {
            return false;
        }

        String nickLimpio = nick.trim();

        if (nickLimpio.length() < 4 || nickLimpio.length() > 12) {
            return false;
        }

        if (!nickLimpio.matches("[A-Za-z0-9]+")) {
            return false;
        }

        return !obtenerTerminosConflictivos().contains(nickLimpio.toLowerCase());
    }

    public static boolean validarPassword(String password) {
        if (password == null || password.length() < 12) {
            return false;
        }

        boolean tieneMayuscula = false;
        boolean tieneMinuscula = false;
        boolean tieneNumero = false;

        for (int i = 0; i < password.length(); i++) {
            char caracter = password.charAt(i);

            if (Character.isUpperCase(caracter)) {
                tieneMayuscula = true;
            } else if (Character.isLowerCase(caracter)) {
                tieneMinuscula = true;
            } else if (Character.isDigit(caracter)) {
                tieneNumero = true;
            }
        }

        return tieneMayuscula && tieneMinuscula && tieneNumero;
    }

    public static String cifrarPassword(String password) {
        if (password == null) {
            return "";
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder resultado = new StringBuilder();

            for (int i = 0; i < hash.length; i++) {
                String hexadecimal = Integer.toHexString(0xff & hash[i]);

                if (hexadecimal.length() == 1) {
                    resultado.append('0');
                }

                resultado.append(hexadecimal);
            }

            return resultado.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No se ha podido cifrar la contraseña.", e);
        }
    }

    private static Set<String> obtenerTerminosConflictivos() {
        if (terminosConflictivos == null) {
            // Se cargan una sola vez para no leer el fichero en cada validacion de nick.
            terminosConflictivos = cargarTerminosConflictivos();
        }

        return terminosConflictivos;
    }

    private static Set<String> cargarTerminosConflictivos() {
        Set<String> terminos = new HashSet<>();

        // El fichero de data permite cambiar la lista sin recompilar la aplicacion.
        if (cargarDesdeFicheroExterno(terminos)) {
            return terminos;
        }

        cargarDesdeRecursos(terminos);
        return terminos;
    }

    private static boolean cargarDesdeFicheroExterno(Set<String> terminos) {
        Path ruta = Paths.get("data", NOMBRE_FICHERO_TERMINOS);

        if (!Files.exists(ruta)) {
            return false;
        }

        try (BufferedReader lector = Files.newBufferedReader(ruta, StandardCharsets.UTF_8)) {
            leerTerminos(lector, terminos);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static void cargarDesdeRecursos(Set<String> terminos) {
        InputStream entrada = ValidadorDatosUsuario.class.getClassLoader().getResourceAsStream(NOMBRE_FICHERO_TERMINOS);

        if (entrada == null) {
            return;
        }

        try (BufferedReader lector = new BufferedReader(new InputStreamReader(entrada, StandardCharsets.UTF_8))) {
            leerTerminos(lector, terminos);
        } catch (IOException e) {
            terminos.clear();
        }
    }

    private static void leerTerminos(BufferedReader lector, Set<String> terminos) throws IOException {
        String linea;

        while ((linea = lector.readLine()) != null) {
            String termino = linea.trim().toLowerCase();

            if (!termino.isEmpty() && !termino.startsWith("#")) {
                terminos.add(termino);
            }
        }
    }
}
