package upmarts.controlador;

import static org.junit.Assert.assertEquals;
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
import upmarts.modelo.Usuario;
import upmarts.persistencia.IAccesoUsuarios;

public class ControladorUsuariosAltaAccesoCajaNegraTest {

    private static final String PASSWORD_VALIDO = "Password1234";
    private static final String DNI_VALIDO = "12345678A";
    private static final String TARJETA_MINIMA = "12345678";
    private static final String TARJETA_MAXIMA = "1234567890123456789";
    private static final String IBAN_VALIDO = "ES7620770024003102575766";
    private static final String ERROR_NOMBRE_VACIO = "El nombre completo no puede estar vacío.";
    private static final String ERROR_NICK_INVALIDO =
            "Nick inválido. Debe tener entre 4 y 12 caracteres alfanuméricos y no usar términos conflictivos.";
    private static final String ERROR_PASSWORD_INVALIDA =
            "Contraseña inválida. Debe tener al menos 12 caracteres, incluir mayúsculas, minúsculas y números.";
    private static final String ERROR_CORREO_INVALIDO = "Correo electrónico inválido.";
    private static final String ERROR_CORREO_DUPLICADO = "Ya existe un usuario registrado con ese correo.";
    private static final String ERROR_NICK_DUPLICADO = "Ya existe un usuario registrado con ese nick.";
    private static final String ERROR_DNI_INVALIDO = "DNI inválido. Debe tener 8 dígitos seguidos de una letra.";
    private static final String ERROR_TARJETA_INVALIDA =
            "Número de tarjeta inválido. Debe contener entre 8 y 19 dígitos.";
    private static final String ERROR_MATRICULA_VACIA = "El número de matrícula no puede estar vacío.";
    private static final String ERROR_VALIDACION_UPM =
            "No se ha podido validar la cuenta UPM. Compruebe correo y contraseña UPM.";
    private static final String ERROR_ANTIGUEDAD_INVALIDA = "La antigüedad debe ser un número entero válido.";

    private PersistenciaEnMemoria persistencia;
    private ValidadorUPMFalso validadorUPM;
    private ControladorUsuarios controlador;

    @Before
    public void setUp() {
        persistencia = new PersistenciaEnMemoria();
        validadorUPM = new ValidadorUPMFalso(true);
        controlador = new ControladorUsuarios(persistencia, validadorUPM);
    }

    @Test
    public void detectarTipoParticipantePorCorreoDevuelveAlumnoUPMParaDominioDeAlumnos() {
        assertEquals(ControladorUsuarios.TIPO_ALUMNO_UPM,
                controlador.detectarTipoParticipantePorCorreo(" alumno@ALUMNOS.UPM.ES "));
    }

    @Test
    public void detectarTipoParticipantePorCorreoDevuelvePersonalUPMParaDominioUPM() {
        assertEquals(ControladorUsuarios.TIPO_PERSONAL_UPM,
                controlador.detectarTipoParticipantePorCorreo("persona@upm.es"));
    }

    @Test
    public void detectarTipoParticipantePorCorreoDevuelveExternoParaDominioNoUPM() {
        assertEquals(ControladorUsuarios.TIPO_EXTERNO,
                controlador.detectarTipoParticipantePorCorreo("persona@example.com"));
    }

    @Test
    public void detectarTipoParticipantePorCorreoDevuelveCorreoInvalidoSiEsNulo() {
        assertEquals(ControladorUsuarios.TIPO_CORREO_INVALIDO,
                controlador.detectarTipoParticipantePorCorreo(null));
    }

    @Test
    public void detectarTipoParticipantePorCorreoDevuelveCorreoInvalidoSiFaltaLaArroba() {
        assertEquals(ControladorUsuarios.TIPO_CORREO_INVALIDO,
                controlador.detectarTipoParticipantePorCorreo("sin-arroba"));
    }

    @Test
    public void registrarParticipanteExternoAceptaValoresLimiteMinimosValidos() {
        boolean registrado = controlador.registrarParticipante(
                "Externo Minimo", "abcd", "externo.min@example.com", PASSWORD_VALIDO,
                DNI_VALIDO, TARJETA_MINIMA, "", Collections.emptyList());

        assertTrue(registrado);
        assertNull(controlador.getUltimoError());
        Usuario usuario = controlador.login("externo.min@example.com", PASSWORD_VALIDO);
        assertNotNull(usuario);
        assertEquals(ParticipanteExterno.class, usuario.getClass());
    }

    @Test
    public void registrarParticipanteExternoAceptaValoresLimiteMaximosValidos() {
        boolean registrado = controlador.registrarParticipante(
                "Externo Maximo", "abcdefghijkl", "externo.max@example.com", PASSWORD_VALIDO,
                DNI_VALIDO, TARJETA_MAXIMA, "", Collections.emptyList());

        assertTrue(registrado);
        assertNull(controlador.getUltimoError());
        Usuario usuario = controlador.login("externo.max@example.com", PASSWORD_VALIDO);
        assertNotNull(usuario);
        assertEquals(ParticipanteExterno.class, usuario.getClass());
    }

    @Test
    public void registrarParticipanteRechazaNombreVacio() {
        boolean registrado = controlador.registrarParticipante(
                "", "externo1", "externo1@example.com", PASSWORD_VALIDO,
                DNI_VALIDO, TARJETA_MINIMA, "", Collections.emptyList());

        assertEquals(false, registrado);
        assertEquals(ERROR_NOMBRE_VACIO, controlador.getUltimoError());
        assertNull(controlador.login("externo1@example.com", PASSWORD_VALIDO));
    }

    @Test
    public void registrarParticipanteRechazaNickDeTresCaracteres() {
        boolean registrado = controlador.registrarParticipante(
                "Externo Uno", "abc", "nick.corto@example.com", PASSWORD_VALIDO,
                DNI_VALIDO, TARJETA_MINIMA, "", Collections.emptyList());

        assertEquals(false, registrado);
        assertEquals(ERROR_NICK_INVALIDO, controlador.getUltimoError());
    }

    @Test
    public void registrarParticipanteRechazaNickDeTreceCaracteres() {
        boolean registrado = controlador.registrarParticipante(
                "Externo Uno", "abcdefghijklm", "nick.largo@example.com", PASSWORD_VALIDO,
                DNI_VALIDO, TARJETA_MINIMA, "", Collections.emptyList());

        assertEquals(false, registrado);
        assertEquals(ERROR_NICK_INVALIDO, controlador.getUltimoError());
    }

    @Test
    public void registrarParticipanteRechazaPasswordDeOnceCaracteres() {
        boolean registrado = controlador.registrarParticipante(
                "Externo Uno", "externo2", "password.corta@example.com", "Passwor1234",
                DNI_VALIDO, TARJETA_MINIMA, "", Collections.emptyList());

        assertEquals(false, registrado);
        assertEquals(ERROR_PASSWORD_INVALIDA, controlador.getUltimoError());
    }

    @Test
    public void registrarParticipanteRechazaPasswordSinMayusculas() {
        boolean registrado = controlador.registrarParticipante(
                "Externo Uno", "externo3", "password.formato@example.com", "password1234",
                DNI_VALIDO, TARJETA_MINIMA, "", Collections.emptyList());

        assertEquals(false, registrado);
        assertEquals(ERROR_PASSWORD_INVALIDA, controlador.getUltimoError());
    }

    @Test
    public void registrarParticipanteRechazaCorreoSinArroba() {
        boolean registrado = controlador.registrarParticipante(
                "Externo Uno", "externo4", "correoinvalido", PASSWORD_VALIDO,
                DNI_VALIDO, TARJETA_MINIMA, "", Collections.emptyList());

        assertEquals(false, registrado);
        assertEquals(ERROR_CORREO_INVALIDO, controlador.getUltimoError());
    }

    @Test
    public void registrarParticipanteRechazaCorreoDuplicado() {
        assertTrue(controlador.registrarParticipante(
                "Externo Base", "externo5", "duplicado@example.com", PASSWORD_VALIDO,
                DNI_VALIDO, TARJETA_MINIMA, "", Collections.emptyList()));

        boolean registrado = controlador.registrarParticipante(
                "Externo Copia", "externo6", "duplicado@example.com", PASSWORD_VALIDO,
                "87654321B", TARJETA_MAXIMA, "", Collections.emptyList());

        assertEquals(false, registrado);
        assertEquals(ERROR_CORREO_DUPLICADO, controlador.getUltimoError());
    }

    @Test
    public void registrarParticipanteRechazaNickDuplicado() {
        assertTrue(controlador.registrarParticipante(
                "Externo Base", "externo7", "nick.base@example.com", PASSWORD_VALIDO,
                DNI_VALIDO, TARJETA_MINIMA, "", Collections.emptyList()));

        boolean registrado = controlador.registrarParticipante(
                "Externo Copia", "externo7", "nick.copia@example.com", PASSWORD_VALIDO,
                "87654321B", TARJETA_MAXIMA, "", Collections.emptyList());

        assertEquals(false, registrado);
        assertEquals(ERROR_NICK_DUPLICADO, controlador.getUltimoError());
    }

    @Test
    public void registrarParticipanteRechazaDNIConSieteDigitos() {
        boolean registrado = controlador.registrarParticipante(
                "Externo Uno", "externo8", "dni.corto@example.com", PASSWORD_VALIDO,
                "1234567A", TARJETA_MINIMA, "", Collections.emptyList());

        assertEquals(false, registrado);
        assertEquals(ERROR_DNI_INVALIDO, controlador.getUltimoError());
    }

    @Test
    public void registrarParticipanteRechazaTarjetaDeSieteDigitos() {
        boolean registrado = controlador.registrarParticipante(
                "Externo Uno", "externo9", "tarjeta.corta@example.com", PASSWORD_VALIDO,
                DNI_VALIDO, "1234567", "", Collections.emptyList());

        assertEquals(false, registrado);
        assertEquals(ERROR_TARJETA_INVALIDA, controlador.getUltimoError());
    }

    @Test
    public void registrarParticipanteRechazaTarjetaDeVeinteDigitos() {
        boolean registrado = controlador.registrarParticipante(
                "Externo Uno", "externoa", "tarjeta.larga@example.com", PASSWORD_VALIDO,
                DNI_VALIDO, "12345678901234567890", "", Collections.emptyList());

        assertEquals(false, registrado);
        assertEquals(ERROR_TARJETA_INVALIDA, controlador.getUltimoError());
    }

    @Test
    public void registrarEstudianteUPMValidoCreaEstudiante() {
        boolean registrado = controlador.registrarParticipante(
                "Alumno Valido", "alumno1", "alumno1@alumnos.upm.es", PASSWORD_VALIDO,
                DNI_VALIDO, TARJETA_MINIMA, "MAT-001", Collections.emptyList());

        assertTrue(registrado);
        assertNull(controlador.getUltimoError());
        Usuario usuario = controlador.login("alumno1@alumnos.upm.es", PASSWORD_VALIDO);
        assertNotNull(usuario);
        assertEquals(EstudianteUPM.class, usuario.getClass());
        assertEquals("MAT-001", ((EstudianteUPM) usuario).getNumeroMatricula());
    }

    @Test
    public void registrarEstudianteUPMRechazaMatriculaVacia() {
        boolean registrado = controlador.registrarParticipante(
                "Alumno Sin Matricula", "alumno2", "alumno2@alumnos.upm.es", PASSWORD_VALIDO,
                DNI_VALIDO, TARJETA_MINIMA, "", Collections.emptyList());

        assertEquals(false, registrado);
        assertEquals(ERROR_MATRICULA_VACIA, controlador.getUltimoError());
    }

    @Test
    public void registrarEstudianteUPMRechazaCuentaNoValidadaPorLDAP() {
        validadorUPM.setResultado(false);

        boolean registrado = controlador.registrarParticipante(
                "Alumno No Validado", "alumno3", "alumno3@alumnos.upm.es", PASSWORD_VALIDO,
                DNI_VALIDO, TARJETA_MINIMA, "MAT-003", Collections.emptyList());

        assertEquals(false, registrado);
        assertEquals(ERROR_VALIDACION_UPM, controlador.getUltimoError());
    }

    @Test
    public void registrarPersonalUPMValidoAceptaAntiguedadCero() {
        boolean registrado = controlador.registrarParticipante(
                "Personal Valido", "perso1", "perso1@upm.es", PASSWORD_VALIDO,
                DNI_VALIDO, TARJETA_MINIMA, "0", Collections.emptyList());

        assertTrue(registrado);
        assertNull(controlador.getUltimoError());
        Usuario usuario = controlador.login("perso1@upm.es", PASSWORD_VALIDO);
        assertNotNull(usuario);
        assertEquals(PersonalUPM.class, usuario.getClass());
        assertEquals(0, ((PersonalUPM) usuario).getAntiguedad());
    }

    @Test
    public void registrarPersonalUPMRechazaAntiguedadNoNumerica() {
        boolean registrado = controlador.registrarParticipante(
                "Personal Invalido", "perso2", "perso2@upm.es", PASSWORD_VALIDO,
                DNI_VALIDO, TARJETA_MINIMA, "seis", Collections.emptyList());

        assertEquals(false, registrado);
        assertEquals(ERROR_ANTIGUEDAD_INVALIDA, controlador.getUltimoError());
    }

    @Test
    public void registrarPersonalUPMRechazaAntiguedadNegativa() {
        boolean registrado = controlador.registrarParticipante(
                "Personal Invalido", "perso3", "perso3@upm.es", PASSWORD_VALIDO,
                DNI_VALIDO, TARJETA_MINIMA, "-1", Collections.emptyList());

        assertEquals(false, registrado);
        assertEquals(ERROR_ANTIGUEDAD_INVALIDA, controlador.getUltimoError());
    }

    @Test
    public void loginDevuelveUsuarioConCorreoSinImportarMayusculas() {
        assertTrue(controlador.registrarParticipante(
                "Externo Login", "externob", "externo.login@example.com", PASSWORD_VALIDO,
                DNI_VALIDO, TARJETA_MINIMA, "", Collections.emptyList()));

        Usuario usuario = controlador.login("  EXTERNO.LOGIN@EXAMPLE.COM ", PASSWORD_VALIDO);

        assertNotNull(usuario);
        assertEquals("externob", usuario.getNombreUsuario());
    }

    @Test
    public void loginDevuelveNullSiCorreoEsNulo() {
        assertNull(controlador.login(null, PASSWORD_VALIDO));
    }

    @Test
    public void loginDevuelveNullSiPasswordEsNula() {
        assertNull(controlador.login("usuario@example.com", null));
    }

    @Test
    public void loginDevuelveNullSiLaPasswordEsIncorrecta() {
        assertTrue(controlador.registrarParticipante(
                "Externo Login", "externoc", "externo.login2@example.com", PASSWORD_VALIDO,
                DNI_VALIDO, TARJETA_MINIMA, "", Collections.emptyList()));

        assertNull(controlador.login("externo.login2@example.com", "Password4321"));
    }

    @Test
    public void registrarInstructorComoAdministradorCreaInstructorValido() {
        boolean registrado = controlador.registrarInstructorComoAdministrador(
                crearAdministrador(), "Instructor Nuevo", "instnew", "instnew@upm.es",
                PASSWORD_VALIDO, "87654321B", IBAN_VALIDO);

        assertTrue(registrado);
        Usuario usuario = controlador.login("instnew@upm.es", PASSWORD_VALIDO);
        assertNotNull(usuario);
        assertEquals(Instructor.class, usuario.getClass());
    }

    @Test
    public void registrarInstructorComoAdministradorRechazaAdministradorNulo() {
        boolean registrado = controlador.registrarInstructorComoAdministrador(
                null, "Instructor Nuevo", "instn01", "instn01@upm.es",
                PASSWORD_VALIDO, "87654321B", IBAN_VALIDO);

        assertEquals(false, registrado);
    }

    @Test
    public void registrarInstructorComoAdministradorRechazaIBANInvalido() {
        boolean registrado = controlador.registrarInstructorComoAdministrador(
                crearAdministrador(), "Instructor Nuevo", "instn02", "instn02@upm.es",
                PASSWORD_VALIDO, "87654321B", "1234");

        assertEquals(false, registrado);
    }

    @Test
    public void ultimoErrorSeLimpiaTrasUnRegistroPosteriorCorrecto() {
        boolean primerRegistro = controlador.registrarParticipante(
                "Externo Uno", "abc", "error.prev@example.com", PASSWORD_VALIDO,
                DNI_VALIDO, TARJETA_MINIMA, "", Collections.emptyList());

        boolean segundoRegistro = controlador.registrarParticipante(
                "Externo Dos", "externod", "ok.despues@example.com", PASSWORD_VALIDO,
                DNI_VALIDO, TARJETA_MINIMA, "", Collections.emptyList());

        assertEquals(false, primerRegistro);
        assertTrue(segundoRegistro);
        assertNull(controlador.getUltimoError());
    }

    private Administrador crearAdministrador() {
        return new Administrador("admin", "Admin", "admin@test.com",
                Usuario.cifrarPassword(PASSWORD_VALIDO), "910000000");
    }

    private static class ValidadorUPMFalso implements IValidadorUPM {
        private boolean resultado;

        ValidadorUPMFalso(boolean resultado) {
            this.resultado = resultado;
        }

        void setResultado(boolean resultado) {
            this.resultado = resultado;
        }

        @Override
        public boolean verificarCredencialesUPM(String correo, String password) {
            return resultado;
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
