package upmarts.controlador;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import upmarts.integracion.IValidadorUPM;
import upmarts.modelo.Administrador;
import upmarts.modelo.DisciplinaArtistica;
import upmarts.modelo.Participante;
import upmarts.modelo.PreferenciaArtistica;
import upmarts.modelo.Usuario;
import upmarts.persistencia.GestorFicheroUsuarios;
import upmarts.persistencia.IAccesoUsuarios;

public class ControladorUsuariosTest {

    private IControladorUsuarios controlador;
    private IValidadorUPM validadorUPM;
    private File ficheroTemporal;

    @Before
    public void preparar() throws Exception {
        ficheroTemporal = File.createTempFile("usuarios-test", ".txt");
        ficheroTemporal.deleteOnExit();

        IAccesoUsuarios acceso = new GestorFicheroUsuarios(ficheroTemporal.getAbsolutePath());
        controlador = new ControladorUsuarios(acceso);

        validadorUPM = (String correo, String password) -> correo != null
                && (correo.endsWith("@upm.es") || correo.endsWith("@alumnos.upm.es"))
                && password != null
                && password.length() >= 12;
    }

    @Test
    public void registrarParticipanteExternoCorrectoDevuelveTrue() {
        boolean resultado = registrarParticipante("Ana Externa", "ana01", "ana@gmail.com", "");
        assertTrue(resultado);
    }

    @Test
    public void detectarCorreoAlumnoUPMDevuelveAlumno() {
        assertEquals(ControladorUsuarios.TIPO_ALUMNO_UPM,
                controlador.detectarTipoParticipantePorCorreo("luis@alumnos.upm.es"));
    }

    @Test
    public void detectarCorreoPersonalUPMDevuelvePersonal() {
        assertEquals(ControladorUsuarios.TIPO_PERSONAL_UPM,
                controlador.detectarTipoParticipantePorCorreo("profesor@upm.es"));
    }

    @Test
    public void registrarAlumnoUPMCorrectoDevuelveTrue() {
        boolean resultado = controlador.registrarParticipante(
                "Luis Alumno", "luis01", "luis@alumnos.upm.es", "Password1234",
                "87654321B", "4111111111111111", "M123456", preferencias(), validadorUPM
        );

        assertTrue(resultado);
    }

    @Test
    public void registrarPersonalUPMCorrectoDevuelveTrue() {
        boolean resultado = controlador.registrarParticipante(
                "Laura Personal", "laura01", "laura@upm.es", "Password1234",
                "87654321B", "4111111111111111", "5", preferencias(), validadorUPM
        );

        assertTrue(resultado);
    }

    @Test
    public void registrarParticipanteConPasswordInvalidaDevuelveFalse() {
        boolean resultado = controlador.registrarParticipante(
                "Ana Externa", "ana01", "ana@gmail.com", "corta",
                "12345678A", "4111111111111111", "", preferencias(), validadorUPM
        );

        assertFalse(resultado);
    }

    @Test
    public void loginConCredencialesCorrectasDevuelveUsuario() {
        registrarParticipante("Ana Externa", "ana01", "ana@gmail.com", "");
        assertNotNull(controlador.login("ana@gmail.com", "Password1234"));
    }

    @Test
    public void loginConCredencialesIncorrectasDevuelveNull() {
        registrarParticipante("Ana Externa", "ana01", "ana@gmail.com", "");
        assertNull(controlador.login("ana@gmail.com", "Password0000"));
    }

    @Test
    public void registrarParticipanteConNickConflictivoDevuelveFalse() {
        boolean resultado = controlador.registrarParticipante(
                "Usuario Root", "root", "root@gmail.com", "Password1234",
                "12345678A", "4111111111111111", "", preferencias(), validadorUPM
        );

        assertFalse(resultado);
    }

    @Test
    public void administradorPuedeDarAltaInstructor() {
        Administrador admin = new Administrador("adminsys", "Admin", "admin@upm.es",
                Usuario.cifrarPassword("Admin123456A"), "910000000");

        boolean resultado = controlador.registrarInstructorComoAdministrador(admin,
                "Instructor Uno", "inst01", "inst@upm.es", "Password1234",
                "12345678A", "ES7620770024003102575766");

        assertTrue(resultado);
    }

    @Test
    public void participantePuedeActualizarPreferencias() {
        registrarParticipante("Ana Externa", "ana01", "ana@gmail.com", "");
        Usuario usuario = controlador.login("ana@gmail.com", "Password1234");

        List<PreferenciaArtistica> nuevas = new ArrayList<>();
        nuevas.add(new PreferenciaArtistica(DisciplinaArtistica.TEATRO, 9));

        boolean resultado = controlador.actualizarPreferencias((Participante) usuario, nuevas);

        assertTrue(resultado);
    }

    @Test
    public void detectarCorreoNoValidoDevuelveCorreoInvalido() {
        assertEquals(ControladorUsuarios.TIPO_CORREO_INVALIDO,
                controlador.detectarTipoParticipantePorCorreo("correo-invalido"));
    }

    @Test
    public void registrarParticipanteConNombreVacioDevuelveFalse() {
        assertFalse(controlador.registrarParticipante(
                "", "ana01", "ana@gmail.com", "Password1234",
                "12345678A", "4111111111111111", "", preferencias(), validadorUPM
        ));
    }

    @Test
    public void registrarParticipanteConCorreoDuplicadoDevuelveFalse() {
        registrarParticipante("Ana Externa", "ana01", "ana@gmail.com", "");

        assertFalse(controlador.registrarParticipante(
                "Ana Otra", "ana02", "ana@gmail.com", "Password1234",
                "12345678A", "4111111111111111", "", preferencias(), validadorUPM
        ));
    }

    @Test
    public void registrarParticipanteConNickDuplicadoDevuelveFalse() {
        registrarParticipante("Ana Externa", "ana01", "ana@gmail.com", "");

        assertFalse(controlador.registrarParticipante(
                "Ana Otra", "ana01", "otra@gmail.com", "Password1234",
                "12345678A", "4111111111111111", "", preferencias(), validadorUPM
        ));
    }

    @Test
    public void registrarParticipanteConCorreoInvalidoDevuelveFalse() {
        assertFalse(controlador.registrarParticipante(
                "Ana Externa", "ana02", "ana.gmail.com", "Password1234",
                "12345678A", "4111111111111111", "", preferencias(), validadorUPM
        ));
    }

    @Test
    public void registrarParticipanteConDNIInvalidoDevuelveFalse() {
        assertFalse(controlador.registrarParticipante(
                "Ana Externa", "ana03", "ana@gmail.com", "Password1234",
                "1234A", "4111111111111111", "", preferencias(), validadorUPM
        ));
    }

    @Test
    public void registrarParticipanteConTarjetaInvalidaDevuelveFalse() {
        assertFalse(controlador.registrarParticipante(
                "Ana Externa", "ana04", "ana@gmail.com", "Password1234",
                "12345678A", "abc", "", preferencias(), validadorUPM
        ));
    }

    @Test
    public void registrarAlumnoUPMSinValidacionUPMDevuelveFalse() {
        IValidadorUPM validadorFalso = (String correo, String password) -> false;

        assertFalse(controlador.registrarParticipante(
                "Luis Alumno", "luis01", "luis@alumnos.upm.es", "Password1234",
                "87654321B", "4111111111111111", "M123456", preferencias(), validadorFalso
        ));
    }

    @Test
    public void registrarPersonalUPMConAntiguedadInvalidaDevuelveFalse() {
        assertFalse(controlador.registrarParticipante(
                "Laura Personal", "laura01", "laura@upm.es", "Password1234",
                "87654321B", "4111111111111111", "-1", preferencias(), validadorUPM
        ));
    }

    @Test
    public void administradorNullNoPuedeRegistrarInstructor() {
        assertFalse(controlador.registrarInstructorComoAdministrador(null,
                "Instructor Uno", "inst01", "inst@upm.es", "Password1234",
                "12345678A", "ES7620770024003102575766"));
    }

    @Test
    public void administradorPuedeDarBajaUsuarioExistente() {
        registrarParticipante("Ana Externa", "ana01", "ana@gmail.com", "");
        Administrador admin = new Administrador("adminsys", "Admin", "admin@upm.es",
                Usuario.cifrarPassword("Admin123456A"), "910000000");

        boolean resultado = controlador.darDeBajaUsuarioComoAdministrador(admin, "ana@gmail.com");

        assertTrue(resultado);
        assertNull(controlador.login("ana@gmail.com", "Password1234"));
    }

    @Test
    public void administradorNoPuedeDarBajaASiMismo() {
        Administrador admin = new Administrador("adminsys", "Admin", "admin@upm.es",
                Usuario.cifrarPassword("Admin123456A"), "910000000");

        assertFalse(controlador.darDeBajaUsuarioComoAdministrador(admin, "admin@upm.es"));
    }

    @Test
    public void participantePuedeDarseDeBaja() {
        registrarParticipante("Ana Externa", "ana01", "ana@gmail.com", "");
        Usuario usuario = controlador.login("ana@gmail.com", "Password1234");

        assertTrue(controlador.darseDeBaja(usuario));
        assertNull(controlador.login("ana@gmail.com", "Password1234"));
    }

    @Test
    public void administradorNoPuedeDarseDeBaja() {
        Administrador admin = new Administrador("adminsys", "Admin", "admin@upm.es",
                Usuario.cifrarPassword("Admin123456A"), "910000000");

        assertFalse(controlador.darseDeBaja(admin));
    }

    @Test
    public void actualizarPreferenciasConParticipanteNoPersistidoDevuelveFalse() {
        Participante externo = new upmarts.modelo.ParticipanteExterno("ana01", "Ana Externa",
                "ana@gmail.com", Usuario.cifrarPassword("Password1234"), "12345678A",
                "4111111111111111", preferencias());

        List<PreferenciaArtistica> nuevas = new ArrayList<>();
        nuevas.add(new PreferenciaArtistica(DisciplinaArtistica.TEATRO, 9));

        assertFalse(controlador.actualizarPreferencias(externo, nuevas));
    }

    @Test
    public void actualizarDatosParticipanteEstudianteCorrectoDevuelveTrue() {
        controlador.registrarParticipante(
                "Luis Alumno", "luis01", "luis@alumnos.upm.es", "Password1234",
                "87654321B", "4111111111111111", "M123456", preferencias(), validadorUPM
        );

        Participante usuario = (Participante) controlador.login("luis@alumnos.upm.es", "Password1234");
        assertNotNull(usuario);

        boolean actualizado = controlador.actualizarDatosParticipante(usuario,
                "Luis Actualizado", "luis02", "luis@alumnos.upm.es", "",
                "87654321B", "4111111111111111", "M654321");

        assertTrue(actualizado);
        assertEquals("Luis Actualizado", usuario.getNombreCompleto());
        assertEquals("luis02", usuario.getNombreUsuario());
        assertEquals("M654321", ((upmarts.modelo.EstudianteUPM) usuario).getNumeroMatricula());
    }

    @Test
    public void listarUsuariosAdministradorDevuelveUsuariosRegistrados() {
        registrarParticipante("Ana Externa", "ana01", "ana@gmail.com", "");
        controlador.registrarInstructorComoAdministrador(new Administrador("adminsys", "Admin", "admin@upm.es",
                Usuario.cifrarPassword("Admin123456A"), "910000000"),
                "Instructor Uno", "inst01", "inst@upm.es", "Password1234",
                "12345678A", "ES7620770024003102575766");

        Administrador admin = new Administrador("adminsys", "Admin", "admin@upm.es",
                Usuario.cifrarPassword("Admin123456A"), "910000000");

        List<Usuario> usuarios = controlador.listarUsuarios(admin);

        assertEquals(2, usuarios.size());
    }

    @Test
    public void listarInstructoresAdministradorDevuelveSoloInstructores() {
        controlador.registrarInstructorComoAdministrador(new Administrador("adminsys", "Admin", "admin@upm.es",
                Usuario.cifrarPassword("Admin123456A"), "910000000"),
                "Instructor Uno", "inst01", "inst@upm.es", "Password1234",
                "12345678A", "ES7620770024003102575766");

        Administrador admin = new Administrador("adminsys", "Admin", "admin@upm.es",
                Usuario.cifrarPassword("Admin123456A"), "910000000");

        List<upmarts.modelo.Instructor> instructores = controlador.listarInstructores(admin);

        assertEquals(1, instructores.size());
        assertEquals("inst01", instructores.get(0).getNombreUsuario());
    }

    @Test
    public void listarParticipantesAdministradorDevuelveSoloParticipantes() {
        registrarParticipante("Ana Externa", "ana01", "ana@gmail.com", "");
        registrarParticipante("Luis Alumno", "luis01", "luis@alumnos.upm.es", "M123456");

        Administrador admin = new Administrador("adminsys", "Admin", "admin@upm.es",
                Usuario.cifrarPassword("Admin123456A"), "910000000");

        List<Participante> participantes = controlador.listarParticipantes(admin);

        assertEquals(2, participantes.size());
    }

    @Test
    public void calcularDescuentoPersonalDevuelveTope() {
        controlador.registrarParticipante(
                "Laura Personal", "laura01", "laura@upm.es", "Password1234",
                "87654321B", "4111111111111111", "20", preferencias(), validadorUPM
        );

        Usuario usuario = controlador.login("laura@upm.es", "Password1234");
        assertEquals(0.5, controlador.calcularDescuento(usuario), 0.0);
    }

    @Test
    public void persistenciaRecuperaUsuariosDeFichero() {
        registrarParticipante("Ana Externa", "ana01", "ana@gmail.com", "");
        controlador.registrarParticipante(
                "Luis Alumno", "luis01", "luis@alumnos.upm.es", "Password1234",
                "87654321B", "4111111111111111", "M123456", preferencias(), validadorUPM
        );

        IAccesoUsuarios acceso = new GestorFicheroUsuarios(ficheroTemporal.getAbsolutePath());
        IControladorUsuarios otroControlador = new ControladorUsuarios(acceso);

        assertNotNull(otroControlador.login("ana@gmail.com", "Password1234"));
        assertNotNull(otroControlador.login("luis@alumnos.upm.es", "Password1234"));
    }

    private boolean registrarParticipante(String nombre, String nick, String correo, String datoEspecifico) {
        return controlador.registrarParticipante(
                nombre, nick, correo, "Password1234", "12345678A",
                "4111111111111111", datoEspecifico, preferencias(), validadorUPM
        );
    }

    private List<PreferenciaArtistica> preferencias() {
        List<PreferenciaArtistica> preferencias = new ArrayList<>();
        preferencias.add(new PreferenciaArtistica(DisciplinaArtistica.MUSICA, 6));
        return preferencias;
    }
}
