# UPM Arts - Entrega 2

Implementación Java/Maven del subconjunto de funcionalidades de alta y acceso de usuarios.

## Cómo abrirlo en Eclipse

1. Descomprimir el ZIP.
2. En Eclipse: File > Import > Maven > Existing Maven Projects.
3. Seleccionar la carpeta `upmarts-entrega2`.
4. Ejecutar `upmarts.App` como Java Application.

## Flujo principal

La aplicación arranca con dos opciones:

- Registrarse.
- Iniciar sesión.

El registro público solo permite crear participantes. El sistema detecta automáticamente el tipo de participante a partir del correo:

- `@alumnos.upm.es`: estudiante UPM.
- `@upm.es`: personal UPM.
- Cualquier otro dominio válido: participante externo.

Los instructores y administradores no tienen registro público. Son cuentas precreadas o dadas de alta por un administrador.

## Usuarios iniciales

Administrador inicial:

- Correo: `admin@upm.es`
- Contraseña: `Admin123456A`

Instructor inicial:

- Correo: `instructor@upm.es`
- Contraseña: `Instructor123A`

## Funcionalidades implementadas

- Registro de participantes externos.
- Registro de estudiantes UPM.
- Registro de personal UPM.
- Validación de correos UPM mediante el adaptador LDAP.
- Inicio de sesión.
- Menú específico según el tipo real de usuario.
- Administrador: alta de instructores, baja de usuarios y listado de usuarios.
- Instructor: consulta de datos y baja voluntaria.
- Participante: consulta de datos, gestión de preferencias artísticas y baja voluntaria.

## Persistencia

Los usuarios se guardan en `data/usuarios.txt`, una línea por usuario y atributos separados por `;`.

Formato usado:

```txt
ADMINISTRADOR;nick;nombre;correo;passwordHash;telefono
INSTRUCTOR;nick;nombre;correo;passwordHash;dni;iban
EXTERNO;nick;nombre;correo;passwordHash;dni;tarjeta;preferencias
ESTUDIANTE_UPM;nick;nombre;correo;passwordHash;dni;tarjeta;matricula;preferencias
PERSONAL_UPM;nick;nombre;correo;passwordHash;dni;tarjeta;antiguedad;preferencias
```

Las preferencias se guardan con este formato:

```txt
MUSICA:7,PINTURA:4,TEATRO:2
```

## ExternalLDAP

El adaptador LDAP está en `upmarts.integracion.AdaptadorLDAP`.
Si se añade `externals-5.1.jar` al build path, el adaptador intenta localizar `ExternalLDAP` por reflexión.
Si no está la librería, usa una validación local mínima para poder ejecutar la práctica en Eclipse.

## Validación de términos conflictivos

La validación del nick no usa una lista fija en código. Se lee desde:

- `data/terminos_conflictivos.txt`, cuando se ejecuta desde el proyecto.
- `src/main/resources/terminos_conflictivos.txt`, como recurso incluido en el `.jar`.

Se ignoran líneas vacías y líneas que empiezan por `#`. La comparación se hace en minúsculas y por coincidencia exacta.
