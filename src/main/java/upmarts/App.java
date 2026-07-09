package upmarts;

import upmarts.vista.VistaPrincipalCLI;

public class App {

    public static void main(String[] args) {
        try {
            VistaPrincipalCLI vistaPrincipal = new VistaPrincipalCLI();
            vistaPrincipal.iniciarAplicacion();
        } catch (Exception e) {
            System.out.println("Se ha producido un error inesperado. La aplicación se cerrará de forma segura.");
        }
    }
}
