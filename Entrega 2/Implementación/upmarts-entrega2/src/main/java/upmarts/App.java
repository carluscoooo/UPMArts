package upmarts;

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
import upmarts.vista.IVistaUsuariosCLI;
import upmarts.vista.VistaPrincipalCLI;
import upmarts.vista.VistaUsuariosCLI;

public class App {

    public static void main(String[] args) {
        IAccesoUsuarios accesoUsuarios = new GestorFicheroUsuarios("data/usuarios.txt");
        crearUsuariosInicialesSiNoExisten(accesoUsuarios);

        IValidadorUPM validadorUPM = new AdaptadorLDAP();
        IControladorUsuarios controladorUsuarios = new ControladorUsuarios(accesoUsuarios);

        Scanner scanner = new Scanner(System.in);
        IVistaUsuariosCLI vistaUsuarios = new VistaUsuariosCLI(controladorUsuarios, validadorUPM, scanner);
        VistaPrincipalCLI vistaPrincipal = new VistaPrincipalCLI(vistaUsuarios, scanner);

        vistaPrincipal.iniciarAplicacion();
        scanner.close();
    }

    private static void crearUsuariosInicialesSiNoExisten(IAccesoUsuarios accesoUsuarios) {
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

    private static boolean existeAdministrador(List<Usuario> usuarios) {
        for (Usuario usuario : usuarios) {
            if (usuario instanceof Administrador) {
                return true;
            }
        }

        return false;
    }

    private static boolean existeInstructor(List<Usuario> usuarios) {
        for (Usuario usuario : usuarios) {
            if (usuario instanceof Instructor) {
                return true;
            }
        }

        return false;
    }
}
