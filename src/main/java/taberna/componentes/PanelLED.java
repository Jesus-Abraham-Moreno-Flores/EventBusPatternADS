package taberna.componentes;

import taberna.Salida;
import taberna.bus.EventBus;
import taberna.eventos.*;

public final class PanelLED {
    public PanelLED(EventBus bus, Salida salida) {
        bus.suscribir(PedidoRealizadoEvent.class, evento -> salida.mostrar("PANEL LED",
                "Nuevo pedido " + evento.pedidoId() + " en la mesa " + evento.mesaId()));
        bus.suscribir(BebidaServidaEvent.class, evento -> salida.mostrar("PANEL LED",
                "¡" + evento.bebida() + " servida en la mesa " + evento.mesaId() + "!"));
        bus.suscribir(ComidaPreparadaEvent.class, evento -> salida.mostrar("PANEL LED",
                "¡" + evento.plato() + " lista! Pedido " + evento.pedidoId()));
        bus.suscribir(BandaTocandoEvent.class, evento -> salida.mostrar("PANEL LED",
                evento.nombreBanda() + ": " + evento.nombreCancion() + " (" + evento.duracion() + " s)"));
    }
}
