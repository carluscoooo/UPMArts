package upmarts.controlador;

import java.util.ArrayList;
import java.util.List;

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
    private static final String ERROR_CORREO_NO_REGISTRADO =
            "Correo electrónico no registrado. No se puede iniciar sesión.";
    private static final String ERROR_PASSWORD_ERRONEA = "Contraseña errónea.";
    private static final String ERROR_CORREO_DUPLICADO = "El correo electrónico ya está en uso. Use otro.";
    private static final String ERROR_NICK_DUPLICADO = "El nick ya está en uso. Use otro.";
    private static final String ERROR_VALIDACION_UPM =
            "No se ha podido validar la cuenta UPM. Compruebe correo y contraseña UPM.";
    private static final String ERROR_PERSISTENCIA_LECTURA =
            "No se pudieron cargar los datos guardados.";
    private static final String ERROR_PERSISTENCIA_GUARDADO =
            "No se pudieron guardar los cambios.";

    private final IAccesoUsuarios persistencia;
    private final IValidadorUPM validadorUPM;
    private List<Usuario> usuarios;
    private String ultimoError;

    public ControladorUsuarios() {
        this(new GestorFicheroUsuarios(RUTA_USUARIOS), new AdaptadorLDAP());
    }

    ControladorUsuarios(IAccesoUsuarios persistencia, IValidadorUPM validadorUPM) {
        this.persistencia = persistencia;
        List<Usuario> usuariosLeidos = persistencia.leerUsuarios();
        this.usuarios = usuariosLeidos != null ? usuariosLeidos : new ArrayList<>();
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
            guardarUsuarios();
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

    @Override
    public String validarNombreRegistro(String nombre) {
        return ValidadorDatosUsuario.validarNombre(nombre);
    }

    @Override
    public String validarNickRegistro(String nick) {
        if (!refrescarUsuarios()) {
            return ultimoError;
        }

        String error = ValidadorDatosUsuario.validarNickConMensaje(nick);
        if (error != null) {
            return error;
        }

        return existeNick(nick) ? ERROR_NICK_DUPLICADO : null;
    }

    @Override
    public String validarCorreoRegistro(String correo) {
        if (!refrescarUsuarios()) {
            return ultimoError;
        }

        String error = ValidadorDatosUsuario.validarCorreo(correo);
        if (error != null) {
            return error;
        }

        return buscarUsuarioPorCorreo(correo) != null ? ERROR_CORREO_DUPLICADO : null;
    }

    @Override
    public String validarPasswordRegistro(String password) {
        return ValidadorDatosUsuario.validarPasswordConMensaje(password);
    }

    @Override
    public String validarDNIRegistro(String dni) {
        return ValidadorDatosUsuario.validarDNI(dni);
    }

    @Override
    public String validarTarjetaRegistro(String tarjeta) {
        return ValidadorDatosUsuario.validarTarjeta(tarjeta);
    }

    @Override
    public String validarDatoEspecificoRegistro(String tipoRegistro, String datoEspecifico) {
        if (TIPO_ALUMNO_UPM.equals(tipoRegistro)) {
            return ValidadorDatosUsuario.validarMatricula(datoEspecifico);
        }

        if (TIPO_PERSONAL_UPM.equals(tipoRegistro)) {
            return ValidadorDatosUsuario.validarAntiguedad(datoEspecifico);
        }

        return null;
    }

    @Override
    public String validarIBANRegistro(String iban) {
        return ValidadorDatosUsuario.validarIBAN(iban);
    }

    @Override
    public String validarNivelPreferencia(String nivel) {
        return ValidadorDatosUsuario.validarNivelPreferencia(nivel);
    }

    @Override
    public String detectarTipoParticipantePorCorreo(String correo) {
        if (ValidadorDatosUsuario.validarCorreo(correo) != null) {
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
                                  List<PreferenciaArtistica> preferenciasArtisticas) {

        if (!refrescarUsuarios()) {
            return false;
        }
        ultimoError = null;

        String error = validarDatosComunes(nombre, nick, correo, password);
        if (error != null) {
            ultimoError = error;
            return false;
        }

        error = ValidadorDatosUsuario.validarDNI(dni);
        if (error != null) {
            ultimoError = error;
            return false;
        }

        error = ValidadorDatosUsuario.validarTarjeta(tarjeta);
        if (error != null) {
            ultimoError = error;
            return false;
        }

        String tipo = detectarTipoParticipantePorCorreo(correo);
        String passwordCifrada = ValidadorDatosUsuario.cifrarPassword(password);

        if (TIPO_EXTERNO.equals(tipo)) {
            ParticipanteExterno participante = new ParticipanteExterno(nick, nombre, correo,
                    passwordCifrada, dni, tarjeta, preferenciasArtisticas);
            usuarios.add(participante);
            return guardarUsuarios();
        }

        if (TIPO_ALUMNO_UPM.equals(tipo)) {
            error = validarDatoEspecificoRegistro(tipo, datoEspecifico);
            if (error != null) {
                ultimoError = error;
                return false;
            }

            if (validadorUPM == null || !validadorUPM.verificarCredencialesUPM(correo, password)) {
                ultimoError = ERROR_VALIDACION_UPM;
                return false;
            }

            EstudianteUPM estudiante = new EstudianteUPM(nick, nombre, correo,
                    passwordCifrada, dni, tarjeta, datoEspecifico, preferenciasArtisticas);
            usuarios.add(estudiante);
            return guardarUsuarios();
        }

        if (TIPO_PERSONAL_UPM.equals(tipo)) {
            if (validadorUPM == null || !validadorUPM.verificarCredencialesUPM(correo, password)) {
                ultimoError = ERROR_VALIDACION_UPM;
                return false;
            }

            error = validarDatoEspecificoRegistro(tipo, datoEspecifico);
            if (error != null) {
                ultimoError = error;
                return false;
            }

            int antiguedad = convertirEntero(datoEspecifico);

            PersonalUPM personal = new PersonalUPM(nick, nombre, correo,
                    passwordCifrada, dni, tarjeta, antiguedad, preferenciasArtisticas);
            usuarios.add(personal);
            return guardarUsuarios();
        }

        ultimoError = "No se pudo registrar el participante por un error desconocido.";
        return false;
    }

    @Override
    public Usuario login(String correo, String password) {
        if (!refrescarUsuarios()) {
            return null;
        }
        ultimoError = null;

        String errorCorreo = ValidadorDatosUsuario.validarCorreo(correo);
        if (errorCorreo != null) {
            ultimoError = errorCorreo;
            return null;
        }

        String errorPassword = ValidadorDatosUsuario.validarPasswordLogin(password);
        if (errorPassword != null) {
            ultimoError = errorPassword;
            return null;
        }

        Usuario usuario = buscarUsuarioPorCorreo(correo);
        if (usuario == null) {
            ultimoError = ERROR_CORREO_NO_REGISTRADO;
            return null;
        }

        String passwordCifrada = ValidadorDatosUsuario.cifrarPassword(password);

        if (!usuario.getContrasena().equals(passwordCifrada)) {
            ultimoError = ERROR_PASSWORD_ERRONEA;
            return null;
        }

        return usuario;
    }

    @Override
    public boolean registrarInstructorComoAdministrador(Administrador administrador, String nombre, String nick,
                                                        String correo, String password, String dni, String iban) {
        if (!refrescarUsuarios()) {
            return false;
        }
        ultimoError = null;

        if (administrador == null) {
            ultimoError = "Administrador inválido.";
            return false;
        }

        String error = validarDatosComunes(nombre, nick, correo, password);
        if (error != null) {
            ultimoError = error;
            return false;
        }

        error = ValidadorDatosUsuario.validarDNI(dni);
        if (error != null) {
            ultimoError = error;
            return false;
        }

        error = ValidadorDatosUsuario.validarIBAN(iban);
        if (error != null) {
            ultimoError = error;
            return false;
        }

        Instructor instructor = new Instructor(nick, nombre, correo,
                ValidadorDatosUsuario.cifrarPassword(password), dni, iban);
        usuarios.add(instructor);
        return guardarUsuarios();
    }

    @Override
    public boolean darDeBajaUsuarioComoAdministrador(Administrador administrador, String correoUsuario) {
        if (!refrescarUsuarios()) {
            return false;
        }

        if (administrador == null || ValidadorDatosUsuario.textoVacio(correoUsuario)) {
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
        return guardarUsuarios();
    }

    @Override
    public boolean darseDeBaja(Usuario usuario) {
        if (!refrescarUsuarios()) {
            return false;
        }

        if (usuario == null || (usuario.getRol() != RolUsuario.INSTRUCTOR && !tieneRolParticipante(usuario))) {
            return false;
        }

        Usuario usuarioGuardado = buscarUsuarioPorCorreo(usuario.getCorreoElectronico());

        if (usuarioGuardado == null) {
            return false;
        }

        usuarios.remove(usuarioGuardado);
        return guardarUsuarios();
    }

    @Override
    public boolean actualizarPreferencias(ParticipanteExterno participante,
                                          List<PreferenciaArtistica> preferenciasArtisticas) {
        if (!refrescarUsuarios()) {
            return false;
        }

        if (participante == null || preferenciasArtisticas == null) {
            return false;
        }

        Usuario usuarioGuardado = buscarUsuarioPorCorreo(participante.getCorreoElectronico());

        if (!tieneRolParticipante(usuarioGuardado)) {
            return false;
        }

        ParticipanteExterno participanteGuardado = (ParticipanteExterno) usuarioGuardado;
        participanteGuardado.setPreferenciasArtisticas(preferenciasArtisticas);
        if (!guardarUsuarios()) {
            return false;
        }
        participante.setPreferenciasArtisticas(preferenciasArtisticas);
        return true;
    }

    @Override
    public boolean actualizarDatosParticipante(ParticipanteExterno participante, String nombre, String nick, String correo,
                                               String password, String dni, String tarjeta, String datoEspecifico) {
        if (!refrescarUsuarios()) {
            return false;
        }
        ultimoError = null;

        if (participante == null) {
            ultimoError = "Participante inválido.";
            return false;
        }

        Usuario usuarioGuardado = buscarUsuarioPorCorreo(participante.getCorreoElectronico());

        if (!tieneRolParticipante(usuarioGuardado)) {
            ultimoError = "No se pudo localizar el participante.";
            return false;
        }

        String error = ValidadorDatosUsuario.validarNombre(nombre);
        if (error != null) {
            ultimoError = error;
            return false;
        }

        error = ValidadorDatosUsuario.validarNickConMensaje(nick);
        if (error != null) {
            ultimoError = error;
            return false;
        }

        error = ValidadorDatosUsuario.validarCorreo(correo);
        if (error != null) {
            ultimoError = error;
            return false;
        }

        if (existeNickDiferente(usuarioGuardado, nick)) {
            ultimoError = ERROR_NICK_DUPLICADO;
            return false;
        }

        if (existeCorreoDiferente(usuarioGuardado, correo)) {
            ultimoError = ERROR_CORREO_DUPLICADO;
            return false;
        }

        error = ValidadorDatosUsuario.validarDNI(dni);
        if (error != null) {
            ultimoError = error;
            return false;
        }

        error = ValidadorDatosUsuario.validarTarjeta(tarjeta);
        if (error != null) {
            ultimoError = error;
            return false;
        }

        ParticipanteExterno participanteGuardado = (ParticipanteExterno) usuarioGuardado;
        String tipoActual = obtenerTipoRegistro(participanteGuardado);
        String tipoCorreo = detectarTipoParticipantePorCorreo(correo);

        // El cambio de correo no debe convertir un alumno UPM en externo, o al contrario.
        if (!tipoActual.equals(tipoCorreo)) {
            ultimoError = "El correo no corresponde con el tipo de participante.";
            return false;
        }

        error = ValidadorDatosUsuario.textoVacio(password)
                ? null
                : ValidadorDatosUsuario.validarPasswordConMensaje(password);
        if (error != null) {
            ultimoError = error;
            return false;
        }

        error = obtenerErrorDatoEspecificoParticipante(participanteGuardado, datoEspecifico);
        if (error != null) {
            ultimoError = error;
            return false;
        }

        usuarioGuardado.setNombreCompleto(nombre.trim());
        usuarioGuardado.setNombreUsuario(nick.trim());
        usuarioGuardado.setCorreoElectronico(correo.trim());

        if (!ValidadorDatosUsuario.textoVacio(password)) {
            usuarioGuardado.setContrasena(ValidadorDatosUsuario.cifrarPassword(password));
        }

        participanteGuardado.setDNI(dni.trim());
        participanteGuardado.setTarjetaCredito(tarjeta.trim());
        aplicarDatoUPMSiProcede(participanteGuardado, datoEspecifico);

        if (!guardarUsuarios()) {
            return false;
        }

        participante.setNombreCompleto(usuarioGuardado.getNombreCompleto());
        participante.setNombreUsuario(usuarioGuardado.getNombreUsuario());
        participante.setCorreoElectronico(usuarioGuardado.getCorreoElectronico());
        participante.setDNI(participanteGuardado.getDNI());
        participante.setTarjetaCredito(participanteGuardado.getTarjetaCredito());

        if (!ValidadorDatosUsuario.textoVacio(password)) {
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
        if (!refrescarUsuarios()) {
            return new ArrayList<>();
        }

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
        if (!refrescarUsuarios()) {
            return new ArrayList<>();
        }

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

    private String obtenerErrorDatoEspecificoParticipante(ParticipanteExterno participante, String datoEspecifico) {
        switch (participante.getRol()) {
            case ESTUDIANTE_UPM:
                return validarDatoEspecificoRegistro(TIPO_ALUMNO_UPM, datoEspecifico);
            case PERSONAL_UPM:
                return validarDatoEspecificoRegistro(TIPO_PERSONAL_UPM, datoEspecifico);
            default:
                return null;
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

    private boolean tieneRolParticipante(Usuario usuario) {
        if (usuario == null) {
            return false;
        }

        return usuario.getRol() == RolUsuario.PARTICIPANTE_EXTERNO
                || usuario.getRol() == RolUsuario.ESTUDIANTE_UPM
                || usuario.getRol() == RolUsuario.PERSONAL_UPM;
    }

    private boolean refrescarUsuarios() {
        try {
            List<Usuario> usuariosLeidos = persistencia.leerUsuarios();
            usuarios = usuariosLeidos != null ? usuariosLeidos : new ArrayList<>();
            return true;
        } catch (RuntimeException e) {
            usuarios = new ArrayList<>();
            ultimoError = ERROR_PERSISTENCIA_LECTURA;
            return false;
        }
    }

    private boolean guardarUsuarios() {
        try {
            persistencia.guardarUsuarios(usuarios);
            return true;
        } catch (RuntimeException e) {
            ultimoError = ERROR_PERSISTENCIA_GUARDADO;
            return false;
        }
    }

    private String validarDatosComunes(String nombre, String nick, String correo, String password) {
        String error = ValidadorDatosUsuario.validarNombre(nombre);
        if (error != null) {
            return error;
        }

        error = ValidadorDatosUsuario.validarNickConMensaje(nick);
        if (error != null) {
            return error;
        }

        error = ValidadorDatosUsuario.validarPasswordConMensaje(password);
        if (error != null) {
            return error;
        }

        error = ValidadorDatosUsuario.validarCorreo(correo);
        if (error != null) {
            return error;
        }

        if (buscarUsuarioPorCorreo(correo) != null) {
            return ERROR_CORREO_DUPLICADO;
        }

        if (existeNick(nick)) {
            return ERROR_NICK_DUPLICADO;
        }

        return null;
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

}
