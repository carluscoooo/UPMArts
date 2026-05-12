Carpeta reservada para la librería externa facilitada por el profesor: externals-5.1.jar.

El proyecto compila sin esta librería porque AdaptadorLDAP usa reflexión para reducir el acoplamiento.
Si se añade la librería externa al build path de Eclipse, AdaptadorLDAP intentará llamar a ExternalLDAP automáticamente.
