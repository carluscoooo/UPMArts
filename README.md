# Proyecto UPMArts

Repositorio del proyecto UPMArts, realizado para la asignatura de Fundamentos de la Ingeniería del Software por el grupo **CITIM21-3-UPMArts**.

UPMArts es una aplicación pensada para apoyar la gestión del Centro de Creación Cultural de la Universidad Politécnica de Madrid. El sistema contempla usuarios de distintos tipos, actividades culturales, espacios, recursos, cursos, sesiones libres y asociaciones culturales.

## Equipo

- FILALI BELHADJ CHAQROUNE, YASSIR
- GOMEZ MORENO, CARLOS
- MARGELINO GONZALES, ERIKA
- PANIS MARAMBA, TRISHALYN
- ZHANG, JIONGHAO

## Enlace de gestión

La información de seguimiento del proyecto se encuentra en Redmine:

[Proyecto CITIM21-3-UPMArts en Redmine](https://fis.etsisi.upm.es/projects/citim21-3-upmarts)

## Estructura del repositorio

```text
citim21-3-upmarts/
├── Entrega 1/
│   ├── Modelado/
│   └── Prototipo/
└── Entrega 2/
    ├── Construccion/
    ├── Diseño/
    └── Pruebas/
```

## Entrega 1

La primera entrega recoge el trabajo de análisis inicial del sistema.

Contenido principal:

- Prototipo visual de UPMArts en PDF.
- Proyecto de modelado en StarUML.
- Diagramas de casos de uso.
- Diagrama de clases de análisis.
- Descripciones extendidas de casos de uso.
- PDF recopilatorio con los diagramas principales.

Archivos y carpetas más importantes:

- `Entrega 1/Prototipo/UPM-Arts.pdf`
- `Entrega 1/Modelado/ StarUML/CITIM21-3-UPMArts.uml`
- `Entrega 1/Modelado/Diagramas/`
- `Entrega 1/Modelado/PDF Recopilatorio/UPM_Arts_Modelado_Completo.pdf`

## Entrega 2

La segunda entrega contiene el diseño, la construcción del proyecto Java y la documentación de pruebas.

Contenido principal:

- Diseño actualizado del sistema.
- Proyecto Java ejecutable con Maven.
- Pruebas unitarias del controlador de usuarios.
- Documento de pruebas unitarias.
- Documento de pruebas de validación.

Archivos y carpetas más importantes:

- `Entrega 2/Diseño/CITIM21-3-UPMArts.uml`
- `Entrega 2/Construccion/upmarts-entrega2/`
- `Entrega 2/Pruebas/Pruebas Unitarias.docx`
- `Entrega 2/Pruebas/Pruebas de Validación.docx`

## Proyecto de software

El proyecto ejecutable está en:

```text
Entrega 2/Construccion/upmarts-entrega2
```

Es una aplicación de consola en Java que implementa la gestión básica de usuarios de UPMArts:

- registro de participantes externos;
- registro de estudiantes UPM;
- registro de personal UPM/PDI/PAS;
- registro de instructores por administrador;
- inicio de sesión;
- listado de usuarios desde administración;
- baja de usuarios;
- modificación de datos y preferencias artísticas;
- persistencia en fichero.

Para compilar y probar:

```bash
cd "Entrega 2/Construccion/upmarts-entrega2"
mvn clean test
```

Para generar y ejecutar el JAR:

```bash
mvn package
java -jar target/upmarts-1.0-SNAPSHOT.jar
```

La explicación detallada del software está en:

```text
Entrega 2/Construccion/upmarts-entrega2/README.md
```

## Requisitos para ejecutar el software

- Java 8 o superior.
- Maven 3.x.
- `externals-2.0.jar` situado en la raíz del proyecto Maven.

El POM utiliza la dependencia externa con `scope` `system`, siguiendo la configuración indicada para la asignatura.

## Organización del código

Dentro del proyecto Maven:

- `src/main/java/upmarts/modelo`: clases del dominio.
- `src/main/java/upmarts/controlador`: lógica principal de los casos de uso de usuarios.
- `src/main/java/upmarts/vista`: interfaz de consola.
- `src/main/java/upmarts/validacion`: reglas de validación de datos.
- `src/main/java/upmarts/persistencia`: lectura y escritura de usuarios.
- `src/main/java/upmarts/integracion`: validación de cuentas UPM mediante la librería externa.
- `src/test/java/upmarts/controlador`: pruebas unitarias.

## Estado de la entrega

El proyecto de software compila con Maven, ejecuta las pruebas unitarias y genera el JAR de la aplicación.

Comprobaciones recomendadas antes de revisar la entrega:

```bash
cd "Entrega 2/Construccion/upmarts-entrega2"
mvn clean test
mvn package
java -jar target/upmarts-1.0-SNAPSHOT.jar
```

Durante Maven puede aparecer un aviso por el uso de `systemPath` para `externals-2.0.jar`. No impide la compilación ni la ejecución del proyecto.
