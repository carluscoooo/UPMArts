package upmarts.vista;

import java.util.NoSuchElementException;
import java.util.Scanner;

import upmarts.controlador.ControladorUsuarios;
import upmarts.controlador.IControladorUsuarios;

public class VistaPrincipalCLI {

    private final Scanner scanner;
    private final IVistaUsuariosCLI vistaUsuariosCLI;
    private boolean entradaCerrada;

    public VistaPrincipalCLI() {
        this(new Scanner(System.in));
    }

    private VistaPrincipalCLI(Scanner scanner) {
        this.scanner = scanner;
        IControladorUsuarios controladorUsuarios = new ControladorUsuarios();
        this.vistaUsuariosCLI = new VistaUsuariosCLI(controladorUsuarios, scanner);
    }

    public VistaPrincipalCLI(IVistaUsuariosCLI vistaUsuariosCLI, Scanner scanner) {
        this.vistaUsuariosCLI = vistaUsuariosCLI;
        this.scanner = scanner;
    }

    public void iniciarAplicacion() {
        int opcion = -1;

        while (opcion != 0 && !entradaCerrada) {
            mostrarCabecera();
            System.out.println("1. Registrarse");
            System.out.println("2. Iniciar sesión");
            System.out.println("0. Salir");
            System.out.print("Seleccione una opción: ");

            opcion = leerEntero();

            if (opcion == 1) {
                ejecutarOperacion(() -> vistaUsuariosCLI.registrarParticipante());
            } else if (opcion == 2) {
                ejecutarOperacion(() -> vistaUsuariosCLI.iniciarSesion());
            } else if (opcion != 0) {
                System.out.println("Opción no válida.");
            }
        }

        System.out.println("Aplicación finalizada.");

        if (scanner != null) {
            scanner.close();
        }
    }

    private void mostrarCabecera() {
        System.out.println();
        System.out.println("=================================");
        System.out.println("          UPM Arts CLI           ");
        System.out.println("=================================");
    }

    private int leerEntero() {
        String texto = leerLinea();
        if (texto == null) {
            return 0;
        }

        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String leerLinea() {
        try {
            if (scanner == null || !scanner.hasNextLine()) {
                entradaCerrada = true;
                return null;
            }

            return scanner.nextLine();
        } catch (IllegalStateException | NoSuchElementException e) {
            entradaCerrada = true;
            return null;
        }
    }

    private void ejecutarOperacion(Runnable operacion) {
        try {
            operacion.run();
        } catch (RuntimeException e) {
            System.out.println("No se pudo completar la operación. Vuelva a intentarlo.");
        }
    }
}
