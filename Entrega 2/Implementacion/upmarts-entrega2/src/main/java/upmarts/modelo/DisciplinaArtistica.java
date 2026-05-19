package upmarts.modelo;

public enum DisciplinaArtistica {
    MUSICA,
    PINTURA,
    TEATRO;

    public static DisciplinaArtistica desdeTexto(String texto) {
        if (texto == null) {
            return null;
        }

        String valor = texto.trim().toUpperCase();
        valor = valor.replace("Á", "A");
        valor = valor.replace("É", "E");
        valor = valor.replace("Í", "I");
        valor = valor.replace("Ó", "O");
        valor = valor.replace("Ú", "U");

        try {
            return DisciplinaArtistica.valueOf(valor);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
