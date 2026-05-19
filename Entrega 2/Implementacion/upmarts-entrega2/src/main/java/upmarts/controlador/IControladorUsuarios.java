package upmarts.controlador;

import java.util.List;

import upmarts.integracion.IValidadorUPM;
import upmarts.modelo.Administrador;
import upmarts.modelo.Instructor;
import upmarts.modelo.Participante;
import upmarts.modelo.PreferenciaArtistica;
import upmarts.modelo.Usuario;

public interface IControladorUsuarios {

    String detectarTipoParticipantePorCorreo(String correo);

    boolean registrarParticipante(String nombre, String nick, String correo, String password,
                                  String dni, String tarjeta, String datoEspecifico,
                                  List<PreferenciaArtistica> preferenciasArtisticas,
                                  IValidadorUPM validadorUPM);

    String getUltimoError();

    Usuario login(String correo, String password);

    List<Instructor> listarInstructores(Administrador administrador);

    List<Participante> listarParticipantes(Administrador administrador);

    double calcularDescuento(Usuario usuario);

    boolean registrarInstructorComoAdministrador(Administrador administrador, String nombre, String nick,
                                                 String correo, String password, String dni, String iban);

    boolean darDeBajaUsuarioComoAdministrador(Administrador administrador, String correoUsuario);

    boolean darseDeBaja(Usuario usuario);

    boolean actualizarPreferencias(Participante participante, List<PreferenciaArtistica> preferenciasArtisticas);

    boolean actualizarDatosParticipante(Participante participante, String nombre, String nick, String correo,
                                       String password, String dni, String tarjeta, String datoEspecifico);

    List<Usuario> listarUsuarios(Administrador administrador);
}
