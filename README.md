# UPMArts — Sistema de gestión de un centro de creación cultural

![Java](https://img.shields.io/badge/Java-8%2B-ED8B00?logo=openjdk&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.x-C71A36?logo=apachemaven&logoColor=white)
![JUnit](https://img.shields.io/badge/JUnit-Tests-25A162?logo=junit5&logoColor=white)
![UML](https://img.shields.io/badge/UML-StarUML-blue)

Aplicación de consola en **Java** que implementa la gestión de usuarios del Centro de Creación Cultural de la UPM: registro e inicio de sesión con distintos roles, preferencias artísticas y persistencia en fichero con contraseñas cifradas.

El proyecto cubre el **ciclo completo de desarrollo de software**: análisis y modelado UML, diseño, implementación, pruebas unitarias y documentación (todo disponible en [`docs/`](docs/)).

## Características

- Alta de participantes externos, estudiantes UPM, personal UPM/PDI/PAS e instructores (estos últimos desde el menú de administrador).
- Inicio de sesión con distinción entre correo no registrado y contraseña incorrecta.
- Listado de participantes e instructores desde administración, baja de usuarios y modificación de datos.
- Consulta y modificación de preferencias artísticas (música, pintura, teatro, con nivel de 0 a 10).
- Validación de cuentas UPM mediante una librería externa de validación de credenciales, integrada a través de un adaptador.
- Persistencia en fichero con contraseñas hasheadas (SHA-256): `TIPO;nick;nombre;correo;passwordCifrada;...`

## Arquitectura

Arquitectura en capas con separación estricta de responsabilidades e **interfaces entre capas** para facilitar las pruebas y el bajo acoplamiento:

```
src/main/java/upmarts/
├── vista/          → Menús de consola y lectura de datos (VistaPrincipalCLI, VistaUsuariosCLI)
├── controlador/    → Coordinación de las operaciones y reglas de negocio (ControladorUsuarios)
├── modelo/         → Dominio: jerarquía de usuarios, roles y preferencias artísticas
├── validacion/     → Validación de formato de los datos (DNI, correos, contraseñas, IBAN...)
├── persistencia/   → Lectura y escritura de usuarios en fichero (GestorFicheroUsuarios)
└── integracion/    → Validación de cuentas UPM (AdaptadorLDAP, patrón Adapter)
```

Jerarquía del modelo de usuarios (herencia y polimorfismo):

```
Usuario
├── Administrador
└── UsuarioConDNI
    ├── Instructor
    └── ParticipanteExterno
        └── ParticipanteUPM
            ├── EstudianteUPM
            └── PersonalUPM
```

Decisiones de diseño destacables:

- **Modelado previo con UML** (StarUML): casos de uso, diagramas de clases de análisis y diseño, y descripciones extendidas de casos de uso (`docs/modelado` y `docs/diseno`).
- **Patrón Adapter** (`AdaptadorLDAP`) para integrar la librería externa de validación sin acoplarla al dominio.
- La vista se limita a pedir y mostrar datos; las reglas de negocio se concentran en el controlador y las de formato en `ValidadorDatosUsuario`.

## Pruebas

Pruebas unitarias con **JUnit** sobre el controlador de usuarios, con enfoques de **caja negra** y **caja blanca**: altas válidas e inválidas, duplicados, inicio de sesión, validación UPM y errores de persistencia simulados.

```bash
mvn test
```

La documentación de pruebas está en [`docs/pruebas`](docs/pruebas).

## Requisitos y ejecución

Requiere **Java 8+** y **Maven 3.x**. La librería `externals-2.0.jar` está incluida en la raíz del repositorio (se referencia desde el `pom.xml` con scope `system`; Maven puede mostrar un aviso por ello, que no impide compilar ni ejecutar).

```bash
# Compilar y ejecutar las pruebas
mvn clean test

# Empaquetar y lanzar la aplicación
mvn package
java -jar target/upmarts-1.0-SNAPSHOT.jar
```

Al iniciar por primera vez se crean dos usuarios por defecto:

- Administrador — correo `admin@upm.es`, contraseña `Admin123456A`
- Instructor — correo `instructor@upm.es`, contraseña `Instructor123A`

Los datos se guardan en `data/usuarios.txt` (se genera en la primera ejecución).

## Estructura del repositorio

```
├── src/            → Código fuente y pruebas (proyecto Maven)
├── docs/
│   ├── modelado/   → Casos de uso, diagrama de clases de análisis, proyecto StarUML
│   ├── prototipo/  → Prototipo visual de la aplicación
│   ├── diseno/     → Diagramas de diseño, componentes y despliegue
│   └── pruebas/    → Documentos de pruebas unitarias y de validación
├── pom.xml
└── externals-2.0.jar
```

## Autoría

Desarrollado por **Carlos Gómez Moreno** ([@carluscoooo](https://github.com/carluscoooo)) en colaboración con un equipo de 5 personas.
