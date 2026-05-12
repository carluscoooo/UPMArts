package upmarts.vista;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import upmarts.controlador.ControladorUsuarios;
import upmarts.controlador.IControladorUsuarios;
import upmarts.integracion.IValidadorUPM;
import upmarts.modelo.Administrador;
import upmarts.modelo.DisciplinaArtistica;
import upmarts.modelo.Instructor;
import upmarts.modelo.Participante;
import upmarts.modelo.PreferenciaArtistica;
import upmarts.modelo.Usuario;

public class VistaUsuariosCLI implements IVistaUsuariosCLI {

    private IControladorUsuarios controladorUsuarios;
    private IValidadorUPM validadorUPM;
    private Scanner scanner;

    public VistaUsuariosCLI(IControladorUsuarios controladorUsuarios,
                            IValidadorUPM validadorUPM,
                            Scanner scanner) {
        this.controladorUsuarios = controladorUsuarios;
        this.validadorUPM = validadorUPM;
        this.scanner = scanner;
    }

    public void registrarParticipante() {
        System.out.println();
        System.out.println("--- Registro de participante ---");
        System.out.println("El tipo de participante se detectará automáticamente por el correo.");

        String nombre = pedirTexto("Nombre completo: ");
        String nick = pedirTexto("Nick: ");
        String correo = pedirTexto("Correo electrónico: ");
        String tipoRegistro = controladorUsuarios.detectarTipoParticipantePorCorreo(correo);

        if (ControladorUsuarios.TIPO_CORREO_INVALIDO.equals(tipoRegistro)) {
            System.out.println("El correo introducido no tiene un formato válido.");
            return;
        }

        mostrarTipoDetectado(tipoRegistro);

        String password = pedirTexto("Contraseña: ");
        String dni = pedirTexto("DNI: ");
        String tarjeta = pedirTexto("Tarjeta de crédito/débito: ");
        String datoEspecifico = "";

        if (ControladorUsuarios.TIPO_ALUMNO_UPM.equals(tipoRegistro)) {
            datoEspecifico = pedirTexto("Número de matrícula: ");
        } else if (ControladorUsuarios.TIPO_PERSONAL_UPM.equals(tipoRegistro)) {
            datoEspecifico = pedirTexto("Antigüedad en años: ");
        }

        List<PreferenciaArtistica> preferencias = pedirPreferenciasArtisticas();

        boolean registrado = controladorUsuarios.registrarParticipante(nombre, nick, correo, password,
                dni, tarjeta, datoEspecifico, preferencias, validadorUPM);

        if (registrado) {
            System.out.println("Usuario registrado correctamente.");
        } else {
            System.out.println("No se ha podido completar el registro. Revise los datos introducidos.");
        }
    }

    public void iniciarSesion() {
        System.out.println();
        System.out.println("--- Inicio de sesión ---");
        String correo = pedirTexto("Correo electrónico: ");
        String password = pedirTexto("Contraseña: ");

        Usuario usuario = controladorUsuarios.login(correo, password);

        if (usuario == null) {
            System.out.println("Correo o contraseña incorrectos.");
            return;
        }

        System.out.println("Acceso correcto. Bienvenido/a, " + usuario.getNombreCompleto() + ".");
        mostrarMenuSegunUsuario(usuario);
    }

    private void mostrarMenuSegunUsuario(Usuario usuario) {
        if (usuario instanceof Administrador) {
            mostrarMenuAdministrador((Administrador) usuario);
        } else if (usuario instanceof Instructor) {
            mostrarMenuInstructor((Instructor) usuario);
        } else if (usuario instanceof Participante) {
            mostrarMenuParticipante((Participante) usuario);
        } else {
            System.out.println("No hay operaciones disponibles para este usuario.");
        }
    }

    private void mostrarMenuAdministrador(Administrador administrador) {
        int opcion = -1;

        while (opcion != 0) {
            System.out.println();
            System.out.println("--- Menú de administrador ---");
            System.out.println("1. Dar de alta instructor");
            System.out.println("2. Dar de baja usuario");
            System.out.println("3. Listar usuarios");
            System.out.println("0. Cerrar sesión");
            System.out.print("Seleccione una opción: ");

            opcion = leerEntero();

            if (opcion == 1) {
                registrarInstructor(administrador);
            } else if (opcion == 2) {
                darDeBajaComoAdministrador(administrador);
            } else if (opcion == 3) {
                listarUsuarios(administrador);
            } else if (opcion != 0) {
                System.out.println("Opción no válida.");
            }
        }
    }

    private void mostrarMenuInstructor(Instructor instructor) {
        int opcion = -1;

        while (opcion != 0) {
            System.out.println();
            System.out.println("--- Menú de instructor ---");
            System.out.println("1. Ver mis datos");
            System.out.println("2. Darme de baja");
            System.out.println("0. Cerrar sesión");
            System.out.print("Seleccione una opción: ");

            opcion = leerEntero();

            if (opcion == 1) {
                mostrarDatosUsuario(instructor);
            } else if (opcion == 2) {
                if (controladorUsuarios.darseDeBaja(instructor)) {
                    System.out.println("La baja se ha realizado correctamente.");
                    opcion = 0;
                } else {
                    System.out.println("No se ha podido realizar la baja.");
                }
            } else if (opcion != 0) {
                System.out.println("Opción no válida.");
            }
        }
    }

    private void mostrarMenuParticipante(Participante participante) {
        int opcion = -1;

        while (opcion != 0) {
            System.out.println();
            System.out.println("--- Menú de participante ---");
            System.out.println("1. Ver mis datos");
            System.out.println("2. Ver preferencias artísticas");
            System.out.println("3. Modificar preferencias artísticas");
            System.out.println("4. Darme de baja");
            System.out.println("0. Cerrar sesión");
            System.out.print("Seleccione una opción: ");

            opcion = leerEntero();

            if (opcion == 1) {
                mostrarDatosUsuario(participante);
            } else if (opcion == 2) {
                mostrarPreferencias(participante);
            } else if (opcion == 3) {
                modificarPreferencias(participante);
            } else if (opcion == 4) {
                if (controladorUsuarios.darseDeBaja(participante)) {
                    System.out.println("La baja se ha realizado correctamente.");
                    opcion = 0;
                } else {
                    System.out.println("No se ha podido realizar la baja.");
                }
            } else if (opcion != 0) {
                System.out.println("Opción no válida.");
            }
        }
    }

    private void registrarInstructor(Administrador administrador) {
        System.out.println();
        System.out.println("--- Alta de instructor ---");
        String nombre = pedirTexto("Nombre completo: ");
        String nick = pedirTexto("Nick: ");
        String correo = pedirTexto("Correo electrónico: ");
        String password = pedirTexto("Contraseña: ");
        String dni = pedirTexto("DNI: ");
        String iban = pedirTexto("IBAN: ");

        boolean registrado = controladorUsuarios.registrarInstructorComoAdministrador(
                administrador, nombre, nick, correo, password, dni, iban);

        if (registrado) {
            System.out.println("Instructor registrado correctamente.");
        } else {
            System.out.println("No se ha podido registrar el instructor.");
        }
    }

    private void darDeBajaComoAdministrador(Administrador administrador) {
        System.out.println();
        System.out.println("--- Baja de usuario ---");
        String correo = pedirTexto("Correo del usuario que se desea dar de baja: ");

        boolean eliminado = controladorUsuarios.darDeBajaUsuarioComoAdministrador(administrador, correo);

        if (eliminado) {
            System.out.println("Usuario dado de baja correctamente.");
        } else {
            System.out.println("No se ha podido dar de baja el usuario indicado.");
        }
    }

    private void listarUsuarios(Administrador administrador) {
        List<Usuario> usuarios = controladorUsuarios.listarUsuarios(administrador);

        System.out.println();
        System.out.println("--- Usuarios registrados ---");

        if (usuarios.isEmpty()) {
            System.out.println("No hay usuarios registrados o no tiene permisos.");
            return;
        }

        for (int i = 0; i < usuarios.size(); i++) {
            System.out.println((i + 1) + ". " + usuarios.get(i));
        }
    }

    private void mostrarDatosUsuario(Usuario usuario) {
        System.out.println();
        System.out.println("--- Mis datos ---");
        System.out.println("Tipo: " + usuario.getRolSistema());
        System.out.println("Nick: " + usuario.getNombreUsuario());
        System.out.println("Nombre completo: " + usuario.getNombreCompleto());
        System.out.println("Correo: " + usuario.getCorreoElectronico());
    }

    private void mostrarPreferencias(Participante participante) {
        System.out.println();
        System.out.println("--- Preferencias artísticas ---");
        List<PreferenciaArtistica> preferencias = participante.getPreferenciasArtisticas();

        if (preferencias.isEmpty()) {
            System.out.println("No hay preferencias registradas.");
            return;
        }

        for (int i = 0; i < preferencias.size(); i++) {
            System.out.println("- " + preferencias.get(i));
        }
    }

    private void modificarPreferencias(Participante participante) {
        List<PreferenciaArtistica> preferencias = pedirPreferenciasArtisticas();

        if (controladorUsuarios.actualizarPreferencias(participante, preferencias)) {
            System.out.println("Preferencias actualizadas correctamente.");
        } else {
            System.out.println("No se han podido actualizar las preferencias.");
        }
    }

    private List<PreferenciaArtistica> pedirPreferenciasArtisticas() {
        List<PreferenciaArtistica> preferencias = new ArrayList<PreferenciaArtistica>();

        System.out.println();
        System.out.println("--- Preferencias artísticas ---");
        System.out.println("Indique un nivel de 1 a 10. Escriba 0 si no quiere indicar esa disciplina.");

        for (DisciplinaArtistica disciplina : DisciplinaArtistica.values()) {
            int nivel = pedirEntero("Nivel en " + disciplina.name().toLowerCase() + " (0-10): ");

            if (nivel >= 1 && nivel <= 10) {
                preferencias.add(new PreferenciaArtistica(disciplina, nivel));
            }
        }

        return preferencias;
    }

    private void mostrarTipoDetectado(String tipoRegistro) {
        if (ControladorUsuarios.TIPO_ALUMNO_UPM.equals(tipoRegistro)) {
            System.out.println("Tipo detectado: estudiante UPM.");
        } else if (ControladorUsuarios.TIPO_PERSONAL_UPM.equals(tipoRegistro)) {
            System.out.println("Tipo detectado: personal UPM.");
        } else {
            System.out.println("Tipo detectado: participante externo.");
        }
    }

    private String pedirTexto(String mensaje) {
        System.out.print(mensaje);
        return scanner.nextLine().trim();
    }

    private int pedirEntero(String mensaje) {
        System.out.print(mensaje);
        return leerEntero();
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
