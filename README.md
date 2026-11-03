# La Taberna del Bus de Eventos

Simulación en Java 21 de **El Gólem Alquímico**. Los componentes se comunican
mediante eventos inmutables y un bus de publicación y suscripción por tipo.

## Ejecutar

Se necesita un **JDK 21 o superior**, con `java` y `javac` en PATH o `JAVA_HOME`
apuntando a la carpeta del JDK. No requiere Maven ni dependencias externas.

En PowerShell, desde la raíz del proyecto:

```powershell
.\ejecutar.ps1
```

La simulación dura aproximadamente 14 segundos y termina automáticamente.
Para omitir los colores ANSI: `./ejecutar.ps1 -SinColor`.
Para ejecutar las pruebas: `./ejecutar.ps1 -Pruebas`.

## Funcionamiento

`SistemaDePedidos` publica un pedido. `Barra` selecciona las bebidas y programa
su entrega a los 2 segundos; `Cocina` selecciona la comida y programa su entrega
a los 5 segundos. Ambos publican el resultado en el bus. `PanelLED` muestra
los cuatro tipos de eventos. `Banda` anuncia una canción cada 4 segundos y
`SistemaDeSonido` ajusta el ecualizador. El planificador permite solapar las
preparaciones sin bloquear el reparto de eventos.

El bus ofrece `suscribir`, `desuscribir` y `publicar`. La distribución es síncrona
y por clase exacta; el trabajo diferido lo programan los componentes. Las
publicaciones concurrentes usan una instantánea de suscriptores. Un cambio de
suscripción afecta las publicaciones siguientes. Registrar dos veces la misma
instancia de `Consumer` no duplica las notificaciones. Si un suscriptor falla
con una excepción de ejecución, se informa por consola y se notifica al resto.

Cada evento es un `record`. El pedido copia su lista con `List.copyOf`, y cada
producto es también inmutable. Los componentes conocen el bus y los eventos,
sin referencias a otros componentes. `Main` únicamente conecta las piezas.

## Escenario de demostración

| Tiempo aproximado | Resultado observable |
| --- | --- |
| 0 s | Pedido 123, mesa 5: cerveza y hamburguesa. Comienza la banda. |
| 1 s | Pedido 124, mesa 2: agua y pizza, mientras sigue el primer pedido. |
| 2 s | El panel anuncia la cerveza de la mesa 5. |
| 3 s | Se sirve agua y se agregan humo e historial musical al bus. |
| 4 s | Suena **Through the Fire and Flames** y se activa el humo. |
| 5 s | Se anuncia la hamburguesa del pedido 123. |
| 6 s | Se anuncia la pizza del pedido 124. |
| 8 s | Suena otra canción; el humo no se activa. |
| 10 s | Se desuscribe el humo usando el mismo `Consumer` registrado. |
| 12 s | Se repite la canción épica: sonido, panel e historial reaccionan; humo no. |
| 14 s | Se cancela la música y se cierra el planificador. |

Los tiempos son retardos programados y pueden variar ligeramente según el
sistema operativo. Las líneas del mismo instante pueden intercalarse. El dato
`duracion` indica segundos de la canción original; el intervalo de 4 segundos
acelera la demostración y no reproduce audio real.

## Extensibilidad

`SistemaDeHumo` y `HistorialMusical` se incorporan únicamente creando nuevos
suscriptores y conectándolos en `Main`. `Banda`, `EventBus` y los demás
componentes mantienen su código del primer commit. Así se demuestra el
principio abierto/cerrado: extender mediante nuevos receptores sin cambiar
publicadores. El historial cuenta anuncios desde su suscripción; no usa archivos.

```java
SistemaDeHumo humo = new SistemaDeHumo(bus, salida);
humo.activar();
new HistorialMusical(bus, salida);
// Más tarde:
humo.desactivar();
```

## Diagramas y entrega

- [Documento PDF](docs/Taberna-EventBus.pdf): explicación, clases, secuencia,
  componentes, escenario y código PlantUML.
- [Fuentes PlantUML e imágenes PNG](docs/diagramas).
- [Salida de una ejecución real](docs/ejecucion.txt).

El diagrama de clases se presenta en dos vistas para conservar la legibilidad:
bus/datos y componentes. La secuencia enfoca el pedido 123 y la música;
el segundo pedido sigue el mismo recorrido un segundo después.

Para regenerar las imágenes, descarga el JAR de [PlantUML](https://plantuml.com/download)
y ejecuta desde la raíz (reemplaza la ruta del JAR):

```powershell
java -jar C:/herramientas/plantuml.jar -charset UTF-8 -tpng "docs/diagramas/*.puml"
```

Para regenerar el PDF con Python y `reportlab` instalado:

```powershell
python docs/generar_pdf.py
```

## Validación de la rúbrica

| Criterio | Evidencia |
| --- | --- |
| Bus y suscripciones | Altas, bajas, filtrado por clase, duplicados, instantánea y aislamiento de fallos. |
| Eventos inmutables | Cuatro records, productos inmutables y copia defensiva de la lista. |
| Desacoplamiento | Ningún componente invoca otro; la colaboración funcional pasa por el bus. |
| Extensibilidad | Humo e historial se agregan sin cambios en el bus ni publicadores. |
| Diagramas | UML de clases, secuencia y componentes, con PNG y fuentes PlantUML. |
| Flujo funcional | Dos pedidos solapados, retardos de 2 y 5 s, música, sonido y panel. |

Las siete pruebas comprueban tipos, duplicados y bajas; cambios durante el reparto
y eventos anidados; aislamiento de fallos; publicaciones concurrentes;
inmutabilidad; preparación en paralelo; y activación/desactivación del humo.
El mensaje `fallo de prueba esperado` es deliberado y comprueba que otros
suscriptores siguen recibiendo el evento.

Se usa un bus local en memoria, suficiente para la tarea. No persiste eventos,
no reintenta entregas y no coordina procesos distribuidos. Los receptores deben
ser breves y seguros si reciben publicaciones desde varios hilos; las demoras
de la taberna se delegan al planificador.

## Fuentes

- Enunciado proporcionado: **La Taberna del Bus de Eventos**, `Instrucciones.pdf`.
- Rúbrica proporcionada en las dos imágenes adjuntas.
- Akamai. [What Is an Event Bus?](https://www.akamai.com/glossary/what-is-an-event-bus).
  Base conceptual: publicadores, consumidores, distribución por clase y
  desacoplamiento mediante un intermediario.
- [Event Driven Architecture (EDA): Todo lo que DEBERÍAS saber | Tutorial en Español](https://www.youtube.com/watch?v=GdelFsukQwE).
  Video indicado como referencia por el alumno. Solo se pudo verificar el título;
  no se obtuvo una transcripción, por lo que no se le atribuyen detalles concretos.

No se requiere interfaz gráfica: el enunciado solicita notificaciones en consola.
