package upmarts.vista;

import java.util.NoSuchElementException;
import java.util.Scanner;

import upmarts.modelo.Administrador;
import upmarts.modelo.Instructor;
import upmarts.modelo.ParticipanteExterno;
import upmarts.modelo.Usuario;

public class VistaPrincipalCLI {

    private final Scanner scanner;
    private final IVistaUsuariosCLI vistaUsuariosCLI;
    private boolean entradaCerrada;

    public VistaPrincipalCLI() {
        this(new Scanner(System.in));
    }

    private VistaPrincipalCLI(Scanner scanner) {
        this(new VistaUsuariosCLI(scanner), scanner);
    }

    public VistaPrincipalCLI(IVistaUsuariosCLI vistaUsuariosCLI, Scanner scanner) {
        this.scanner = scanner;
        this.vistaUsuariosCLI = vistaUsuariosCLI;
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
                ejecutarOperacion(() -> {
                    Usuario usuario = vistaUsuariosCLI.iniciarSesion();
                    if (usuario != null) {
                        mostrarMenuSegunUsuario(usuario);
                    }
                });
            } else if (opcion != 0) {
                System.out.println("Opción no válida.");
            }
        }

        System.out.println("Aplicación finalizada.");

        if (scanner != null) {
            scanner.close();
        }
    }

    private void mostrarMenuSegunUsuario(Usuario usuario) {
        switch (usuario.getRol()) {
            case ADMINISTRADOR:
                mostrarMenuAdministrador((Administrador) usuario);
                break;
            case INSTRUCTOR:
                mostrarMenuInstructor((Instructor) usuario);
                break;
            case PARTICIPANTE_EXTERNO:
            case ESTUDIANTE_UPM:
            case PERSONAL_UPM:
                mostrarMenuParticipante((ParticipanteExterno) usuario);
                break;
            default:
                System.out.println("No hay operaciones disponibles para este usuario.");
                break;
        }
    }

    private void mostrarMenuAdministrador(Administrador administrador) {
        int opcion = -1;

        while (opcion != 0 && !entradaCerrada) {
            System.out.println();
            System.out.println("--- Menú de administrador ---");
            System.out.println("1. Dar de alta instructor");
            System.out.println("2. Dar de baja usuario");
            System.out.println("3. Listar participantes");
            System.out.println("4. Listar instructores");
            System.out.println("0. Cerrar sesión");
            System.out.print("Seleccione una opción: ");

            opcion = leerEntero();

            if (opcion == 1) {
                vistaUsuariosCLI.registrarInstructor(administrador);
            } else if (opcion == 2) {
                vistaUsuariosCLI.darDeBajaComoAdministrador(administrador);
            } else if (opcion == 3) {
                vistaUsuariosCLI.listarParticipantes(administrador);
            } else if (opcion == 4) {
                vistaUsuariosCLI.listarInstructores(administrador);
            } else if (opcion != 0) {
                System.out.println("Opción no válida.");
            }
        }
    }

    private void mostrarMenuInstructor(Instructor instructor) {
        int opcion = -1;

        while (opcion != 0 && !entradaCerrada) {
            System.out.println();
            System.out.println("--- Menú de instructor ---");
            System.out.println("1. Ver mis datos");
            System.out.println("2. Darme de baja");
            System.out.println("0. Cerrar sesión");
            System.out.print("Seleccione una opción: ");

            opcion = leerEntero();

            if (opcion == 1) {
                vistaUsuariosCLI.mostrarDatosUsuario(instructor);
            } else if (opcion == 2) {
                if (vistaUsuariosCLI.darseDeBaja(instructor)) {
                    opcion = 0;
                }
            } else if (opcion != 0) {
                System.out.println("Opción no válida.");
            }
        }
    }

    private void mostrarMenuParticipante(ParticipanteExterno participante) {
        int opcion = -1;

        while (opcion != 0 && !entradaCerrada) {
            System.out.println();
            System.out.println("--- Menú de participante ---");
            System.out.println("1. Ver mis datos");
            System.out.println("2. Ver preferencias artísticas");
            System.out.println("3. Modificar datos");
            System.out.println("4. Modificar preferencias artísticas");
            System.out.println("5. Darme de baja");
            System.out.println("0. Cerrar sesión");
            System.out.print("Seleccione una opción: ");

            opcion = leerEntero();

            if (opcion == 1) {
                vistaUsuariosCLI.mostrarDatosUsuario(participante);
            } else if (opcion == 2) {
                vistaUsuariosCLI.mostrarPreferencias(participante);
            } else if (opcion == 3) {
                vistaUsuariosCLI.modificarDatosParticipante(participante);
            } else if (opcion == 4) {
                vistaUsuariosCLI.modificarPreferencias(participante);
            } else if (opcion == 5) {
                if (vistaUsuariosCLI.darseDeBaja(participante)) {
                    opcion = 0;
                }
            } else if (opcion != 0) {
                System.out.println("Opción no válida.");
            }
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
