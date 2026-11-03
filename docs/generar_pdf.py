"""Genera el PDF de entrega. Requiere reportlab y los PNG de PlantUML."""
from pathlib import Path
from textwrap import wrap

from reportlab.lib import colors
from reportlab.lib.pagesizes import A4, landscape
from reportlab.lib.styles import ParagraphStyle
from reportlab.lib.utils import ImageReader
from reportlab.pdfgen import canvas
from reportlab.platypus import Paragraph

ROOT = Path(__file__).resolve().parent
W, H = landscape(A4)
AZUL = colors.HexColor("#18354d")
ESTILO = ParagraphStyle("texto", fontName="Helvetica", fontSize=11, leading=16, textColor=AZUL)


def generar():
    pdf = canvas.Canvas(str(ROOT / "Taberna-EventBus.pdf"), pagesize=(W, H))
    pdf.setTitle("La Taberna del Bus de Eventos - El Gólem Alquímico")
    pdf.setAuthor("Jesús Abraham Moreno Flores")
    pagina = 0

    def comenzar(titulo, subtitulo="Arquitectura Event Bus | Java 21"):
        nonlocal pagina
        pagina += 1
        pdf.setFillColor(AZUL)
        pdf.rect(0, H - 12, W, 12, fill=1, stroke=0)
        pdf.setFont("Helvetica-Bold", 21)
        pdf.drawString(36, H - 49, titulo)
        pdf.setFont("Helvetica", 10)
        pdf.drawString(36, H - 69, subtitulo)
        pdf.setStrokeColor(colors.HexColor("#d5dfe7"))
        pdf.line(36, 31, W - 36, 31)
        pdf.setFont("Helvetica", 9)
        pdf.drawString(36, 18, "El Gólem Alquímico | Diseño y demostración")
        pdf.drawRightString(W - 36, 18, str(pagina))

    def texto(titulo, parrafos, subtitulo="Arquitectura Event Bus | Java 21"):
        comenzar(titulo, subtitulo)
        y = H - 102
        for contenido in parrafos:
            p = Paragraph(contenido, ESTILO)
            _, alto = p.wrap(W - 80, H)
            if y - alto < 50:
                raise ValueError(f"Texto fuera de página: {titulo}")
            p.drawOn(pdf, 40, y - alto)
            y -= alto + 14
        pdf.showPage()

    texto("La Taberna del Bus de Eventos", [
        "<b>Problema.</b> La taberna necesita coordinar pedidos, barra, cocina, música, sonido y notificaciones sin que cada componente invoque a los demás.",
        "<b>Solución.</b> Los publicadores emiten objetos inmutables. EventBus distribuye cada objeto a los consumidores registrados para su clase exacta. Un consumidor puede reaccionar y publicar otro evento. Main crea y conecta las piezas.",
        "<b>Concurrencia.</b> El bus reparte notificaciones de forma síncrona y breve. Barra y Cocina programan entregas independientes a los 2 y 5 segundos mediante ScheduledExecutorService. La banda anuncia canciones cada 4 segundos mientras las preparaciones siguen pendientes.",
        "<b>Suscripciones.</b> Altas y bajas sincronizadas; el reparto usa una copia de los registros y ejecuta receptores fuera del bloqueo. Los cambios afectan publicaciones futuras. No se repite el registro del mismo Consumer. Una RuntimeException de un receptor se informa y no interrumpe al resto.",
        "<b>Datos.</b> Los cuatro eventos son records. PedidoRealizadoEvent copia su lista de ItemPedido con List.copyOf; cada producto también es un record inmutable. Los datos se validan al construirlos.",
        "<b>Alcance.</b> Consola, sin dependencias Java externas. El bus es local y no persiste mensajes ni incluye reintentos. La duración original de las canciones se conserva como dato; el intervalo musical se acelera para la demostración.",
    ], "Jesús Abraham Moreno Flores | Tarea de arquitectura de software")

    diagramas = [
        ("clases-eventos", "UML de clases: bus y eventos", "La inmutabilidad incluye la lista del pedido y sus elementos."),
        ("clases-componentes", "UML de clases: componentes", "Las flechas indican dependencias; los componentes colaboran a través del bus."),
        ("componentes", "UML de componentes", "Las flechas representan publicación y entrega; los receptores se registran por tipo."),
        ("secuencia", "UML de secuencia: pedidos", "Pedido 123; el pedido 124 sigue el mismo recorrido desde t = 1 s."),
        ("secuencia-musica", "UML de secuencia: música", "Cada anuncio publica un BandaTocandoEvent; el humo se agrega y se retira en ejecución."),
    ]
    for nombre, titulo, pie in diagramas:
        comenzar(titulo)
        imagen = ImageReader(str(ROOT / "diagramas" / f"{nombre}.png"))
        w, h = imagen.getSize()
        escala = min((W - 72) / w, (H - 150) / h)
        ancho, alto = w * escala, h * escala
        pdf.drawImage(imagen, (W - ancho) / 2, 67 + (H - 150 - alto) / 2,
                      width=ancho, height=alto, mask="auto")
        pdf.setFillColor(AZUL)
        pdf.setFont("Helvetica", 9)
        pdf.drawString(36, 46, pie)
        pdf.showPage()

    texto("Escenario y comprobación", [
        "<b>0-3 s.</b> Mesa 5 pide cerveza y hamburguesa (pedido 123). La banda anuncia El Algoritmo del Amor y sonido ajusta su ecualizador. A los 1 s, mesa 2 pide agua y pizza (pedido 124). El panel anuncia la cerveza a los 2 s y el agua a los 3 s. Main incorpora Humo e HistorialMusical a los 3 s.",
        "<b>4-6 s.</b> Through the Fire and Flames activa el humo y registra el primer anuncio del historial. El panel anuncia la hamburguesa a los 5 s y la pizza a los 6 s. Las preparaciones se solapan con la música.",
        "<b>8-14 s.</b> Se anuncia otra canción normal. A los 10 s se desuscribe el humo. A los 12 s vuelve la canción épica: panel, sonido e historial reaccionan; el humo ya no recibe el evento. A los 14 s termina la simulación y se cierra el planificador.",
        "<b>Abierto/cerrado.</b> SistemaDeHumo e HistorialMusical se añaden en el segundo commit. Solo se cambia la composición en Main. Banda, EventBus y los componentes previos permanecen sin cambios. El humo conserva su Consumer para retirarlo correctamente.",
        "<b>Validación.</b> Siete pruebas cubren registro/baja/tipos/duplicados; cambios durante el reparto y eventos anidados; fallos aislados; publicación concurrente; inmutabilidad; preparación en paralelo; y humo dinámico. docs/ejecucion.txt contiene una ejecución completa real con marcas de tiempo.",
        "<b>Ejecutar.</b> JDK 21 o superior. En PowerShell: <font name='Courier'>./ejecutar.ps1</font>. Pruebas: <font name='Courier'>./ejecutar.ps1 -Pruebas</font>. Sin colores: <font name='Courier'>./ejecutar.ps1 -SinColor</font>. Los tiempos son aproximados; las líneas del mismo instante pueden intercalarse.",
    ])

    texto("Fundamento y referencias", [
        "Akamai describe productores, consumidores y un bus intermediario que dirige los eventos a receptores interesados. El proyecto aplica esa separación: quien anuncia un pedido o canción desconoce qué componentes reaccionan. Las preparaciones asíncronas se implementan con tareas programadas y un bus local sencillo.",
        "<b>Enunciado y rúbrica.</b> La Taberna del Bus de Eventos, Instrucciones.pdf, y dos imágenes proporcionadas. README.md relaciona cada criterio con evidencia: bus, eventos inmutables, desacoplamiento, extensibilidad, UML y flujo funcional.",
        '<b>Akamai.</b> What Is an Event Bus? <link href="https://www.akamai.com/glossary/what-is-an-event-bus" color="#175985">https://www.akamai.com/glossary/what-is-an-event-bus</link>. Consulta: 2 de noviembre de 2026.',
        '<b>Video indicado.</b> Event Driven Architecture (EDA): Todo lo que DEBERÍAS saber | Tutorial en Español. <link href="https://www.youtube.com/watch?v=GdelFsukQwE" color="#175985">https://www.youtube.com/watch?v=GdelFsukQwE</link>. Se verificó el título, pero no se obtuvo una transcripción. No se le atribuyen explicaciones específicas.',
        '<b>PlantUML.</b> Imágenes generadas con PlantUML 1.2025.0. El código se incluye a continuación; las fuentes .puml y PNG están en docs/diagramas. <link href="https://plantuml.com" color="#175985">https://plantuml.com</link>.',
        '<b>Repositorio.</b> <link href="https://github.com/Jesus-Abraham-Moreno-Flores/EventBusPatternADS" color="#175985">github.com/Jesus-Abraham-Moreno-Flores/EventBusPatternADS</link>.',
    ])

    for nombre, _, _ in diagramas:
        comenzar("Código PlantUML", f"docs/diagramas/{nombre}.puml")
        y = H - 96
        pdf.setFont("Courier", 8.8)
        for linea in (ROOT / "diagramas" / f"{nombre}.puml").read_text(encoding="utf-8").splitlines():
            for fragmento in wrap(linea, width=137, replace_whitespace=False, drop_whitespace=False) or [""]:
                if y < 47:
                    pdf.showPage()
                    comenzar("Código PlantUML (continuación)", f"docs/diagramas/{nombre}.puml")
                    pdf.setFont("Courier", 8.8)
                    y = H - 96
                pdf.drawString(38, y, fragmento)
                y -= 10.6
        pdf.showPage()
    pdf.save()
    print(ROOT / "Taberna-EventBus.pdf")


if __name__ == "__main__":
    generar()
