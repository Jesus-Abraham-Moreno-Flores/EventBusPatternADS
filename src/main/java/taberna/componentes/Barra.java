package taberna.componentes;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import taberna.Salida;
import taberna.bus.EventBus;
import taberna.eventos.BebidaServidaEvent;
import taberna.eventos.PedidoRealizadoEvent;
import taberna.modelo.ItemPedido;
import taberna.modelo.TipoItem;

public final class Barra {
    private final EventBus bus;
    private final ScheduledExecutorService planificador;
    private final Salida salida;

    public Barra(EventBus bus, ScheduledExecutorService planificador, Salida salida) {
        this.bus = bus;
        this.planificador = planificador;
        this.salida = salida;
        bus.suscribir(PedidoRealizadoEvent.class, this::preparar);
    }

    private void preparar(PedidoRealizadoEvent pedido) {
        for (ItemPedido item : pedido.items()) {
            if (item.tipo() == TipoItem.BEBIDA) {
                salida.mostrar("BARRA", "Preparando " + item.nombre() + " para mesa " + pedido.mesaId());
                planificador.schedule(() -> bus.publicar(
                        new BebidaServidaEvent(pedido.mesaId(), item.nombre())), 2, TimeUnit.SECONDS);
            }
        }
    }
}
