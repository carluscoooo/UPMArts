package upmarts.modelo;

public class PreferenciaArtistica {

    private DisciplinaArtistica disciplina;
    private int nivelExperiencia;

    public PreferenciaArtistica(DisciplinaArtistica disciplina, int nivelExperiencia) {
        this.disciplina = disciplina;
        setNivelExperiencia(nivelExperiencia);
    }

    public DisciplinaArtistica getDisciplina() {
        return disciplina;
    }

    public void setDisciplina(DisciplinaArtistica disciplina) {
        this.disciplina = disciplina;
    }

    public int getNivelExperiencia() {
        return nivelExperiencia;
    }

    public void setNivelExperiencia(int nivelExperiencia) {
        if (nivelExperiencia < 1 || nivelExperiencia > 10) {
            throw new IllegalArgumentException("El nivel de experiencia debe estar entre 1 y 10.");
        }

        this.nivelExperiencia = nivelExperiencia;
    }
}
