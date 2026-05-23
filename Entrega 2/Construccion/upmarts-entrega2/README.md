# UPMArts - Construcción

Este directorio contiene el proyecto Java de la segunda entrega de UPMArts. La aplicación se ejecuta por consola y desarrolla la parte de gestión de usuarios del centro cultural.

El proyecto está preparado como proyecto Maven y utiliza persistencia en fichero para guardar los usuarios registrados.

## Funcionalidades implementadas

- Alta de participantes externos.
- Alta de estudiantes UPM.
- Alta de personal UPM/PDI/PAS.
- Alta de instructores desde el menú de administrador.
- Inicio de sesión con distinción entre correo no registrado y contraseña incorrecta.
- Listado de participantes e instructores desde administración.
- Baja de usuarios.
- Modificación de datos de participante.
- Consulta y modificación de preferencias artísticas.
- Validación de cuentas UPM mediante la librería externa indicada para la asignatura.

## Requisitos

- Java 8 o superior.
- Maven 3.x.
- `externals-2.0.jar` en la raíz de este proyecto.

El fichero `externals-2.0.jar` es necesario porque el `pom.xml` lo referencia como dependencia de tipo `system`.

## Compilación, pruebas y ejecución

Para compilar y ejecutar las pruebas:

```bash
mvn clean test
```

Para generar el JAR:

```bash
mvn package
```

Para lanzar la aplicación empaquetada:

```bash
java -jar target/upmarts-1.0-SNAPSHOT.jar
```

La clase principal es:

```text
upmarts.App
```

Si se ejecuta desde VS Code o Eclipse sin usar Maven, el classpath debe incluir también `externals-2.0.jar`. Para evitar problemas de configuración, se recomienda compilar y empaquetar con Maven.

## Usuarios iniciales

Al iniciar el controlador se crean, si no existen, dos usuarios iniciales:

- Administrador:
  - correo: `admin@upm.es`
  - contraseña: `Admin123456A`
- Instructor:
  - correo: `instructor@upm.es`
  - contraseña: `Instructor123A`

Los usuarios se almacenan en:

```text
data/usuarios.txt
```

Las contraseñas no se guardan en claro; se almacena su hash SHA-256.

## Estructura del código

- `upmarts.App`: punto de entrada y control final de errores.
- `upmarts.vista`: menús de consola y lectura de datos.
- `upmarts.controlador`: coordinación de las operaciones de usuario.
- `upmarts.validacion`: validaciones de formato de los datos introducidos.
- `upmarts.modelo`: clases del dominio y enumerados.
- `upmarts.persistencia`: lectura y escritura de usuarios en fichero.
- `upmarts.integracion`: adaptación de la validación externa UPM.

La vista se limita a pedir datos y mostrar resultados. Las reglas de negocio se concentran en el controlador, y las reglas de formato se delegan en `ValidadorDatosUsuario`.

## Modelo de usuarios

La jerarquía usada en el modelo es:

```text
Usuario
├── Administrador
└── UsuarioConDNI
    ├── Instructor
    └── ParticipanteExterno
        └── ParticipanteUPM
            ├── EstudianteUPM
            └── PersonalUPM
```

Los tipos de usuario se identifican con el enum `RolUsuario`:

- `ADMINISTRADOR`
- `INSTRUCTOR`
- `PARTICIPANTE_EXTERNO`
- `ESTUDIANTE_UPM`
- `PERSONAL_UPM`

## Validaciones principales

Las validaciones de formato se encuentran en:

```text
src/main/java/upmarts/validacion/ValidadorDatosUsuario.java
```

Reglas principales:

- Nick obligatorio, alfanumérico, de 4 a 12 caracteres, sin términos conflictivos y no repetido.
- Nombre completo obligatorio.
- Correo obligatorio, con formato válido y no repetido.
- Contraseña obligatoria con al menos 12 caracteres, una mayúscula, una minúscula y un número.
- DNI obligatorio con 8 dígitos y una letra.
- Tarjeta obligatoria con exactamente 16 dígitos.
- IBAN obligatorio con formato español: `ES` seguido de 22 dígitos.
- Matrícula obligatoria para estudiantes UPM.
- Antigüedad obligatoria para personal UPM/PDI/PAS, numérica y mayor o igual que 0.
- Preferencias artísticas de música, pintura y teatro, con nivel de 0 a 10. El valor 0 indica que no se registra esa disciplina.

Las comprobaciones que dependen del estado del sistema se realizan desde el controlador: nick duplicado, correo duplicado, correo no registrado en inicio de sesión, contraseña errónea, permisos de administrador, validación UPM y errores de persistencia.

## Validación UPM

La validación externa está encapsulada en:

```text
src/main/java/upmarts/integracion/AdaptadorLDAP.java
```

Para usuarios UPM se comprueba que la cuenta sea válida y que el rol encaje con el dominio del correo:

- `@alumnos.upm.es`: estudiante UPM.
- `@upm.es`: personal UPM/PDI/PAS.

Si el correo no pertenece a un dominio UPM, el usuario se registra como participante externo y no se aplica esta validación.

## Persistencia

La persistencia se gestiona en:

```text
src/main/java/upmarts/persistencia/GestorFicheroUsuarios.java
```

El formato general de cada línea de `data/usuarios.txt` es:

```text
TIPO;nick;nombre;correo;passwordCifrada;camposPropiosDelRol
```

Si una línea del fichero no puede convertirse en usuario, se omite para permitir que el resto de datos se carguen correctamente.

## Pruebas

Las pruebas unitarias están en:

```text
src/test/java/upmarts/controlador
```

Clases incluidas:

- `ControladorUsuariosAltaAccesoCajaNegraTest`: pruebas de caja negra.
- `ControladorUsuariosAltaAccesoCajaBlancaTest`: pruebas de caja blanca.

Actualmente se prueban altas válidas e inválidas, duplicados, inicio de sesión, validación UPM, validaciones de preferencias y errores de persistencia simulados.

Para ejecutarlas:

```bash
mvn test
```

## Archivos importantes

- `pom.xml`: configuración Maven.
- `externals-2.0.jar`: librería externa indicada para la asignatura.
- `data/usuarios.txt`: fichero de usuarios persistidos.
- `src/main/resources/terminos_conflictivos.txt`: lista por defecto de términos no permitidos para el nick.
- `target/`: carpeta generada al compilar. No forma parte del código fuente.

## Nota sobre Maven

Maven puede mostrar un aviso por el uso de `systemPath` en la dependencia externa. El aviso no impide compilar, ejecutar pruebas ni generar el JAR.
