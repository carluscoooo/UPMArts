package upmarts.controlador;

import java.util.List;

import upmarts.modelo.Administrador;
import upmarts.modelo.Instructor;
import upmarts.modelo.ParticipanteExterno;
import upmarts.modelo.PreferenciaArtistica;
import upmarts.modelo.Usuario;

public interface IControladorUsuarios {

    String detectarTipoParticipantePorCorreo(String correo);

    boolean registrarParticipante(String nombre, String nick, String correo, String password,
                                  String dni, String tarjeta, String datoEspecifico,
                                  List<PreferenciaArtistica> preferenciasArtisticas);

    String getUltimoError();

    String validarNombreRegistro(String nombre);

    String validarNickRegistro(String nick);

    String validarCorreoRegistro(String correo);

    String validarPasswordRegistro(String password);

    String validarDNIRegistro(String dni);

    String validarTarjetaRegistro(String tarjeta);

    String validarDatoEspecificoRegistro(String tipoRegistro, String datoEspecifico);

    String validarIBANRegistro(String iban);

    String validarNivelPreferencia(String nivel);

    Usuario login(String correo, String password);

    List<Instructor> listarInstructores(Administrador administrador);

    List<ParticipanteExterno> listarParticipantes(Administrador administrador);

    double calcularDescuento(Usuario usuario);

    boolean registrarInstructorComoAdministrador(Administrador administrador, String nombre, String nick,
                                                 String correo, String password, String dni, String iban);

    boolean darDeBajaUsuarioComoAdministrador(Administrador administrador, String correoUsuario);

    boolean darseDeBaja(Usuario usuario);

    boolean actualizarPreferencias(ParticipanteExterno participante, List<PreferenciaArtistica> preferenciasArtisticas);

    boolean actualizarDatosParticipante(ParticipanteExterno participante, String nombre, String nick, String correo,
                                       String password, String dni, String tarjeta, String datoEspecifico);

}
