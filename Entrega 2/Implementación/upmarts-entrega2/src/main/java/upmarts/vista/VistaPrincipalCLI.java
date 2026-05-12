package upmarts.vista;

import java.util.Scanner;

public class VistaPrincipalCLI {

    private Scanner scanner;
    private IVistaUsuariosCLI vistaUsuariosCLI;

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
