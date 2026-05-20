# UPM Arts - Entrega 2

Aplicacion CLI en Java para la gestion de usuarios de UPM Arts.

## Requisitos

- Java 8 o superior
- Maven

## Ejecucion

```bash
mvn clean test
mvn -q package
java -jar target/upmarts-1.0-SNAPSHOT.jar
```

## Estructura util del proyecto

- `src/main/java`: codigo fuente
- `src/main/resources`: recursos empaquetados
- `data/usuarios.txt`: persistencia local de usuarios
- `lib/maven-repo`: repositorio Maven local con la dependencia `externals:5.1`
- `pom.xml`: configuracion del build

## Validacion UPM

La integracion LDAP vive en `upmarts.integracion.AdaptadorLDAP` y usa la dependencia
`es.upm.etsisi.califyme:externals:5.1` desde el repositorio local declarado en `pom.xml`.

## Terminos conflictivos

La validacion de nick carga primero `data/terminos_conflictivos.txt` si existe y, en su
defecto, usa `src/main/resources/terminos_conflictivos.txt`.
