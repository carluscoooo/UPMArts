# UPMArts - Proyecto de software

Aplicación de consola en Java desarrollada para la segunda entrega de UPMArts. Esta parte del repositorio contiene el proyecto Maven ejecutable, las clases de dominio, la persistencia en fichero y las pruebas unitarias.

El objetivo de esta aplicación es gestionar el alta, acceso y administración básica de usuarios del centro cultural UPMArts.

## Funcionalidades

- Registro de participantes externos.
- Registro de estudiantes UPM y personal UPM/PDI/PAS.
- Registro de instructores desde el menú de administración.
- Inicio de sesión con mensajes de error específicos.
- Listado de participantes e instructores desde administrador.
- Baja de usuarios.
- Consulta y modificación de datos personales.
- Consulta y modificación de preferencias artísticas.
- Persistencia de usuarios en fichero plano.

## Requisitos

- Java 8 o superior.
- Maven 3.x.
- El fichero `externals-2.0.jar` en la raíz de este proyecto.

La dependencia externa se declara en el `pom.xml` con `scope` `system`, tal como se indica en la configuración de la asignatura.

## Compilación y ejecución

Desde esta carpeta:

```bash
mvn clean test
```

Para generar el JAR:

```bash
mvn clean package
```

Para ejecutar la aplicación empaquetada:

```bash
java -jar target/upmarts-1.0-SNAPSHOT.jar
```

La clase principal es:

```text
upmarts.App
```

Si se ejecuta desde VS Code o Eclipse sin Maven, hay que asegurarse de que el classpath incluya `externals-2.0.jar`. Aun así, la forma más fiable de compilar y empaquetar es Maven.

## Usuarios iniciales

La aplicación crea usuarios iniciales si no existen:

- Administrador:
  - correo: `admin@upm.es`
  - contraseña: `Admin123456A`
- Instructor:
  - correo: `instructor@upm.es`
  - contraseña: `Instructor123A`

Los usuarios se guardan en:

```text
data/usuarios.txt
```

Las contraseñas se almacenan cifradas con SHA-256, no en texto plano.

## Estructura del código

- `upmarts.App`: punto de entrada de la aplicación y control final de errores.
- `upmarts.vista`: menús de consola, lectura de datos y mensajes al usuario.
- `upmarts.controlador`: coordinación de los casos de uso de usuarios.
- `upmarts.validacion`: validaciones de formato y reglas simples de entrada.
- `upmarts.modelo`: clases del dominio y enumerados.
- `upmarts.persistencia`: lectura y escritura de usuarios en fichero.
- `upmarts.integracion`: adaptación de la librería externa de validación UPM.

La vista no contiene reglas de negocio. Recoge datos y llama al controlador. El controlador decide el flujo de cada operación y delega las validaciones de formato en `ValidadorDatosUsuario`.

## Modelo de usuarios

La jerarquía principal es:

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

Los roles se identifican mediante el enum `RolUsuario`:

- `ADMINISTRADOR`
- `INSTRUCTOR`
- `PARTICIPANTE_EXTERNO`
- `ESTUDIANTE_UPM`
- `PERSONAL_UPM`

Esta identificación evita depender de comprobaciones con `instanceof` para saber el tipo de usuario.

## Validaciones principales

Las reglas de formato están centralizadas en:

```text
src/main/java/upmarts/validacion/ValidadorDatosUsuario.java
```

Reglas más importantes:

- Nick obligatorio, alfanumérico, de 4 a 12 caracteres y sin términos conflictivos.
- Nombre completo obligatorio.
- Correo obligatorio, con formato válido y no repetido.
- Contraseña obligatoria, con al menos 12 caracteres, una mayúscula, una minúscula y un número.
- DNI obligatorio con 8 dígitos y una letra.
- Tarjeta obligatoria con exactamente 16 dígitos.
- IBAN obligatorio con formato español: `ES` seguido de 22 dígitos.
- Matrícula obligatoria para estudiantes UPM.
- Antigüedad obligatoria para personal UPM/PDI/PAS, numérica y mayor o igual que 0.
- Preferencias artísticas de `MUSICA`, `PINTURA` y `TEATRO`, con nivel de 0 a 10.

El controlador mantiene las comprobaciones que dependen del estado del sistema:

- nick ya usado;
- correo ya registrado;
- correo no registrado en inicio de sesión;
- contraseña incorrecta;
- permisos de administrador;
- validación externa de cuentas UPM;
- errores de lectura o guardado.

## Validación UPM

La integración con la librería externa está encapsulada en:

```text
src/main/java/upmarts/integracion/AdaptadorLDAP.java
```

Se comprueba que la cuenta exista y que el rol devuelto por el sistema externo encaje con el correo:

- `@alumnos.upm.es`: estudiante UPM.
- `@upm.es`: personal UPM/PDI/PAS.

Si la librería externa no está disponible en tiempo de ejecución, el adaptador aplica una validación mínima de respaldo para facilitar las pruebas locales.

## Persistencia

La clase responsable de guardar y cargar usuarios es:

```text
src/main/java/upmarts/persistencia/GestorFicheroUsuarios.java
```

El formato general de cada línea es:

```text
TIPO;nick;nombre;correo;passwordCifrada;camposPropiosDelRol
```

Si una línea del fichero está corrupta, se ignora para no impedir la carga del resto de usuarios.

## Pruebas

Las pruebas se encuentran en:

```text
src/test/java/upmarts/controlador
```

Clases principales:

- `ControladorUsuariosAltaAccesoCajaNegraTest`
- `ControladorUsuariosAltaAccesoCajaBlancaTest`

Para ejecutarlas:

```bash
mvn test
```

Cubren altas válidas e inválidas, duplicados, inicio de sesión, usuarios UPM, persistencia simulada y ramas principales del controlador.

## Avisos conocidos

Durante la ejecución de Maven puede aparecer un aviso sobre `systemPath`, porque `externals-2.0.jar` se referencia desde el propio proyecto. Es un aviso esperado por la configuración indicada para la asignatura y no impide compilar, probar ni ejecutar.

La carpeta `target/` se genera automáticamente al compilar y no debe entregarse como código fuente.

## Antes de entregar

Comprobaciones recomendadas:

```bash
mvn clean test
mvn package
java -jar target/upmarts-1.0-SNAPSHOT.jar
```

También conviene revisar que `data/usuarios.txt` no contenga datos de pruebas manuales que no deban entregarse.
