package upmarts.controlador;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import upmarts.integracion.IValidadorUPM;
import upmarts.modelo.Administrador;
import upmarts.modelo.EstudianteUPM;
import upmarts.modelo.Instructor;
import upmarts.modelo.ParticipanteExterno;
import upmarts.modelo.PersonalUPM;
import upmarts.modelo.PreferenciaArtistica;
import upmarts.modelo.Usuario;
import upmarts.persistencia.IAccesoUsuarios;

/**
 * Pruebas de caja negra para alta y acceso de usuarios.
 *
 * Comprueban entradas y salidas esperadas sin depender de ficheros reales ni
 * del servicio externo UPM.
 */
public class ControladorUsuariosAltaAccesoCajaNegraTest {

    private static final String PASSWORD_VALIDO = "Password1234";
    private static final String DNI_VALIDO = "12345678A";
    private static final String TARJETA_VALIDA = "1234567890123456";
    private static final String IBAN_VALIDO = "ES7620770024003102575766";

    private ControladorUsuarios controlador;

    @Before
    public void setUp() {
        controlador = crearControlador();
    }

    @Test
    public void CN01_detectarTipoParticipantePorCorreoDevuelveElTipoSegunDominio() {
        assertEquals(ControladorUsuarios.TIPO_ALUMNO_UPM,
                controlador.detectarTipoParticipantePorCorreo("alumno@alumnos.upm.es"));
        assertEquals(ControladorUsuarios.TIPO_PERSONAL_UPM,
                controlador.detectarTipoParticipantePorCorreo("persona@upm.es"));
        assertEquals(ControladorUsuarios.TIPO_EXTERNO,
                controlador.detectarTipoParticipantePorCorreo("externo@gmail.com"));
        assertEquals(ControladorUsuarios.TIPO_CORREO_INVALIDO,
                controlador.detectarTipoParticipantePorCorreo("correo-invalido"));
    }

    @Test
    public void CN02_registrarParticipanteExternoConDatosValidosDevuelveTrueYCreaParticipanteExterno() {
        boolean resultado = registrarParticipante(
                "Participante Externo", "ext1", "externo1@gmail.com", "");

        Usuario usuario = controlador.login("externo1@gmail.com", PASSWORD_VALIDO);

        assertTrue(resultado);
        assertNotNull(usuario);
        assertTrue(usuario instanceof ParticipanteExterno);
    }

    @Test
    public void CN03_registrarEstudianteUPMConDatosValidosDevuelveTrueYCreaEstudianteUPM() {
        boolean resultado = registrarParticipante(
                "Alumno UPM", "alum1", "alumno1@alumnos.upm.es", "M001");

        Usuario usuario = controlador.login("alumno1@alumnos.upm.es", PASSWORD_VALIDO);

        assertTrue(resultado);
        assertNotNull(usuario);
        assertTrue(usuario instanceof EstudianteUPM);
    }

    @Test
    public void CN04_registrarPersonalUPMConDatosValidosDevuelveTrueYCreaPersonalUPM() {
        boolean resultado = registrarParticipante(
                "Personal UPM", "pers1", "personal1@upm.es", "3");

        Usuario usuario = controlador.login("personal1@upm.es", PASSWORD_VALIDO);

        assertTrue(resultado);
        assertNotNull(usuario);
        assertTrue(usuario instanceof PersonalUPM);
    }

    @Test
    public void CN05_registrarInstructorConAdministradorYDatosValidosDevuelveTrueYCreaInstructor() {
        Administrador administrador = crearAdministrador();

        boolean resultado = controlador.registrarInstructorComoAdministrador(
                administrador, "Instructor Uno", "inst1", "inst1@upm.es", PASSWORD_VALIDO,
                DNI_VALIDO, IBAN_VALIDO);

        Usuario usuario = controlador.login("inst1@upm.es", PASSWORD_VALIDO);

        assertTrue(resultado);
        assertNotNull(usuario);
        assertTrue(usuario instanceof Instructor);
    }

    @Test
    public void CN06_loginConCredencialesCorrectasDevuelveUsuario() {
        assertTrue(registrarParticipante(
                "Participante Login", "log1", "login1@gmail.com", ""));

        Usuario usuario = controlador.login("login1@gmail.com", PASSWORD_VALIDO);

        assertNotNull(usuario);
        assertEquals("login1@gmail.com", usuario.getCorreoElectronico());
    }

    @Test
    public void CN07_loginConCorreoNoRegistradoDevuelveNull() {
        Usuario usuario = controlador.login("noexiste@gmail.com", PASSWORD_VALIDO);

        assertNull(usuario);
    }

    @Test
    public void CN08_loginConPasswordIncorrectaDevuelveNull() {
        assertTrue(registrarParticipante(
                "Participante Login", "log2", "login2@gmail.com", ""));

        Usuario usuario = controlador.login("login2@gmail.com", "PasswordIncorrecta123");

        assertNull(usuario);
    }

    @Test
    public void CN09_registrarParticipanteRechazaNombreVacio() {
        boolean resultado = registrarParticipante(
                "   ", "ext2", "externo2@gmail.com", "");

        assertFalse(resultado);
        assertNull(controlador.login("externo2@gmail.com", PASSWORD_VALIDO));
    }

    @Test
    public void CN10_registrarParticipanteRechazaNickInvalido() {
        boolean resultado = registrarParticipante(
                "Participante", "abc", "externo3@gmail.com", "");

        assertFalse(resultado);
        assertNull(controlador.login("externo3@gmail.com", PASSWORD_VALIDO));
    }

    @Test
    public void CN11_registrarParticipanteRechazaPasswordInvalida() {
        boolean resultado = controlador.registrarParticipante(
                "Participante", "ext3", "externo4@gmail.com", "password1234",
                DNI_VALIDO, TARJETA_VALIDA, "", sinPreferencias());

        assertFalse(resultado);
        assertNull(controlador.login("externo4@gmail.com", "password1234"));
    }

    @Test
    public void CN12_registrarParticipanteRechazaCorreoInvalido() {
        boolean resultado = registrarParticipante(
                "Participante", "ext4", "correo-invalido", "");

        assertFalse(resultado);
    }

    @Test
    public void CN13_registrarParticipanteRechazaCorreoDuplicado() {
        assertTrue(registrarParticipante(
                "Participante Uno", "ext5", "duplicado@gmail.com", ""));

        boolean resultado = controlador.registrarParticipante(
                "Participante Dos", "ext6", "duplicado@gmail.com", PASSWORD_VALIDO,
                "87654321B", TARJETA_VALIDA, "", sinPreferencias());

        assertFalse(resultado);
    }

    @Test
    public void CN14_registrarParticipanteRechazaNickDuplicado() {
        assertTrue(registrarParticipante(
                "Participante Uno", "ext7", "externo7@gmail.com", ""));

        boolean resultado = controlador.registrarParticipante(
                "Participante Dos", "ext7", "externo8@gmail.com", PASSWORD_VALIDO,
                "87654321B", TARJETA_VALIDA, "", sinPreferencias());

        assertFalse(resultado);
    }

    @Test
    public void CN15_registrarParticipanteRechazaDNIInvalido() {
        boolean resultado = controlador.registrarParticipante(
                "Participante", "ext8", "externo9@gmail.com", PASSWORD_VALIDO,
                "1234567A", TARJETA_VALIDA, "", sinPreferencias());

        assertFalse(resultado);
    }

    @Test
    public void CN16_registrarParticipanteRechazaTarjetaInvalida() {
        boolean resultado = controlador.registrarParticipante(
                "Participante", "ext9", "externo10@gmail.com", PASSWORD_VALIDO,
                DNI_VALIDO, "1234567", "", sinPreferencias());

        assertFalse(resultado);
    }

    @Test
    public void CN17_registrarEstudianteUPMRechazaMatriculaVacia() {
        boolean resultado = registrarParticipante(
                "Alumno UPM", "alum2", "alumno2@alumnos.upm.es", "   ");

        assertFalse(resultado);
        assertNull(controlador.login("alumno2@alumnos.upm.es", PASSWORD_VALIDO));
    }

    @Test
    public void CN18_registrarPersonalUPMRechazaAntiguedadNoNumerica() {
        boolean resultado = registrarParticipante(
                "Personal UPM", "pers2", "personal2@upm.es", "tres");

        assertFalse(resultado);
        assertNull(controlador.login("personal2@upm.es", PASSWORD_VALIDO));
    }

    @Test
    public void CN19_registrarInstructorRechazaAdministradorNulo() {
        boolean resultado = controlador.registrarInstructorComoAdministrador(
                null, "Instructor Uno", "inst2", "inst2@upm.es", PASSWORD_VALIDO,
                DNI_VALIDO, IBAN_VALIDO);

        assertFalse(resultado);
        assertNull(controlador.login("inst2@upm.es", PASSWORD_VALIDO));
    }

    @Test
    public void CN20_registrarInstructorRechazaIBANInvalido() {
        Administrador administrador = crearAdministrador();

        boolean resultado = controlador.registrarInstructorComoAdministrador(
                administrador, "Instructor Uno", "inst3", "inst3@upm.es", PASSWORD_VALIDO,
                DNI_VALIDO, "1234");

        assertFalse(resultado);
        assertNull(controlador.login("inst3@upm.es", PASSWORD_VALIDO));
    }

    private boolean registrarParticipante(String nombre, String nick, String correo, String datoEspecifico) {
        return controlador.registrarParticipante(
                nombre, nick, correo, PASSWORD_VALIDO,
                DNI_VALIDO, TARJETA_VALIDA, datoEspecifico, sinPreferencias());
    }

    private static List<PreferenciaArtistica> sinPreferencias() {
        return Collections.<PreferenciaArtistica>emptyList();
    }

    private static ControladorUsuarios crearControlador() {
        return new ControladorUsuarios(new PersistenciaEnMemoria(), new ValidadorUPMDePruebaAcepta());
    }

    private static Administrador crearAdministrador() {
        return new Administrador("admin1", "Administrador Uno", "admin1@upm.es", PASSWORD_VALIDO, "910000000");
    }

    private static class ValidadorUPMDePruebaAcepta implements IValidadorUPM {
        @Override
        public boolean verificarCredencialesUPM(String correo, String password) {
            return true;
        }
    }

    private static class PersistenciaEnMemoria implements IAccesoUsuarios {
        private List<Usuario> usuarios = new ArrayList<Usuario>();

        @Override
        public void guardarUsuarios(List<Usuario> usuarios) {
            this.usuarios = new ArrayList<Usuario>(usuarios);
        }

        @Override
        public List<Usuario> leerUsuarios() {
            return new ArrayList<Usuario>(usuarios);
        }
    }
}
