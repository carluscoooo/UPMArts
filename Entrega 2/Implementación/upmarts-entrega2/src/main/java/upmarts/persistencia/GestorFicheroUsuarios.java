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
import upmarts.modelo.Participante;
import upmarts.modelo.ParticipanteExterno;
import upmarts.modelo.PersonalUPM;
import upmarts.modelo.PreferenciaArtistica;
import upmarts.modelo.Usuario;

public class GestorFicheroUsuarios implements IAccesoUsuarios {

    private static final String TIPO_ADMINISTRADOR = "ADMINISTRADOR";
    private static final String TIPO_INSTRUCTOR = "INSTRUCTOR";
    private static final String TIPO_EXTERNO = "EXTERNO";
    private static final String TIPO_ESTUDIANTE_UPM = "ESTUDIANTE_UPM";
    private static final String TIPO_PERSONAL_UPM = "PERSONAL_UPM";

    private String rutaFichero;

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

    public void guardarUsuarios(List<Usuario> usuarios) {
        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter(rutaFichero, false));

            for (Usuario usuario : usuarios) {
                writer.write(convertirUsuarioALinea(usuario));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error guardando usuarios: " + e.getMessage());
        } finally {
            cerrarWriter(writer);
        }
    }

    public List<Usuario> leerUsuarios() {
        List<Usuario> usuarios = new ArrayList<Usuario>();
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(rutaFichero));
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
        } finally {
            cerrarReader(reader);
        }

        return usuarios;
    }

    private String convertirUsuarioALinea(Usuario usuario) {
        StringBuilder linea = new StringBuilder();
        linea.append(obtenerCodigoTipo(usuario)).append(";");
        linea.append(limpiar(usuario.getNombreUsuario())).append(";");
        linea.append(limpiar(usuario.getNombreCompleto())).append(";");
        linea.append(limpiar(usuario.getCorreoElectronico())).append(";");
        linea.append(limpiar(usuario.getContrasena()));

        if (usuario instanceof Administrador) {
            Administrador administrador = (Administrador) usuario;
            linea.append(";").append(limpiar(administrador.getTelefonoAdministrador()));
        } else if (usuario instanceof Instructor) {
            Instructor instructor = (Instructor) usuario;
            linea.append(";").append(limpiar(instructor.getDNI()));
            linea.append(";").append(limpiar(instructor.getIBAN()));
        } else if (usuario instanceof EstudianteUPM) {
            EstudianteUPM estudiante = (EstudianteUPM) usuario;
            linea.append(";").append(limpiar(estudiante.getDNI()));
            linea.append(";").append(limpiar(estudiante.getTarjetaCredito()));
            linea.append(";").append(limpiar(estudiante.getNumeroMatricula()));
            linea.append(";").append(convertirPreferenciasATexto(estudiante.getPreferenciasArtisticas()));
        } else if (usuario instanceof PersonalUPM) {
            PersonalUPM personal = (PersonalUPM) usuario;
            linea.append(";").append(limpiar(personal.getDNI()));
            linea.append(";").append(limpiar(personal.getTarjetaCredito()));
            linea.append(";").append(personal.getAntiguedad());
            linea.append(";").append(convertirPreferenciasATexto(personal.getPreferenciasArtisticas()));
        } else if (usuario instanceof ParticipanteExterno) {
            ParticipanteExterno externo = (ParticipanteExterno) usuario;
            linea.append(";").append(limpiar(externo.getDNI()));
            linea.append(";").append(limpiar(externo.getTarjetaCredito()));
            linea.append(";").append(convertirPreferenciasATexto(externo.getPreferenciasArtisticas()));
        }

        return linea.toString();
    }

    private String obtenerCodigoTipo(Usuario usuario) {
        if (usuario instanceof Administrador) {
            return TIPO_ADMINISTRADOR;
        }

        if (usuario instanceof Instructor) {
            return TIPO_INSTRUCTOR;
        }

        if (usuario instanceof EstudianteUPM) {
            return TIPO_ESTUDIANTE_UPM;
        }

        if (usuario instanceof PersonalUPM) {
            return TIPO_PERSONAL_UPM;
        }

        if (usuario instanceof ParticipanteExterno) {
            return TIPO_EXTERNO;
        }

        return "DESCONOCIDO";
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

    private String convertirPreferenciasATexto(List<PreferenciaArtistica> preferencias) {
        if (preferencias == null || preferencias.isEmpty()) {
            return "";
        }

        StringBuilder texto = new StringBuilder();

        for (int i = 0; i < preferencias.size(); i++) {
            PreferenciaArtistica preferencia = preferencias.get(i);

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

    private List<PreferenciaArtistica> convertirTextoAPreferencias(String texto) {
        List<PreferenciaArtistica> preferencias = new ArrayList<PreferenciaArtistica>();

        if (texto == null || texto.trim().isEmpty()) {
            return preferencias;
        }

        String[] elementos = texto.split(",");

        for (int i = 0; i < elementos.length; i++) {
            String[] partes = elementos[i].split(":");

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
        } catch (Exception e) {
            return 0;
        }
    }

    private String limpiar(String texto) {
        if (texto == null) {
            return "";
        }

        return texto.replace(";", ",").trim();
    }

    private void cerrarWriter(BufferedWriter writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                System.out.println("Error cerrando el fichero: " + e.getMessage());
            }
        }
    }

    private void cerrarReader(BufferedReader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                System.out.println("Error cerrando el fichero: " + e.getMessage());
            }
        }
    }
}
