# UPMArts

Repositorio del proyecto UPMArts, realizado por el grupo **CITIM21-3-UPMArts** para la asignatura de Fundamentos de la Ingeniería del Software.

UPMArts plantea un sistema de apoyo a la gestión del Centro de Creación Cultural de la Universidad Politécnica de Madrid. El proyecto contempla el registro y acceso de usuarios, la organización de actividades culturales, la gestión de espacios y recursos, y la participación de usuarios externos y miembros de la comunidad universitaria.

## Integrantes

- FILALI BELHADJ CHAQROUNE, YASSIR
- GOMEZ MORENO, CARLOS
- MARGELINO GONZALES, ERIKA
- PANIS MARAMBA, TRISHALYN
- ZHANG, JIONGHAO

## Seguimiento del proyecto

La planificación, las tareas y la información de seguimiento del trabajo se encuentran en Redmine:

[Proyecto CITIM21-3-UPMArts en Redmine](https://fis.etsisi.upm.es/projects/citim21-3-upmarts)

## Contenido del repositorio

El repositorio está organizado por entregas:

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

La primera entrega recoge el análisis inicial del sistema.

Incluye:

- prototipo visual de UPMArts;
- proyecto de modelado en StarUML;
- diagramas de casos de uso;
- diagrama de clases de análisis;
- descripciones extendidas de casos de uso;
- PDF recopilatorio con los diagramas principales.

Archivos principales:

- `Entrega 1/Prototipo/UPM-Arts.pdf`
- `Entrega 1/Modelado/ StarUML/CITIM21-3-UPMArts.uml`
- `Entrega 1/Modelado/Diagramas/`
- `Entrega 1/Modelado/PDF Recopilatorio/UPM_Arts_Modelado_Completo.pdf`

## Entrega 2

La segunda entrega contiene el diseño actualizado, la implementación y la documentación de pruebas.

Incluye:

- modelo de diseño en StarUML;
- proyecto Java ejecutable con Maven;
- pruebas unitarias del controlador de usuarios;
- documento de pruebas unitarias;
- documento de pruebas de validación.

Archivos principales:

- `Entrega 2/Diseño/CITIM21-3-UPMArts.uml`
- `Entrega 2/Construccion/upmarts-entrega2/`
- `Entrega 2/Pruebas/Pruebas Unitarias.docx`
- `Entrega 2/Pruebas/Pruebas de Validación.docx`

## Proyecto Java

El código ejecutable se encuentra en:

```text
Entrega 2/Construccion/upmarts-entrega2
```

Esta aplicación de consola implementa la parte de usuarios de UPMArts:

- registro de participantes externos;
- registro de estudiantes UPM;
- registro de personal UPM/PDI/PAS;
- registro de instructores por parte de un administrador;
- inicio de sesión;
- listado de participantes e instructores desde administración;
- baja de usuarios;
- modificación de datos y preferencias artísticas;
- persistencia en fichero.

Para compilar y ejecutar las pruebas:

```bash
cd "Entrega 2/Construccion/upmarts-entrega2"
mvn clean test
```

Para generar el JAR:

```bash
mvn package
```

Para ejecutar la aplicación:

```bash
java -jar target/upmarts-1.0-SNAPSHOT.jar
```

El README específico del proyecto Java está en:

```text
Entrega 2/Construccion/upmarts-entrega2/README.md
```

## Requisitos de ejecución

- Java 8 o superior.
- Maven 3.x.
- Fichero `externals-2.0.jar` en la raíz del proyecto Maven.

La dependencia externa se referencia desde el `pom.xml` con `scope` `system`, siguiendo la configuración indicada para la asignatura.

## Estado de la entrega

El proyecto Maven compila correctamente, ejecuta las pruebas unitarias y genera el JAR de la aplicación. Antes de revisar la entrega se recomienda ejecutar:

```bash
cd "Entrega 2/Construccion/upmarts-entrega2"
mvn clean test
mvn package
```

Durante Maven puede aparecer un aviso relacionado con `systemPath` y `externals-2.0.jar`. Es un aviso esperado por la forma en la que se incluye la librería externa y no impide compilar ni ejecutar el proyecto.
