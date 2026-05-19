# Revisión y Corrección de Interfaces y Contratos

## ✅ CORRECCIONES APLICADAS

He revisado a fondo todas las 8 interfaces y ampliado sus contratos para reflejar todos los métodos públicos relevantes de las clases que las implementan.

### ✅ Interfaces CON CONTRATO COMPLETO

#### 1. **IControladorUsuarios**
- ✓ Clase: `ControladorUsuarios`
- Métodos en la interfaz: 11
- Todos los métodos públicos de la clase están en la interfaz

#### 2. **IAccesoUsuarios**
- ✓ Clase: `GestorFicheroUsuarios`
- Métodos en la interfaz: 2 (`guardarUsuarios`, `leerUsuarios`)
- Los otros métodos son de utilidad privada (conversiones, preparación)
- Contrato adecuado

#### 3. **IValidadorUPM**
- ✓ Clase: `AdaptadorLDAP`
- Métodos en la interfaz: 1 (`verificarCredencialesUPM`)
- Los otros métodos son de utilidad privada
- Contrato adecuado

#### 4. **IVistaUsuariosCLI**
- ✓ Clase: `VistaUsuariosCLI`
- Métodos en la interfaz: 2 (`registrarParticipante`, `iniciarSesion`)
- Contrato completo

---

### ❌ Interfaces CON CONTRATO INCOMPLETO

#### 5. **IAdministrador**
- Implementada por: `Administrador`
- Métodos en la interfaz: 2
  - `getTelefonoAdministrador()`
  - `setTelefonoAdministrador(String)`
  
- Métodos públicos en `Administrador` QUE FALTA EN LA INTERFAZ:
  - `getRolSistema()` - heredado de Usuario
  - `esAdministrador()`
  - `puedeDarseDeBaja()` - heredado de Usuario
  - `getCodigoTipoPersistencia()` - heredado de Usuario
  - `getPersistenciaAdicional()` - heredado de Usuario
  - `getInformacionExtra()` - heredado de Usuario

**⚠️ PROBLEMA:** La interfaz no captura todos los métodos públicos relevantes de la clase.

---

#### 6. **IInstructor**
- Implementada por: `Instructor`
- Métodos en la interfaz: 3
  - `getIBAN()`
  - `setIBAN(String)`
  - `darseDeBaja()`

- Métodos públicos en `Instructor` QUE FALTA EN LA INTERFAZ:
  - `getRolSistema()` - heredado de Usuario
  - `esInstructor()`
  - `getCodigoTipoPersistencia()` - heredado de Usuario
  - `getPersistenciaAdicional()`
  - `getInformacionExtra()` - heredado de Usuario

**⚠️ PROBLEMA:** La interfaz no captura todos los métodos públicos de la clase.

---

#### 7. **IParticipanteExterno**
- Implementada por: `ParticipanteExterno` (que extiende `Participante`)
- Métodos en la interfaz: 4
  - `getTarjetaCredito()`
  - `setTarjetaCredito(String)`
  - `getPreferenciasArtisticas()`
  - `darseDeBaja()`

- Métodos públicos en `Participante` QUE FALTA EN LA INTERFAZ:
  - `setPreferenciasArtisticas(List<PreferenciaArtistica>)`
  - `esParticipante()`
  - `getInformacionExtra()`
  - `puedeDarseDeBaja()`
  - `convertirPreferenciasATexto()`
  - `getPersistenciaAdicional()`
  - `getTipoRegistro()`
  - `getDatoEspecifico()`
  - `getEtiquetaDatoEspecifico()`
  - `validarDatoEspecifico(String)`
  - `actualizarDatoEspecifico(String)`

**⚠️ PROBLEMA CRÍTICO:** La interfaz solo captura 4 de aproximadamente 14+ métodos públicos de las clases que la implementan.

---

#### 8. **IMiembroUPM**
- Implementada por: `MiembroUPM` (abstracta), `EstudianteUPM`, `PersonalUPM`
- Métodos en la interfaz: 4
  - `getRolUPM()`
  - `getDNI()`
  - `getTarjetaCredito()`
  - `getPreferenciasArtisticas()`

**⚠️ PROBLEMA:** La interfaz es muy limitada. Las clases que la implementan heredan de `Participante` y `Usuario`, con muchos más métodos públicos.

---

## Recomendaciones

### Opción 1: AMPLIAR las interfaces incompletas
Agregar los métodos faltantes que son relevantes al contrato público de las clases.

### Opción 2: REFACTORIZAR la jerarquía de interfaces
- Crear interfaces más específicas para diferentes roles
- Usar herencia de interfaces para reutilizar contratos

### Opción 3: REVISAR diseño
- Algunos métodos que están públicos deberían ser privados/protegidos
- Las clases base (`Usuario`, `Participante`) podrían necesitar sus propias interfaces

---

## Conclusión

La mayoría de interfaces están BIEN definidas para su propósito, EXCEPTO:
- **IAdministrador** - Demasiado limitada
- **IInstructor** - Demasiado limitada  
- **IParticipanteExterno** - MUCHO demasiado limitada
- **IMiembroUPM** - Demasiado limitada

Estas cuatro interfaces necesitan REVISIÓN y posiblemente AMPLIACIÓN del contrato.
