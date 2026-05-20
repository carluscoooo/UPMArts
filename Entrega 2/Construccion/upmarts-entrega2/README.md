# UPM Arts - Entrega 2

Aplicacion CLI en Java para la gestion de usuarios del sistema UPM Arts.

## Resumen

El proyecto implementa el alta, acceso y gestion basica de usuarios para una aplicacion de consola.
El sistema distingue administradores, instructores, participantes externos, estudiantes UPM y personal UPM.

La persistencia de usuarios se realiza en fichero plano y la validacion de cuentas UPM se delega en un
adaptador LDAP que usa la dependencia local `externals:5.1`.

## Requisitos

- Java 8 o superior
- Maven 3.x

## Como ejecutar

Para compilar y ejecutar las pruebas:

```bash
mvn clean test
```

Para generar el paquete:

```bash
mvn clean package
```

Para lanzar la aplicacion:

```bash
java -jar target/upmarts-1.0-SNAPSHOT.jar
```

La clase principal es `upmarts.App`.

## Datos del proyecto

La carpeta `data` ya forma parte del proyecto y contiene los usuarios persistidos:

- `data/usuarios.txt`: almacenamiento de usuarios

El controlador y la capa de persistencia trabajan con esa ruta:

- `upmarts.controlador.ControladorUsuarios`
- `upmarts.persistencia.GestorFicheroUsuarios`

## Usuarios iniciales

El proyecto arranca con usuarios precargados en `data/usuarios.txt`. Ademas, el controlador garantiza que
existan al menos un administrador y un instructor.

Usuarios disponibles en el fichero actual:

- Administrador: `admin@upm.es`
- Instructor: `instructor@upm.es`
- Estudiante UPM: `carlos.gomez.moreno@alumnos.upm.es`
- Personal UPM: `luis.martin@upm.es`
- Participante externo: `pedro@externo.com`

Credenciales conocidas del conjunto inicial:

- `admin@upm.es` -> `Admin123456A`
- `instructor@upm.es` -> `Instructor123A`
- `carlos.gomez.moreno@alumnos.upm.es` -> `Admin123456A`
- `luis.martin@upm.es` -> `Instructor123A`
- `pedro@externo.com` -> `Instructor123A`

Las contrasenas se almacenan cifradas con SHA-256.

## Tipos de usuario

- `Administrador`: puede listar usuarios, dar de alta instructores y dar de baja usuarios.
- `Instructor`: usuario interno con DNI e IBAN.
- `ParticipanteExterno`: participante general con DNI, tarjeta y preferencias artisticas.
- `EstudianteUPM`: participante UPM con matricula.
- `PersonalUPM`: participante UPM con antiguedad.

La jerarquia actual relevante es:

- `Usuario`
- `UsuarioConDNI`
- `ParticipanteExterno`
- `MiembroUPM`
- `EstudianteUPM` / `PersonalUPM`

## Reglas de validacion

Alta de participantes:

- nombre no vacio
- nick alfanumerico de 4 a 12 caracteres
- password de al menos 12 caracteres con mayusculas, minusculas y numeros
- correo con formato basico valido
- correo y nick no duplicados
- DNI con 8 digitos y una letra
- tarjeta con entre 8 y 19 digitos

Segun el correo:

- `@alumnos.upm.es` -> estudiante UPM
- `@upm.es` -> personal UPM
- cualquier otro dominio valido -> participante externo

Para cuentas UPM, el alta pasa por `upmarts.integracion.AdaptadorLDAP`.

## LDAP y dependencia externa

La validacion UPM utiliza:

- dependencia Maven: `es.upm.etsisi.califyme:externals:5.1`
- repositorio local: `lib/maven-repo`

No se usa `systemPath`. La dependencia se resuelve desde el repositorio Maven local declarado en `pom.xml`.

## Terminos conflictivos

La validacion de nick usa:

- primero `data/terminos_conflictivos.txt` si existe
- en su defecto `src/main/resources/terminos_conflictivos.txt`

## Estructura del proyecto

- `src/main/java`: codigo fuente
- `src/main/resources`: recursos empaquetados
- `src/test/java`: pruebas JUnit
- `data`: datos persistidos del proyecto
- `lib/maven-repo`: repositorio local de la dependencia externa
- `pom.xml`: build Maven

## Pruebas

Las pruebas automatizadas estan implementadas con JUnit 4 dentro de la estructura Maven del proyecto.

En particular, para la controladora de usuarios hay pruebas de:

- caja negra
- caja blanca

Archivos principales:

- `src/test/java/upmarts/controlador/ControladorUsuariosAltaAccesoCajaNegraTest.java`
- `src/test/java/upmarts/controlador/ControladorUsuariosAltaAccesoCajaBlancaTest.java`

## Salida de compilacion

El empaquetado Maven genera la carpeta `target`, que contiene los artefactos compilados y no debe
seguirse en Git. El proyecto ya incluye `.gitignore` para evitarlo.
