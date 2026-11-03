package taberna.componentes;

import java.util.concurrent.atomic.AtomicInteger;
import taberna.Salida;
import taberna.bus.EventBus;
import taberna.eventos.BandaTocandoEvent;

/** Segundo ejemplo de extensión: cuenta los anuncios recibidos desde su alta. */
public final class HistorialMusical {
    public HistorialMusical(EventBus bus, Salida salida) {
        AtomicInteger canciones = new AtomicInteger();
        bus.suscribir(BandaTocandoEvent.class, evento -> salida.mostrar("HISTORIAL",
                "Canción " + canciones.incrementAndGet() + ": " + evento.nombreCancion()));
    }
}
