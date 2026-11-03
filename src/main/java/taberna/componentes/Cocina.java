package taberna.componentes;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import taberna.Salida;
import taberna.bus.EventBus;
import taberna.eventos.ComidaPreparadaEvent;
import taberna.eventos.PedidoRealizadoEvent;
import taberna.modelo.ItemPedido;
import taberna.modelo.TipoItem;

public final class Cocina {
    private final EventBus bus;
    private final ScheduledExecutorService planificador;
    private final Salida salida;

    public Cocina(EventBus bus, ScheduledExecutorService planificador, Salida salida) {
        this.bus = bus;
        this.planificador = planificador;
        this.salida = salida;
        bus.suscribir(PedidoRealizadoEvent.class, this::preparar);
    }

    private void preparar(PedidoRealizadoEvent pedido) {
        for (ItemPedido item : pedido.items()) {
            if (item.tipo() == TipoItem.COMIDA) {
                salida.mostrar("COCINA", "Preparando " + item.nombre() + " del pedido " + pedido.pedidoId());
                planificador.schedule(() -> bus.publicar(
                        new ComidaPreparadaEvent(pedido.pedidoId(), item.nombre())), 5, TimeUnit.SECONDS);
            }
        }
    }
}
