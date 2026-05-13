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
        return usuario.convertirAlineaPersistencia();
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
