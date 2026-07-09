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
import java.util.regex.Pattern;

public final class ValidadorDatosUsuario {

    private static final String NOMBRE_FICHERO_TERMINOS = "terminos_conflictivos.txt";
    private static final Pattern PATRON_CORREO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PATRON_DNI = Pattern.compile("[0-9]{8}[A-Za-z]");
    private static final Pattern PATRON_TARJETA = Pattern.compile("[0-9]{16}");
    private static final Pattern PATRON_IBAN_ES = Pattern.compile("(?i)ES[0-9]{22}");

    private static final String ERROR_NOMBRE_OBLIGATORIO = "El nombre completo es obligatorio.";
    private static final String ERROR_NICK_OBLIGATORIO = "El nick es obligatorio.";
    private static final String ERROR_NICK_LONGITUD =
            "Nick no válido. Debe tener entre 4 y 12 caracteres alfanuméricos.";
    private static final String ERROR_NICK_CARACTERES =
            "Nick no válido. Solo puede contener letras y números.";
    private static final String ERROR_NICK_CONFLICTIVO =
            "Nick no válido. Usa un término conflictivo; elija otro nick.";
    private static final String ERROR_PASSWORD_OBLIGATORIA = "La contraseña es obligatoria.";
    private static final String ERROR_PASSWORD_INVALIDA =
            "Contraseña no válida. Debe tener al menos 12 caracteres, una mayúscula, una minúscula y un número.";
    private static final String ERROR_CORREO_OBLIGATORIO = "El correo electrónico es obligatorio.";
    private static final String ERROR_CORREO_INVALIDO =
            "Correo electrónico no válido. Use un formato como usuario@dominio.com.";
    private static final String ERROR_DNI_OBLIGATORIO = "El DNI es obligatorio.";
    private static final String ERROR_DNI_INVALIDO =
            "DNI no válido. Debe tener 8 dígitos seguidos de una letra.";
    private static final String ERROR_TARJETA_OBLIGATORIA = "La tarjeta de crédito/débito es obligatoria.";
    private static final String ERROR_TARJETA_NO_NUMERICA =
            "Número de tarjeta no válido. Debe contener solo 16 dígitos.";
    private static final String ERROR_TARJETA_LONGITUD =
            "Número de tarjeta no válido. Debe contener exactamente 16 dígitos.";
    private static final String ERROR_IBAN_OBLIGATORIO = "El IBAN es obligatorio.";
    private static final String ERROR_IBAN_INVALIDO =
            "IBAN no válido. Debe tener formato español: ES seguido de 22 dígitos.";
    private static final String ERROR_MATRICULA_OBLIGATORIA = "El número de matrícula es obligatorio.";
    private static final String ERROR_ANTIGUEDAD_OBLIGATORIA = "La antigüedad es obligatoria.";
    private static final String ERROR_ANTIGUEDAD_NO_NUMERICA =
            "Antigüedad no válida. Debe ser un número entero mayor o igual que 0.";
    private static final String ERROR_ANTIGUEDAD_NEGATIVA =
            "Antigüedad no válida. No puede ser negativa; use un número entero mayor o igual que 0.";
    private static final String ERROR_NIVEL_OBLIGATORIO =
            "El nivel de habilidad es obligatorio. Use 0 si no quiere indicar esta disciplina.";
    private static final String ERROR_NIVEL_NO_NUMERICO = "El nivel de habilidad debe ser numérico.";
    private static final String ERROR_NIVEL_MENOR_CERO = "El nivel de habilidad no puede ser menor que 0.";
    private static final String ERROR_NIVEL_MAYOR_DIEZ = "El nivel de habilidad no puede ser mayor que 10.";

    private static Set<String> terminosConflictivos;

    private ValidadorDatosUsuario() {
    }

    public static String validarNombre(String nombre) {
        return textoVacio(nombre) ? ERROR_NOMBRE_OBLIGATORIO : null;
    }

    public static String validarNickConMensaje(String nick) {
        if (textoVacio(nick)) {
            return ERROR_NICK_OBLIGATORIO;
        }

        String nickLimpio = nick.trim();

        if (nickLimpio.length() < 4 || nickLimpio.length() > 12) {
            return ERROR_NICK_LONGITUD;
        }

        if (!nickLimpio.matches("[A-Za-z0-9]+")) {
            return ERROR_NICK_CARACTERES;
        }

        return esTerminoConflictivo(nickLimpio) ? ERROR_NICK_CONFLICTIVO : null;
    }

    public static boolean esTerminoConflictivo(String nick) {
        if (textoVacio(nick)) {
            return false;
        }

        return obtenerTerminosConflictivos().contains(nick.trim().toLowerCase());
    }

    public static String validarPasswordConMensaje(String password) {
        String error = validarPasswordLogin(password);
        if (error != null) {
            return error;
        }

        return passwordCumpleComplejidad(password) ? null : ERROR_PASSWORD_INVALIDA;
    }

    public static String validarPasswordLogin(String password) {
        return textoVacio(password) ? ERROR_PASSWORD_OBLIGATORIA : null;
    }

    public static String validarCorreo(String correo) {
        if (textoVacio(correo)) {
            return ERROR_CORREO_OBLIGATORIO;
        }

        return PATRON_CORREO.matcher(correo.trim()).matches() ? null : ERROR_CORREO_INVALIDO;
    }

    public static String validarDNI(String dni) {
        if (textoVacio(dni)) {
            return ERROR_DNI_OBLIGATORIO;
        }

        return PATRON_DNI.matcher(dni.trim()).matches() ? null : ERROR_DNI_INVALIDO;
    }

    public static String validarTarjeta(String tarjeta) {
        if (textoVacio(tarjeta)) {
            return ERROR_TARJETA_OBLIGATORIA;
        }

        String tarjetaLimpia = tarjeta.trim();

        if (!tarjetaLimpia.matches("[0-9]+")) {
            return ERROR_TARJETA_NO_NUMERICA;
        }

        return PATRON_TARJETA.matcher(tarjetaLimpia).matches() ? null : ERROR_TARJETA_LONGITUD;
    }

    public static String validarIBAN(String iban) {
        if (textoVacio(iban)) {
            return ERROR_IBAN_OBLIGATORIO;
        }

        return PATRON_IBAN_ES.matcher(iban.trim()).matches() ? null : ERROR_IBAN_INVALIDO;
    }

    public static String validarMatricula(String matricula) {
        return textoVacio(matricula) ? ERROR_MATRICULA_OBLIGATORIA : null;
    }

    public static String validarAntiguedad(String antiguedad) {
        if (textoVacio(antiguedad)) {
            return ERROR_ANTIGUEDAD_OBLIGATORIA;
        }

        try {
            int valor = Integer.parseInt(antiguedad.trim());
            return valor < 0 ? ERROR_ANTIGUEDAD_NEGATIVA : null;
        } catch (NumberFormatException e) {
            return ERROR_ANTIGUEDAD_NO_NUMERICA;
        }
    }

    public static String validarNivelPreferencia(String nivel) {
        if (textoVacio(nivel)) {
            return ERROR_NIVEL_OBLIGATORIO;
        }

        try {
            int valor = Integer.parseInt(nivel.trim());

            if (valor < 0) {
                return ERROR_NIVEL_MENOR_CERO;
            }

            return valor > 10 ? ERROR_NIVEL_MAYOR_DIEZ : null;
        } catch (NumberFormatException e) {
            return ERROR_NIVEL_NO_NUMERICO;
        }
    }

    public static boolean textoVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    private static boolean passwordCumpleComplejidad(String password) {
        if (password.length() < 12) {
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
            // Se guarda un hash para no persistir la contrasena en claro.
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
