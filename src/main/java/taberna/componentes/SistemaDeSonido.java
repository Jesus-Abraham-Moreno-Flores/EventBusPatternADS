package taberna.componentes;

import taberna.Salida;
import taberna.bus.EventBus;
import taberna.eventos.BandaTocandoEvent;

public final class SistemaDeSonido {
    public SistemaDeSonido(EventBus bus, Salida salida) {
        bus.suscribir(BandaTocandoEvent.class, evento -> salida.mostrar(
                "SONIDO", "Ajustando ecualizador para: " + evento.nombreCancion()));
    }
}
