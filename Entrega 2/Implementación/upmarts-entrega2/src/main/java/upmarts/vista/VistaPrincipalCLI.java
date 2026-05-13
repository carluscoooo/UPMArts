package upmarts.vista;

import java.util.List;
import java.util.Scanner;

import upmarts.controlador.ControladorUsuarios;
import upmarts.controlador.IControladorUsuarios;
import upmarts.integracion.AdaptadorLDAP;
import upmarts.integracion.IValidadorUPM;
import upmarts.modelo.Administrador;
import upmarts.modelo.Instructor;
import upmarts.modelo.Usuario;
import upmarts.persistencia.GestorFicheroUsuarios;
import upmarts.persistencia.IAccesoUsuarios;

public class VistaPrincipalCLI {

    private static final String RUTA_USUARIOS = "data/usuarios.txt";

    private Scanner scanner;
    private IVistaUsuariosCLI vistaUsuariosCLI;
    private IAccesoUsuarios accesoUsuarios;
    private IValidadorUPM validadorUPM;
    private IControladorUsuarios controladorUsuarios;

    public VistaPrincipalCLI() {
        this(new Scanner(System.in));
    }

    private VistaPrincipalCLI(Scanner scanner) {
        this.scanner = scanner;
        this.accesoUsuarios = new GestorFicheroUsuarios(RUTA_USUARIOS);
        crearUsuariosInicialesSiNoExisten(accesoUsuarios);
        this.validadorUPM = new AdaptadorLDAP();
        this.controladorUsuarios = new ControladorUsuarios(accesoUsuarios);
        this.vistaUsuariosCLI = new VistaUsuariosCLI(controladorUsuarios, validadorUPM, scanner);
    }

    public VistaPrincipalCLI(IVistaUsuariosCLI vistaUsuariosCLI, Scanner scanner) {
        this.vistaUsuariosCLI = vistaUsuariosCLI;
        this.scanner = scanner;
    }

    public void iniciarAplicacion() {
        int opcion = -1;

        while (opcion != 0) {
            mostrarCabecera();
            System.out.println("1. Registrarse");
            System.out.println("2. Iniciar sesión");
            System.out.println("0. Salir");
            System.out.print("Seleccione una opción: ");

            opcion = leerEntero();

            if (opcion == 1) {
                vistaUsuariosCLI.registrarParticipante();
            } else if (opcion == 2) {
                vistaUsuariosCLI.iniciarSesion();
            } else if (opcion != 0) {
                System.out.println("Opción no válida.");
            }
        }

        System.out.println("Aplicación finalizada.");

        if (scanner != null) {
            scanner.close();
        }
    }

    private void crearUsuariosInicialesSiNoExisten(IAccesoUsuarios accesoUsuarios) {
        List<Usuario> usuarios = accesoUsuarios.leerUsuarios();

        if (!existeAdministrador(usuarios)) {
            usuarios.add(new Administrador(
                    "adminsys",
                    "Administrador Principal",
                    "admin@upm.es",
                    Usuario.cifrarPassword("Admin123456A"),
                    "910000000"
            ));
        }

        if (!existeInstructor(usuarios)) {
            usuarios.add(new Instructor(
                    "profarte1",
                    "Instructor Inicial",
                    "instructor@upm.es",
                    Usuario.cifrarPassword("Instructor123A"),
                    "12345678A",
                    "ES7620770024003102575766"
            ));
        }

        accesoUsuarios.guardarUsuarios(usuarios);
    }

    private boolean existeAdministrador(List<Usuario> usuarios) {
        for (Usuario usuario : usuarios) {
            if (usuario.esAdministrador()) {
                return true;
            }
        }

        return false;
    }

    private boolean existeInstructor(List<Usuario> usuarios) {
        for (Usuario usuario : usuarios) {
            if (usuario.esInstructor()) {
                return true;
            }
        }

        return false;
    }

    private void mostrarCabecera() {
        System.out.println();
        System.out.println("=================================");
        System.out.println("          UPM Arts CLI           ");
        System.out.println("=================================");
    }

    private int leerEntero() {
        try {
            String texto = scanner.nextLine();
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
