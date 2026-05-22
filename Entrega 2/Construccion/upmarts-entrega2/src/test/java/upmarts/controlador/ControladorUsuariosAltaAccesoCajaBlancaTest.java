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
 * Pruebas de caja blanca para alta y acceso de usuarios.
 *
 * Cubren las ramas principales del ControladorUsuarios a traves de sus metodos
 * publicos. Los metodos privados quedan probados indirectamente.
 */
public class ControladorUsuariosAltaAccesoCajaBlancaTest {

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
    public void CB01_detectarTipoParticipanteEntraEnRamaCorreoInvalido() {
        assertEquals(ControladorUsuarios.TIPO_CORREO_INVALIDO,
                controlador.detectarTipoParticipantePorCorreo("correo-invalido"));
    }

    @Test
    public void CB02_detectarTipoParticipanteEntraEnRamaAlumnoUPM() {
        assertEquals(ControladorUsuarios.TIPO_ALUMNO_UPM,
                controlador.detectarTipoParticipantePorCorreo("alumno@alumnos.upm.es"));
    }

    @Test
    public void CB03_detectarTipoParticipanteEntraEnRamaPersonalUPM() {
        assertEquals(ControladorUsuarios.TIPO_PERSONAL_UPM,
                controlador.detectarTipoParticipantePorCorreo("persona@upm.es"));
    }

    @Test
    public void CB04_detectarTipoParticipanteCaeEnRamaExterno() {
        assertEquals(ControladorUsuarios.TIPO_EXTERNO,
                controlador.detectarTipoParticipantePorCorreo("externo@gmail.com"));
    }

    @Test
    public void CB05_loginEntraEnRamaInicialConCorreoNulo() {
        assertNull(controlador.login(null, PASSWORD_VALIDO));
    }

    @Test
    public void CB06_loginRecorreUsuariosYEncuentraCorreoYPasswordCorrectos() {
        assertTrue(registrarParticipante(
                "Usuario Login", "logb1", "loginb1@gmail.com", ""));

        Usuario usuario = controlador.login("loginb1@gmail.com", PASSWORD_VALIDO);

        assertNotNull(usuario);
        assertEquals("loginb1@gmail.com", usuario.getCorreoElectronico());
    }

    @Test
    public void CB07_loginRecorreUsuariosYNoEncuentraCorreo() {
        assertNull(controlador.login("correo-no-registrado@gmail.com", PASSWORD_VALIDO));
    }

    @Test
    public void CB08_loginEncuentraCorreoPeroPasswordNoCoincide() {
        assertTrue(registrarParticipante(
                "Usuario Login", "logb2", "loginb2@gmail.com", ""));

        Usuario usuario = controlador.login("loginb2@gmail.com", "PasswordIncorrecta123");

        assertNull(usuario);
    }

    @Test
    public void CB09_registrarParticipanteFallaEnValidacionDeNombre() {
        boolean resultado = registrarParticipante(
                "   ", "extb1", "externob1@gmail.com", "");

        assertFalse(resultado);
    }

    @Test
    public void CB10_registrarParticipanteFallaEnValidacionDeNick() {
        boolean resultado = registrarParticipante(
                "Usuario", "abc", "externob2@gmail.com", "");

        assertFalse(resultado);
    }

    @Test
    public void CB11_registrarParticipanteFallaEnValidacionDePassword() {
        boolean resultado = controlador.registrarParticipante(
                "Usuario", "extb3", "externob3@gmail.com", "password1234",
                DNI_VALIDO, TARJETA_VALIDA, "", sinPreferencias());

        assertFalse(resultado);
    }

    @Test
    public void CB12_registrarParticipanteFallaEnValidacionDeCorreo() {
        boolean resultado = registrarParticipante(
                "Usuario", "extb4", "correo-invalido", "");

        assertFalse(resultado);
    }

    @Test
    public void CB13_registrarParticipanteEntraEnRamaCorreoDuplicado() {
        assertTrue(registrarParticipante(
                "Usuario Uno", "extb5", "duplicadob@gmail.com", ""));

        boolean resultado = controlador.registrarParticipante(
                "Usuario Dos", "extb6", "duplicadob@gmail.com", PASSWORD_VALIDO,
                "87654321B", TARJETA_VALIDA, "", sinPreferencias());

        assertFalse(resultado);
    }

    @Test
    public void CB14_registrarParticipanteEntraEnRamaNickDuplicado() {
        assertTrue(registrarParticipante(
                "Usuario Uno", "extb7", "externob7@gmail.com", ""));

        boolean resultado = controlador.registrarParticipante(
                "Usuario Dos", "extb7", "externob8@gmail.com", PASSWORD_VALIDO,
                "87654321B", TARJETA_VALIDA, "", sinPreferencias());

        assertFalse(resultado);
    }

    @Test
    public void CB15_registrarParticipanteFallaEnValidacionDeDNI() {
        boolean resultado = controlador.registrarParticipante(
                "Usuario", "extb8", "externob9@gmail.com", PASSWORD_VALIDO,
                "1234567A", TARJETA_VALIDA, "", sinPreferencias());

        assertFalse(resultado);
    }

    @Test
    public void CB16_registrarParticipanteFallaEnValidacionDeTarjeta() {
        boolean resultado = controlador.registrarParticipante(
                "Usuario", "extb9", "externob10@gmail.com", PASSWORD_VALIDO,
                DNI_VALIDO, "1234567", "", sinPreferencias());

        assertFalse(resultado);
    }

    @Test
    public void CB17_registrarParticipanteEntraEnCaminoDeExitoExterno() {
        boolean resultado = registrarParticipante(
                "Usuario Externo", "extb10", "externob11@gmail.com", "");

        Usuario usuario = controlador.login("externob11@gmail.com", PASSWORD_VALIDO);

        assertTrue(resultado);
        assertNotNull(usuario);
        assertTrue(usuario instanceof ParticipanteExterno);
    }

    @Test
    public void CB18_registrarParticipanteEntraEnCaminoAlumnoYFallaPorMatriculaVacia() {
        boolean resultado = registrarParticipante(
                "Alumno UPM", "alumb1", "alumnob1@alumnos.upm.es", "   ");

        assertFalse(resultado);
    }

    @Test
    public void CB19_registrarParticipanteEntraEnCaminoDeExitoAlumnoUPM() {
        boolean resultado = registrarParticipante(
                "Alumno UPM", "alumb2", "alumnob2@alumnos.upm.es", "M002");

        Usuario usuario = controlador.login("alumnob2@alumnos.upm.es", PASSWORD_VALIDO);

        assertTrue(resultado);
        assertNotNull(usuario);
        assertTrue(usuario instanceof EstudianteUPM);
    }

    @Test
    public void CB20_registrarParticipanteEntraEnCaminoPersonalYFallaPorAntiguedadNoNumerica() {
        boolean resultado = registrarParticipante(
                "Personal UPM", "persb1", "personalb1@upm.es", "tres");

        assertFalse(resultado);
    }

    @Test
    public void CB21_registrarParticipanteEntraEnCaminoDeExitoPersonalUPM() {
        boolean resultado = registrarParticipante(
                "Personal UPM", "persb2", "personalb2@upm.es", "3");

        Usuario usuario = controlador.login("personalb2@upm.es", PASSWORD_VALIDO);

        assertTrue(resultado);
        assertNotNull(usuario);
        assertTrue(usuario instanceof PersonalUPM);
    }

    @Test
    public void CB22_registrarInstructorEntraEnRamaAdministradorNulo() {
        boolean resultado = controlador.registrarInstructorComoAdministrador(
                null, "Instructor", "instb1", "instb1@upm.es", PASSWORD_VALIDO,
                DNI_VALIDO, IBAN_VALIDO);

        assertFalse(resultado);
    }

    @Test
    public void CB23_registrarInstructorFallaEnValidacionDeDatosComunes() {
        boolean resultado = controlador.registrarInstructorComoAdministrador(
                crearAdministrador(), "   ", "instb2", "instb2@upm.es", PASSWORD_VALIDO,
                DNI_VALIDO, IBAN_VALIDO);

        assertFalse(resultado);
    }

    @Test
    public void CB24_registrarInstructorFallaEnValidacionDeDNI() {
        boolean resultado = controlador.registrarInstructorComoAdministrador(
                crearAdministrador(), "Instructor", "instb3", "instb3@upm.es", PASSWORD_VALIDO,
                "1234567A", IBAN_VALIDO);

        assertFalse(resultado);
    }

    @Test
    public void CB25_registrarInstructorFallaEnValidacionDeIBAN() {
        boolean resultado = controlador.registrarInstructorComoAdministrador(
                crearAdministrador(), "Instructor", "instb4", "instb4@upm.es", PASSWORD_VALIDO,
                DNI_VALIDO, "1234");

        assertFalse(resultado);
    }

    @Test
    public void CB26_registrarInstructorEntraEnCaminoDeExito() {
        boolean resultado = controlador.registrarInstructorComoAdministrador(
                crearAdministrador(), "Instructor", "instb5", "instb5@upm.es", PASSWORD_VALIDO,
                DNI_VALIDO, IBAN_VALIDO);

        Usuario usuario = controlador.login("instb5@upm.es", PASSWORD_VALIDO);

        assertTrue(resultado);
        assertNotNull(usuario);
        assertTrue(usuario instanceof Instructor);
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
