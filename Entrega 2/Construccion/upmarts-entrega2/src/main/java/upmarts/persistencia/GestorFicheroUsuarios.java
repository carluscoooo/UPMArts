package upmarts.persistencia;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import upmarts.modelo.Administrador;
import upmarts.modelo.DisciplinaArtistica;
import upmarts.modelo.EstudianteUPM;
import upmarts.modelo.Instructor;
import upmarts.modelo.ParticipanteExterno;
import upmarts.modelo.PersonalUPM;
import upmarts.modelo.PreferenciaArtistica;
import upmarts.modelo.RolUsuario;
import upmarts.modelo.Usuario;

public class GestorFicheroUsuarios implements IAccesoUsuarios {

    private static final String TIPO_ADMINISTRADOR = "ADMINISTRADOR";
    private static final String TIPO_INSTRUCTOR = "INSTRUCTOR";
    private static final String TIPO_EXTERNO = "EXTERNO";
    private static final String TIPO_ESTUDIANTE_UPM = "ESTUDIANTE_UPM";
    private static final String TIPO_PERSONAL_UPM = "PERSONAL_UPM";

    private final String rutaFichero;

    public GestorFicheroUsuarios() {
        this("data/usuarios.txt");
    }

    public GestorFicheroUsuarios(String rutaFichero) {
        this.rutaFichero = rutaFichero;
        prepararFichero();
    }

    private void prepararFichero() {
        try {
            File fichero = new File(rutaFichero);
            File carpeta = fichero.getParentFile();

            if (carpeta != null && !carpeta.exists()) {
                carpeta.mkdirs();
            }

            if (!fichero.exists()) {
                fichero.createNewFile();
            }
        } catch (IOException e) {
            System.out.println("No se pudo preparar el fichero de usuarios: " + e.getMessage());
        }
    }

    @Override
    public void guardarUsuarios(List<Usuario> usuarios) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(rutaFichero, false))) {
            for (Usuario usuario : usuarios) {
                writer.write(convertirUsuarioALinea(usuario));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error guardando usuarios: " + e.getMessage());
        }
    }

    @Override
    public List<Usuario> leerUsuarios() {
        List<Usuario> usuarios = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(rutaFichero))) {
            String linea;

            while ((linea = reader.readLine()) != null) {
                if (!linea.trim().isEmpty()) {
                    Usuario usuario = convertirLineaAUsuario(linea);

                    if (usuario != null) {
                        usuarios.add(usuario);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error leyendo usuarios: " + e.getMessage());
        }

        return usuarios;
    }

    private String convertirUsuarioALinea(Usuario usuario) {
        // Formato del fichero: tipo;nick;nombre;correo;password;campos propios del rol.
        StringBuilder linea = new StringBuilder();
        linea.append(obtenerCodigoTipo(usuario)).append(";");
        linea.append(limpiarParaPersistencia(usuario.getNombreUsuario())).append(";");
        linea.append(limpiarParaPersistencia(usuario.getNombreCompleto())).append(";");
        linea.append(limpiarParaPersistencia(usuario.getCorreoElectronico())).append(";");
        linea.append(limpiarParaPersistencia(usuario.getContrasena()));

        switch (usuario.getRol()) {
            case ADMINISTRADOR:
                Administrador administrador = (Administrador) usuario;
                linea.append(";").append(limpiarParaPersistencia(administrador.getTelefonoAdministrador()));
                break;
            case INSTRUCTOR:
                Instructor instructor = (Instructor) usuario;
                linea.append(";").append(limpiarParaPersistencia(instructor.getDNI()));
                linea.append(";").append(limpiarParaPersistencia(instructor.getIBAN()));
                break;
            case ESTUDIANTE_UPM:
                EstudianteUPM estudiante = (EstudianteUPM) usuario;
                linea.append(";").append(limpiarParaPersistencia(estudiante.getDNI()));
                linea.append(";").append(limpiarParaPersistencia(estudiante.getTarjetaCredito()));
                linea.append(";").append(limpiarParaPersistencia(estudiante.getNumeroMatricula()));
                linea.append(";").append(limpiarParaPersistencia(convertirPreferenciasATexto(estudiante)));
                break;
            case PERSONAL_UPM:
                PersonalUPM personal = (PersonalUPM) usuario;
                linea.append(";").append(limpiarParaPersistencia(personal.getDNI()));
                linea.append(";").append(limpiarParaPersistencia(personal.getTarjetaCredito()));
                linea.append(";").append(personal.getAntiguedad());
                linea.append(";").append(limpiarParaPersistencia(convertirPreferenciasATexto(personal)));
                break;
            case PARTICIPANTE_EXTERNO:
                ParticipanteExterno participante = (ParticipanteExterno) usuario;
                linea.append(";").append(limpiarParaPersistencia(participante.getDNI()));
                linea.append(";").append(limpiarParaPersistencia(participante.getTarjetaCredito()));
                linea.append(";").append(limpiarParaPersistencia(convertirPreferenciasATexto(participante)));
                break;
            default:
                break;
        }

        return linea.toString();
    }

    private String obtenerCodigoTipo(Usuario usuario) {
        RolUsuario rol = usuario.getRol();

        switch (rol) {
            case ADMINISTRADOR:
                return TIPO_ADMINISTRADOR;
            case INSTRUCTOR:
                return TIPO_INSTRUCTOR;
            case ESTUDIANTE_UPM:
                return TIPO_ESTUDIANTE_UPM;
            case PERSONAL_UPM:
                return TIPO_PERSONAL_UPM;
            case PARTICIPANTE_EXTERNO:
                return TIPO_EXTERNO;
            default:
                return "";
        }
    }

    private String convertirPreferenciasATexto(ParticipanteExterno participante) {
        List<PreferenciaArtistica> preferencias = participante.getPreferenciasArtisticas();

        if (preferencias.isEmpty()) {
            return "";
        }

        StringBuilder texto = new StringBuilder();

        for (PreferenciaArtistica preferencia : preferencias) {
            if (preferencia != null && preferencia.getDisciplina() != null) {
                if (texto.length() > 0) {
                    texto.append(",");
                }

                texto.append(preferencia.getDisciplina().name());
                texto.append(":");
                texto.append(preferencia.getNivelExperiencia());
            }
        }

        return texto.toString();
    }

    private String limpiarParaPersistencia(String texto) {
        if (texto == null) {
            return "";
        }

        return texto.replace(";", ",").trim();
    }

    private Usuario convertirLineaAUsuario(String linea) {
        String[] partes = linea.split(";", -1);

        if (partes.length < 5) {
            return null;
        }

        String tipo = partes[0].trim();
        String nick = partes[1];
        String nombreCompleto = partes[2];
        String correo = partes[3];
        String password = partes[4];

        if (TIPO_ADMINISTRADOR.equals(tipo) && partes.length >= 6) {
            return new Administrador(nick, nombreCompleto, correo, password, partes[5]);
        }

        if (TIPO_INSTRUCTOR.equals(tipo) && partes.length >= 7) {
            return new Instructor(nick, nombreCompleto, correo, password, partes[5], partes[6]);
        }

        if (TIPO_EXTERNO.equals(tipo) && partes.length >= 8) {
            return new ParticipanteExterno(nick, nombreCompleto, correo, password,
                    partes[5], partes[6], convertirTextoAPreferencias(partes[7]));
        }

        if (TIPO_ESTUDIANTE_UPM.equals(tipo) && partes.length >= 9) {
            return new EstudianteUPM(nick, nombreCompleto, correo, password,
                    partes[5], partes[6], partes[7], convertirTextoAPreferencias(partes[8]));
        }

        if (TIPO_PERSONAL_UPM.equals(tipo) && partes.length >= 9) {
            int antiguedad = convertirEntero(partes[7]);
            return new PersonalUPM(nick, nombreCompleto, correo, password,
                    partes[5], partes[6], antiguedad, convertirTextoAPreferencias(partes[8]));
        }

        return null;
    }

    private List<PreferenciaArtistica> convertirTextoAPreferencias(String texto) {
        List<PreferenciaArtistica> preferencias = new ArrayList<>();

        if (texto == null || texto.trim().isEmpty()) {
            return preferencias;
        }

        String[] elementos = texto.split(",");

        for (String elemento : elementos) {
            String[] partes = elemento.split(":");
            if (partes.length == 2) {
                DisciplinaArtistica disciplina = DisciplinaArtistica.desdeTexto(partes[0]);
                int nivel = convertirEntero(partes[1]);

                if (disciplina != null && nivel >= 1 && nivel <= 10) {
                    preferencias.add(new PreferenciaArtistica(disciplina, nivel));
                }
            }
        }

        return preferencias;
    }

    private int convertirEntero(String texto) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
