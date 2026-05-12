package upmarts.controlador;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

        validadorUPM = new IValidadorUPM() {
            public boolean verificarCredencialesUPM(String correo, String password) {
                return correo != null
                        && (correo.endsWith("@upm.es") || correo.endsWith("@alumnos.upm.es"))
                        && password != null
                        && password.length() >= 12;
            }
        };
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

        List<PreferenciaArtistica> nuevas = new ArrayList<PreferenciaArtistica>();
        nuevas.add(new PreferenciaArtistica(DisciplinaArtistica.TEATRO, 9));

        boolean resultado = controlador.actualizarPreferencias((Participante) usuario, nuevas);

        assertTrue(resultado);
    }

    private boolean registrarParticipante(String nombre, String nick, String correo, String datoEspecifico) {
        return controlador.registrarParticipante(
                nombre, nick, correo, "Password1234", "12345678A",
                "4111111111111111", datoEspecifico, preferencias(), validadorUPM
        );
    }

    private List<PreferenciaArtistica> preferencias() {
        List<PreferenciaArtistica> preferencias = new ArrayList<PreferenciaArtistica>();
        preferencias.add(new PreferenciaArtistica(DisciplinaArtistica.MUSICA, 6));
        return preferencias;
    }
}
