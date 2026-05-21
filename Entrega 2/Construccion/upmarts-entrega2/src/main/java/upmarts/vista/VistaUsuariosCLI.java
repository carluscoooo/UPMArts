package upmarts.vista;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import upmarts.controlador.ControladorUsuarios;
import upmarts.controlador.IControladorUsuarios;
import upmarts.modelo.Administrador;
import upmarts.modelo.DisciplinaArtistica;
import upmarts.modelo.EstudianteUPM;
import upmarts.modelo.Instructor;
import upmarts.modelo.ParticipanteExterno;
import upmarts.modelo.PersonalUPM;
import upmarts.modelo.PreferenciaArtistica;
import upmarts.modelo.Usuario;

public class VistaUsuariosCLI implements IVistaUsuariosCLI {

    private final IControladorUsuarios controladorUsuarios;
    private final Scanner scanner;

    public VistaUsuariosCLI(IControladorUsuarios controladorUsuarios, Scanner scanner) {
        this.controladorUsuarios = controladorUsuarios;
        this.scanner = scanner;
    }

    @Override
    public void registrarParticipante() {
        System.out.println();
        System.out.println("--- Registro de participante ---");
        System.out.println("El tipo de participante se detectará automáticamente por el correo.");

        String nombre = pedirTextoValidado("Nombre completo: ",
                valor -> controladorUsuarios.validarNombreRegistro(valor));
        String nick = pedirTextoValidado("Nick: ",
                valor -> controladorUsuarios.validarNickRegistro(valor));
        String correo = pedirTextoValidado("Correo electrónico: ",
                valor -> controladorUsuarios.validarCorreoRegistro(valor));
        String tipoRegistro = controladorUsuarios.detectarTipoParticipantePorCorreo(correo);

        mostrarTipoDetectado(tipoRegistro);

        String password = pedirTextoValidado("Contraseña: ",
                valor -> controladorUsuarios.validarPasswordRegistro(valor));
        String dni = pedirTextoValidado("DNI: ",
                valor -> controladorUsuarios.validarDNIRegistro(valor));
        String tarjeta = pedirTextoValidado("Tarjeta de crédito/débito: ",
                valor -> controladorUsuarios.validarTarjetaRegistro(valor));
        String datoEspecifico = "";

        if (ControladorUsuarios.TIPO_ALUMNO_UPM.equals(tipoRegistro)) {
            datoEspecifico = pedirTextoValidado("Número de matrícula: ",
                    valor -> controladorUsuarios.validarDatoEspecificoRegistro(tipoRegistro, valor));
        } else if (ControladorUsuarios.TIPO_PERSONAL_UPM.equals(tipoRegistro)) {
            datoEspecifico = pedirTextoValidado("Antigüedad en años: ",
                    valor -> controladorUsuarios.validarDatoEspecificoRegistro(tipoRegistro, valor));
        }

        List<PreferenciaArtistica> preferencias = pedirPreferenciasArtisticas();

        boolean registrado = controladorUsuarios.registrarParticipante(nombre, nick, correo, password,
                dni, tarjeta, datoEspecifico, preferencias) ;

        if (registrado) {
            System.out.println("Usuario registrado correctamente.");
        } else {
            System.out.println("No se ha podido completar el registro.");
            String mensaje = controladorUsuarios.getUltimoError();
            if (mensaje != null && !mensaje.trim().isEmpty()) {
                System.out.println("Motivo: " + mensaje);
            }
        }
    }

    @Override
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

        while (opcion != 0) {
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
                registrarInstructor(administrador);
            } else if (opcion == 2) {
                darDeBajaComoAdministrador(administrador);
            } else if (opcion == 3) {
                listarParticipantes(administrador);
            } else if (opcion == 4) {
                listarInstructores(administrador);
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

    private void mostrarMenuParticipante(ParticipanteExterno participante) {
        int opcion = -1;

        while (opcion != 0) {
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
                mostrarDatosUsuario(participante);
            } else if (opcion == 2) {
                mostrarPreferencias(participante);
            } else if (opcion == 3) {
                modificarDatosParticipante(participante);
            } else if (opcion == 4) {
                modificarPreferencias(participante);
            } else if (opcion == 5) {
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
        String nombre = pedirTextoValidado("Nombre completo: ",
                valor -> controladorUsuarios.validarNombreRegistro(valor));
        String nick = pedirTextoValidado("Nick: ",
                valor -> controladorUsuarios.validarNickRegistro(valor));
        String correo = pedirTextoValidado("Correo electrónico: ",
                valor -> controladorUsuarios.validarCorreoRegistro(valor));
        String password = pedirTextoValidado("Contraseña: ",
                valor -> controladorUsuarios.validarPasswordRegistro(valor));
        String dni = pedirTextoValidado("DNI: ",
                valor -> controladorUsuarios.validarDNIRegistro(valor));
        String iban = pedirTextoValidado("IBAN: ",
                valor -> controladorUsuarios.validarIBANRegistro(valor));

        boolean registrado = controladorUsuarios.registrarInstructorComoAdministrador(
                administrador, nombre, nick, correo, password, dni, iban);

        if (registrado) {
            System.out.println("Instructor registrado correctamente.");
        } else {
            System.out.println("No se ha podido registrar el instructor.");
            String mensaje = controladorUsuarios.getUltimoError();
            if (mensaje != null && !mensaje.trim().isEmpty()) {
                System.out.println("Motivo: " + mensaje);
            }
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

    private void listarParticipantes(Administrador administrador) {
        List<ParticipanteExterno> participantes = controladorUsuarios.listarParticipantes(administrador);

        System.out.println();
        System.out.println("--- Lista de participantes ---");

        if (participantes.isEmpty()) {
            System.out.println("No hay participantes registrados o no tiene permisos.");
            return;
        }

        for (int i = 0; i < participantes.size(); i++) {
            ParticipanteExterno participante = participantes.get(i);
            System.out.println((i + 1) + ". " + obtenerRolSistema(participante));
            System.out.println("   Nick: " + participante.getNombreUsuario());
            System.out.println("   Nombre completo: " + participante.getNombreCompleto());
            System.out.println("   Correo: " + participante.getCorreoElectronico());
            System.out.println(obtenerInformacionExtra(participante));

            double descuento = controladorUsuarios.calcularDescuento(participante) * 100.0;
            System.out.printf("   Descuento: %.0f%%%n", descuento);
            System.out.println();
        }
    }

    private void listarInstructores(Administrador administrador) {
        List<Instructor> instructores = controladorUsuarios.listarInstructores(administrador);

        System.out.println();
        System.out.println("--- Lista de instructores ---");

        if (instructores.isEmpty()) {
            System.out.println("No hay instructores registrados o no tiene permisos.");
            return;
        }

        for (int i = 0; i < instructores.size(); i++) {
            Instructor instructor = instructores.get(i);
            System.out.println((i + 1) + ". " + obtenerRolSistema(instructor));
            System.out.println("   Nick: " + instructor.getNombreUsuario());
            System.out.println("   Nombre completo: " + instructor.getNombreCompleto());
            System.out.println("   Correo: " + instructor.getCorreoElectronico());
            System.out.println("   DNI: " + instructor.getDNI());
            System.out.println("   IBAN: " + instructor.getIBAN());
            System.out.println();
        }
    }

    private void mostrarDatosUsuario(Usuario usuario) {
        System.out.println();
        System.out.println("--- Mis datos ---");
        System.out.println("Tipo: " + obtenerRolSistema(usuario));
        System.out.println("Nick: " + usuario.getNombreUsuario());
        System.out.println("Nombre completo: " + usuario.getNombreCompleto());
        System.out.println("Correo: " + usuario.getCorreoElectronico());

        String informacionExtra = obtenerInformacionExtra(usuario);
        if (informacionExtra != null && !informacionExtra.trim().isEmpty()) {
            System.out.println(informacionExtra);
        }
    }

    private void mostrarPreferencias(ParticipanteExterno participante) {
        System.out.println();
        System.out.println("--- Preferencias artísticas ---");
        List<PreferenciaArtistica> preferencias = participante.getPreferenciasArtisticas();

        if (preferencias.isEmpty()) {
            System.out.println("No hay preferencias registradas.");
            return;
        }

        for (int i = 0; i < preferencias.size(); i++) {
            System.out.println("- " + formatearPreferencia(preferencias.get(i)));
        }
    }

    private void modificarPreferencias(ParticipanteExterno participante) {
        List<PreferenciaArtistica> preferencias = pedirPreferenciasArtisticas();

        if (controladorUsuarios.actualizarPreferencias(participante, preferencias)) {
            System.out.println("Preferencias actualizadas correctamente.");
        } else {
            System.out.println("No se han podido actualizar las preferencias.");
        }
    }

    private void modificarDatosParticipante(ParticipanteExterno participante) {
        int opcion = -1;

        while (opcion != 0) {
            System.out.println();
            System.out.println("--- Modificar datos ---");
            System.out.println("1. Nombre completo");
            System.out.println("2. Nick");
            System.out.println("3. Correo electrónico");
            System.out.println("4. Contraseña");
            System.out.println("5. DNI");
            System.out.println("6. Tarjeta de crédito/débito");

            String etiquetaDatoEspecifico = obtenerEtiquetaDatoEspecifico(participante);
            if (!etiquetaDatoEspecifico.isEmpty()) {
                System.out.println("7. " + etiquetaDatoEspecifico);
            }

            System.out.println("0. Volver");
            System.out.print("Seleccione una opción: ");

            opcion = leerEntero();
            String nombre = participante.getNombreCompleto();
            String nick = participante.getNombreUsuario();
            String correo = participante.getCorreoElectronico();
            String dni = participante.getDNI();
            String tarjeta = participante.getTarjetaCredito();
            String datoEspecifico = obtenerDatoEspecifico(participante);

            String nuevoValor;
            boolean actualizado = false;

            switch (opcion) {
                case 1:
                    nuevoValor = pedirTexto("Nombre completo (actual: " + nombre + "): ");
                    if (!textoVacio(nuevoValor)) {
                        actualizado = controladorUsuarios.actualizarDatosParticipante(participante,
                                nuevoValor, nick, correo, "", dni, tarjeta, datoEspecifico);
                    }
                    break;
                case 2:
                    nuevoValor = pedirTexto("Nick (actual: " + nick + "): ");
                    if (!textoVacio(nuevoValor)) {
                        actualizado = controladorUsuarios.actualizarDatosParticipante(participante,
                                nombre, nuevoValor, correo, "", dni, tarjeta, datoEspecifico);
                    }
                    break;
                case 3:
                    nuevoValor = pedirTexto("Correo electrónico (actual: " + correo + "): ");
                    if (!textoVacio(nuevoValor)) {
                        actualizado = controladorUsuarios.actualizarDatosParticipante(participante,
                                nombre, nick, nuevoValor, "", dni, tarjeta, datoEspecifico);
                    }
                    break;
                case 4:
                    nuevoValor = pedirTexto("Nueva contraseña (dejar vacío para no cambiar): ");
                    if (!textoVacio(nuevoValor)) {
                        actualizado = controladorUsuarios.actualizarDatosParticipante(participante,
                                nombre, nick, correo, nuevoValor, dni, tarjeta, datoEspecifico);
                    }
                    break;
                case 5:
                    nuevoValor = pedirTexto("DNI (actual: " + dni + "): ");
                    if (!textoVacio(nuevoValor)) {
                        actualizado = controladorUsuarios.actualizarDatosParticipante(participante,
                                nombre, nick, correo, "", nuevoValor, tarjeta, datoEspecifico);
                    }
                    break;
                case 6:
                    nuevoValor = pedirTexto("Tarjeta de crédito/débito (actual: " + tarjeta + "): ");
                    if (!textoVacio(nuevoValor)) {
                        actualizado = controladorUsuarios.actualizarDatosParticipante(participante,
                                nombre, nick, correo, "", dni, nuevoValor, datoEspecifico);
                    }
                    break;
                case 7:
                    if (!etiquetaDatoEspecifico.isEmpty()) {
                        nuevoValor = pedirTexto(etiquetaDatoEspecifico + " (actual: " + datoEspecifico + "): ");
                        if (!textoVacio(nuevoValor)) {
                            actualizado = controladorUsuarios.actualizarDatosParticipante(participante,
                                    nombre, nick, correo, "", dni, tarjeta, nuevoValor);
                        }
                    }
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opción no válida.");
            }

            if (opcion >= 1 && opcion <= 7) {
                if (actualizado) {
                    System.out.println("Dato actualizado correctamente.");
                } else if (!textoVacio(controladorUsuarios.getUltimoError())) {
                    System.out.println("No se pudo actualizar el dato: " + controladorUsuarios.getUltimoError());
                } else if (opcion != 0) {
                    System.out.println("No se realizó ningún cambio.");
                }
            }
        }
    }

    private String obtenerRolSistema(Usuario usuario) {
        switch (usuario.getRol()) {
            case ADMINISTRADOR:
                return "ADMINISTRADOR";
            case INSTRUCTOR:
                return "INSTRUCTOR";
            case ESTUDIANTE_UPM:
                return "ESTUDIANTE UPM";
            case PERSONAL_UPM:
                return "PERSONAL UPM";
            case PARTICIPANTE_EXTERNO:
                return "PARTICIPANTE EXTERNO";
            default:
                return "DESCONOCIDO";
        }
    }

    private String obtenerInformacionExtra(Usuario usuario) {
        switch (usuario.getRol()) {
            case ADMINISTRADOR:
                Administrador administrador = (Administrador) usuario;
                return "   Telefono: " + administrador.getTelefonoAdministrador();
            case INSTRUCTOR:
                Instructor instructor = (Instructor) usuario;
                return "   DNI: " + instructor.getDNI() + "\n   IBAN: " + instructor.getIBAN();
            case ESTUDIANTE_UPM:
                EstudianteUPM estudiante = (EstudianteUPM) usuario;
                return obtenerInformacionParticipante(estudiante)
                        + "\n   Matricula: " + estudiante.getNumeroMatricula();
            case PERSONAL_UPM:
                PersonalUPM personal = (PersonalUPM) usuario;
                return obtenerInformacionParticipante(personal)
                        + "\n   Antiguedad: " + personal.getAntiguedad() + " anios";
            case PARTICIPANTE_EXTERNO:
                return obtenerInformacionParticipante((ParticipanteExterno) usuario);
            default:
                return "";
        }
    }

    private String obtenerEtiquetaDatoEspecifico(ParticipanteExterno participante) {
        switch (participante.getRol()) {
            case ESTUDIANTE_UPM:
                return "Numero de matricula";
            case PERSONAL_UPM:
                return "Antiguedad en anios";
            default:
                return "";
        }
    }

    private String obtenerDatoEspecifico(ParticipanteExterno participante) {
        switch (participante.getRol()) {
            case ESTUDIANTE_UPM:
                return ((EstudianteUPM) participante).getNumeroMatricula();
            case PERSONAL_UPM:
                return String.valueOf(((PersonalUPM) participante).getAntiguedad());
            default:
                return "";
        }
    }

    private String formatearPreferencia(PreferenciaArtistica preferencia) {
        return preferencia.getDisciplina() + " (nivel " + preferencia.getNivelExperiencia() + ")";
    }

    private String obtenerInformacionParticipante(ParticipanteExterno participante) {
        return "   DNI: " + participante.getDNI()
                + "\n   Tarjeta: " + participante.getTarjetaCredito();
    }

    private boolean textoVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    private List<PreferenciaArtistica> pedirPreferenciasArtisticas() {
        List<PreferenciaArtistica> preferencias = new ArrayList<>();

        System.out.println();
        System.out.println("--- Preferencias artísticas ---");
        System.out.println("Indique un nivel de 1 a 10. Escriba 0 si no quiere indicar esa disciplina.");

        for (DisciplinaArtistica disciplina : DisciplinaArtistica.values()) {
            int nivel = pedirNivelPreferencia(disciplina);

            if (nivel >= 1 && nivel <= 10) {
                preferencias.add(new PreferenciaArtistica(disciplina, nivel));
            }
        }

        return preferencias;
    }

    private void mostrarTipoDetectado(String tipoRegistro) {
        if (null == tipoRegistro) {
            System.out.println("Tipo detectado: participante externo.");
        } else switch (tipoRegistro) {
            case ControladorUsuarios.TIPO_ALUMNO_UPM:
                System.out.println("Tipo detectado: estudiante UPM.");
                break;
            case ControladorUsuarios.TIPO_PERSONAL_UPM:
                System.out.println("Tipo detectado: personal UPM.");
                break;
            default:
                System.out.println("Tipo detectado: participante externo.");
                break;
        }
    }

    private String pedirTexto(String mensaje) {
        System.out.print(mensaje);
        return scanner.nextLine().trim();
    }

    private String pedirTextoValidado(String mensaje, ValidadorEntrada validador) {
        while (true) {
            String valor = pedirTexto(mensaje);
            String error = validador.validar(valor);

            if (textoVacio(error)) {
                return valor;
            }

            System.out.println("Dato no válido: " + error);
        }
    }

    private int pedirNivelPreferencia(DisciplinaArtistica disciplina) {
        while (true) {
            int nivel = pedirEntero("Nivel en " + disciplina.name().toLowerCase() + " (0-10): ");

            if (nivel >= 0 && nivel <= 10) {
                return nivel;
            }

            System.out.println("Dato no válido: indique un número entre 0 y 10.");
        }
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

    private interface ValidadorEntrada {
        String validar(String valor);
    }
}
