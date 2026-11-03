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
