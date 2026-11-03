package taberna.componentes;

import java.util.function.Consumer;
import taberna.Salida;
import taberna.bus.EventBus;
import taberna.eventos.BandaTocandoEvent;

public final class SistemaDeHumo {
    private final EventBus bus;
    private final Consumer<BandaTocandoEvent> receptor;

    public SistemaDeHumo(EventBus bus, Salida salida) {
        this.bus = bus;
        receptor = evento -> {
            if (evento.nombreCancion().equals("Through the Fire and Flames")) {
                salida.mostrar("HUMO", "¡Efectos de humo para: " + evento.nombreCancion() + "!");
            }
        };
    }

    public void activar() {
        bus.suscribir(BandaTocandoEvent.class, receptor);
    }

    public void desactivar() {
        bus.desuscribir(BandaTocandoEvent.class, receptor);
    }
}
