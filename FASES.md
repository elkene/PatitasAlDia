# Patitas al Día: plan de trabajo por fases

Aplicación Android para llevar el registro de mascotas, sus vacunas y sus citas al veterinario. Proyecto final de Aplicaciones Móviles, Ingeniería en Software y Tecnologías Emergentes, UABC Campus Otay.

Este documento explica qué hace cada fase, qué archivos involucra, cómo se probó y en qué estado está. El diseño (casos de uso, navegación y distribución de pantallas) es el del documento **M5.2 Diseño del Proyecto**.

---

## Resumen del estado

| Fase | Nombre | Estado |
|---|---|---|
| 0 | Preparación | Terminada |
| 1 | Modelo y base de datos | Terminada |
| 2 | Lista de mascotas | Terminada |
| 3 | Formulario de mascota | Terminada |
| 4 | Detalle en teléfono | Terminada |
| 5 | Tableta y orientación | Terminada |
| 6 | Guardar el estado | Terminada |
| 7 | Foto de la mascota | Terminada |
| 8 | Comunicación con el veterinario | Terminada |
| 9 | Pulido | Terminada |
| 10 | Entregables | **En progreso**: borrador del manual hecho (fuera del repositorio); faltan los datos de la portada, el video y el archivo comprimido |

Las fases 0 a 9 compilan, pasan sus pruebas automáticas y se probaron en un teléfono físico (Android 17) y en el emulador Pixel Tablet (Android 15). Lo único que falta es la fase 10.

---

## Requisitos del proyecto

| # | Requisito | Cómo se cumple | Fase | Estado |
|---|---|---|---|---|
| 1 | Java o Kotlin en Android Studio | Kotlin | 0 | Cumplido |
| 2 | Al menos 2 pantallas con 2 actividades | `MainActivity`, `DetalleMascotaActivity` y `FormMascotaActivity`, con fragmentos de lista y detalle | 2, 3 y 4 | Cumplido |
| 3 | Funcionar en teléfono y tableta aprovechando la pantalla | Patrón lista-detalle: en la tableta la lista y el detalle se ven lado a lado | 5 | Cumplido |
| 4 | Funcionar en ambas orientaciones | Layouts `layout-land` para el detalle y el formulario | 5 | Cumplido |
| 5 | Guardar el estado al rotar o al salir y regresar | `ViewModel` + `SavedStateHandle`; diálogos como `DialogFragment` | 6 | Cumplido |
| 6 | Guardar los datos en SQLite | `DbHelper` (`SQLiteOpenHelper`) con 3 tablas y SQL escrito a mano | 1 | Cumplido |
| 7 | Usar una característica del dispositivo | Cámara (`TakePicture` + `FileProvider`) y galería (`PickVisualMedia`) | 7 | Cumplido |
| 8 | Usar algún tipo de comunicación | Llamada, SMS y correo con el historial de vacunas (Intents implícitos) | 8 | Cumplido |
| 9 | Proyecto, manual de usuario y video | Ver fase 10 | 10 | **En progreso** |

---

## Cómo abrir y correr el proyecto

1. Clonar el repositorio y abrirlo en Android Studio (versión reciente, con AGP 9).
2. Esperar a que Gradle sincronice.
3. Crear, si no existen, dos emuladores en el Device Manager: un teléfono (Pixel) y una tableta (Pixel Tablet).
4. Correr la app con el botón Run.
5. Pruebas:
   - Pruebas unitarias (en la computadora): `./gradlew testDebugUnitTest`
   - Pruebas instrumentadas (en un emulador encendido): `./gradlew connectedDebugAndroidTest`

**Cuidado:** `connectedDebugAndroidTest` desinstala la app al terminar, y con eso se borran sus datos. Si tienes un teléfono personal conectado, corre las pruebas solo en el emulador:
- Windows PowerShell: `$env:ANDROID_SERIAL="emulator-5554"; ./gradlew connectedDebugAndroidTest`
- Git Bash: `ANDROID_SERIAL=emulator-5554 ./gradlew connectedDebugAndroidTest`

En modo depuración, la primera vez que se abre la app crea tres mascotas de ejemplo (Firulais, Michi y Kiwi) con vacunas y citas. Para empezar con la base vacía o volver a generar los datos, desinstala la app.

---

## Reglas técnicas del proyecto

- **Lenguaje:** Kotlin. **Interfaz:** Views con layouts XML, actividades y fragmentos. **No** se usa Jetpack Compose.
- **Base de datos:** `SQLiteOpenHelper` con SQL escrito a mano. **No** se usa Room (lo exige el requisito 6).
- **View Binding** en todas las actividades, fragmentos, adaptadores y diálogos. No se usa `findViewById`.
- **Textos:** todos en `res/values/strings.xml`. Colores en `colors.xml` y medidas en `dimens.xml`. Lint lo verifica (sin avisos).
- **Idioma:** comentarios, textos de la interfaz y mensajes de commit en español.
- **Hilos:** las operaciones de base de datos nunca se hacen en el hilo principal: los ViewModel usan `viewModelScope.launch(Dispatchers.IO)` o `withContext(Dispatchers.IO)`.
- **Dependencias:** no agregar librerías sin acordarlo con el equipo. La única externa para imágenes es Coil.
- **Nombres:** clases en `PascalCase`, funciones y variables en `camelCase`, ids de XML en `snake_case`.
- **Fechas:** en la base `yyyy-MM-dd` y horas `HH:mm`; en pantalla `dd/MM/yyyy`. La conversión está en `FechaUtils`.
- **Commits:** en español y en imperativo. Ejemplo: "Agrega formulario de mascota con validación".

---

## Arquitectura

Es un MVVM sencillo:

```
Actividad / Fragmento  --observa-->  ViewModel  --llama-->  DAO  --SQL-->  DbHelper (patitas.db)
   (muestra datos)                  (conserva datos       (único lugar
                                     al rotar)              con SQL)
```

- **Modelos** (`model/`): `data class` que representan una fila de cada tabla, más los catálogos de especie y sexo y el estado de las vacunas.
- **DAOs** (`data/`): convierten objetos en filas (`ContentValues`) y filas en objetos (`Cursor`). Son el único lugar con SQL.
- **ViewModel** (`viewmodel/`): pide los datos a los DAOs en un hilo de fondo y los publica en `LiveData`. Sobrevive a la rotación; lo que debe sobrevivir a que Android cierre la app va en su `SavedStateHandle`.
- **Interfaz** (`ui/`): actividades, fragmentos, adaptadores y diálogos. Solo observan el `LiveData` y avisan al ViewModel lo que hace el usuario.
- **Utilidades** (`util/`): fechas, validaciones, fotos, comunicación y selectores de fecha y hora.

### Estructura de paquetes

```
app/src/main/java/com/example/patitasaldia/
├── data/
│   ├── DbHelper.kt              Crea la base y las tablas (nombres de columnas en TablaMascotas, etc.)
│   ├── MascotaDao.kt
│   ├── VacunaDao.kt
│   ├── CitaDao.kt
│   ├── CursorExt.kt             Leer columnas que pueden ser NULL
│   └── DatosPrueba.kt           Mascotas de ejemplo (solo en depuración)
├── model/
│   ├── Mascota.kt, Vacuna.kt, Cita.kt
│   ├── Catalogos.kt             Valores de especie y sexo que se guardan en la base
│   └── EstadoVacuna.kt          Vencida, próxima (< 30 días), al día o sin próxima dosis
├── ui/
│   ├── main/
│   │   ├── MainActivity.kt          Lista y, en tableta, el panel de detalle
│   │   ├── ListaMascotasFragment.kt
│   │   └── MascotaAdapter.kt
│   ├── detalle/
│   │   ├── DetalleMascotaActivity.kt  Solo teléfono
│   │   ├── DetalleMascotaFragment.kt  Teléfono y tableta
│   │   ├── VacunaAdapter.kt
│   │   └── CitaAdapter.kt
│   ├── form/
│   │   └── FormMascotaActivity.kt
│   └── dialogs/
│       ├── NuevaVacunaDialog.kt
│       ├── NuevaCitaDialog.kt
│       └── ConfirmarEliminarDialog.kt
├── viewmodel/
│   ├── MascotasViewModel.kt
│   ├── DetalleMascotaViewModel.kt
│   └── FormMascotaViewModel.kt
└── util/
    ├── FechaUtils.kt            yyyy-MM-dd <-> dd/MM/yyyy, días entre fechas, edad
    ├── Validaciones.kt          Teléfono, correo y peso
    ├── OrganizadorCitas.kt      Separa citas próximas y pasadas
    ├── FotoUtils.kt             Crear, copiar y borrar archivos de foto
    ├── ImagenMascota.kt         Muestra la foto con Coil o el ícono de la especie
    ├── ComunicacionUtils.kt     Llamar, SMS y correo
    ├── Selectores.kt            Calendario y reloj de Material (y reconexión al rotar)
    ├── TextosCatalogo.kt        Texto e ícono de cada especie y sexo
    ├── CamposTexto.kt           Mostrar la X de borrar solo si el campo tiene texto
    └── Insets.kt                Espacio para barras del sistema y teclado

app/src/main/res/
├── layout/            Teléfono vertical (y piezas reutilizables include_*.xml)
├── layout-land/       Teléfono horizontal: detalle y formulario en dos columnas
├── layout-sw600dp/    Tableta: pantalla principal con dos paneles y detalle ancho
├── drawable/          Íconos (Material Design), íconos por especie e ícono de la app
├── menu/              Acciones de la barra superior
├── mipmap-anydpi*/    Ícono de la app (adaptativo y para Android 7)
├── values/            strings, colors, dimens, themes
├── values-night/      Tema y colores de aviso en modo oscuro
└── xml/file_paths.xml Carpeta que el FileProvider comparte con la cámara
```

### Pantallas y navegación

| Desde | Acción | Hacia | Cómo |
|---|---|---|---|
| Lista | Botón (+) | Formulario (alta) | Intent explícito sin id |
| Lista | Toque (teléfono) | `DetalleMascotaActivity` | Intent explícito con el id |
| Lista | Toque (tableta) | Panel derecho | Se reemplaza `DetalleMascotaFragment` en `contenedor_detalle` |
| Detalle | Editar (lápiz) | Formulario (edición) | Intent explícito con el id |
| Detalle | Agregar vacuna o cita | Diálogo | `DialogFragment` sobre la misma pantalla |
| Detalle | Eliminar (basura) | Diálogo de confirmación | `ConfirmarEliminarDialog` + Fragment Result API |
| Detalle | Llamar, SMS o correo | App del sistema | Intent implícito |

---

## Fase 0. Preparación

**Estado: Terminada.**

- [x] Crear el proyecto con la plantilla **Empty Views Activity**, nombre "Patitas al Día" y paquete `com.example.patitasaldia`
- [x] Crear dos emuladores: un teléfono (Pixel) y una tableta (Pixel Tablet)
- [x] Inicializar el repositorio Git con un `.gitignore` de Android
- [x] Agregar las dependencias: `lifecycle-viewmodel-ktx`, `lifecycle-livedata-ktx`, `fragment-ktx`, `activity-ktx`, `recyclerview`, `material` y `coil`
- [x] Activar View Binding en `app/build.gradle.kts`
- [x] Definir colores, tema y `strings.xml` base

**Notas:**
- AGP 9 ya compila Kotlin, así que no hace falta un plugin extra.
- Las versiones de las dependencias están en `gradle/libs.versions.toml`. Coil está en la versión 2.x (`io.coil-kt:coil`).
- Paleta: naranja como color principal y verde azulado como secundario, con modo claro y oscuro.

---

## Fase 1. Modelo y base de datos

**Estado: Terminada.**

- [x] Crear las clases de datos `Mascota`, `Vacuna` y `Cita`
- [x] Crear `DbHelper` con las 3 tablas, llaves foráneas activadas y `onUpgrade`
- [x] Crear `MascotaDao`, `VacunaDao` y `CitaDao`
- [x] Insertar 3 mascotas de prueba (solo en depuración) y verificarlas con el Database Inspector

### Base de datos

- Archivo `patitas.db`, versión 1.
- Las llaves foráneas se activan en `onConfigure`. Gracias a eso, al borrar una mascota se borran solas sus vacunas y citas (`ON DELETE CASCADE`).
- Las fechas se guardan como texto `yyyy-MM-dd`: ordenarlas como texto da el mismo resultado que ordenarlas como fecha.
- La foto **no** se guarda en la base: solo la ruta del archivo.
- `DbHelper` es una sola instancia para toda la app: `DbHelper.obtenerInstancia(context)`.

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

| DAO | Métodos | Orden de las consultas |
|---|---|---|
| `MascotaDao` | `insertar`, `actualizar`, `eliminar(id)`, `obtenerTodas`, `obtenerPorId` | Por nombre, sin distinguir mayúsculas |
| `VacunaDao` | `insertar`, `actualizar`, `eliminar(id)`, `obtenerPorMascota` | De la más reciente a la más antigua |
| `CitaDao` | `insertar`, `actualizar`, `eliminar(id)`, `obtenerPorMascota` | Por fecha y hora |

**Datos de prueba:** las fechas se calculan a partir del día en que se crea la base, así que siempre hay una vacuna vencida, otra que vence en menos de 30 días, citas pasadas y futuras, y una mascota sin veterinario (Kiwi).

**Pruebas:** `DaosTest` (8 pruebas instrumentadas con base en memoria).

---

## Fase 2. Lista de mascotas

**Estado: Terminada.**

- [x] Crear `MascotasViewModel` que cargue la lista desde `MascotaDao`
- [x] Crear `ListaMascotasFragment` con `RecyclerView` y `MascotaAdapter` (`ListAdapter` con `DiffUtil`)
- [x] Diseñar `item_mascota.xml` (foto circular, nombre y especie)
- [x] Mostrar un mensaje cuando no hay mascotas registradas
- [x] Botón flotante (+) que abra `FormMascotaActivity`

**Cómo funciona:**
- `ListaMascotasFragment` obtiene el ViewModel con `activityViewModels()`, así que es el mismo objeto que usa `MainActivity`.
- La lista se recarga en `onResume`, así se actualiza al regresar del formulario o del detalle.
- `DiffUtil` compara la lista nueva con la anterior y solo redibuja las filas que cambiaron.

---

## Fase 3. Formulario de mascota

**Estado: Terminada.**

- [x] `FormMascotaActivity` en modo alta (sin id) y edición (con id)
- [x] Selector de especie y de sexo con *chips*
- [x] Selector de fecha de nacimiento con `MaterialDatePicker` (no deja elegir fechas futuras)
- [x] Validación: nombre y especie obligatorios; teléfono de 7 a 15 dígitos; correo con formato válido; peso mayor que 0
- [x] Guardar en la base y regresar a la lista actualizada
- [x] Botones de cámara y galería (funcionales desde la fase 7)

**Cómo funciona:**
- Para abrir el formulario: `FormMascotaActivity.crearIntent(context)` (alta) o `crearIntent(context, mascotaId)` (edición).
- `FormMascotaViewModel` recibe el id directamente en su `SavedStateHandle` (los extras del Intent llegan ahí solos), carga la mascota en modo edición y guarda en un hilo de fondo.
- En la base se guardan valores fijos ("Perro", "Macho"); el texto que se ve sale de `strings.xml` (`TextosCatalogo.kt`).
- La fecha se guarda en el campo como texto `dd/MM/yyyy` y se convierte a `yyyy-MM-dd` al guardar. `MaterialDatePicker` trabaja en UTC; `FechaUtils` convierte también en UTC para que la fecha no se recorra un día.
- Los errores aparecen debajo de cada campo (`TextInputLayout`).
- En horizontal la foto va a la izquierda y los campos a la derecha (`layout-land/activity_form_mascota.xml`).

**Pruebas:** `ValidacionesTest` y `FechaUtilsTest` (unitarias). Manualmente: guardar vacío muestra errores; teléfono "123" muestra error; alta y edición funcionan.

---

## Fase 4. Detalle en teléfono

**Estado: Terminada.**

- [x] `DetalleMascotaViewModel` carga la mascota, sus vacunas y sus citas
- [x] `DetalleMascotaFragment` con foto, datos, edad calculada y veterinario
- [x] `DetalleMascotaActivity` contiene el fragmento y recibe el id
- [x] Vacunas con `VacunaAdapter`: borde y etiqueta rojos si están vencidas ("Venció hace 15 días") y ámbar si vencen en menos de 30 días ("Vence en 7 días")
- [x] Citas con `CitaAdapter`, separadas en "Próximas" y "Pasadas" (las pasadas se ven atenuadas)
- [x] `NuevaVacunaDialog` y `NuevaCitaDialog` para agregar y editar (al tocar una fila se edita)
- [x] Eliminar vacunas y citas (botón de basura o pulsación larga) y eliminar la mascota, siempre con confirmación
- [x] Botón para editar la mascota (lápiz en la barra superior)

**Cómo funciona:**
- `DetalleMascotaFragment` recibe el id en sus `arguments` y no conoce a la actividad que lo contiene; le avisa con la interfaz `DetalleMascotaFragment.Listener`. Por eso sirve igual en el teléfono y en el panel de la tableta.
- Editar y Eliminar se agregan a la barra superior de la actividad con un `MenuProvider`.
- Los diálogos guardan usando el ViewModel del fragmento padre (`viewModels(ownerProducer = { requireParentFragment() })`).
- La regla de vacunas está en `EstadoVacuna` y la separación de citas en `OrganizadorCitas`. Son funciones sin Android, con pruebas unitarias.

**Pruebas:** `EstadoVacunaTest` y `OrganizadorCitasTest` (unitarias).

---

## Fase 5. Tableta y orientación

**Estado: Terminada.**

- [x] `layout-sw600dp/activity_main.xml` con dos paneles (lista 1/3, detalle 2/3)
- [x] `MainActivity` detecta los dos paneles y muestra el detalle en el panel derecho
- [x] Mensaje "Selecciona una mascota para ver su información" cuando no hay selección
- [x] La mascota seleccionada se resalta en la lista
- [x] `layout-land/fragment_detalle_mascota.xml`: información a la izquierda, vacunas y citas a la derecha
- [x] `layout-land/activity_form_mascota.xml`: foto a la izquierda, campos a la derecha
- [x] Al eliminar la mascota seleccionada, el panel derecho se limpia
- [x] Probado en teléfono vertical y horizontal, y tableta vertical y horizontal

**Cómo funciona:**
- La detección se hace revisando si existe `R.id.contenedor_detalle` (solo está en el layout de tableta). No se revisa el tamaño de pantalla en código.
- `layout-sw600dp` tiene prioridad sobre `layout-land`, así que la tableta usa su propio diseño en ambas orientaciones.
- Las pantallas están hechas con piezas reutilizables (`include_detalle_info.xml`, `include_detalle_vacunas.xml`, `include_detalle_citas.xml`, `include_form_foto.xml`, `include_form_campos.xml`). Cada orientación o tamaño las acomoda distinto, sin repetir código.
- Si la pantalla pasa de tableta a teléfono (pantalla dividida o cambio de tamaño), `MainActivity` quita el detalle restaurado para que la app no se cierre por un contenedor que ya no existe. Se probó cambiando la densidad del emulador.

---

## Fase 6. Guardar el estado

**Estado: Terminada.**

- [x] Los ViewModel conservan los datos al rotar
- [x] La mascota seleccionada en la tableta se guarda en el `SavedStateHandle` de `MascotasViewModel`
- [x] El formulario conserva lo escrito, la especie y el sexo elegidos, la fecha, la foto y el modo (alta o edición)
- [x] Probado con "No conservar actividades" y cerrando el proceso de la app en segundo plano

| Caso | Solución |
|---|---|
| Rotación | Los ViewModel sobreviven y conservan listas y datos |
| Android cierra la app en segundo plano | `SavedStateHandle` (fotos, mascota seleccionada) y el estado automático de las vistas con id (campos de texto y chips) |
| Diálogos y selectores de fecha y hora | Son `DialogFragment`: Android los recrea solos; `Selectores` vuelve a conectar su acción después de rotar |

**Pruebas realizadas:**
- Formulario a medio llenar con "No conservar actividades": al regresar seguían el nombre, la especie y la raza.
- Formulario con el proceso cerrado (`adb shell am kill`): igual.
- Diálogo de vacuna y calendario abiertos y girando la pantalla dos veces: se conservaron.
- Tableta con una mascota seleccionada y el proceso cerrado: al volver se mostró la misma mascota.

**Para probarlo a mano:** en Opciones de desarrollador activar "No conservar actividades", llenar medio formulario, ir a otra app y regresar.

---

## Fase 7. Foto de la mascota

**Estado: Terminada.**

- [x] `FileProvider` en el manifiesto y `res/xml/file_paths.xml`
- [x] `FotoUtils` (crear archivo, copiar desde la galería, borrar)
- [x] Tomar foto con la cámara (`TakePicture`)
- [x] Elegir foto de la galería (`PickVisualMedia`, no necesita permisos)
- [x] Mostrar la foto con Coil en la lista, el detalle y el formulario
- [x] Ícono por defecto según la especie (perro, gato, ave y huella para "Otro")
- [x] Borrar el archivo de foto al cambiarla, quitarla, salir del formulario sin guardar o eliminar la mascota

**Cómo funciona:**
- Las fotos se guardan en `filesDir/fotos/` (almacenamiento interno de la app, sin permisos).
- Cámara: se crea el archivo antes y se pasa su `Uri` (del `FileProvider`) a la app de cámara. La ruta pendiente se guarda en el `SavedStateHandle`, por si Android cierra la app mientras la cámara está abierta.
- No se declara el permiso `CAMERA` porque la foto la toma la app de cámara del sistema. `uses-feature camera required=false` permite instalar la app en dispositivos sin cámara; en ellos el botón Cámara se oculta.
- Galería: la imagen elegida se copia a la carpeta de la app porque el `Uri` de la galería puede dejar de funcionar.
- La foto original no se borra hasta que el usuario guarda otra.

**Pruebas:** en el emulador se tomó una foto con la cámara virtual y se eligió otra de la galería. Se verificó en `files/fotos` que solo queda el archivo en uso, que la foto descartada se borra y que al eliminar la mascota se borra su foto.

---

## Fase 8. Comunicación con el veterinario

**Estado: Terminada.**

- [x] `ComunicacionUtils` con llamada, SMS y correo
- [x] Texto del historial de vacunas para el correo (datos de la mascota y cada vacuna con sus fechas y notas)
- [x] Botones en el detalle, deshabilitados si falta el teléfono o el correo del veterinario
- [x] Manejo de `ActivityNotFoundException` con un aviso (`Toast`)
- [x] Probado en teléfono y tableta

| Acción | Intent | Resultado probado |
|---|---|---|
| Llamar | `ACTION_DIAL` con `tel:` | Abre el marcador con el número (no llama) |
| SMS | `ACTION_SENDTO` con `smsto:` y `sms_body` | Abre Mensajes con "Hola, quisiera agendar una cita para mi mascota Firulais." |
| Correo | `ACTION_SENDTO` con `mailto:`, `EXTRA_EMAIL`, `EXTRA_SUBJECT` y `EXTRA_TEXT` | Abre la app de correo con el destinatario, asunto e historial |

**Pruebas:** `ComunicacionUtilsTest` (instrumentada) revisa el texto del historial.

---

## Fase 9. Pulido

**Estado: Terminada.**

- [x] Ícono de la app: huella blanca sobre el naranja de la app (adaptativo desde Android 8 y versión para Android 7)
- [x] Colores, márgenes y tamaños revisados en modo claro y oscuro
- [x] Sin textos fijos fuera de `strings.xml` (lint sin avisos)
- [x] La app no se cierra con campos vacíos, sin foto o sin datos del veterinario
- [x] Datos de prueba detrás de una bandera: solo se insertan si la app es de depuración (`FLAG_DEBUGGABLE`)

**Nota:** lint solo avisa que existen versiones más nuevas de algunas librerías. No se actualizaron para no arriesgar cambios a esta altura del proyecto.

---

## Fase 10. Entregables

**Estado: En progreso.** El borrador del manual de usuario está hecho, pero se comparte por fuera del repositorio. Falta llenar la portada del manual, grabar el video y generar el archivo comprimido.

- [x] Manual de usuario (borrador en Word, se comparte por fuera del repositorio). Falta llenar en la portada: facultad, nombres, matrículas, grupo y docente. Opcional: agregar una captura del correo y del teléfono en horizontal.
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
   - Registrar vacunas y citas (y qué significan los colores rojo y ámbar)
   - Contactar al veterinario (llamada, SMS y correo)
6. Uso en tableta y en orientación horizontal
7. Preguntas frecuentes y solución de problemas. Por ejemplo: "¿Por qué el botón Llamar está gris?" (falta el teléfono del veterinario) o "No aparece el botón Cámara" (el dispositivo no tiene cámara).

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

### Archivo comprimido

Comprimir la carpeta del proyecto **sin** `build/`, `app/build/` ni `.gradle/`. Una forma sencilla es `git archive --format=zip -o PatitasAlDia.zip HEAD`, que solo incluye los archivos del repositorio.
