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

    public ControladorUsuarios(IAccesoUsuarios persistencia) {
        this.persistencia = persistencia;
        this.usuarios = persistencia.leerUsuarios();
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

        if (!validarDatosComunes(nombre, nick, correo, password)) {
            return false;
        }

        if (!validarDNI(dni) || !validarTarjeta(tarjeta)) {
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
            if (!validarUPM(correo, password, validadorUPM) || textoVacio(datoEspecifico)) {
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
                return false;
            }

            int antiguedad = convertirEntero(datoEspecifico);

            if (antiguedad < 0) {
                return false;
            }

            PersonalUPM personal = new PersonalUPM(nick, nombre, correo,
                    passwordCifrada, dni, tarjeta, antiguedad, preferenciasArtisticas);
            usuarios.add(personal);
            persistencia.guardarUsuarios(usuarios);
            return true;
        }

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

        if (usuario == null || usuario instanceof Administrador) {
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

        if (!(usuarioGuardado instanceof Participante)) {
            return false;
        }

        Participante participanteGuardado = (Participante) usuarioGuardado;
        participanteGuardado.setPreferenciasArtisticas(preferenciasArtisticas);
        persistencia.guardarUsuarios(usuarios);
        participante.setPreferenciasArtisticas(preferenciasArtisticas);
        return true;
    }

    public List<Usuario> listarUsuarios(Administrador administrador) {
        refrescarUsuarios();

        if (administrador == null) {
            return new ArrayList<Usuario>();
        }

        return new ArrayList<Usuario>(usuarios);
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
