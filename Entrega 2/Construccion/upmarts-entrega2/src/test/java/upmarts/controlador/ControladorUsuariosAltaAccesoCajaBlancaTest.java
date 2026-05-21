package upmarts.controlador;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import upmarts.integracion.IValidadorUPM;
import upmarts.modelo.Administrador;
import upmarts.modelo.Instructor;
import upmarts.modelo.ParticipanteExterno;
import upmarts.modelo.RolUsuario;
import upmarts.modelo.Usuario;
import upmarts.persistencia.IAccesoUsuarios;
import upmarts.validacion.ValidadorDatosUsuario;

public class ControladorUsuariosAltaAccesoCajaBlancaTest {

    private static final String PASSWORD_VALIDO = "Password1234";
    private static final String DNI_VALIDO = "12345678A";
    private static final String TARJETA_VALIDA = "12345678";

    @Test
    public void constructorCreaAdministradorEInstructorSiLaPersistenciaEmpiezaVacia() {
        PersistenciaEnMemoria persistencia = new PersistenciaEnMemoria();
        new ControladorUsuarios(persistencia, new ValidadorUPMContador(true));

        assertEquals(2, persistencia.usuarios.size());
        assertTrue(contieneAdministrador(persistencia.usuarios));
        assertTrue(contieneInstructor(persistencia.usuarios));
        assertEquals(1, persistencia.vecesGuardado);
    }

    @Test
    public void constructorNoGuardaNadaSiYaExistenAdministradorEInstructor() {
        PersistenciaEnMemoria persistencia = new PersistenciaEnMemoria();
        persistencia.usuarios.add(crearAdministradorInicial());
        persistencia.usuarios.add(crearInstructorInicial());
        new ControladorUsuarios(persistencia, new ValidadorUPMContador(true));

        assertEquals(2, persistencia.usuarios.size());
        assertEquals(0, persistencia.vecesGuardado);
    }

    @Test
    public void constructorAgregaSoloInstructorCuandoYaExisteAdministrador() {
        PersistenciaEnMemoria persistencia = new PersistenciaEnMemoria();
        persistencia.usuarios.add(crearAdministradorInicial());
        new ControladorUsuarios(persistencia, new ValidadorUPMContador(true));

        assertEquals(2, persistencia.usuarios.size());
        assertTrue(contieneAdministrador(persistencia.usuarios));
        assertTrue(contieneInstructor(persistencia.usuarios));
        assertEquals(1, persistencia.vecesGuardado);
    }

    @Test
    public void registrarParticipanteExternoSigueLaRutaDeExternoSinInvocarLDAP() {
        PersistenciaEnMemoria persistencia = new PersistenciaEnMemoria();
        ValidadorUPMContador validador = new ValidadorUPMContador(true);
        ControladorUsuarios controlador = new ControladorUsuarios(persistencia, validador);

        boolean registrado = controlador.registrarParticipante(
                "Participante Externo", "externo1", "externo1@example.com", PASSWORD_VALIDO,
                DNI_VALIDO, TARJETA_VALIDA, "", Collections.emptyList());

        Usuario usuarioGuardado = buscarPorCorreo(persistencia.usuarios, "externo1@example.com");
        assertTrue(registrado);
        assertEquals(0, validador.llamadas);
        assertEquals(ParticipanteExterno.class, usuarioGuardado.getClass());
        assertEquals(ValidadorDatosUsuario.cifrarPassword(PASSWORD_VALIDO), usuarioGuardado.getContrasena());
        assertNotEquals(PASSWORD_VALIDO, usuarioGuardado.getContrasena());
    }

    @Test
    public void registrarParticipanteAlumnoCortaLaEjecucionAntesDeLDAPSiLaMatriculaEsVacia() {
        PersistenciaEnMemoria persistencia = new PersistenciaEnMemoria();
        ValidadorUPMContador validador = new ValidadorUPMContador(true);
        ControladorUsuarios controlador = new ControladorUsuarios(persistencia, validador);

        boolean registrado = controlador.registrarParticipante(
                "Alumno Sin Matricula", "alumno1", "alumno1@alumnos.upm.es", PASSWORD_VALIDO,
                DNI_VALIDO, TARJETA_VALIDA, "   ", Collections.emptyList());

        assertFalse(registrado);
        assertEquals(0, validador.llamadas);
        assertNull(buscarPorCorreo(persistencia.usuarios, "alumno1@alumnos.upm.es"));
    }

    @Test
    public void registrarParticipanteAlumnoSigueLaRutaDeLDAPNegativa() {
        PersistenciaEnMemoria persistencia = new PersistenciaEnMemoria();
        ValidadorUPMContador validador = new ValidadorUPMContador(false);
        ControladorUsuarios controlador = new ControladorUsuarios(persistencia, validador);

        boolean registrado = controlador.registrarParticipante(
                "Alumno No Validado", "alumno2", "alumno2@alumnos.upm.es", PASSWORD_VALIDO,
                DNI_VALIDO, TARJETA_VALIDA, "MAT-002", Collections.emptyList());

        assertFalse(registrado);
        assertEquals(1, validador.llamadas);
        assertNull(buscarPorCorreo(persistencia.usuarios, "alumno2@alumnos.upm.es"));
    }

    @Test
    public void registrarParticipanteAlumnoSigueLaRutaDeExitoTrasLDAP() {
        PersistenciaEnMemoria persistencia = new PersistenciaEnMemoria();
        ValidadorUPMContador validador = new ValidadorUPMContador(true);
        ControladorUsuarios controlador = new ControladorUsuarios(persistencia, validador);

        boolean registrado = controlador.registrarParticipante(
                "Alumno Validado", "alumno3", "alumno3@alumnos.upm.es", PASSWORD_VALIDO,
                DNI_VALIDO, TARJETA_VALIDA, "MAT-003", Collections.emptyList());

        assertTrue(registrado);
        assertEquals(1, validador.llamadas);
        assertTrue(buscarPorCorreo(persistencia.usuarios, "alumno3@alumnos.upm.es") != null);
    }

    @Test
    public void registrarParticipantePersonalDetieneLaRutaSiLDAPFallaAntesDeConvertirAntiguedad() {
        PersistenciaEnMemoria persistencia = new PersistenciaEnMemoria();
        ValidadorUPMContador validador = new ValidadorUPMContador(false);
        ControladorUsuarios controlador = new ControladorUsuarios(persistencia, validador);

        boolean registrado = controlador.registrarParticipante(
                "Personal No Validado", "perso1", "perso1@upm.es", PASSWORD_VALIDO,
                DNI_VALIDO, TARJETA_VALIDA, "texto", Collections.emptyList());

        assertFalse(registrado);
        assertEquals(1, validador.llamadas);
        assertNull(buscarPorCorreo(persistencia.usuarios, "perso1@upm.es"));
    }

    @Test
    public void registrarParticipantePersonalSigueLaRutaDeAntiguedadInvalidaTrasLDAPCorrecto() {
        PersistenciaEnMemoria persistencia = new PersistenciaEnMemoria();
        ValidadorUPMContador validador = new ValidadorUPMContador(true);
        ControladorUsuarios controlador = new ControladorUsuarios(persistencia, validador);

        boolean registrado = controlador.registrarParticipante(
                "Personal Invalido", "perso2", "perso2@upm.es", PASSWORD_VALIDO,
                DNI_VALIDO, TARJETA_VALIDA, "texto", Collections.emptyList());

        assertFalse(registrado);
        assertEquals(1, validador.llamadas);
        assertNull(buscarPorCorreo(persistencia.usuarios, "perso2@upm.es"));
    }

    @Test
    public void registrarParticipanteUsaLaRamaDeCorreoDuplicadoCuandoExisteEnLaPrimeraPosicion() {
        PersistenciaEnMemoria persistencia = new PersistenciaEnMemoria();
        persistencia.usuarios.add(crearExterno("uno", "repetido@example.com"));
        ControladorUsuarios controlador = new ControladorUsuarios(persistencia, new ValidadorUPMContador(true));

        boolean registrado = controlador.registrarParticipante(
                "Duplicado", "externo2", "repetido@example.com", PASSWORD_VALIDO,
                "87654321B", TARJETA_VALIDA, "", Collections.emptyList());

        assertFalse(registrado);
    }

    @Test
    public void registrarParticipanteUsaLaRamaDeNickDuplicadoTrasRecorrerMasDeUnUsuario() {
        PersistenciaEnMemoria persistencia = new PersistenciaEnMemoria();
        persistencia.usuarios.add(crearExterno("uno", "uno@example.com"));
        persistencia.usuarios.add(crearExterno("dos", "dos@example.com"));
        persistencia.usuarios.add(crearExterno("nickrepetido", "tres@example.com"));
        ControladorUsuarios controlador = new ControladorUsuarios(persistencia, new ValidadorUPMContador(true));

        boolean registrado = controlador.registrarParticipante(
                "Duplicado", "nickrepetido", "cuatro@example.com", PASSWORD_VALIDO,
                "87654321B", TARJETA_VALIDA, "", Collections.emptyList());

        assertFalse(registrado);
    }

    @Test
    public void loginDevuelveNullCuandoLaBusquedaRecorreCeroUsuarios() {
        PersistenciaEnMemoria persistencia = new PersistenciaEnMemoria();
        ControladorUsuarios controlador = new ControladorUsuarios(persistencia, new ValidadorUPMContador(true));
        persistencia.usuarios.clear();

        assertNull(controlador.login("nadie@example.com", PASSWORD_VALIDO));
    }

    @Test
    public void loginEncuentraUsuarioEnUnaSolaIteracion() {
        PersistenciaEnMemoria persistencia = new PersistenciaEnMemoria();
        ControladorUsuarios controlador = new ControladorUsuarios(persistencia, new ValidadorUPMContador(true));
        persistencia.usuarios.clear();
        persistencia.usuarios.add(crearExterno("uno", "uno@example.com"));

        Usuario usuario = controlador.login("uno@example.com", PASSWORD_VALIDO);

        assertEquals("uno", usuario.getNombreUsuario());
    }

    @Test
    public void loginEncuentraUsuarioEnLaSegundaIteracion() {
        PersistenciaEnMemoria persistencia = new PersistenciaEnMemoria();
        ControladorUsuarios controlador = new ControladorUsuarios(persistencia, new ValidadorUPMContador(true));
        persistencia.usuarios.clear();
        persistencia.usuarios.add(crearExterno("uno", "uno@example.com"));
        persistencia.usuarios.add(crearExterno("dos", "dos@example.com"));

        Usuario usuario = controlador.login("dos@example.com", PASSWORD_VALIDO);

        assertEquals("dos", usuario.getNombreUsuario());
    }

    @Test
    public void loginRecorreUnNumeroTipicoDeUsuariosHastaEncontrarElUltimo() {
        PersistenciaEnMemoria persistencia = new PersistenciaEnMemoria();
        ControladorUsuarios controlador = new ControladorUsuarios(persistencia, new ValidadorUPMContador(true));
        persistencia.usuarios.clear();
        persistencia.usuarios.add(crearExterno("uno", "uno@example.com"));
        persistencia.usuarios.add(crearExterno("dos", "dos@example.com"));
        persistencia.usuarios.add(crearExterno("tres", "tres@example.com"));
        persistencia.usuarios.add(crearExterno("cuatro", "cuatro@example.com"));

        Usuario usuario = controlador.login("cuatro@example.com", PASSWORD_VALIDO);

        assertEquals("cuatro", usuario.getNombreUsuario());
    }

    @Test
    public void loginDevuelveNullSiCorreoONPasswordSonNulos() {
        PersistenciaEnMemoria persistencia = new PersistenciaEnMemoria();
        ControladorUsuarios controlador = new ControladorUsuarios(persistencia, new ValidadorUPMContador(true));

        assertNull(controlador.login(null, PASSWORD_VALIDO));
        assertNull(controlador.login("uno@example.com", null));
    }

    @Test
    public void registrarInstructorSigueLaRamaDeAdministradorNulo() {
        PersistenciaEnMemoria persistencia = new PersistenciaEnMemoria();
        ControladorUsuarios controlador = new ControladorUsuarios(persistencia, new ValidadorUPMContador(true));

        boolean registrado = controlador.registrarInstructorComoAdministrador(
                null, "Instructor", "inst1", "inst1@upm.es",
                PASSWORD_VALIDO, "87654321B", "ES7620770024003102575766");

        assertFalse(registrado);
    }

    @Test
    public void registrarInstructorSigueLaRamaDeDatosComunesInvalidos() {
        PersistenciaEnMemoria persistencia = new PersistenciaEnMemoria();
        ControladorUsuarios controlador = new ControladorUsuarios(persistencia, new ValidadorUPMContador(true));

        boolean registrado = controlador.registrarInstructorComoAdministrador(
                crearAdministradorDePrueba(), "", "inst2", "inst2@upm.es",
                PASSWORD_VALIDO, "87654321B", "ES7620770024003102575766");

        assertFalse(registrado);
    }

    @Test
    public void registrarInstructorSigueLaRamaDeDniOIbanInvalidos() {
        PersistenciaEnMemoria persistencia = new PersistenciaEnMemoria();
        ControladorUsuarios controlador = new ControladorUsuarios(persistencia, new ValidadorUPMContador(true));

        boolean registrado = controlador.registrarInstructorComoAdministrador(
                crearAdministradorDePrueba(), "Instructor", "inst3", "inst3@upm.es",
                PASSWORD_VALIDO, "87654321", "ES7620770024003102575766");

        assertFalse(registrado);
    }

    @Test
    public void registrarInstructorSigueLaRutaDeExito() {
        PersistenciaEnMemoria persistencia = new PersistenciaEnMemoria();
        ControladorUsuarios controlador = new ControladorUsuarios(persistencia, new ValidadorUPMContador(true));

        boolean registrado = controlador.registrarInstructorComoAdministrador(
                crearAdministradorDePrueba(), "Instructor", "inst4", "inst4@upm.es",
                PASSWORD_VALIDO, "87654321B", "ES7620770024003102575766");

        assertTrue(registrado);
        assertTrue(buscarPorCorreo(persistencia.usuarios, "inst4@upm.es") != null);
    }

    private static Administrador crearAdministradorInicial() {
        return new Administrador(
                "adminsys",
                "Administrador Principal",
                "admin@upm.es",
                ValidadorDatosUsuario.cifrarPassword("Admin123456A"),
                "910000000"
        );
    }

    private static Administrador crearAdministradorDePrueba() {
        return new Administrador("admin", "Admin", "admin@test.com",
                ValidadorDatosUsuario.cifrarPassword(PASSWORD_VALIDO), "910000000");
    }

    private static Instructor crearInstructorInicial() {
        return new Instructor(
                "profarte1",
                "Instructor Inicial",
                "instructor@upm.es",
                ValidadorDatosUsuario.cifrarPassword("Instructor123A"),
                "12345678A",
                "ES7620770024003102575766"
        );
    }

    private static ParticipanteExterno crearExterno(String nick, String correo) {
        return new ParticipanteExterno(
                nick,
                "Participante " + nick,
                correo,
                ValidadorDatosUsuario.cifrarPassword(PASSWORD_VALIDO),
                DNI_VALIDO,
                TARJETA_VALIDA,
                Collections.emptyList()
        );
    }

    private static boolean contieneAdministrador(List<Usuario> usuarios) {
        for (Usuario usuario : usuarios) {
            if (usuario.getRol() == RolUsuario.ADMINISTRADOR) {
                return true;
            }
        }
        return false;
    }

    private static boolean contieneInstructor(List<Usuario> usuarios) {
        for (Usuario usuario : usuarios) {
            if (usuario.getRol() == RolUsuario.INSTRUCTOR) {
                return true;
            }
        }
        return false;
    }

    private static Usuario buscarPorCorreo(List<Usuario> usuarios, String correo) {
        for (Usuario usuario : usuarios) {
            if (usuario.getCorreoElectronico().equalsIgnoreCase(correo)) {
                return usuario;
            }
        }
        return null;
    }

    private static class ValidadorUPMContador implements IValidadorUPM {
        private final boolean resultado;
        private int llamadas;

        ValidadorUPMContador(boolean resultado) {
            this.resultado = resultado;
        }

        @Override
        public boolean verificarCredencialesUPM(String correo, String password) {
            llamadas++;
            return resultado;
        }
    }

    private static class PersistenciaEnMemoria implements IAccesoUsuarios {
        private final List<Usuario> usuarios = new ArrayList<>();
        private int vecesGuardado;

        @Override
        public void guardarUsuarios(List<Usuario> usuarios) {
            this.usuarios.clear();
            this.usuarios.addAll(usuarios);
            vecesGuardado++;
        }

        @Override
        public List<Usuario> leerUsuarios() {
            return new ArrayList<>(usuarios);
        }
    }
}
