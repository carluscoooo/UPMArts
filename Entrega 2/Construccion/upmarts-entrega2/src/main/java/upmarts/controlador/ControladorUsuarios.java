package upmarts.controlador;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import upmarts.integracion.AdaptadorLDAP;
import upmarts.integracion.IValidadorUPM;
import upmarts.modelo.Administrador;
import upmarts.modelo.EstudianteUPM;
import upmarts.modelo.Instructor;
import upmarts.modelo.ParticipanteExterno;
import upmarts.modelo.PersonalUPM;
import upmarts.modelo.PreferenciaArtistica;
import upmarts.modelo.RolUsuario;
import upmarts.modelo.Usuario;
import upmarts.persistencia.GestorFicheroUsuarios;
import upmarts.persistencia.IAccesoUsuarios;
import upmarts.validacion.ValidadorDatosUsuario;

public class ControladorUsuarios implements IControladorUsuarios {

    public static final String TIPO_ALUMNO_UPM = "ALUMNO_UPM";
    public static final String TIPO_PERSONAL_UPM = "PERSONAL_UPM";
    public static final String TIPO_EXTERNO = "EXTERNO";
    public static final String TIPO_CORREO_INVALIDO = "CORREO_INVALIDO";

    private static final String RUTA_USUARIOS = "data/usuarios.txt";
    private static final Pattern PATRON_CORREO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PATRON_DNI = Pattern.compile("[0-9]{8}[A-Za-z]");
    private static final Pattern PATRON_TARJETA = Pattern.compile("[0-9]{8,19}");
    private static final Pattern PATRON_IBAN = Pattern.compile("[A-Za-z]{2}[0-9A-Za-z]{13,32}");

    private static final String ERROR_NOMBRE_VACIO = "El nombre completo no puede estar vacío.";
    private static final String ERROR_NICK_INVALIDO =
            "Nick inválido. Debe tener entre 4 y 12 caracteres alfanuméricos y no usar términos conflictivos.";
    private static final String ERROR_PASSWORD_INVALIDA =
            "Contraseña inválida. Debe tener al menos 12 caracteres, incluir mayúsculas, minúsculas y números.";
    private static final String ERROR_CORREO_INVALIDO = "Correo electrónico inválido.";
    private static final String ERROR_CORREO_DUPLICADO = "Ya existe un usuario registrado con ese correo.";
    private static final String ERROR_NICK_DUPLICADO = "Ya existe un usuario registrado con ese nick.";
    private static final String ERROR_DNI_INVALIDO = "DNI inválido. Debe tener 8 dígitos seguidos de una letra.";
    private static final String ERROR_TARJETA_INVALIDA =
            "Número de tarjeta inválido. Debe contener entre 8 y 19 dígitos.";
    private static final String ERROR_IBAN_INVALIDO =
            "IBAN inválido. Debe empezar por dos letras y contener entre 15 y 34 caracteres alfanuméricos.";
    private static final String ERROR_MATRICULA_VACIA = "El número de matrícula no puede estar vacío.";
    private static final String ERROR_VALIDACION_UPM =
            "No se ha podido validar la cuenta UPM. Compruebe correo y contraseña UPM.";
    private static final String ERROR_ANTIGUEDAD_INVALIDA = "La antigüedad debe ser un número entero válido.";

    private final IAccesoUsuarios persistencia;
    private final IValidadorUPM validadorUPM;
    private List<Usuario> usuarios;
    private String ultimoError;

    public ControladorUsuarios() {
        this(new GestorFicheroUsuarios(RUTA_USUARIOS), new AdaptadorLDAP());
    }

    ControladorUsuarios(IAccesoUsuarios persistencia, IValidadorUPM validadorUPM) {
        this.persistencia = persistencia;
        this.usuarios = persistencia.leerUsuarios();
        this.validadorUPM = validadorUPM;
        crearUsuariosInicialesSiNoExisten();
    }

    private void crearUsuariosInicialesSiNoExisten() {
        boolean hayCambios = false;

        if (!existeAdministradorInicial()) {
            usuarios.add(new Administrador(
                    "adminsys",
                    "Administrador Principal",
                    "admin@upm.es",
                    ValidadorDatosUsuario.cifrarPassword("Admin123456A"),
                    "910000000"
            ));
            hayCambios = true;
        }

        if (!existeInstructorInicial()) {
            usuarios.add(new Instructor(
                    "profarte1",
                    "Instructor Inicial",
                    "instructor@upm.es",
                    ValidadorDatosUsuario.cifrarPassword("Instructor123A"),
                    "12345678A",
                    "ES7620770024003102575766"
            ));
            hayCambios = true;
        }

        if (hayCambios) {
            persistencia.guardarUsuarios(usuarios);
        }
    }

    private boolean existeAdministradorInicial() {
        for (Usuario usuario : usuarios) {
            if (usuario.getRol() == RolUsuario.ADMINISTRADOR) {
                return true;
            }
        }
        return false;
    }

    private boolean existeInstructorInicial() {
        for (Usuario usuario : usuarios) {
            if (usuario.getRol() == RolUsuario.INSTRUCTOR) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getUltimoError() {
        return ultimoError;
    }

    private void setUltimoError(String mensaje) {
        this.ultimoError = mensaje;
    }

    @Override
    public String validarNombreRegistro(String nombre) {
        return validarNombre(nombre);
    }

    @Override
    public String validarNickRegistro(String nick) {
        refrescarUsuarios();

        String error = validarFormatoNick(nick);
        if (error != null) {
            return error;
        }

        return existeNick(nick) ? ERROR_NICK_DUPLICADO : null;
    }

    @Override
    public String validarCorreoRegistro(String correo) {
        refrescarUsuarios();

        String error = validarFormatoCorreo(correo);
        if (error != null) {
            return error;
        }

        return existeCorreo(correo) ? ERROR_CORREO_DUPLICADO : null;
    }

    @Override
    public String validarPasswordRegistro(String password) {
        return validarFormatoPassword(password);
    }

    @Override
    public String validarDNIRegistro(String dni) {
        return validarFormatoDNI(dni);
    }

    @Override
    public String validarTarjetaRegistro(String tarjeta) {
        return validarFormatoTarjeta(tarjeta);
    }

    @Override
    public String validarDatoEspecificoRegistro(String tipoRegistro, String datoEspecifico) {
        if (TIPO_ALUMNO_UPM.equals(tipoRegistro)) {
            return textoVacio(datoEspecifico) ? ERROR_MATRICULA_VACIA : null;
        }

        if (TIPO_PERSONAL_UPM.equals(tipoRegistro)) {
            return convertirEntero(datoEspecifico) < 0 ? ERROR_ANTIGUEDAD_INVALIDA : null;
        }

        return null;
    }

    @Override
    public String validarIBANRegistro(String iban) {
        return validarFormatoIBAN(iban);
    }

    @Override
    public String detectarTipoParticipantePorCorreo(String correo) {
        if (!validarCorreo(correo)) {
            return TIPO_CORREO_INVALIDO;
        }

        String correoNormalizado = correo.trim().toLowerCase();

        if (correoNormalizado.endsWith("@alumnos.upm.es")) {
            return TIPO_ALUMNO_UPM;
        }

        if (correoNormalizado.endsWith("@upm.es")) {
            return TIPO_PERSONAL_UPM;
        }

        return TIPO_EXTERNO;
    }

    @Override
    public boolean registrarParticipante(String nombre, String nick, String correo, String password,
                                  String dni, String tarjeta, String datoEspecifico,
                                  List<PreferenciaArtistica> preferenciasArtisticas){
    
        refrescarUsuarios();
        setUltimoError(null);

        String error = validarNombre(nombre);
        if (error != null) {
            setUltimoError(error);
            return false;
        }

        error = validarFormatoNick(nick);
        if (error != null) {
            setUltimoError(error);
            return false;
        }

        error = validarFormatoPassword(password);
        if (error != null) {
            setUltimoError(error);
            return false;
        }

        error = validarFormatoCorreo(correo);
        if (error != null) {
            setUltimoError(error);
            return false;
        }

        if (existeCorreo(correo)) {
            setUltimoError(ERROR_CORREO_DUPLICADO);
            return false;
        }

        if (existeNick(nick)) {
            setUltimoError(ERROR_NICK_DUPLICADO);
            return false;
        }

        error = validarFormatoDNI(dni);
        if (error != null) {
            setUltimoError(error);
            return false;
        }

        error = validarFormatoTarjeta(tarjeta);
        if (error != null) {
            setUltimoError(error);
            return false;
        }

        String tipo = detectarTipoParticipantePorCorreo(correo);
        String passwordCifrada = ValidadorDatosUsuario.cifrarPassword(password);

        if (TIPO_EXTERNO.equals(tipo)) {
            ParticipanteExterno participante = new ParticipanteExterno(nick, nombre, correo,
                    passwordCifrada, dni, tarjeta, preferenciasArtisticas);
            usuarios.add(participante);
            persistencia.guardarUsuarios(usuarios);
            return true;
        }

        if (TIPO_ALUMNO_UPM.equals(tipo)) {
            error = validarDatoEspecificoRegistro(tipo, datoEspecifico);
            if (error != null) {
                setUltimoError(error);
                return false;
            }

            if (!validarUPM(correo, password)) {
                setUltimoError(ERROR_VALIDACION_UPM);
                return false;
            }

            EstudianteUPM estudiante = new EstudianteUPM(nick, nombre, correo,
                    passwordCifrada, dni, tarjeta, datoEspecifico, preferenciasArtisticas);
            usuarios.add(estudiante);
            persistencia.guardarUsuarios(usuarios);
            return true;
        }

        if (TIPO_PERSONAL_UPM.equals(tipo)) {
            if (!validarUPM(correo, password)) {
                setUltimoError(ERROR_VALIDACION_UPM);
                return false;
            }

            error = validarDatoEspecificoRegistro(tipo, datoEspecifico);
            if (error != null) {
                setUltimoError(error);
                return false;
            }

            int antiguedad = convertirEntero(datoEspecifico);

            PersonalUPM personal = new PersonalUPM(nick, nombre, correo,
                    passwordCifrada, dni, tarjeta, antiguedad, preferenciasArtisticas);
            usuarios.add(personal);
            persistencia.guardarUsuarios(usuarios);
            return true;
        }

        setUltimoError("No se pudo registrar el participante por un error desconocido.");
        return false;
    }

    @Override
    public Usuario login(String correo, String password) {
        refrescarUsuarios();

        if (correo == null || password == null) {
            return null;
        }

        String passwordCifrada = ValidadorDatosUsuario.cifrarPassword(password);

        for (Usuario usuario : usuarios) {
            if (usuario.getCorreoElectronico().equalsIgnoreCase(correo.trim())
                    && usuario.getContrasena().equals(passwordCifrada)) {
                return usuario;
            }
        }

        return null;
    }

    @Override
    public boolean registrarInstructorComoAdministrador(Administrador administrador, String nombre, String nick,
                                                        String correo, String password, String dni, String iban) {
        refrescarUsuarios();
        setUltimoError(null);

        if (administrador == null) {
            setUltimoError("Administrador inválido.");
            return false;
        }

        String error = validarDatosComunes(nombre, nick, correo, password);
        if (error != null) {
            setUltimoError(error);
            return false;
        }

        error = validarFormatoDNI(dni);
        if (error != null) {
            setUltimoError(error);
            return false;
        }

        error = validarFormatoIBAN(iban);
        if (error != null) {
            setUltimoError(error);
            return false;
        }

        Instructor instructor = new Instructor(nick, nombre, correo,
                ValidadorDatosUsuario.cifrarPassword(password), dni, iban);
        usuarios.add(instructor);
        persistencia.guardarUsuarios(usuarios);
        return true;
    }

    @Override
    public boolean darDeBajaUsuarioComoAdministrador(Administrador administrador, String correoUsuario) {
        refrescarUsuarios();

        if (administrador == null || textoVacio(correoUsuario)) {
            return false;
        }

        if (administrador.getCorreoElectronico().equalsIgnoreCase(correoUsuario.trim())) {
            return false;
        }

        Usuario usuario = buscarUsuarioPorCorreo(correoUsuario);

        if (usuario == null) {
            return false;
        }

        usuarios.remove(usuario);
        persistencia.guardarUsuarios(usuarios);
        return true;
    }

    @Override
    public boolean darseDeBaja(Usuario usuario) {
        refrescarUsuarios();

        if (usuario == null || !esBajaPermitida(usuario)) {
            return false;
        }

        Usuario usuarioGuardado = buscarUsuarioPorCorreo(usuario.getCorreoElectronico());

        if (usuarioGuardado == null) {
            return false;
        }

        usuarios.remove(usuarioGuardado);
        persistencia.guardarUsuarios(usuarios);
        return true;
    }

    @Override
    public boolean actualizarPreferencias(ParticipanteExterno participante,
                                          List<PreferenciaArtistica> preferenciasArtisticas) {
        refrescarUsuarios();

        if (participante == null || preferenciasArtisticas == null) {
            return false;
        }

        Usuario usuarioGuardado = buscarUsuarioPorCorreo(participante.getCorreoElectronico());

        if (!tieneRolParticipante(usuarioGuardado)) {
            return false;
        }

        ParticipanteExterno participanteGuardado = (ParticipanteExterno) usuarioGuardado;
        participanteGuardado.setPreferenciasArtisticas(preferenciasArtisticas);
        persistencia.guardarUsuarios(usuarios);
        participante.setPreferenciasArtisticas(preferenciasArtisticas);
        return true;
    }

    @Override
    public boolean actualizarDatosParticipante(ParticipanteExterno participante, String nombre, String nick, String correo,
                                               String password, String dni, String tarjeta, String datoEspecifico) {
        refrescarUsuarios();
        setUltimoError(null);

        if (participante == null) {
            setUltimoError("Participante inválido.");
            return false;
        }

        Usuario usuarioGuardado = buscarUsuarioPorCorreo(participante.getCorreoElectronico());

        if (!tieneRolParticipante(usuarioGuardado)) {
            setUltimoError("No se pudo localizar el participante.");
            return false;
        }

        String error = validarNombre(nombre);
        if (error != null) {
            setUltimoError(error);
            return false;
        }

        error = validarFormatoNick(nick);
        if (error != null) {
            setUltimoError(error);
            return false;
        }

        error = validarFormatoCorreo(correo);
        if (error != null) {
            setUltimoError(error);
            return false;
        }

        if (existeNickDiferente(usuarioGuardado, nick)) {
            setUltimoError("El nick ya está en uso.");
            return false;
        }

        if (existeCorreoDiferente(usuarioGuardado, correo)) {
            setUltimoError("El correo electrónico ya está en uso.");
            return false;
        }

        error = validarFormatoDNI(dni);
        if (error != null) {
            setUltimoError(error);
            return false;
        }

        error = validarFormatoTarjeta(tarjeta);
        if (error != null) {
            setUltimoError(error);
            return false;
        }

        ParticipanteExterno participanteGuardado = (ParticipanteExterno) usuarioGuardado;
        String tipoActual = obtenerTipoRegistro(participanteGuardado);
        String tipoCorreo = detectarTipoParticipantePorCorreo(correo);

        // El cambio de correo no debe convertir un alumno UPM en externo, o al contrario.
        if (!tipoActual.equals(tipoCorreo)) {
            setUltimoError("El correo no corresponde con el tipo de participante.");
            return false;
        }

        error = textoVacio(password) ? null : validarFormatoPassword(password);
        if (error != null) {
            setUltimoError(error);
            return false;
        }

        if (tieneDatoEspecifico(participanteGuardado)) {
            if (!validarDatoEspecificoParticipante(participanteGuardado, datoEspecifico)) {
                setUltimoError("El dato específico no es válido.");
                return false;
            }
        }

        usuarioGuardado.setNombreCompleto(nombre.trim());
        usuarioGuardado.setNombreUsuario(nick.trim());
        usuarioGuardado.setCorreoElectronico(correo.trim());

        if (!textoVacio(password)) {
            usuarioGuardado.setContrasena(ValidadorDatosUsuario.cifrarPassword(password));
        }

        participanteGuardado.setDNI(dni.trim());
        participanteGuardado.setTarjetaCredito(tarjeta.trim());
        aplicarDatoUPMSiProcede(participanteGuardado, datoEspecifico);

        persistencia.guardarUsuarios(usuarios);

        participante.setNombreCompleto(usuarioGuardado.getNombreCompleto());
        participante.setNombreUsuario(usuarioGuardado.getNombreUsuario());
        participante.setCorreoElectronico(usuarioGuardado.getCorreoElectronico());
        participante.setDNI(participanteGuardado.getDNI());
        participante.setTarjetaCredito(participanteGuardado.getTarjetaCredito());

        if (!textoVacio(password)) {
            participante.setContrasena(ValidadorDatosUsuario.cifrarPassword(password));
        }

        if (tieneDatoEspecifico(participante)) {
            aplicarDatoUPMSiProcede(participante, datoEspecifico);
        }

        return true;
    }

    private boolean existeNickDiferente(Usuario usuarioActual, String nick) {
        if (nick == null) {
            return false;
        }

        String nickLimpio = nick.trim();

        for (Usuario usuario : usuarios) {
            if (usuario != usuarioActual && usuario.getNombreUsuario().equalsIgnoreCase(nickLimpio)) {
                return true;
            }
        }

        return false;
    }

    private boolean existeCorreoDiferente(Usuario usuarioActual, String correo) {
        if (correo == null) {
            return false;
        }

        String correoLimpio = correo.trim();

        for (Usuario usuario : usuarios) {
            if (usuario != usuarioActual && usuario.getCorreoElectronico().equalsIgnoreCase(correoLimpio)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public List<Instructor> listarInstructores(Administrador administrador) {
        refrescarUsuarios();

        if (administrador == null) {
            return new ArrayList<>();
        }

        List<Instructor> instructores = new ArrayList<>();

        for (Usuario usuario : usuarios) {
            if (usuario.getRol() == RolUsuario.INSTRUCTOR) {
                instructores.add((Instructor) usuario);
            }
        }

        return instructores;
    }

    @Override
    public List<ParticipanteExterno> listarParticipantes(Administrador administrador) {
        refrescarUsuarios();

        if (administrador == null) {
            return new ArrayList<>();
        }

        List<ParticipanteExterno> participantes = new ArrayList<>();

        for (Usuario usuario : usuarios) {
            if (tieneRolParticipante(usuario)) {
                participantes.add((ParticipanteExterno) usuario);
            }
        }

        return participantes;
    }

    @Override
    public double calcularDescuento(Usuario usuario) {
        if (usuario == null) {
            return 0.0;
        }

        return calcularDescuentoUsuario(usuario);
    }

    private boolean esBajaPermitida(Usuario usuario) {
        return usuario.getRol() == RolUsuario.INSTRUCTOR || tieneRolParticipante(usuario);
    }

    private String obtenerTipoRegistro(ParticipanteExterno participante) {
        switch (participante.getRol()) {
            case ESTUDIANTE_UPM:
                return TIPO_ALUMNO_UPM;
            case PERSONAL_UPM:
                return TIPO_PERSONAL_UPM;
            default:
                return TIPO_EXTERNO;
        }
    }

    private boolean tieneDatoEspecifico(ParticipanteExterno participante) {
        return participante.getRol() == RolUsuario.ESTUDIANTE_UPM
                || participante.getRol() == RolUsuario.PERSONAL_UPM;
    }

    private boolean validarDatoEspecificoParticipante(ParticipanteExterno participante, String datoEspecifico) {
        switch (participante.getRol()) {
            case ESTUDIANTE_UPM:
                return validarDatoEspecificoRegistro(TIPO_ALUMNO_UPM, datoEspecifico) == null;
            case PERSONAL_UPM:
                return validarDatoEspecificoRegistro(TIPO_PERSONAL_UPM, datoEspecifico) == null;
            default:
                return true;
        }
    }

    private void aplicarDatoUPMSiProcede(ParticipanteExterno participante, String datoEspecifico) {
        // Los participantes UPM guardan un dato adicional distinto segun su rol.
        switch (participante.getRol()) {
            case ESTUDIANTE_UPM:
                ((EstudianteUPM) participante).setNumeroMatricula(datoEspecifico.trim());
                break;
            case PERSONAL_UPM:
                ((PersonalUPM) participante).setAntiguedad(convertirEntero(datoEspecifico));
                break;
            default:
                break;
        }
    }

    private double calcularDescuentoUsuario(Usuario usuario) {
        switch (usuario.getRol()) {
            case ESTUDIANTE_UPM:
                return 0.25;
            case PERSONAL_UPM:
                PersonalUPM personal = (PersonalUPM) usuario;
                return Math.min(0.25 + personal.getAntiguedad() * 0.03, 0.5);
            default:
                return 0.0;
        }
    }

    private boolean tieneRolParticipante(Usuario usuario) {
        if (usuario == null) {
            return false;
        }

        return usuario.getRol() == RolUsuario.PARTICIPANTE_EXTERNO
                || usuario.getRol() == RolUsuario.ESTUDIANTE_UPM
                || usuario.getRol() == RolUsuario.PERSONAL_UPM;
    }

    private void refrescarUsuarios() {
        usuarios = persistencia.leerUsuarios();
    }

    private String validarDatosComunes(String nombre, String nick, String correo, String password) {
        String error = validarNombre(nombre);
        if (error != null) {
            return error;
        }

        error = validarFormatoNick(nick);
        if (error != null) {
            return error;
        }

        error = validarFormatoPassword(password);
        if (error != null) {
            return error;
        }

        error = validarFormatoCorreo(correo);
        if (error != null) {
            return error;
        }

        if (existeCorreo(correo)) {
            return ERROR_CORREO_DUPLICADO;
        }

        if (existeNick(nick)) {
            return ERROR_NICK_DUPLICADO;
        }

        return null;
    }

    private boolean validarUPM(String correo, String password) {
        if (this.validadorUPM == null) {
            return false;
        }

        return this.validadorUPM.verificarCredencialesUPM(correo, password);
    }

    private boolean validarCorreo(String correo) {
        return validarFormatoCorreo(correo) == null;
    }

    private String validarNombre(String nombre) {
        return textoVacio(nombre) ? ERROR_NOMBRE_VACIO : null;
    }

    private String validarFormatoNick(String nick) {
        return ValidadorDatosUsuario.validarNick(nick) ? null : ERROR_NICK_INVALIDO;
    }

    private String validarFormatoPassword(String password) {
        return ValidadorDatosUsuario.validarPassword(password) ? null : ERROR_PASSWORD_INVALIDA;
    }

    private String validarFormatoCorreo(String correo) {
        if (textoVacio(correo)) {
            return ERROR_CORREO_INVALIDO;
        }

        return PATRON_CORREO.matcher(correo.trim()).matches() ? null : ERROR_CORREO_INVALIDO;
    }

    private String validarFormatoDNI(String dni) {
        if (textoVacio(dni)) {
            return ERROR_DNI_INVALIDO;
        }

        return PATRON_DNI.matcher(dni.trim()).matches() ? null : ERROR_DNI_INVALIDO;
    }

    private String validarFormatoTarjeta(String tarjeta) {
        if (textoVacio(tarjeta)) {
            return ERROR_TARJETA_INVALIDA;
        }

        return PATRON_TARJETA.matcher(tarjeta.trim()).matches() ? null : ERROR_TARJETA_INVALIDA;
    }

    private String validarFormatoIBAN(String iban) {
        if (textoVacio(iban)) {
            return ERROR_IBAN_INVALIDO;
        }

        return PATRON_IBAN.matcher(iban.trim()).matches() ? null : ERROR_IBAN_INVALIDO;
    }

    private boolean existeCorreo(String correo) {
        return buscarUsuarioPorCorreo(correo) != null;
    }

    private boolean existeNick(String nick) {
        if (nick == null) {
            return false;
        }

        for (Usuario usuario : usuarios) {
            if (usuario.getNombreUsuario().equalsIgnoreCase(nick.trim())) {
                return true;
            }
        }

        return false;
    }

    private Usuario buscarUsuarioPorCorreo(String correo) {
        if (correo == null) {
            return null;
        }

        for (Usuario usuario : usuarios) {
            if (usuario.getCorreoElectronico().equalsIgnoreCase(correo.trim())) {
                return usuario;
            }
        }

        return null;
    }

    private int convertirEntero(String texto) {
        if (texto == null) {
            return -1;
        }

        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private boolean textoVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }
}
