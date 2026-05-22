# UPMArts - Entrega 2

Aplicacion de consola en Java para gestionar usuarios del sistema UPMArts.

El proyecto esta organizado como una aplicacion Maven y cubre el registro, inicio de sesion y gestion basica de usuarios del centro cultural.

## Funcionalidades principales

- Registro de participantes.
- Deteccion automatica del tipo de participante segun el correo:
  - `@alumnos.upm.es`: estudiante UPM.
  - `@upm.es`: personal UPM/PDI/PAS.
  - otros dominios validos: participante externo.
- Registro de instructores desde el menu de administrador.
- Inicio de sesion con mensajes de error especificos.
- Listado de participantes e instructores desde administrador.
- Baja de usuarios.
- Consulta y modificacion de datos de participante.
- Consulta y modificacion de preferencias artisticas.
- Persistencia en fichero plano.

## Requisitos

- Java 8 o superior.
- Maven 3.x.
- El fichero `externals-2.0.jar` debe estar en la raiz del proyecto, porque el `pom.xml` lo referencia con `systemPath`.

## Compilar, probar y ejecutar

Para compilar y lanzar las pruebas:

```bash
mvn clean test
```

Para generar el JAR:

```bash
mvn clean package
```

Para ejecutar la aplicacion empaquetada:

```bash
java -jar target/upmarts-1.0-SNAPSHOT.jar
```

La clase principal es:

```text
upmarts.App
```

Si se ejecuta desde VS Code sin Maven, hay que asegurarse de que el classpath incluye las clases compiladas y `externals-2.0.jar`. La forma mas fiable de compilar y empaquetar el proyecto es Maven.

## Avisos conocidos de Maven

Durante `mvn package` puede aparecer un aviso parecido a este:

```text
'dependencies.dependency.systemPath' ... should not point at files within the project directory
```

Ese aviso aparece porque el POM de la asignatura usa una dependencia con `scope` `system` y `systemPath`. No impide compilar ni ejecutar los tests.

El empaquetado tambien genera un JAR de Javadoc mediante `maven-javadoc-plugin`.

## Usuarios iniciales

El controlador garantiza que existan al menos estos usuarios:

- Administrador:
  - correo: `admin@upm.es`
  - password: `Admin123456A`
- Instructor:
  - correo: `instructor@upm.es`
  - password: `Instructor123A`

El fichero `data/usuarios.txt` puede contener mas usuarios creados durante las pruebas manuales. Las contrasenas se guardan cifradas con SHA-256, no en texto plano.

## Roles del sistema

Los roles se identifican con el enum `RolUsuario`:

- `ADMINISTRADOR`
- `INSTRUCTOR`
- `PARTICIPANTE_EXTERNO`
- `ESTUDIANTE_UPM`
- `PERSONAL_UPM`

La jerarquia principal del modelo es:

- `Usuario`
- `UsuarioConDNI`
- `Administrador`
- `Instructor`
- `ParticipanteExterno`
- `EstudianteUPM`
- `PersonalUPM`

`EstudianteUPM` y `PersonalUPM` heredan de `ParticipanteExterno`, porque tambien son participantes y comparten DNI, tarjeta y preferencias artisticas.

## Estructura del codigo

- `upmarts.App`: punto de entrada de la aplicacion.
- `upmarts.vista`: interfaz de consola. Pide datos y muestra mensajes.
- `upmarts.controlador`: coordina los casos de uso y decide el flujo de registro, login y administracion.
- `upmarts.validacion`: validaciones de formato y reglas simples de entrada.
- `upmarts.modelo`: clases de dominio y enums.
- `upmarts.persistencia`: lectura y escritura de usuarios en fichero.
- `upmarts.integracion`: adaptador para validar cuentas UPM con la libreria externa.

La vista no valida reglas por su cuenta. Llama al controlador, y el controlador delega las validaciones de formato en `ValidadorDatosUsuario`.

## Reglas de validacion

Las validaciones de formato estan centralizadas en:

```text
src/main/java/upmarts/validacion/ValidadorDatosUsuario.java
```

Reglas principales:

- Nombre completo obligatorio.
- Nick obligatorio, alfanumerico, de 4 a 12 caracteres y sin terminos conflictivos.
- Correo obligatorio y con formato tipo `usuario@dominio.com`.
- Correo no repetido entre usuarios.
- Password obligatoria, con al menos 12 caracteres, una mayuscula, una minuscula y un numero.
- DNI obligatorio con 8 digitos y 1 letra.
- Tarjeta obligatoria con exactamente 16 digitos.
- IBAN obligatorio con formato espanol: `ES` seguido de 22 digitos.
- Matricula obligatoria para estudiantes UPM.
- Antiguedad obligatoria para personal UPM, numerica y mayor o igual que 0.
- Preferencias artisticas: `MUSICA`, `PINTURA` y `TEATRO`, con nivel de 0 a 10. El valor 0 significa que no se registra esa preferencia.

El controlador mantiene las validaciones que dependen del estado del sistema:

- nick duplicado;
- correo duplicado;
- correo no registrado al iniciar sesion;
- password erronea en login;
- permisos de administrador;
- validacion externa UPM;
- errores de lectura o guardado en persistencia.

## Terminos conflictivos

La lista de terminos no permitidos para el nick se carga en este orden:

1. `data/terminos_conflictivos.txt`, si existe.
2. `src/main/resources/terminos_conflictivos.txt`, como recurso empaquetado.

Esto permite cambiar la lista externa sin recompilar, pero el proyecto tambien tiene una lista por defecto en recursos.

## Validacion UPM

La validacion de cuentas UPM esta encapsulada en:

```text
src/main/java/upmarts/integracion/AdaptadorLDAP.java
```

El adaptador usa reflexion para llamar a la libreria externa incluida en `externals-2.0.jar`. Comprueba:

- que el correo pertenezca a un dominio UPM;
- que la cuenta exista segun la libreria externa;
- que el rol devuelto sea compatible con el correo:
  - `@alumnos.upm.es` debe corresponder a alumno;
  - `@upm.es` debe corresponder a PDI o PAS.

Si la libreria externa no esta disponible en tiempo de ejecucion, el adaptador usa una validacion minima de respaldo para poder probar la aplicacion fuera de Maven.

## Persistencia

Los usuarios se guardan en:

```text
data/usuarios.txt
```

La clase responsable es:

```text
src/main/java/upmarts/persistencia/GestorFicheroUsuarios.java
```

Formato general:

```text
TIPO;nick;nombre;correo;passwordCifrada;camposPropiosDelRol
```

Ejemplos de campos propios:

- Administrador: telefono.
- Instructor: DNI e IBAN.
- Participante externo: DNI, tarjeta y preferencias.
- Estudiante UPM: DNI, tarjeta, matricula y preferencias.
- Personal UPM: DNI, tarjeta, antiguedad y preferencias.

Si una linea del fichero no se puede convertir a usuario, se ignora para no romper la lectura completa.

## Gestion de errores

La aplicacion tiene un `try/catch` final en `App` para evitar que un error no controlado muestre una traza al usuario.

Ademas:

- el controlador guarda el ultimo error en `ultimoError`;
- la vista muestra ese mensaje cuando una operacion no se puede completar;
- los errores de lectura y guardado de usuarios se transforman en mensajes controlados;
- el login distingue entre correo no registrado y password erronea.

## Pruebas

Las pruebas estan en:

```text
src/test/java/upmarts/controlador
```

Clases principales:

- `ControladorUsuariosAltaAccesoCajaNegraTest`
- `ControladorUsuariosAltaAccesoCajaBlancaTest`

Se ejecutan con:

```bash
mvn test
```

Actualmente cubren altas validas e invalidas, login, validaciones de datos, duplicados, rutas de UPM, persistencia simulada y ramas principales del controlador.

## Archivos importantes

- `pom.xml`: configuracion Maven.
- `externals-2.0.jar`: dependencia externa exigida por el POM.
- `data/usuarios.txt`: datos persistidos de usuarios.
- `src/main/resources/terminos_conflictivos.txt`: lista por defecto de nicks no permitidos.
- `target/`: salida de compilacion generada por Maven. No debe versionarse.

## Notas para desarrollo

- Mantener las reglas de formato en `ValidadorDatosUsuario`.
- Mantener en `ControladorUsuarios` solo la logica que necesita estado, persistencia, permisos o integracion externa.
- Evitar que la vista contenga reglas de negocio; debe pedir datos y mostrar respuestas.
- Si se cambia el formato de `data/usuarios.txt`, actualizar tambien `GestorFicheroUsuarios` y las pruebas.
- Si se cambia una regla de validacion, actualizar las pruebas de caja negra.
