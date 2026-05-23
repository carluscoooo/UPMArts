package upmarts.controlador;

import java.util.ArrayList;
import java.util.Collections;
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
import upmarts.modelo.PreferenciaArtistica;
import upmarts.modelo.RolUsuario;
import upmarts.modelo.Usuario;
import upmarts.persistencia.IAccesoUsuarios;

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
        // Prueba CB01
        assertEquals(ControladorUsuarios.TIPO_CORREO_INVALIDO,
                controlador.detectarTipoParticipantePorCorreo("correo-invalido"));
    }

    @Test
    public void CB02_detectarTipoParticipanteEntraEnRamaAlumnoUPM() {
        // Prueba CB02
        assertEquals(ControladorUsuarios.TIPO_ALUMNO_UPM,
                controlador.detectarTipoParticipantePorCorreo("alumno@alumnos.upm.es"));
    }

    @Test
    public void CB03_detectarTipoParticipanteEntraEnRamaPersonalUPM() {
        // Prueba CB03
        assertEquals(ControladorUsuarios.TIPO_PERSONAL_UPM,
                controlador.detectarTipoParticipantePorCorreo("persona@upm.es"));
    }

    @Test
    public void CB04_detectarTipoParticipanteCaeEnRamaExterno() {
        // Prueba CB04
        assertEquals(ControladorUsuarios.TIPO_EXTERNO,
                controlador.detectarTipoParticipantePorCorreo("externo@gmail.com"));
    }

    @Test
    public void CB05_loginEntraEnRamaCorreoInvalidoNuloOVacio() {
        // Prueba CB05
        assertNull(controlador.login(null, PASSWORD_VALIDO));
        assertNull(controlador.login("   ", PASSWORD_VALIDO));
    }

    @Test
    public void CB06_loginRecorreUsuariosYEncuentraCorreoYPasswordCorrectos() {
        // Prueba CB06
        assertTrue(registrarParticipante("Usuario Login", "logb1", "loginb1@gmail.com", ""));

        Usuario usuario = controlador.login("loginb1@gmail.com", PASSWORD_VALIDO);

        assertNotNull(usuario);
        assertEquals("loginb1@gmail.com", usuario.getCorreoElectronico());
    }

    @Test
    public void CB07_loginRecorreUsuariosYNoEncuentraCorreo() {
        // Prueba CB07
        assertNull(controlador.login("correo-no-registrado@gmail.com", PASSWORD_VALIDO));
    }

    @Test
    public void CB08_loginEncuentraCorreoPeroPasswordNoCoincide() {
        // Prueba CB08
        assertTrue(registrarParticipante("Usuario Login", "logb2", "loginb2@gmail.com", ""));

        Usuario usuario = controlador.login("loginb2@gmail.com", "PasswordIncorrecta123");

        assertNull(usuario);
    }

    @Test
    public void CB09_registrarParticipanteFallaEnValidacionDeNombre() {
        // Prueba CB09
        boolean resultado = registrarParticipante("   ", "exb01", "externob1@gmail.com", "");

        assertFalse(resultado);
    }

    @Test
    public void CB10_registrarParticipanteFallaEnValidacionDeNick() {
        // Prueba CB10
        boolean resultado = registrarParticipante("Usuario", "abc", "externob2@gmail.com", "");

        assertFalse(resultado);
    }

    @Test
    public void CB11_registrarParticipanteFallaEnValidacionDePassword() {
        // Prueba CB11
        boolean resultado = controlador.registrarParticipante(
                "Usuario", "exb02", "externob3@gmail.com", "password1234",
                DNI_VALIDO, TARJETA_VALIDA, "", sinPreferencias());

        assertFalse(resultado);
    }

    @Test
    public void CB12_registrarParticipanteFallaEnValidacionDeCorreo() {
        // Prueba CB12
        boolean resultado = registrarParticipante("Usuario", "exb03", "correo-invalido", "");

        assertFalse(resultado);
    }

    @Test
    public void CB13_registrarParticipanteEntraEnRamaCorreoDuplicado() {
        // Prueba CB13
        assertTrue(registrarParticipante("Usuario Uno", "exb04", "duplicadob@gmail.com", ""));

        boolean resultado = controlador.registrarParticipante(
                "Usuario Dos", "exb05", "duplicadob@gmail.com", PASSWORD_VALIDO,
                "87654321B", TARJETA_VALIDA, "", sinPreferencias());

        assertFalse(resultado);
    }

    @Test
    public void CB14_registrarParticipanteEntraEnRamaNickDuplicado() {
        // Prueba CB14
        assertTrue(registrarParticipante("Usuario Uno", "exb06", "externob7@gmail.com", ""));

        boolean resultado = controlador.registrarParticipante(
                "Usuario Dos", "exb06", "externob8@gmail.com", PASSWORD_VALIDO,
                "87654321B", TARJETA_VALIDA, "", sinPreferencias());

        assertFalse(resultado);
    }

    @Test
    public void CB15_registrarParticipanteFallaEnValidacionDeDNI() {
        // Prueba CB15
        boolean resultado = controlador.registrarParticipante(
                "Usuario", "exb07", "externob9@gmail.com", PASSWORD_VALIDO,
                "1234567A", TARJETA_VALIDA, "", sinPreferencias());

        assertFalse(resultado);
    }

    @Test
    public void CB16_registrarParticipanteFallaEnValidacionDeTarjeta() {
        // Prueba CB16
        boolean resultado = controlador.registrarParticipante(
                "Usuario", "exb08", "externob10@gmail.com", PASSWORD_VALIDO,
                DNI_VALIDO, "1234567", "", sinPreferencias());

        assertFalse(resultado);
    }

    @Test
    public void CB17_registrarParticipanteEntraEnCaminoDeExitoExterno() {
        // Prueba CB17
        boolean resultado = registrarParticipante("Usuario Externo", "exb09", "externob11@gmail.com", "");

        Usuario usuario = controlador.login("externob11@gmail.com", PASSWORD_VALIDO);

        assertTrue(resultado);
        assertNotNull(usuario);
        assertEquals(RolUsuario.PARTICIPANTE_EXTERNO, usuario.getRol());
    }

    @Test
    public void CB18_registrarParticipanteEntraEnCaminoAlumnoYFallaPorMatriculaVacia() {
        // Prueba CB18
        boolean resultado = registrarParticipante("Alumno UPM", "alub1", "alumnob1@alumnos.upm.es", "   ");

        assertFalse(resultado);
    }

    @Test
    public void CB19_registrarParticipanteEntraEnCaminoDeExitoAlumnoUPM() {
        // Prueba CB19
        boolean resultado = registrarParticipante("Alumno UPM", "alub2", "alumnob2@alumnos.upm.es", "M002");

        Usuario usuario = controlador.login("alumnob2@alumnos.upm.es", PASSWORD_VALIDO);

        assertTrue(resultado);
        assertNotNull(usuario);
        assertEquals(RolUsuario.ESTUDIANTE_UPM, usuario.getRol());
    }

    @Test
    public void CB20_registrarParticipanteEntraEnCaminoPersonalYFallaPorAntiguedadNoNumerica() {
        // Prueba CB20
        boolean resultado = registrarParticipante("Personal UPM", "perb1", "personalb1@upm.es", "tres");

        assertFalse(resultado);
    }

    @Test
    public void CB21_registrarParticipanteEntraEnCaminoDeExitoPersonalUPM() {
        // Prueba CB21
        boolean resultado = registrarParticipante("Personal UPM", "perb2", "personalb2@upm.es", "3");

        Usuario usuario = controlador.login("personalb2@upm.es", PASSWORD_VALIDO);

        assertTrue(resultado);
        assertNotNull(usuario);
        assertEquals(RolUsuario.PERSONAL_UPM, usuario.getRol());
    }

    @Test
    public void CB22_registrarInstructorEntraEnRamaAdministradorNulo() {
        // Prueba CB22
        boolean resultado = controlador.registrarInstructorComoAdministrador(
                null, "Instructor", "inb01", "instb1@upm.es",
                PASSWORD_VALIDO, DNI_VALIDO, IBAN_VALIDO);

        assertFalse(resultado);
    }

    @Test
    public void CB23_registrarInstructorFallaEnValidacionDeDatosComunes() {
        // Prueba CB23
        boolean resultado = controlador.registrarInstructorComoAdministrador(
                crearAdministrador(), "   ", "inb02", "instb2@upm.es",
                PASSWORD_VALIDO, DNI_VALIDO, IBAN_VALIDO);

        assertFalse(resultado);
    }

    @Test
    public void CB24_registrarInstructorFallaEnValidacionDeDNI() {
        // Prueba CB24
        boolean resultado = controlador.registrarInstructorComoAdministrador(
                crearAdministrador(), "Instructor", "inb03", "instb3@upm.es",
                PASSWORD_VALIDO, "1234567A", IBAN_VALIDO);

        assertFalse(resultado);
    }

    @Test
    public void CB25_registrarInstructorFallaEnValidacionDeIBAN() {
        // Prueba CB25
        boolean resultado = controlador.registrarInstructorComoAdministrador(
                crearAdministrador(), "Instructor", "inb04", "instb4@upm.es",
                PASSWORD_VALIDO, DNI_VALIDO, "1234");

        assertFalse(resultado);
    }

    @Test
    public void CB26_registrarInstructorEntraEnCaminoDeExito() {
        // Prueba CB26
        boolean resultado = controlador.registrarInstructorComoAdministrador(
                crearAdministrador(), "Instructor", "inb05", "instb5@upm.es",
                PASSWORD_VALIDO, DNI_VALIDO, IBAN_VALIDO);

        Usuario usuario = controlador.login("instb5@upm.es", PASSWORD_VALIDO);

        assertTrue(resultado);
        assertNotNull(usuario);
        assertEquals(RolUsuario.INSTRUCTOR, usuario.getRol());
    }

    @Test
    public void CB27_loginEntraEnRamaPasswordNulaOVaciaConCorreoValido() {
        // Prueba CB27
        assertTrue(registrarParticipante("Usuario Login", "logb3", "loginb3@gmail.com", ""));

        assertNull(controlador.login("loginb3@gmail.com", null));
        assertNull(controlador.login("loginb3@gmail.com", "   "));
    }

    @Test
    public void CB28_registrarParticipanteAlumnoFallaPorValidadorUPM() {
        // Prueba CB28
        ControladorUsuarios controladorUPM = new ControladorUsuarios(
                new PersistenciaEnMemoria(), new ValidadorUPMDePruebaRechaza());

        boolean resultado = controladorUPM.registrarParticipante(
                "Alumno UPM", "alub3", "alumnob3@alumnos.upm.es", PASSWORD_VALIDO,
                DNI_VALIDO, TARJETA_VALIDA, "M003", sinPreferencias());

        assertFalse(resultado);
    }

    @Test
    public void CB29_registrarParticipantePersonalFallaPorValidadorUPM() {
        // Prueba CB29
        ControladorUsuarios controladorUPM = new ControladorUsuarios(
                new PersistenciaEnMemoria(), new ValidadorUPMDePruebaRechaza());

        boolean resultado = controladorUPM.registrarParticipante(
                "Personal UPM", "perb3", "personalb3@upm.es", PASSWORD_VALIDO,
                DNI_VALIDO, TARJETA_VALIDA, "3", sinPreferencias());

        assertFalse(resultado);
    }

    @Test
    public void CB30_registrarParticipanteFallaAlLeerPersistencia() {
        // Prueba CB30
        ControladorUsuarios controladorConFallo = new ControladorUsuarios(
                new PersistenciaFallaEnLecturaTrasConstructor(), new ValidadorUPMDePruebaAcepta());

        boolean resultado = controladorConFallo.registrarParticipante(
                "Usuario", "exb10", "externob12@gmail.com", PASSWORD_VALIDO,
                DNI_VALIDO, TARJETA_VALIDA, "", sinPreferencias());

        assertFalse(resultado);
    }

    @Test
    public void CB31_registrarParticipanteFallaAlGuardarPersistencia() {
        // Prueba CB31
        ControladorUsuarios controladorConFallo = new ControladorUsuarios(
                new PersistenciaFallaEnGuardadoTrasConstructor(), new ValidadorUPMDePruebaAcepta());

        boolean resultado = controladorConFallo.registrarParticipante(
                "Usuario", "exb11", "externob13@gmail.com", PASSWORD_VALIDO,
                DNI_VALIDO, TARJETA_VALIDA, "", sinPreferencias());

        assertFalse(resultado);
    }

    @Test
    public void CB32_registrarInstructorFallaAlLeerPersistencia() {
        // Prueba CB32
        ControladorUsuarios controladorConFallo = new ControladorUsuarios(
                new PersistenciaFallaEnLecturaTrasConstructor(), new ValidadorUPMDePruebaAcepta());

        boolean resultado = controladorConFallo.registrarInstructorComoAdministrador(
                crearAdministrador(), "Instructor", "inb06", "instb6@upm.es",
                PASSWORD_VALIDO, DNI_VALIDO, IBAN_VALIDO);

        assertFalse(resultado);
    }

    @Test
    public void CB33_registrarInstructorFallaAlGuardarPersistencia() {
        // Prueba CB33
        ControladorUsuarios controladorConFallo = new ControladorUsuarios(
                new PersistenciaFallaEnGuardadoTrasConstructor(), new ValidadorUPMDePruebaAcepta());

        boolean resultado = controladorConFallo.registrarInstructorComoAdministrador(
                crearAdministrador(), "Instructor", "inb07", "instb7@upm.es",
                PASSWORD_VALIDO, DNI_VALIDO, IBAN_VALIDO);

        assertFalse(resultado);
    }

    @Test
    public void CB34_validarNivelPreferenciaEntraEnRamaValorCero() {
        // Prueba CB34
        assertNull(controlador.validarNivelPreferencia("0"));
    }

    @Test
    public void CB35_validarNivelPreferenciaEntraEnRamaValorEntreUnoYDiez() {
        // Prueba CB35
        assertNull(controlador.validarNivelPreferencia("5"));
    }

    @Test
    public void CB36_validarNivelPreferenciaEntraEnRamaNivelVacio() {
        // Prueba CB36
        assertNotNull(controlador.validarNivelPreferencia("   "));
    }

    @Test
    public void CB37_validarNivelPreferenciaEntraEnRamaMenorQueCero() {
        // Prueba CB37
        assertNotNull(controlador.validarNivelPreferencia("-1"));
    }

    @Test
    public void CB38_validarNivelPreferenciaEntraEnRamaMayorQueDiez() {
        // Prueba CB38
        assertNotNull(controlador.validarNivelPreferencia("11"));
    }

    @Test
    public void CB39_validarNivelPreferenciaEntraEnRamaNoNumerico() {
        // Prueba CB39
        assertNotNull(controlador.validarNivelPreferencia("alto"));
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

    private static class ValidadorUPMDePruebaRechaza implements IValidadorUPM {
        @Override
        public boolean verificarCredencialesUPM(String correo, String password) {
            return false;
        }
    }

    private static class PersistenciaEnMemoria implements IAccesoUsuarios {
        private List<Usuario> usuarios = new ArrayList<>();

        @Override
        public void guardarUsuarios(List<Usuario> usuarios) {
            this.usuarios = new ArrayList<>(usuarios);
        }

        @Override
        public List<Usuario> leerUsuarios() {
            return new ArrayList<>(usuarios);
        }
    }

    private static class PersistenciaFallaEnLecturaTrasConstructor extends PersistenciaEnMemoria {
        private int lecturas;

        @Override
        public List<Usuario> leerUsuarios() {
            lecturas++;

            // La primera lectura se deja pasar porque la usa el constructor del controlador.
            if (lecturas > 1) {
                throw new RuntimeException("Error de lectura de prueba.");
            }

            return super.leerUsuarios();
        }
    }

    private static class PersistenciaFallaEnGuardadoTrasConstructor extends PersistenciaEnMemoria {
        private int guardados;

        @Override
        public void guardarUsuarios(List<Usuario> usuarios) {
            guardados++;

            // El primer guardado puede crear usuarios iniciales; el fallo se fuerza en la operacion probada.
            if (guardados > 1) {
                throw new RuntimeException("Error de guardado de prueba.");
            }

            super.guardarUsuarios(usuarios);
        }
    }
}
