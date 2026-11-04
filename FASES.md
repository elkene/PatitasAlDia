# Patitas al Día: plan de trabajo por fases

Aplicación Android para llevar el registro de mascotas, sus vacunas y sus citas al veterinario. Proyecto final de Aplicaciones Móviles, Ingeniería en Software y Tecnologías Emergentes, UABC Campus Otay.

Este documento explica qué hace cada fase, qué archivos involucra, cómo se prueba y en qué estado está. Antes de empezar una fase, lee su sección completa y revisa que la fase anterior esté terminada.

---

## Resumen del estado

| Fase | Nombre | Estado |
|---|---|---|
| 0 | Preparación | Terminada |
| 1 | Modelo y base de datos | Terminada |
| 2 | Lista de mascotas | Terminada |
| 3 | Formulario de mascota | Pendiente (ya existe el esqueleto de la pantalla) |
| 4 | Detalle en teléfono | Pendiente |
| 5 | Tableta y orientación | Pendiente |
| 6 | Guardar el estado | Pendiente |
| 7 | Foto de la mascota | Pendiente |
| 8 | Comunicación con el veterinario | Pendiente |
| 9 | Pulido | Pendiente |
| 10 | Entregables | Pendiente |

Al terminar una tarea, marca su casilla en este archivo. Al terminar una fase, actualiza también la tabla de arriba.

---

## Requisitos del proyecto

| # | Requisito | Cómo se cumple | Fase |
|---|---|---|---|
| 1 | Java o Kotlin en Android Studio | Kotlin | 0 |
| 2 | Al menos 2 pantallas con 2 actividades | `MainActivity`, `DetalleMascotaActivity` y `FormMascotaActivity`, con fragmentos de lista y detalle | 2, 3 y 4 |
| 3 | Funcionar en teléfono y tableta aprovechando la pantalla | Patrón lista-detalle: en la tableta se ven la lista y el detalle lado a lado | 5 |
| 4 | Funcionar en ambas orientaciones | Layouts `layout-land` para el detalle y el formulario | 5 |
| 5 | Guardar el estado al rotar o al salir y regresar | `ViewModel` y `SavedStateHandle` / `onSaveInstanceState` | 6 |
| 6 | Guardar los datos en SQLite | `DbHelper` (`SQLiteOpenHelper`) con 3 tablas | 1 |
| 7 | Usar una característica del dispositivo | Cámara y galería para la foto de la mascota | 7 |
| 8 | Usar algún tipo de comunicación | Llamada, SMS y correo al veterinario | 8 |
| 9 | Proyecto, manual de usuario y video | Ver fase 10 | 10 |

---

## Cómo abrir y correr el proyecto

1. Clonar el repositorio y abrirlo en Android Studio (versión reciente, con AGP 9).
2. Esperar a que Gradle sincronice.
3. Crear, si no existen, dos emuladores en el Device Manager: un teléfono (Pixel) y una tableta (Pixel Tablet).
4. Correr la app con el botón Run.
5. Pruebas:
   - Pruebas unitarias: `./gradlew testDebugUnitTest`
   - Pruebas en el emulador (base de datos): con un emulador encendido, `./gradlew connectedDebugAndroidTest`

En modo depuración la app crea tres mascotas de ejemplo (Firulais, Michi y Kiwi) con vacunas y citas la primera vez que se abre. Si quieres empezar con la base vacía o volver a generar los datos, desinstala la app del emulador.

---

## Reglas técnicas del proyecto

- **Lenguaje:** Kotlin. **Interfaz:** Views con layouts XML, actividades y fragmentos. **No** se usa Jetpack Compose.
- **Base de datos:** `SQLiteOpenHelper` con SQL escrito a mano. **No** se usa Room (lo exige el requisito 6).
- **View Binding** en todas las actividades, fragmentos y adaptadores. No usar `findViewById`.
- **Textos:** todos en `res/values/strings.xml`, nunca escritos directo en layouts ni en código. Colores en `colors.xml` y medidas repetidas en `dimens.xml`.
- **Idioma:** comentarios, textos de la interfaz y mensajes de commit en español.
- **Hilos:** las operaciones de base de datos nunca se hacen en el hilo principal. Los ViewModel las lanzan con `viewModelScope.launch(Dispatchers.IO)`.
- **Dependencias:** no agregar librerías nuevas sin acordarlo con el equipo. La única librería externa aprobada para imágenes es Coil.
- **Nombres:** clases en `PascalCase`, funciones y variables en `camelCase`, ids de XML en `snake_case` (por ejemplo `btn_llamar`, `txt_nombre`).
- **Fechas:** en la base `yyyy-MM-dd` y las horas `HH:mm`; en pantalla `dd/MM/yyyy`. La conversión se hace en `FechaUtils`.
- **Commits:** uno por tarea o grupo pequeño de tareas, en español y en imperativo. Ejemplo: "Agrega formulario de mascota con validación".

---

## Arquitectura

Es un MVVM sencillo:

```
Actividad / Fragmento  --observa-->  ViewModel  --llama-->  DAO  --SQL-->  DbHelper (patitas.db)
   (muestra datos)                  (conserva datos       (único lugar
                                     al rotar)              con SQL)
```

- **Modelos** (`model/`): `data class` que representan una fila de cada tabla.
- **DAOs** (`data/`): convierten objetos en filas (`ContentValues`) y filas en objetos (`Cursor`). Son el único lugar donde se escribe SQL.
- **ViewModel** (`viewmodel/`): pide los datos a los DAOs en un hilo de fondo y los publica en `LiveData`. Sobrevive a la rotación de pantalla.
- **Interfaz** (`ui/`): actividades, fragmentos, adaptadores y diálogos. Solo observan el `LiveData` y le avisan al ViewModel lo que hace el usuario.

### Estructura de paquetes

Los archivos marcados como "pendiente" todavía no existen.

```
app/src/main/java/com/example/patitasaldia/
├── data/
│   ├── DbHelper.kt            Crea la base y las tablas
│   ├── MascotaDao.kt
│   ├── VacunaDao.kt
│   ├── CitaDao.kt
│   ├── CursorExt.kt           Funciones para leer columnas que pueden ser NULL
│   └── DatosPrueba.kt         Mascotas de ejemplo (solo en depuración)
├── model/
│   ├── Mascota.kt
│   ├── Vacuna.kt
│   └── Cita.kt
├── ui/
│   ├── main/
│   │   ├── MainActivity.kt
│   │   ├── ListaMascotasFragment.kt
│   │   └── MascotaAdapter.kt
│   ├── detalle/                (pendiente, fase 4)
│   │   ├── DetalleMascotaActivity.kt
│   │   ├── DetalleMascotaFragment.kt
│   │   ├── VacunaAdapter.kt
│   │   └── CitaAdapter.kt
│   ├── form/
│   │   └── FormMascotaActivity.kt   (esqueleto; se completa en la fase 3)
│   └── dialogs/                (pendiente, fase 4)
│       ├── NuevaVacunaDialog.kt
│       └── NuevaCitaDialog.kt
├── viewmodel/
│   ├── MascotasViewModel.kt
│   └── DetalleMascotaViewModel.kt   (pendiente, fase 4)
└── util/                       (pendiente)
    ├── FechaUtils.kt           (fase 3)
    ├── FotoUtils.kt            (fase 7)
    └── ComunicacionUtils.kt    (fase 8)
```

### Pantallas y navegación

| Pantalla | Tipo | Contenido |
|---|---|---|
| `MainActivity` | Actividad | Contiene `ListaMascotasFragment` y, en la tableta, también `DetalleMascotaFragment`. Botón flotante (+) para agregar |
| `DetalleMascotaActivity` | Actividad (solo teléfono) | Contiene `DetalleMascotaFragment` |
| `FormMascotaActivity` | Actividad | Formulario para agregar o editar una mascota |
| `ListaMascotasFragment` | Fragmento | `RecyclerView` con las mascotas |
| `DetalleMascotaFragment` | Fragmento | Foto, datos, botones de comunicación, vacunas y citas |
| `NuevaVacunaDialog` | `DialogFragment` | Formulario de vacuna (agregar y editar) |
| `NuevaCitaDialog` | `DialogFragment` | Formulario de cita (agregar y editar) |

```
MainActivity (lista)
 ├── (+) ──────────────────────────> FormMascotaActivity (nueva)
 └── toque en una mascota
      ├── Teléfono ───> DetalleMascotaActivity ──> DetalleMascotaFragment
      └── Tableta  ───> DetalleMascotaFragment en el panel derecho

DetalleMascotaFragment
 ├── Editar ───────> FormMascotaActivity (edición, recibe el id)
 ├── Eliminar ─────> diálogo de confirmación
 ├── + Vacuna ─────> NuevaVacunaDialog
 ├── + Cita ───────> NuevaCitaDialog
 └── Llamar / SMS / Correo ──> Intents implícitos
```

---

## Fase 0. Preparación

**Estado: Terminada.**

**Objetivo:** dejar el proyecto listo para programar: lenguaje, dependencias, View Binding y tema visual.

- [x] Crear el proyecto con la plantilla **Empty Views Activity**, nombre "Patitas al Día" y paquete `com.example.patitasaldia`
- [x] Crear dos emuladores: un teléfono (Pixel) y una tableta (Pixel Tablet)
- [x] Inicializar el repositorio Git con un `.gitignore` de Android
- [x] Agregar las dependencias: `lifecycle-viewmodel-ktx`, `lifecycle-livedata-ktx`, `fragment-ktx`, `activity-ktx`, `recyclerview`, `material` y `coil`
- [x] Activar View Binding en `app/build.gradle.kts`
- [x] Definir colores, tema y `strings.xml` base

**Qué se hizo:**
- El código pasó de Java a Kotlin. AGP 9 ya compila Kotlin, así que no hace falta un plugin extra.
- Las versiones de las dependencias están en `gradle/libs.versions.toml`. Coil está en la versión 2.x (`io.coil-kt:coil`).
- Paleta de colores: naranja como color principal y verde azulado como secundario, con variantes para modo claro (`values/themes.xml`) y oscuro (`values-night/themes.xml`). Ya están definidos los colores `aviso_vencida` y `aviso_proxima` para la fase 4.

---

## Fase 1. Modelo y base de datos

**Estado: Terminada.**

**Objetivo:** crear la capa de datos (requisito 6). Las demás fases leen y guardan información solo a través de los DAOs.

- [x] Crear las clases de datos `Mascota`, `Vacuna` y `Cita` en el paquete `model`
- [x] Crear `DbHelper` con las 3 tablas, llaves foráneas activadas y `onUpgrade`
- [x] Crear `MascotaDao`, `VacunaDao` y `CitaDao`
- [x] Insertar 3 mascotas de prueba (solo en modo depuración) y verificarlas con el Database Inspector

### Base de datos

- Archivo `patitas.db`, versión 1.
- Las llaves foráneas se activan en `onConfigure` con `setForeignKeyConstraintsEnabled(true)`. Gracias a esto, al borrar una mascota se borran solas sus vacunas y citas (`ON DELETE CASCADE`).
- Las fechas se guardan como texto `yyyy-MM-dd` y las horas como `HH:mm`. En ese formato, ordenar como texto da el mismo resultado que ordenar como fecha.
- La foto **no** se guarda en la base, solo la ruta del archivo.
- `DbHelper` se usa como una sola instancia para toda la app: `DbHelper.obtenerInstancia(context)`.
- Los nombres de tablas y columnas están en los objetos `TablaMascotas`, `TablaVacunas` y `TablaCitas`, dentro de `DbHelper.kt`. Úsalos en lugar de escribir los nombres a mano.

```sql
CREATE TABLE mascotas (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    especie TEXT NOT NULL,          -- Perro, Gato, Ave, Otro
    raza TEXT, sexo TEXT,           -- sexo: Macho, Hembra
    fecha_nacimiento TEXT,          -- yyyy-MM-dd
    peso REAL,                      -- kg
    foto_ruta TEXT,
    vet_nombre TEXT, vet_telefono TEXT, vet_correo TEXT
);
CREATE TABLE vacunas (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    mascota_id INTEGER NOT NULL,
    nombre TEXT NOT NULL,
    fecha_aplicacion TEXT NOT NULL, -- yyyy-MM-dd
    proxima_dosis TEXT,             -- yyyy-MM-dd
    notas TEXT,
    FOREIGN KEY (mascota_id) REFERENCES mascotas(id) ON DELETE CASCADE
);
CREATE TABLE citas (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    mascota_id INTEGER NOT NULL,
    fecha TEXT NOT NULL,            -- yyyy-MM-dd
    hora TEXT,                      -- HH:mm
    motivo TEXT NOT NULL,
    notas TEXT,
    FOREIGN KEY (mascota_id) REFERENCES mascotas(id) ON DELETE CASCADE
);
```

### Métodos de los DAOs

| DAO | Métodos | Orden de las consultas |
|---|---|---|
| `MascotaDao` | `insertar`, `actualizar`, `eliminar(id)`, `obtenerTodas`, `obtenerPorId` | Por nombre, sin distinguir mayúsculas |
| `VacunaDao` | `insertar`, `actualizar`, `eliminar(id)`, `obtenerPorMascota` | De la más reciente a la más antigua |
| `CitaDao` | `insertar`, `actualizar`, `eliminar(id)`, `obtenerPorMascota` | Por fecha y hora, de la más antigua a la más nueva |

- `insertar` regresa el id nuevo, o `-1` si falló (por ejemplo, si la mascota no existe).
- `actualizar` y `eliminar` regresan cuántas filas cambiaron.
- Todas las consultas usan parámetros `?` y cierran el `Cursor` con `use { }`.

### Datos de prueba

`DatosPrueba.kt` inserta a Firulais (perro), Michi (gato) y Kiwi (ave), solo si la app es de depuración. Las fechas se calculan a partir del día en que se crea la base, para que siempre existan:
- una vacuna vencida y otra que vence en menos de 30 días (Firulais);
- citas pasadas y futuras;
- una mascota sin datos del veterinario (Kiwi), para probar que los botones de comunicación se deshabilitan.

### Cómo se prueba

- `app/src/androidTest/.../data/DaosTest.kt` tiene 8 pruebas: guardar y leer todos los campos, campos vacíos, actualizar, orden, borrado en cascada y llave foránea. Corren con `./gradlew connectedDebugAndroidTest` y usan una base en memoria, sin tocar `patitas.db`.
- Con la app corriendo: **View > Tool Windows > App Inspection > Database Inspector** y abrir `patitas.db`.

---

## Fase 2. Lista de mascotas

**Estado: Terminada.**

**Objetivo:** pantalla principal con la lista de mascotas guardadas.

- [x] Crear `MascotasViewModel` que cargue la lista desde `MascotaDao`
- [x] Crear `ListaMascotasFragment` con `RecyclerView` y `MascotaAdapter` (`ListAdapter` con `DiffUtil`)
- [x] Diseñar `item_mascota.xml` (foto circular, nombre y especie)
- [x] Mostrar un mensaje cuando no hay mascotas registradas
- [x] Botón flotante (+) que abra `FormMascotaActivity`

**Cómo funciona:**
- `MascotasViewModel` (hereda de `AndroidViewModel`) lee las mascotas en un hilo de fondo y las publica en `mascotas: LiveData<List<Mascota>>`.
- `ListaMascotasFragment` obtiene el ViewModel con `activityViewModels()`, así que es el mismo objeto que usa `MainActivity`. Esto servirá en la fase 5 para guardar la mascota seleccionada. El fragmento recarga la lista en `onResume`, para que se actualice al regresar del formulario o del detalle.
- `MascotaAdapter` usa `DiffUtil`: solo redibuja las filas que cambiaron.
- Cuando el usuario toca una mascota, el fragmento llama a `onMascotaSeleccionada(mascota)` en la actividad, a través de la interfaz `ListaMascotasFragment.AlSeleccionarMascota`.
- `activity_main.xml` usa `CoordinatorLayout` con barra superior (`MaterialToolbar`), un `FragmentContainerView` (id `contenedor_lista`) y el botón flotante (id `fab_agregar`).

**Lo que queda para otras fases (no son pendientes de esta):**
- `MainActivity.onMascotaSeleccionada` está vacío. Abrir el detalle se hace en la fase 4 (teléfono) y la fase 5 (tableta).
- Todas las mascotas muestran una huella (`ic_huella`). La foto real y el ícono por especie son de la fase 7. Al cargar una foto, hay que quitar el `tint` y el `padding` del `ImageView`, que solo sirven para el ícono.

---

## Fase 3. Formulario de mascota

**Estado: Pendiente.** Ya existe el esqueleto: `FormMascotaActivity` muestra la barra superior con el título correcto ("Nueva mascota" o "Editar mascota") y la flecha para regresar. Falta todo el formulario.

**Objetivo:** agregar y editar mascotas.

- [ ] Crear `FormMascotaActivity` en modo alta (sin id) y edición (con id)
- [ ] Selector de especie y de sexo (spinner o chips)
- [ ] Selector de fecha de nacimiento con `MaterialDatePicker`
- [ ] Validar campos obligatorios (nombre y especie), formato del correo y del teléfono
- [ ] Guardar en la base de datos y regresar a la lista actualizada
- [ ] Botones de cámara y galería sin funcionalidad todavía (se completan en la fase 7)

**Cómo hacerlo:**
- Para abrir el formulario usa siempre `FormMascotaActivity.crearIntent(context)` (alta) o `FormMascotaActivity.crearIntent(context, mascotaId)` (edición). Dentro de la actividad, la variable `mascotaId` es `null` en modo alta.
- Campos: nombre, especie (Perro, Gato, Ave, Otro), raza, sexo (Macho, Hembra), fecha de nacimiento, peso en kg, y datos del veterinario (nombre, teléfono y correo). Usa `TextInputLayout` de Material para mostrar los errores debajo de cada campo.
- Crea `util/FechaUtils.kt` con la conversión `yyyy-MM-dd` (base) a `dd/MM/yyyy` (pantalla) y al revés. `MaterialDatePicker` regresa milisegundos en UTC; conviértelos con zona horaria UTC para que no se recorra un día.
- Validación: el correo con `Patterns.EMAIL_ADDRESS` y el teléfono con `Patterns.PHONE` (o mínimo 10 dígitos). Ambos son opcionales, pero si se escriben deben ser válidos.
- Conviene crear un ViewModel para el formulario (por ejemplo `FormMascotaViewModel`) que cargue la mascota en modo edición y guarde con `MascotaDao.insertar` o `actualizar` en `Dispatchers.IO`. Al terminar, cerrar la actividad con `finish()`. La lista se actualiza sola porque recarga en `onResume`.
- Los textos de etiquetas, errores y opciones van en `strings.xml`. Las opciones de especie y sexo pueden ir en un `string-array`.
- Para la fase 5 se necesitará `layout-land/activity_form_mascota.xml`: conviene poner los campos dentro de un `NestedScrollView` desde ahora.

**Cómo probar:** agregar una mascota nueva, ver que aparezca en la lista, intentar guardar sin nombre (debe mostrar error) y escribir un correo inválido.

---

## Fase 4. Detalle en teléfono

**Estado: Pendiente.**

**Objetivo:** ver toda la información de una mascota y administrar sus vacunas y citas.

- [ ] Crear `DetalleMascotaViewModel` que cargue la mascota, sus vacunas y sus citas
- [ ] Crear `DetalleMascotaFragment` con los datos de la mascota
- [ ] Crear `DetalleMascotaActivity` que contenga el fragmento y reciba el id
- [ ] Lista de vacunas con `VacunaAdapter`, resaltando las vencidas o próximas
- [ ] Lista de citas con `CitaAdapter`, separando próximas y pasadas
- [ ] `NuevaVacunaDialog` y `NuevaCitaDialog` para agregar y editar
- [ ] Eliminar vacunas y citas (pulsación larga o deslizando) y eliminar la mascota con confirmación
- [ ] Botón para editar la mascota

**Cómo hacerlo:**
- En `MainActivity.onMascotaSeleccionada`, abrir `DetalleMascotaActivity` pasándole el id con un extra.
- `DetalleMascotaFragment` debe recibir el id por `arguments` (crearlo con una función `newInstance(id)`), porque en la fase 5 se reutiliza en el panel derecho de la tableta.
- Vacunas: resaltar con `aviso_vencida` si `proxima_dosis` ya pasó, y con `aviso_proxima` si faltan menos de 30 días.
- Citas: separar próximas (fecha y hora mayores o iguales a hoy) de pasadas. La separación se hace en el ViewModel, no en SQL.
- Los diálogos se muestran con `show(supportFragmentManager, TAG)` para que se recreen solos al rotar. Sirven tanto para agregar como para editar (reciben el id de la vacuna o cita, si existe).
- Al eliminar una mascota, mostrar un `MaterialAlertDialogBuilder` de confirmación. Las vacunas y citas se borran solas por el `ON DELETE CASCADE`.
- Botón Editar: `FormMascotaActivity.crearIntent(context, id)`. Al regresar, recargar el detalle en `onResume`.
- Dejar los botones de Llamar, SMS y Correo en el layout; su funcionalidad es de la fase 8.

**Cómo probar:** con los datos de prueba, Firulais debe mostrar una vacuna resaltada como vencida y otra como próxima, una cita pasada y una próxima.

---

## Fase 5. Tableta y orientación

**Estado: Pendiente.**

**Objetivo:** aprovechar la pantalla grande de la tableta (requisito 3) y adaptar las pantallas en horizontal (requisito 4).

- [ ] Crear `layout-sw600dp/activity_main.xml` con los dos paneles
- [ ] En `MainActivity`, detectar si hay dos paneles y mostrar el detalle en el panel derecho
- [ ] Mensaje "Selecciona una mascota" cuando no hay selección en la tableta
- [ ] Resaltar en la lista la mascota seleccionada (solo en la tableta)
- [ ] Crear `layout-land/fragment_detalle_mascota.xml`
- [ ] Crear `layout-land/activity_form_mascota.xml`
- [ ] Al pasar de tableta horizontal a vertical, o al eliminar la mascota seleccionada, que el panel derecho no quede con datos viejos
- [ ] Probar en teléfono vertical, teléfono horizontal, tableta vertical y tableta horizontal

**Cómo hacerlo:**

```
TELÉFONO (vertical)          TABLETA (sw600dp)
┌──────────────┐             ┌────────────┬─────────────────────┐
│ Lista        │  toque →    │ Lista      │ Detalle de la       │
│ (Main)       │  abre       │ (~1/3)     │ mascota elegida     │
│              │  Detalle    │            │ (~2/3)              │
└──────────────┘  Activity   └────────────┴─────────────────────┘
```

- `layout-sw600dp/activity_main.xml` lleva el mismo `contenedor_lista` más un contenedor con id `contenedor_detalle`.
- Detección: después de `setContentView`, si existe `R.id.contenedor_detalle` la app está en una tableta y el detalle se pone en ese contenedor con `supportFragmentManager.commit { replace(...) }`. Si no existe, se abre `DetalleMascotaActivity`. **No** se revisa el tamaño de pantalla en código: Android elige el layout correcto.
- El id de la mascota seleccionada se guarda en `MascotasViewModel`, que ya comparten la actividad y la lista.
- `layout-land/fragment_detalle_mascota.xml`: foto y datos a la izquierda; vacunas y citas a la derecha.
- `layout-land/activity_form_mascota.xml`: foto a la izquierda y campos a la derecha.

---

## Fase 6. Guardar el estado

**Estado: Pendiente.**

**Objetivo:** que no se pierda nada al rotar ni cuando Android cierra la app en segundo plano (requisito 5).

- [ ] Confirmar que los ViewModel conservan los datos al rotar
- [ ] Guardar el id seleccionado con `SavedStateHandle` u `onSaveInstanceState`
- [ ] Guardar el estado del formulario (foto temporal, fecha elegida, modo alta o edición)
- [ ] Probar con "No conservar actividades" activado en todas las pantallas

**Dos casos distintos:**

| Caso | Qué pasa | Solución |
|---|---|---|
| Rotación | La actividad se destruye y se vuelve a crear | `ViewModel`: conserva la lista, la selección, las vacunas y las citas |
| Android cierra la app en segundo plano | Se pierde todo lo que está en memoria, incluido el ViewModel | `SavedStateHandle` u `onSaveInstanceState`: guardar el id seleccionado y los datos del formulario |

Qué se guarda en cada pantalla:
- `MainActivity`: id de la mascota seleccionada (tableta).
- `DetalleMascotaActivity`: id de la mascota.
- `FormMascotaActivity`: la ruta de la foto temporal, la fecha elegida y el modo (alta o edición). Los `EditText` con id guardan su texto solos; la foto y los valores de los selectores se guardan a mano.
- Diálogos: se recrean solos si se mostraron con `show(supportFragmentManager, TAG)`.

**Prueba obligatoria:** en el emulador, activar **Opciones de desarrollador > No conservar actividades**. Llenar medio formulario, cambiar a otra app, regresar y comprobar que los datos siguen ahí.

---

## Fase 7. Foto de la mascota

**Estado: Pendiente.**

**Objetivo:** usar la cámara y la galería del dispositivo (requisito 7).

- [ ] Configurar el `FileProvider` en el manifiesto y `res/xml/file_paths.xml`
- [ ] Crear `FotoUtils` (crear archivo, copiar desde la galería, borrar archivo)
- [ ] Tomar foto con la cámara
- [ ] Elegir foto de la galería
- [ ] Mostrar la foto con Coil en la lista, el detalle y el formulario
- [ ] Ícono por defecto según la especie si no hay foto
- [ ] Borrar el archivo de la foto al cambiarla o al eliminar la mascota

**Cómo hacerlo:**
- **Cámara:** `registerForActivityResult(ActivityResultContracts.TakePicture())`. Antes, crear un archivo en `filesDir/fotos/mascota_<timestamp>.jpg`, obtener su `Uri` con `FileProvider.getUriForFile(...)` y pasárselo a la cámara. Si el resultado es `false`, borrar el archivo vacío.
- **Galería:** `registerForActivityResult(ActivityResultContracts.PickVisualMedia())` con `ImageOnly`. No requiere permisos. Copiar la imagen a `filesDir/fotos/`, porque el `Uri` original puede dejar de funcionar.
- **Manifiesto:**
  - `<provider>` con `android:authorities="${applicationId}.fileprovider"`.
  - `res/xml/file_paths.xml` con `<files-path name="fotos" path="fotos/" />`.
  - `<uses-feature android:name="android.hardware.camera" android:required="false" />`, para que la app se pueda instalar en tabletas sin cámara.
- **Permisos:** `TakePicture` no necesita el permiso `CAMERA` mientras no se declare en el manifiesto. Si se declara, hay que pedirlo en tiempo de ejecución.
- **Mostrar:** Coil (`imageView.load(File(ruta))`). En `item_mascota.xml`, al cargar una foto quitar el `tint` y el `padding` del `ImageView`. Si no hay foto, mostrar un ícono según la especie (crearlos en `drawable/`).
- **Borrar la foto al eliminar la mascota:** esto todavía no está hecho en `MascotaDao.eliminar`. Hay que borrar el archivo con `FotoUtils` desde el ViewModel, antes o después de eliminar la fila.

---

## Fase 8. Comunicación con el veterinario

**Estado: Pendiente.**

**Objetivo:** llamar, enviar SMS y enviar el historial de vacunas por correo (requisito 8).

- [ ] Crear `ComunicacionUtils` con las funciones de llamada, SMS y correo
- [ ] Armar el texto del historial de vacunas para el correo
- [ ] Agregar los botones en el detalle y deshabilitarlos si faltan datos del veterinario
- [ ] Manejar `ActivityNotFoundException`
- [ ] Probar en el emulador del teléfono y en el de la tableta

**Cómo hacerlo:** todo con `Intent` implícitos; ninguno requiere permisos.

| Acción | Intent | Detalle |
|---|---|---|
| Llamar | `Intent(Intent.ACTION_DIAL, Uri.parse("tel:$telefono"))` | Solo abre el marcador, no llama directamente |
| SMS | `Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$telefono"))` con el extra `"sms_body"` | Texto: "Hola, quisiera agendar una cita para mi mascota <nombre>." |
| Correo | `Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))` con `EXTRA_EMAIL`, `EXTRA_SUBJECT` y `EXTRA_TEXT` | Asunto: "Historial de vacunas de <nombre>". Cuerpo: datos de la mascota y lista de vacunas con fechas |

- Si falta el teléfono o el correo del veterinario, deshabilitar el botón correspondiente. Kiwi, en los datos de prueba, no tiene veterinario.
- Envolver cada `startActivity` en `try/catch (ActivityNotFoundException)` y mostrar un `Toast` si no hay una app que lo maneje (por ejemplo, una tableta sin SMS).
- En Android 11 o superior, agregar al manifiesto un bloque `<queries>` para `tel`, `smsto` y `mailto`, o simplemente intentar lanzar el intent y capturar la excepción.

---

## Fase 9. Pulido

**Estado: Pendiente.**

- [ ] Ícono de la app (Image Asset Studio)
- [ ] Revisar colores, márgenes y tamaños de texto
- [ ] Revisar que no haya textos fijos fuera de `strings.xml`
- [ ] Revisar que la app no se cierre con campos vacíos, sin foto o sin datos del veterinario
- [ ] Quitar los datos de prueba o dejarlos detrás de una bandera
- [ ] Generar el APK de depuración y probarlo en un dispositivo físico si es posible

**Nota:** los datos de prueba ya están detrás de una bandera: solo se insertan si la app es de depuración (`FLAG_DEBUGGABLE`). La versión release no los incluye. Para desactivarlos también en depuración, cambiar el valor por defecto de `insertarDatosPrueba` en `DbHelper`.

---

## Fase 10. Entregables

**Estado: Pendiente.**

- [ ] Manual de usuario
- [ ] Video de demostración
- [ ] Comprimir el proyecto sin las carpetas `build/` ni `.gradle/`

### Manual de usuario

En español, con acentos y la letra ñ correctas:

1. Portada (UABC, materia, nombres, matrículas, grupo y fecha)
2. Introducción: qué es Patitas al Día y para qué sirve
3. Requisitos para instalar la app (Android 7.0 o superior)
4. Instalación
5. Uso, con capturas de pantalla:
   - Agregar una mascota (con foto de la cámara y de la galería)
   - Ver, editar y eliminar una mascota
   - Registrar vacunas y citas
   - Contactar al veterinario (llamada, SMS y correo)
6. Uso en tableta y en orientación horizontal
7. Preguntas frecuentes y solución de problemas

### Video (3 a 5 minutos)

1. Presentación de la app
2. Agregar una mascota tomando una foto con la cámara
3. Agregar otra con una foto de la galería
4. Registrar vacunas y citas, mostrando el aviso de vacuna próxima
5. Rotar el teléfono en varias pantallas, incluido el formulario a medio llenar
6. Salir de la app y regresar para mostrar que el estado se conserva
7. Mostrar la app en la tableta con los dos paneles, en vertical y en horizontal
8. Llamar, mandar SMS y enviar el historial de vacunas por correo
9. Cierre
