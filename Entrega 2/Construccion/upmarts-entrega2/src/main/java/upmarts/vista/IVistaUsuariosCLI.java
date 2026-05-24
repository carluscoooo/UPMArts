package upmarts.vista;

import upmarts.modelo.Administrador;
import upmarts.modelo.ParticipanteExterno;
import upmarts.modelo.Usuario;

public interface IVistaUsuariosCLI {
    void registrarParticipante();

    Usuario iniciarSesion();

    void registrarInstructor(Administrador administrador);

    void darDeBajaComoAdministrador(Administrador administrador);

    void listarParticipantes(Administrador administrador);

    void listarInstructores(Administrador administrador);

    void mostrarDatosUsuario(Usuario usuario);

    void mostrarPreferencias(ParticipanteExterno participante);

    void modificarPreferencias(ParticipanteExterno participante);

    void modificarDatosParticipante(ParticipanteExterno participante);

    boolean darseDeBaja(Usuario usuario);
}
