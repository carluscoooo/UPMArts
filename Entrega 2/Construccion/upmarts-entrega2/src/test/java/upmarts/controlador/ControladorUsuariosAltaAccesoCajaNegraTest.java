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
        // Prueba CN01
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
    public void CN02_registrarParticipanteExternoConDatosValidosDevuelveTrue() {
        // Prueba CN02
        boolean resultado = registrarParticipante("Participante Externo", "ext01", "externo1@gmail.com", "");

        Usuario usuario = controlador.login("externo1@gmail.com", PASSWORD_VALIDO);

        assertTrue(resultado);
        assertNotNull(usuario);
        assertEquals(RolUsuario.PARTICIPANTE_EXTERNO, usuario.getRol());
    }

    @Test
    public void CN03_registrarEstudianteUPMConDatosValidosDevuelveTrue() {
        // Prueba CN03
        boolean resultado = registrarParticipante("Alumno UPM", "alu01", "alumno1@alumnos.upm.es", "M001");

        Usuario usuario = controlador.login("alumno1@alumnos.upm.es", PASSWORD_VALIDO);

        assertTrue(resultado);
        assertNotNull(usuario);
        assertEquals(RolUsuario.ESTUDIANTE_UPM, usuario.getRol());
    }

    @Test
    public void CN04_registrarPersonalUPMConDatosValidosDevuelveTrue() {
        // Prueba CN04
        boolean resultado = registrarParticipante("Personal UPM", "per01", "personal1@upm.es", "3");

        Usuario usuario = controlador.login("personal1@upm.es", PASSWORD_VALIDO);

        assertTrue(resultado);
        assertNotNull(usuario);
        assertEquals(RolUsuario.PERSONAL_UPM, usuario.getRol());
    }

    @Test
    public void CN05_registrarInstructorConDatosValidosDevuelveTrue() {
        // Prueba CN05
        boolean resultado = controlador.registrarInstructorComoAdministrador(
                crearAdministrador(), "Instructor Uno", "ins01", "inst1@upm.es",
                PASSWORD_VALIDO, DNI_VALIDO, IBAN_VALIDO);

        Usuario usuario = controlador.login("inst1@upm.es", PASSWORD_VALIDO);

        assertTrue(resultado);
        assertNotNull(usuario);
        assertEquals(RolUsuario.INSTRUCTOR, usuario.getRol());
    }

    @Test
    public void CN06_loginConCredencialesCorrectasDevuelveUsuario() {
        // Prueba CN06
        assertTrue(registrarParticipante("Participante Login", "log01", "login1@gmail.com", ""));

        Usuario usuario = controlador.login("login1@gmail.com", PASSWORD_VALIDO);

        assertNotNull(usuario);
        assertEquals("login1@gmail.com", usuario.getCorreoElectronico());
    }

    @Test
    public void CN07_loginConCorreoNoRegistradoDevuelveNull() {
        // Prueba CN07
        Usuario usuario = controlador.login("noexiste@gmail.com", PASSWORD_VALIDO);

        assertNull(usuario);
    }

    @Test
    public void CN08_loginConPasswordIncorrectaDevuelveNull() {
        // Prueba CN08
        assertTrue(registrarParticipante("Participante Login", "log02", "login2@gmail.com", ""));

        Usuario usuario = controlador.login("login2@gmail.com", "PasswordIncorrecta123");

        assertNull(usuario);
    }

    @Test
    public void CN09_registrarParticipanteRechazaNombreVacio() {
        // Prueba CN09
        boolean resultado = registrarParticipante("   ", "ext02", "externo2@gmail.com", "");

        assertFalse(resultado);
        assertNull(controlador.login("externo2@gmail.com", PASSWORD_VALIDO));
    }

    @Test
    public void CN10_registrarParticipanteRechazaNickDemasiadoCorto() {
        // Prueba CN10
        boolean resultado = registrarParticipante("Participante", "abc", "externo3@gmail.com", "");

        assertFalse(resultado);
        assertNull(controlador.login("externo3@gmail.com", PASSWORD_VALIDO));
    }

    @Test
    public void CN11_registrarParticipanteRechazaNickDemasiadoLargo() {
        // Prueba CN11
        boolean resultado = registrarParticipante("Participante", "usuario1234567", "externo4@gmail.com", "");

        assertFalse(resultado);
        assertNull(controlador.login("externo4@gmail.com", PASSWORD_VALIDO));
    }

    @Test
    public void CN12_registrarParticipanteRechazaNickConCaracteresNoAlfanumericos() {
        // Prueba CN12
        boolean resultado = registrarParticipante("Participante", "user_1", "externo5@gmail.com", "");

        assertFalse(resultado);
        assertNull(controlador.login("externo5@gmail.com", PASSWORD_VALIDO));
    }

    @Test
    public void CN13_registrarParticipanteRechazaNickConflictivo() {
        // Prueba CN13
        boolean resultado = registrarParticipante("Participante", "admin", "externo6@gmail.com", "");

        assertFalse(resultado);
        assertNull(controlador.login("externo6@gmail.com", PASSWORD_VALIDO));
    }

    @Test
    public void CN14_registrarParticipanteRechazaPasswordSinMayuscula() {
        // Prueba CN14
        boolean resultado = registrarParticipanteConPassword(
                "Participante", "ext03", "externo7@gmail.com", "password1234", "");

        assertFalse(resultado);
        assertNull(controlador.login("externo7@gmail.com", "password1234"));
    }

    @Test
    public void CN15_registrarParticipanteRechazaPasswordDemasiadoCorta() {
        // Prueba CN15
        boolean resultado = registrarParticipanteConPassword(
                "Participante", "ext04", "externo8@gmail.com", "Pass123", "");

        assertFalse(resultado);
        assertNull(controlador.login("externo8@gmail.com", "Pass123"));
    }

    @Test
    public void CN16_registrarParticipanteRechazaPasswordSinMinuscula() {
        // Prueba CN16
        boolean resultado = registrarParticipanteConPassword(
                "Participante", "ext05", "externo9@gmail.com", "PASSWORD1234", "");

        assertFalse(resultado);
        assertNull(controlador.login("externo9@gmail.com", "PASSWORD1234"));
    }

    @Test
    public void CN17_registrarParticipanteRechazaPasswordSinNumero() {
        // Prueba CN17
        boolean resultado = registrarParticipanteConPassword(
                "Participante", "ext06", "externo10@gmail.com", "PasswordValida", "");

        assertFalse(resultado);
        assertNull(controlador.login("externo10@gmail.com", "PasswordValida"));
    }

    @Test
    public void CN18_registrarParticipanteRechazaCorreoVacio() {
        // Prueba CN18
        boolean resultado = registrarParticipante("Participante", "ext07", "   ", "");

        assertFalse(resultado);
    }

    @Test
    public void CN19_registrarParticipanteRechazaCorreoInvalido() {
        // Prueba CN19
        boolean resultado = registrarParticipante("Participante", "ext08", "correo-invalido", "");

        assertFalse(resultado);
    }

    @Test
    public void CN20_registrarParticipanteRechazaCorreoDuplicado() {
        // Prueba CN20
        assertTrue(registrarParticipante("Participante Uno", "ext09", "duplicado@gmail.com", ""));

        boolean resultado = controlador.registrarParticipante(
                "Participante Dos", "ext10", "duplicado@gmail.com", PASSWORD_VALIDO,
                "87654321B", TARJETA_VALIDA, "", sinPreferencias());

        assertFalse(resultado);
    }

    @Test
    public void CN21_registrarParticipanteRechazaNickDuplicado() {
        // Prueba CN21
        assertTrue(registrarParticipante("Participante Uno", "ext11", "externo11@gmail.com", ""));

        boolean resultado = controlador.registrarParticipante(
                "Participante Dos", "ext11", "externo12@gmail.com", PASSWORD_VALIDO,
                "87654321B", TARJETA_VALIDA, "", sinPreferencias());

        assertFalse(resultado);
    }

    @Test
    public void CN22_registrarUsuarioUPMRechazaValidacionExternaFallida() {
        // Prueba CN22
        ControladorUsuarios controladorUPM = new ControladorUsuarios(
                new PersistenciaEnMemoria(), new ValidadorUPMDePruebaRechaza());

        boolean resultado = controladorUPM.registrarParticipante(
                "Alumno UPM", "alu02", "alumno2@alumnos.upm.es", PASSWORD_VALIDO,
                DNI_VALIDO, TARJETA_VALIDA, "M002", sinPreferencias());

        assertFalse(resultado);
        assertNull(controladorUPM.login("alumno2@alumnos.upm.es", PASSWORD_VALIDO));
    }

    @Test
    public void CN23_registrarParticipanteRechazaDNIInvalido() {
        // Prueba CN23
        boolean resultado = controlador.registrarParticipante(
                "Participante", "ext12", "externo13@gmail.com", PASSWORD_VALIDO,
                "1234567A", TARJETA_VALIDA, "", sinPreferencias());

        assertFalse(resultado);
    }

    @Test
    public void CN24_registrarParticipanteRechazaTarjetaVacia() {
        // Prueba CN24
        boolean resultado = controlador.registrarParticipante(
                "Participante", "ext13", "externo14@gmail.com", PASSWORD_VALIDO,
                DNI_VALIDO, "   ", "", sinPreferencias());

        assertFalse(resultado);
    }

    @Test
    public void CN25_registrarParticipanteRechazaTarjetaConMenosDeDieciseisDigitos() {
        // Prueba CN25
        boolean resultado = controlador.registrarParticipante(
                "Participante", "ext14", "externo15@gmail.com", PASSWORD_VALIDO,
                DNI_VALIDO, "123456789012345", "", sinPreferencias());

        assertFalse(resultado);
    }

    @Test
    public void CN26_registrarParticipanteRechazaTarjetaConMasDeDieciseisDigitos() {
        // Prueba CN26
        boolean resultado = controlador.registrarParticipante(
                "Participante", "ext15", "externo16@gmail.com", PASSWORD_VALIDO,
                DNI_VALIDO, "12345678901234567", "", sinPreferencias());

        assertFalse(resultado);
    }

    @Test
    public void CN27_registrarParticipanteRechazaTarjetaNoNumerica() {
        // Prueba CN27
        boolean resultado = controlador.registrarParticipante(
                "Participante", "ext16", "externo17@gmail.com", PASSWORD_VALIDO,
                DNI_VALIDO, "123456789012345A", "", sinPreferencias());

        assertFalse(resultado);
    }

    @Test
    public void CN28_registrarEstudianteUPMRechazaMatriculaVacia() {
        // Prueba CN28
        boolean resultado = registrarParticipante("Alumno UPM", "alu03", "alumno3@alumnos.upm.es", "   ");

        assertFalse(resultado);
        assertNull(controlador.login("alumno3@alumnos.upm.es", PASSWORD_VALIDO));
    }

    @Test
    public void CN29_registrarPersonalUPMRechazaAntiguedadVacia() {
        // Prueba CN29
        boolean resultado = registrarParticipante("Personal UPM", "per02", "personal2@upm.es", "   ");

        assertFalse(resultado);
        assertNull(controlador.login("personal2@upm.es", PASSWORD_VALIDO));
    }

    @Test
    public void CN30_registrarPersonalUPMRechazaAntiguedadNoNumerica() {
        // Prueba CN30
        boolean resultado = registrarParticipante("Personal UPM", "per03", "personal3@upm.es", "tres");

        assertFalse(resultado);
        assertNull(controlador.login("personal3@upm.es", PASSWORD_VALIDO));
    }

    @Test
    public void CN31_registrarPersonalUPMRechazaAntiguedadNegativa() {
        // Prueba CN31
        boolean resultado = registrarParticipante("Personal UPM", "per04", "personal4@upm.es", "-1");

        assertFalse(resultado);
        assertNull(controlador.login("personal4@upm.es", PASSWORD_VALIDO));
    }

    @Test
    public void CN32_validarNivelPreferenciaAceptaCero() {
        // Prueba CN32
        assertNull(controlador.validarNivelPreferencia("0"));
    }

    @Test
    public void CN33_validarNivelPreferenciaAceptaValorEntreUnoYDiez() {
        // Prueba CN33
        assertNull(controlador.validarNivelPreferencia("5"));
    }

    @Test
    public void CN34_validarNivelPreferenciaRechazaNivelVacio() {
        // Prueba CN34
        assertNotNull(controlador.validarNivelPreferencia("   "));
    }

    @Test
    public void CN35_validarNivelPreferenciaRechazaNivelMenorQueCero() {
        // Prueba CN35
        assertNotNull(controlador.validarNivelPreferencia("-1"));
    }

    @Test
    public void CN36_validarNivelPreferenciaRechazaNivelMayorQueDiez() {
        // Prueba CN36
        assertNotNull(controlador.validarNivelPreferencia("11"));
    }

    @Test
    public void CN37_validarNivelPreferenciaRechazaNivelNoNumerico() {
        // Prueba CN37
        assertNotNull(controlador.validarNivelPreferencia("alto"));
    }

    @Test
    public void CN38_registrarInstructorRechazaAdministradorNulo() {
        // Prueba CN38
        boolean resultado = controlador.registrarInstructorComoAdministrador(
                null, "Instructor Uno", "ins02", "inst2@upm.es",
                PASSWORD_VALIDO, DNI_VALIDO, IBAN_VALIDO);

        assertFalse(resultado);
        assertNull(controlador.login("inst2@upm.es", PASSWORD_VALIDO));
    }

    @Test
    public void CN39_registrarInstructorRechazaIBANVacio() {
        // Prueba CN39
        boolean resultado = controlador.registrarInstructorComoAdministrador(
                crearAdministrador(), "Instructor Uno", "ins03", "inst3@upm.es",
                PASSWORD_VALIDO, DNI_VALIDO, "   ");

        assertFalse(resultado);
        assertNull(controlador.login("inst3@upm.es", PASSWORD_VALIDO));
    }

    @Test
    public void CN40_registrarInstructorRechazaIBANQueNoEmpiezaPorES() {
        // Prueba CN40
        boolean resultado = controlador.registrarInstructorComoAdministrador(
                crearAdministrador(), "Instructor Uno", "ins04", "inst4@upm.es",
                PASSWORD_VALIDO, DNI_VALIDO, "FR7620770024003102575766");

        assertFalse(resultado);
        assertNull(controlador.login("inst4@upm.es", PASSWORD_VALIDO));
    }

    @Test
    public void CN41_registrarInstructorRechazaIBANConLongitudIncorrecta() {
        // Prueba CN41
        boolean resultado = controlador.registrarInstructorComoAdministrador(
                crearAdministrador(), "Instructor Uno", "ins05", "inst5@upm.es",
                PASSWORD_VALIDO, DNI_VALIDO, "ES123");

        assertFalse(resultado);
        assertNull(controlador.login("inst5@upm.es", PASSWORD_VALIDO));
    }

    @Test
    public void CN42_registrarInstructorRechazaIBANConLetrasDespuesDeES() {
        // Prueba CN42
        boolean resultado = controlador.registrarInstructorComoAdministrador(
                crearAdministrador(), "Instructor Uno", "ins06", "inst6@upm.es",
                PASSWORD_VALIDO, DNI_VALIDO, "ES76207700240031025757AA");

        assertFalse(resultado);
        assertNull(controlador.login("inst6@upm.es", PASSWORD_VALIDO));
    }

    @Test
    public void CN43_loginRechazaCorreoVacio() {
        // Prueba CN43
        Usuario usuario = controlador.login("   ", PASSWORD_VALIDO);

        assertNull(usuario);
    }

    @Test
    public void CN44_loginRechazaCorreoConFormatoIncorrecto() {
        // Prueba CN44
        Usuario usuario = controlador.login("correo-invalido", PASSWORD_VALIDO);

        assertNull(usuario);
    }

    @Test
    public void CN45_loginRechazaPasswordVacia() {
        // Prueba CN45
        assertTrue(registrarParticipante("Participante Login", "log03", "login3@gmail.com", ""));

        Usuario usuario = controlador.login("login3@gmail.com", "   ");

        assertNull(usuario);
    }

    @Test
    public void CN46_loginRechazaPasswordIncorrecta() {
        // Prueba CN46
        assertTrue(registrarParticipante("Participante Login", "log04", "login4@gmail.com", ""));

        Usuario usuario = controlador.login("login4@gmail.com", "PasswordIncorrecta123");

        assertNull(usuario);
    }

    private boolean registrarParticipante(String nombre, String nick, String correo, String datoEspecifico) {
        return controlador.registrarParticipante(
                nombre, nick, correo, PASSWORD_VALIDO,
                DNI_VALIDO, TARJETA_VALIDA, datoEspecifico, sinPreferencias());
    }

    private boolean registrarParticipanteConPassword(String nombre, String nick, String correo,
                                                     String password, String datoEspecifico) {
        return controlador.registrarParticipante(
                nombre, nick, correo, password,
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
}
