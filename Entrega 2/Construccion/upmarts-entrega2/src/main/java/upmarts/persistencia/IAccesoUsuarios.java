package upmarts.persistencia;

import java.util.List;
import upmarts.modelo.Usuario;

public interface IAccesoUsuarios {
    void guardarUsuarios(List<Usuario> usuarios);
    List<Usuario> leerUsuarios();
}
