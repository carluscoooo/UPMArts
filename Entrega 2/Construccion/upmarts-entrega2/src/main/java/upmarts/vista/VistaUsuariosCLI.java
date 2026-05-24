package upmarts.vista;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
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
import upmarts.validacion.ValidadorDatosUsuario;

public class VistaUsuariosCLI implements IVistaUsuariosCLI {

    private final IControladorUsuarios controladorUsuarios;
    private final Scanner scanner;

    public VistaUsuariosCLI(Scanner scanner) {
        this(new ControladorUsuarios(), scanner);
    }

    public VistaUsuariosCLI(IControladorUsuarios controladorUsuarios, Scanner scanner) {
        this.controladorUsuarios = controladorUsuarios;
        this.scanner = scanner;
    }

    @Override
    public void registrarParticipante() {
        System.out.println();
        System.out.println("--- Registro de participante ---");
        System.out.println("El tipo de participante se detectará automáticamente por el correo.");

        String nombre = pedirNombre();
        String nick = pedirNick();
        String correo = pedirCorreo();
        String tipoRegistro = controladorUsuarios.detectarTipoParticipantePorCorreo(correo);

        mostrarTipoDetectado(tipoRegistro);

        String password = pedirPassword();
        String dni = pedirDNI();
        String tarjeta = pedirTarjeta();
        String datoEspecifico = "";

        if (ControladorUsuarios.TIPO_ALUMNO_UPM.equals(tipoRegistro)) {
            datoEspecifico = pedirDatoEspecifico("Número de matrícula: ", tipoRegistro);
        } else if (ControladorUsuarios.TIPO_PERSONAL_UPM.equals(tipoRegistro)) {
            datoEspecifico = pedirDatoEspecifico("Antigüedad en años: ", tipoRegistro);
        }

        List<PreferenciaArtistica> preferencias = pedirPreferenciasArtisticas();

        boolean registrado = controladorUsuarios.registrarParticipante(nombre, nick, correo, password,
                dni, tarjeta, datoEspecifico, preferencias);

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
    public Usuario iniciarSesion() {
        System.out.println();
        System.out.println("--- Inicio de sesión ---");
        String correo = pedirTexto("Correo electrónico: ");
        String password = pedirTexto("Contraseña: ");

        Usuario usuario = controladorUsuarios.login(correo, password);

        if (usuario == null) {
            if (!ValidadorDatosUsuario.textoVacio(controladorUsuarios.getUltimoError())) {
                System.out.println("No se pudo iniciar sesión: " + controladorUsuarios.getUltimoError());
            } else {
                System.out.println("Correo o contraseña incorrectos.");
            }
            return null;
        }

        System.out.println("Acceso correcto. Bienvenido/a, " + usuario.getNombreCompleto() + ".");
        return usuario;
    }

    @Override
    public void registrarInstructor(Administrador administrador) {
        System.out.println();
        System.out.println("--- Alta de instructor ---");
        String nombre = pedirNombre();
        String nick = pedirNick();
        String correo = pedirCorreo();
        String password = pedirPassword();
        String dni = pedirDNI();
        String iban = pedirIBAN();

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

    @Override
    public void darDeBajaComoAdministrador(Administrador administrador) {
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

    @Override
    public void listarParticipantes(Administrador administrador) {
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

    @Override
    public void listarInstructores(Administrador administrador) {
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

    @Override
    public void mostrarDatosUsuario(Usuario usuario) {
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

    @Override
    public void mostrarPreferencias(ParticipanteExterno participante) {
        System.out.println();
        System.out.println("--- Preferencias artísticas ---");
        List<PreferenciaArtistica> preferencias = participante.getPreferenciasArtisticas();

        if (preferencias.isEmpty()) {
            System.out.println("No hay preferencias registradas.");
            return;
        }

        for (int i = 0; i < preferencias.size(); i++) {
            PreferenciaArtistica preferencia = preferencias.get(i);
            System.out.println("- " + preferencia.getDisciplina()
                    + " (nivel " + preferencia.getNivelExperiencia() + ")");
        }
    }

    @Override
    public void modificarPreferencias(ParticipanteExterno participante) {
        List<PreferenciaArtistica> preferencias = pedirPreferenciasArtisticas();

        if (controladorUsuarios.actualizarPreferencias(participante, preferencias)) {
            System.out.println("Preferencias actualizadas correctamente.");
        } else {
            System.out.println("No se han podido actualizar las preferencias.");
        }
    }

    @Override
    public void modificarDatosParticipante(ParticipanteExterno participante) {
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
                    if (!ValidadorDatosUsuario.textoVacio(nuevoValor)) {
                        actualizado = controladorUsuarios.actualizarDatosParticipante(participante,
                                nuevoValor, nick, correo, "", dni, tarjeta, datoEspecifico);
                    }
                    break;
                case 2:
                    nuevoValor = pedirTexto("Nick (actual: " + nick + "): ");
                    if (!ValidadorDatosUsuario.textoVacio(nuevoValor)) {
                        actualizado = controladorUsuarios.actualizarDatosParticipante(participante,
                                nombre, nuevoValor, correo, "", dni, tarjeta, datoEspecifico);
                    }
                    break;
                case 3:
                    nuevoValor = pedirTexto("Correo electrónico (actual: " + correo + "): ");
                    if (!ValidadorDatosUsuario.textoVacio(nuevoValor)) {
                        actualizado = controladorUsuarios.actualizarDatosParticipante(participante,
                                nombre, nick, nuevoValor, "", dni, tarjeta, datoEspecifico);
                    }
                    break;
                case 4:
                    nuevoValor = pedirTexto("Nueva contraseña (dejar vacío para no cambiar): ");
                    if (!ValidadorDatosUsuario.textoVacio(nuevoValor)) {
                        actualizado = controladorUsuarios.actualizarDatosParticipante(participante,
                                nombre, nick, correo, nuevoValor, dni, tarjeta, datoEspecifico);
                    }
                    break;
                case 5:
                    nuevoValor = pedirTexto("DNI (actual: " + dni + "): ");
                    if (!ValidadorDatosUsuario.textoVacio(nuevoValor)) {
                        actualizado = controladorUsuarios.actualizarDatosParticipante(participante,
                                nombre, nick, correo, "", nuevoValor, tarjeta, datoEspecifico);
                    }
                    break;
                case 6:
                    nuevoValor = pedirTexto("Tarjeta de crédito/débito (actual: " + tarjeta + "): ");
                    if (!ValidadorDatosUsuario.textoVacio(nuevoValor)) {
                        actualizado = controladorUsuarios.actualizarDatosParticipante(participante,
                                nombre, nick, correo, "", dni, nuevoValor, datoEspecifico);
                    }
                    break;
                case 7:
                    if (!etiquetaDatoEspecifico.isEmpty()) {
                        nuevoValor = pedirTexto(etiquetaDatoEspecifico + " (actual: " + datoEspecifico + "): ");
                        if (!ValidadorDatosUsuario.textoVacio(nuevoValor)) {
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
                } else if (!ValidadorDatosUsuario.textoVacio(controladorUsuarios.getUltimoError())) {
                    System.out.println("No se pudo actualizar el dato: " + controladorUsuarios.getUltimoError());
                } else if (opcion != 0) {
                    System.out.println("No se realizó ningún cambio.");
                }
            }
        }
    }

    @Override
    public boolean darseDeBaja(Usuario usuario) {
        if (controladorUsuarios.darseDeBaja(usuario)) {
            System.out.println("La baja se ha realizado correctamente.");
            return true;
        }

        System.out.println("No se ha podido realizar la baja.");
        return false;
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
                return "   Teléfono: " + administrador.getTelefonoAdministrador();
            case INSTRUCTOR:
                Instructor instructor = (Instructor) usuario;
                return "   DNI: " + instructor.getDNI() + "\n   IBAN: " + instructor.getIBAN();
            case ESTUDIANTE_UPM:
                EstudianteUPM estudiante = (EstudianteUPM) usuario;
                return obtenerInformacionParticipante(estudiante)
                        + "\n   Matrícula: " + estudiante.getNumeroMatricula();
            case PERSONAL_UPM:
                PersonalUPM personal = (PersonalUPM) usuario;
                return obtenerInformacionParticipante(personal)
                        + "\n   Antigüedad: " + personal.getAntiguedad() + " años";
            case PARTICIPANTE_EXTERNO:
                return obtenerInformacionParticipante((ParticipanteExterno) usuario);
            default:
                return "";
        }
    }

    private String obtenerEtiquetaDatoEspecifico(ParticipanteExterno participante) {
        switch (participante.getRol()) {
            case ESTUDIANTE_UPM:
                return "Número de matrícula";
            case PERSONAL_UPM:
                return "Antigüedad en años";
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

    private String obtenerInformacionParticipante(ParticipanteExterno participante) {
        return "   DNI: " + participante.getDNI()
                + "\n   Tarjeta: " + participante.getTarjetaCredito();
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
        String texto = leerLinea();
        if (texto == null) {
            throw new IllegalStateException("La entrada de datos se ha cerrado.");
        }

        return texto.trim();
    }

    private String pedirNombre() {
        while (true) {
            String valor = pedirTexto("Nombre completo: ");
            String error = controladorUsuarios.validarNombreRegistro(valor);

            if (ValidadorDatosUsuario.textoVacio(error)) {
                return valor;
            }

            mostrarErrorDato(error);
        }
    }

    private String pedirNick() {
        while (true) {
            String valor = pedirTexto("Nick: ");
            String error = controladorUsuarios.validarNickRegistro(valor);

            if (ValidadorDatosUsuario.textoVacio(error)) {
                return valor;
            }

            mostrarErrorDato(error);
        }
    }

    private String pedirCorreo() {
        while (true) {
            String valor = pedirTexto("Correo electrónico: ");
            String error = controladorUsuarios.validarCorreoRegistro(valor);

            if (ValidadorDatosUsuario.textoVacio(error)) {
                return valor;
            }

            mostrarErrorDato(error);
        }
    }

    private String pedirPassword() {
        while (true) {
            String valor = pedirTexto("Contraseña: ");
            String error = controladorUsuarios.validarPasswordRegistro(valor);

            if (ValidadorDatosUsuario.textoVacio(error)) {
                return valor;
            }

            mostrarErrorDato(error);
        }
    }

    private String pedirDNI() {
        while (true) {
            String valor = pedirTexto("DNI: ");
            String error = controladorUsuarios.validarDNIRegistro(valor);

            if (ValidadorDatosUsuario.textoVacio(error)) {
                return valor;
            }

            mostrarErrorDato(error);
        }
    }

    private String pedirTarjeta() {
        while (true) {
            String valor = pedirTexto("Tarjeta de crédito/débito: ");
            String error = controladorUsuarios.validarTarjetaRegistro(valor);

            if (ValidadorDatosUsuario.textoVacio(error)) {
                return valor;
            }

            mostrarErrorDato(error);
        }
    }

    private String pedirIBAN() {
        while (true) {
            String valor = pedirTexto("IBAN: ");
            String error = controladorUsuarios.validarIBANRegistro(valor);

            if (ValidadorDatosUsuario.textoVacio(error)) {
                return valor;
            }

            mostrarErrorDato(error);
        }
    }

    private String pedirDatoEspecifico(String mensaje, String tipoRegistro) {
        while (true) {
            String valor = pedirTexto(mensaje);
            String error = controladorUsuarios.validarDatoEspecificoRegistro(tipoRegistro, valor);

            if (ValidadorDatosUsuario.textoVacio(error)) {
                return valor;
            }

            mostrarErrorDato(error);
        }
    }

    private int pedirNivelPreferencia(DisciplinaArtistica disciplina) {
        while (true) {
            String texto = pedirTexto("Nivel en " + disciplina.name().toLowerCase() + " (0-10): ");
            String error = controladorUsuarios.validarNivelPreferencia(texto);

            if (ValidadorDatosUsuario.textoVacio(error)) {
                return Integer.parseInt(texto.trim());
            }

            mostrarErrorDato(error);
        }
    }

    private void mostrarErrorDato(String error) {
        System.out.println("Dato no válido: " + error);
    }

    private int leerEntero() {
        String texto = leerLinea();
        if (texto == null) {
            throw new IllegalStateException("La entrada de datos se ha cerrado.");
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
                return null;
            }

            return scanner.nextLine();
        } catch (IllegalStateException | NoSuchElementException e) {
            return null;
        }
    }

}
