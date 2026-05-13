package upmarts.controlador;

import java.util.ArrayList;
import java.util.List;

import upmarts.integracion.IValidadorUPM;
import upmarts.modelo.Administrador;
import upmarts.modelo.EstudianteUPM;
import upmarts.modelo.Instructor;
import upmarts.modelo.Participante;
import upmarts.modelo.ParticipanteExterno;
import upmarts.modelo.PersonalUPM;
import upmarts.modelo.PreferenciaArtistica;
import upmarts.modelo.Usuario;
import upmarts.persistencia.IAccesoUsuarios;

public class ControladorUsuarios implements IControladorUsuarios {

    public static final String TIPO_ALUMNO_UPM = "ALUMNO_UPM";
    public static final String TIPO_PERSONAL_UPM = "PERSONAL_UPM";
    public static final String TIPO_EXTERNO = "EXTERNO";
    public static final String TIPO_CORREO_INVALIDO = "CORREO_INVALIDO";

    private IAccesoUsuarios persistencia;
    private List<Usuario> usuarios;
    private String ultimoError;

    public ControladorUsuarios(IAccesoUsuarios persistencia) {
        this.persistencia = persistencia;
        this.usuarios = persistencia.leerUsuarios();
    }

    public String getUltimoError() {
        return ultimoError;
    }

    private void setUltimoError(String mensaje) {
        this.ultimoError = mensaje;
    }

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

    public boolean registrarParticipante(String nombre, String nick, String correo, String password,
                                         String dni, String tarjeta, String datoEspecifico,
                                         List<PreferenciaArtistica> preferenciasArtisticas,
                                         IValidadorUPM validadorUPM) {
        refrescarUsuarios();
        setUltimoError(null);

        if (textoVacio(nombre)) {
            setUltimoError("El nombre completo no puede estar vacío.");
            return false;
        }

        if (!Usuario.validarNick(nick)) {
            setUltimoError("Nick inválido. Debe tener entre 4 y 12 caracteres alfanuméricos y no usar términos conflictivos.");
            return false;
        }

        if (!Usuario.validarPassword(password)) {
            setUltimoError("Contraseña inválida. Debe tener al menos 12 caracteres, incluir mayúsculas, minúsculas y números.");
            return false;
        }

        if (!validarCorreo(correo)) {
            setUltimoError("Correo electrónico inválido.");
            return false;
        }

        if (existeCorreo(correo)) {
            setUltimoError("Ya existe un usuario registrado con ese correo.");
            return false;
        }

        if (existeNick(nick)) {
            setUltimoError("Ya existe un usuario registrado con ese nick.");
            return false;
        }

        if (!validarDNI(dni)) {
            setUltimoError("DNI inválido. Debe tener 8 dígitos seguidos de una letra.");
            return false;
        }

        if (!validarTarjeta(tarjeta)) {
            setUltimoError("Número de tarjeta inválido. Debe contener entre 8 y 19 dígitos.");
            return false;
        }

        String tipo = detectarTipoParticipantePorCorreo(correo);
        String passwordCifrada = Usuario.cifrarPassword(password);

        if (TIPO_EXTERNO.equals(tipo)) {
            ParticipanteExterno participante = new ParticipanteExterno(nick, nombre, correo,
                    passwordCifrada, dni, tarjeta, preferenciasArtisticas);
            usuarios.add(participante);
            persistencia.guardarUsuarios(usuarios);
            return true;
        }

        if (TIPO_ALUMNO_UPM.equals(tipo)) {
            if (textoVacio(datoEspecifico)) {
                setUltimoError("El número de matrícula no puede estar vacío.");
                return false;
            }

            if (!validarUPM(correo, password, validadorUPM)) {
                setUltimoError("No se ha podido validar la cuenta UPM. Compruebe correo y contraseña UPM.");
                return false;
            }

            EstudianteUPM estudiante = new EstudianteUPM(nick, nombre, correo,
                    passwordCifrada, dni, tarjeta, datoEspecifico, preferenciasArtisticas);
            usuarios.add(estudiante);
            persistencia.guardarUsuarios(usuarios);
            return true;
        }

        if (TIPO_PERSONAL_UPM.equals(tipo)) {
            if (!validarUPM(correo, password, validadorUPM)) {
                setUltimoError("No se ha podido validar la cuenta UPM. Compruebe correo y contraseña UPM.");
                return false;
            }

            int antiguedad = convertirEntero(datoEspecifico);

            if (antiguedad < 0) {
                setUltimoError("La antigüedad debe ser un número entero válido.");
                return false;
            }

            PersonalUPM personal = new PersonalUPM(nick, nombre, correo,
                    passwordCifrada, dni, tarjeta, antiguedad, preferenciasArtisticas);
            usuarios.add(personal);
            persistencia.guardarUsuarios(usuarios);
            return true;
        }

        setUltimoError("No se pudo registrar el participante por un error desconocido.");
        return false;
    }

    public Usuario login(String correo, String password) {
        refrescarUsuarios();

        if (correo == null || password == null) {
            return null;
        }

        String passwordCifrada = Usuario.cifrarPassword(password);

        for (Usuario usuario : usuarios) {
            if (usuario.getCorreoElectronico().equalsIgnoreCase(correo.trim())
                    && usuario.getContrasena().equals(passwordCifrada)) {
                return usuario;
            }
        }

        return null;
    }

    public boolean registrarInstructorComoAdministrador(Administrador administrador, String nombre, String nick,
                                                        String correo, String password, String dni, String iban) {
        refrescarUsuarios();

        if (administrador == null) {
            return false;
        }

        if (!validarDatosComunes(nombre, nick, correo, password)) {
            return false;
        }

        if (!validarDNI(dni) || !validarIBAN(iban)) {
            return false;
        }

        Instructor instructor = new Instructor(nick, nombre, correo,
                Usuario.cifrarPassword(password), dni, iban);
        usuarios.add(instructor);
        persistencia.guardarUsuarios(usuarios);
        return true;
    }

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

    public boolean darseDeBaja(Usuario usuario) {
        refrescarUsuarios();

        if (usuario == null || !usuario.puedeDarseDeBaja()) {
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

    public boolean actualizarPreferencias(Participante participante,
                                          List<PreferenciaArtistica> preferenciasArtisticas) {
        refrescarUsuarios();

        if (participante == null || preferenciasArtisticas == null) {
            return false;
        }

        Usuario usuarioGuardado = buscarUsuarioPorCorreo(participante.getCorreoElectronico());

        if (usuarioGuardado == null || !usuarioGuardado.esParticipante()) {
            return false;
        }

        Participante participanteGuardado = (Participante) usuarioGuardado;
        participanteGuardado.setPreferenciasArtisticas(preferenciasArtisticas);
        persistencia.guardarUsuarios(usuarios);
        participante.setPreferenciasArtisticas(preferenciasArtisticas);
        return true;
    }

    public boolean actualizarDatosParticipante(Participante participante, String nombre, String nick, String correo,
                                               String password, String dni, String tarjeta, String datoEspecifico) {
        refrescarUsuarios();
        setUltimoError(null);

        if (participante == null) {
            setUltimoError("Participante inválido.");
            return false;
        }

        Usuario usuarioGuardado = buscarUsuarioPorCorreo(participante.getCorreoElectronico());

        if (usuarioGuardado == null || !usuarioGuardado.esParticipante()) {
            setUltimoError("No se pudo localizar el participante.");
            return false;
        }

        if (textoVacio(nombre)) {
            setUltimoError("El nombre completo no puede estar vacío.");
            return false;
        }

        if (!Usuario.validarNick(nick)) {
            setUltimoError("Nick inválido. Debe tener entre 4 y 12 caracteres alfanuméricos y no usar términos conflictivos.");
            return false;
        }

        if (!validarCorreo(correo)) {
            setUltimoError("Correo electrónico inválido.");
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

        if (!validarDNI(dni)) {
            setUltimoError("DNI inválido. Debe tener 8 dígitos y una letra.");
            return false;
        }

        if (!validarTarjeta(tarjeta)) {
            setUltimoError("Tarjeta inválida. Debe tener entre 8 y 19 dígitos.");
            return false;
        }

        Participante participanteGuardado = (Participante) usuarioGuardado;
        String tipoActual = participanteGuardado.getTipoRegistro();
        String tipoCorreo = detectarTipoParticipantePorCorreo(correo);

        if (!tipoActual.equals(tipoCorreo)) {
            setUltimoError("El correo no corresponde con el tipo de participante.");
            return false;
        }

        if (!textoVacio(password) && !Usuario.validarPassword(password)) {
            setUltimoError("Contraseña inválida. Debe tener al menos 12 caracteres, incluir mayúsculas, minúsculas y números.");
            return false;
        }

        if (!participanteGuardado.getEtiquetaDatoEspecifico().isEmpty()) {
            if (!participanteGuardado.validarDatoEspecifico(datoEspecifico)) {
                setUltimoError("El dato específico no es válido.");
                return false;
            }
        }

        usuarioGuardado.setNombreCompleto(nombre.trim());
        usuarioGuardado.setNombreUsuario(nick.trim());
        usuarioGuardado.setCorreoElectronico(correo.trim());

        if (!textoVacio(password)) {
            usuarioGuardado.setContrasena(Usuario.cifrarPassword(password));
        }

        participanteGuardado.setDNI(dni.trim());
        participanteGuardado.setTarjetaCredito(tarjeta.trim());
        participanteGuardado.actualizarDatoEspecifico(datoEspecifico);

        persistencia.guardarUsuarios(usuarios);

        participante.setNombreCompleto(usuarioGuardado.getNombreCompleto());
        participante.setNombreUsuario(usuarioGuardado.getNombreUsuario());
        participante.setCorreoElectronico(usuarioGuardado.getCorreoElectronico());
        participante.setDNI(participanteGuardado.getDNI());
        participante.setTarjetaCredito(participanteGuardado.getTarjetaCredito());

        if (!textoVacio(password)) {
            participante.setContrasena(Usuario.cifrarPassword(password));
        }

        if (!participante.getEtiquetaDatoEspecifico().isEmpty()) {
            participante.actualizarDatoEspecifico(datoEspecifico);
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

    public List<Usuario> listarUsuarios(Administrador administrador) {
        refrescarUsuarios();

        if (administrador == null) {
            return new ArrayList<Usuario>();
        }

        return new ArrayList<Usuario>(usuarios);
    }

    public List<Instructor> listarInstructores(Administrador administrador) {
        refrescarUsuarios();

        if (administrador == null) {
            return new ArrayList<Instructor>();
        }

        List<Instructor> instructores = new ArrayList<Instructor>();

        for (Usuario usuario : usuarios) {
            if (usuario.esInstructor()) {
                instructores.add((Instructor) usuario);
            }
        }

        return instructores;
    }

    public List<Participante> listarParticipantes(Administrador administrador) {
        refrescarUsuarios();

        if (administrador == null) {
            return new ArrayList<Participante>();
        }

        List<Participante> participantes = new ArrayList<Participante>();

        for (Usuario usuario : usuarios) {
            if (usuario.esParticipante()) {
                participantes.add((Participante) usuario);
            }
        }

        return participantes;
    }

    public double calcularDescuento(Usuario usuario) {
        if (usuario == null) {
            return 0.0;
        }

        return usuario.obtenerDescuento();
    }

    private void refrescarUsuarios() {
        usuarios = persistencia.leerUsuarios();
    }

    private boolean validarDatosComunes(String nombre, String nick, String correo, String password) {
        if (textoVacio(nombre)) {
            return false;
        }

        if (!Usuario.validarNick(nick)) {
            return false;
        }

        if (!Usuario.validarPassword(password)) {
            return false;
        }

        if (!validarCorreo(correo)) {
            return false;
        }

        if (existeCorreo(correo) || existeNick(nick)) {
            return false;
        }

        return true;
    }

    private boolean validarUPM(String correo, String password, IValidadorUPM validadorUPM) {
        if (validadorUPM == null) {
            return false;
        }

        return validadorUPM.verificarCredencialesUPM(correo, password);
    }

    private boolean validarCorreo(String correo) {
        if (textoVacio(correo)) {
            return false;
        }

        String correoLimpio = correo.trim();
        int posicionArroba = correoLimpio.indexOf('@');

        return posicionArroba > 0 && posicionArroba < correoLimpio.length() - 1;
    }

    private boolean validarDNI(String dni) {
        if (textoVacio(dni)) {
            return false;
        }

        return dni.trim().matches("[0-9]{8}[A-Za-z]");
    }

    private boolean validarTarjeta(String tarjeta) {
        if (textoVacio(tarjeta)) {
            return false;
        }

        return tarjeta.trim().matches("[0-9]{8,19}");
    }

    private boolean validarIBAN(String iban) {
        if (textoVacio(iban)) {
            return false;
        }

        return iban.trim().matches("[A-Za-z]{2}[0-9A-Za-z]{13,32}");
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
        try {
            return Integer.parseInt(texto.trim());
        } catch (Exception e) {
            return -1;
        }
    }

    private boolean textoVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }
}
