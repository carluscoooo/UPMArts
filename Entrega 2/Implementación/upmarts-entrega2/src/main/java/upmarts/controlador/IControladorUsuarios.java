package upmarts.controlador;

import java.util.List;

import upmarts.integracion.IValidadorUPM;
import upmarts.modelo.Administrador;
import upmarts.modelo.Participante;
import upmarts.modelo.PreferenciaArtistica;
import upmarts.modelo.Usuario;

public interface IControladorUsuarios {

    String detectarTipoParticipantePorCorreo(String correo);

    boolean registrarParticipante(String nombre, String nick, String correo, String password,
                                  String dni, String tarjeta, String datoEspecifico,
                                  List<PreferenciaArtistica> preferenciasArtisticas,
                                  IValidadorUPM validadorUPM);

    Usuario login(String correo, String password);

    boolean registrarInstructorComoAdministrador(Administrador administrador, String nombre, String nick,
                                                 String correo, String password, String dni, String iban);

    boolean darDeBajaUsuarioComoAdministrador(Administrador administrador, String correoUsuario);

    boolean darseDeBaja(Usuario usuario);

    boolean actualizarPreferencias(Participante participante, List<PreferenciaArtistica> preferenciasArtisticas);

    List<Usuario> listarUsuarios(Administrador administrador);
}
