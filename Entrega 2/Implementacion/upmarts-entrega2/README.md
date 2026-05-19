# UPM Arts - Entrega 2

Aplicación CLI (Command Line Interface) para gestión de usuarios en el sistema UPM Arts. Implementada en Java con Maven, permite registro, autenticación y gestión de usuarios con diferentes roles.

## Descripción

UPM Arts es un sistema para gestionar actividades artísticas en la Universidad Politécnica de Madrid. Esta entrega implementa el módulo de usuarios, incluyendo registro público de participantes, autenticación y menús específicos según el rol del usuario.

## Funcionalidades Principales

### Registro de Participantes
- **Registro público**: Cualquier usuario puede registrarse como participante.
- **Detección automática de tipo**: Basada en el dominio del correo electrónico.
- **Validación UPM**: Para cuentas universitarias, se verifica contra el sistema LDAP de UPM.

### Inicio de Sesión
- Autenticación por correo y contraseña.
- Acceso a menús específicos según el rol.

### Menús por Rol

#### Participante (Estudiante UPM, Personal UPM, Externo)
- Ver datos personales.
- Ver preferencias artísticas.
- Modificar preferencias artísticas (música, pintura, teatro).
- Darse de baja voluntariamente.

#### Instructor
- Ver datos personales.
- Darse de baja voluntariamente.

#### Administrador
- Dar de alta instructores.
- Dar de baja cualquier usuario.
- Listar todos los usuarios registrados.

## Requisitos para Registrar un Usuario

Para registrar un participante, se deben cumplir los siguientes requisitos:

### Datos Obligatorios
- **Nombre completo**: No puede estar vacío.
- **Nick**: 
  - Entre 4 y 12 caracteres.
  - Solo letras y números (sin espacios ni símbolos).
  - No debe coincidir con términos conflictivos (lista en `terminos_conflictivos.txt`).
- **Correo electrónico**:
  - Formato válido (usuario@dominio).
  - No debe existir ya registrado.
- **Contraseña**:
  - Mínimo 12 caracteres.
  - Debe incluir al menos una mayúscula, una minúscula y un número.
- **DNI**: 8 dígitos seguidos de una letra (ej: 12345678A).
- **Tarjeta de crédito/débito**: Entre 8 y 19 dígitos.

### Datos Específicos por Tipo
- **Estudiante UPM** (`@alumnos.upm.es`):
  - Número de matrícula (no vacío).
  - Validación adicional contra LDAP UPM.
- **Personal UPM** (`@upm.es`):
  - Antigüedad en años (número entero positivo).
  - Validación adicional contra LDAP UPM.
- **Participante Externo** (otros dominios):
  - No requiere datos adicionales.

### Preferencias Artísticas
- Opcionales, pero recomendadas.
- Disciplinas: Música, Pintura, Teatro.
- Nivel: 1-10 (0 para no indicar).

### Validaciones Adicionales
- El nick no debe existir ya.
- El correo no debe existir ya.
- Para cuentas UPM: La contraseña debe ser válida en el sistema LDAP.

## Cómo Funciona la Validación UPM

La validación de cuentas UPM se realiza mediante el `AdaptadorLDAP`, que intenta usar la librería externa `externals-5.1.jar` si está disponible.

### Detección de Tipo de Usuario
El tipo se determina automáticamente por el dominio del correo:
- `@alumnos.upm.es` → Estudiante UPM
- `@upm.es` → Personal UPM
- Otros dominios válidos → Participante Externo

### Validación con Librería Externa
El adaptador intenta validar primero la **existencia de la cuenta UPM** (sin contraseña), y si eso falla, valida las **credenciales completas** (correo + contraseña).

1. **Intento 1: Existencia de cuenta con `servidor.Autenticacion`**:
   - Busca la clase `servidor.Autenticacion` en el classpath (de `externals-5.1.jar`).
   - Llama a `existeCuentaUPMStatic(String correo)` (método estático) o `existeCuentaUPM(String correo)` (método de instancia).
   - Si devuelve `true`, confirma que la cuenta existe en UPM (solo verifica el correo, no la contraseña).
   - **Propósito**: Validar que el correo pertenece a un usuario UPM registrado, sin necesidad de contraseña.

2. **Intento 2: Credenciales completas con `servidor.ExternalLDAP`**:
   - Si el Intento 1 falla (clase no encontrada o método no disponible), busca `servidor.ExternalLDAP`.
   - Llama a métodos como `verificarCredencialesUPM(String correo, String password)` o `LoginLDAP()`.
   - Valida tanto la existencia de la cuenta como la contraseña correcta.
   - **Propósito**: Validación completa de credenciales UPM.

3. **Validación Local (Fallback)**:
   - Si ninguno de los intentos anteriores funciona (librería no disponible), usa validación local básica:
     - Correo termina en `@upm.es` o `@alumnos.upm.es`.
     - Contraseña tiene ≥12 caracteres.
   - **Propósito**: Permitir desarrollo y testing sin la librería externa.

### Integración en Maven
En `pom.xml`, se incluye como dependencia system:
```xml
<dependency>
    <groupId>com.upm</groupId>
    <artifactId>externals</artifactId>
    <version>5.1</version>
    <scope>system</scope>
    <systemPath>${project.basedir}/lib/externals-5.1.jar</systemPath>
</dependency>
```

## Usuarios Iniciales

### Administrador
- Correo: `admin@upm.es`
- Contraseña: `Admin123456A`
- Teléfono: `910000000`
- Rol: Administrador del sistema

### Instructor
- Correo: `instructor@upm.es`
- Contraseña: `Instructor123A`
- DNI: `12345678A`
- IBAN: `ES7620770024003102575766`
- Rol: Instructor UPM

### Estudiante UPM
- Correo: `carlos.gomez.moreno@alumnos.upm.es`
- Contraseña: `Admin123456A`
- DNI: `20543417R`
- Matrícula: `bv0379`
- Preferencias: `MUSICA:7,PINTURA:7,TEATRO:7`
- Rol: Estudiante UPM

### Personal UPM
- Correo: `luis.martin@upm.es`
- Contraseña: `Instructor123A`
- DNI: `45678901E`
- Antigüedad: `5 años`
- Rol: Personal UPM

### Participante Externo
- Correo: `pedro@externo.com`
- Contraseña: `Instructor123A`
- DNI: `55667788G`
- Tarjeta: `5555555555`
- Preferencias: `MUSICA:5,TEATRO:4`
- Rol: Participante Externo

## Persistencia

Los usuarios se almacenan en `data/usuarios.txt` (formato CSV con `;` como separador).

### Formatos por Tipo
```
ADMINISTRADOR;nick;nombre;correo;passwordHash;telefono
INSTRUCTOR;nick;nombre;correo;passwordHash;dni;iban
EXTERNO;nick;nombre;correo;passwordHash;dni;tarjeta;preferencias
ESTUDIANTE_UPM;nick;nombre;correo;passwordHash;dni;tarjeta;matricula;preferencias
PERSONAL_UPM;nick;nombre;correo;passwordHash;dni;tarjeta;antiguedad;preferencias
```

### Preferencias
Formato: `DISCIPLINA:nivel,DISCIPLINA:nivel,...`
Ejemplo: `MUSICA:7,PINTURA:4,TEATRO:2`

## Validación de Términos Conflictivos

Los nicks se validan contra una lista de términos no permitidos:
- Archivo: `data/terminos_conflictivos.txt` (durante desarrollo).
- Recurso: `src/main/resources/terminos_conflictivos.txt` (en JAR).
- Ignora líneas vacías y comentarios (`#`).
- Comparación case-insensitive.

## Tecnologías

- **Java 1.8**
- **Maven** para gestión de dependencias y build.
- **JUnit 4.13.2** para tests.
- **Librería externa**: `externals-5.1.jar` para validación UPM (opcional).

## Cómo Ejecutar

### En Eclipse
1. Importar como proyecto Maven existente.
2. Ejecutar `upmarts.App` como Java Application.

### Con Maven
```bash
mvn clean compile exec:java -Dexec.mainClass="upmarts.App"
```

### Con JAR
```bash
java -cp target/upmarts-1.0-SNAPSHOT.jar upmarts.App
```

## Estructura del Proyecto

```
upmarts-entrega2/
├── src/main/java/upmarts/
│   ├── App.java                    # Punto de entrada
│   ├── controlador/                # Lógica de negocio
│   ├── integracion/                # Adaptadores externos
│   ├── modelo/                     # Entidades
│   ├── persistencia/               # Acceso a datos
│   ├── validacion/                 # Validadores
│   └── vista/                      # Interfaz CLI
├── src/main/resources/             # Recursos empaquetados
├── src/test/java/                  # Tests unitarios
├── data/                           # Datos persistentes
├── lib/                            # Librerías externas
├── pom.xml                         # Configuración Maven
└── README.md                       # Esta documentación
```

## Notas de Desarrollo

- La aplicación es CLI pura (sin GUI).
- Los errores de registro se muestran con mensajes específicos.
- La persistencia es en archivo plano (no base de datos).
- Compatible con Java 8+.
- El adaptador LDAP es extensible por reflexión para facilitar testing.

 ## Cuando salga a producción : 
 - Eiminar el externals-5.1-javadoc.jar (solo es documentación)