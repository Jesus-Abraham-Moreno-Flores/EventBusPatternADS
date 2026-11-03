package taberna.componentes;

import java.util.List;
import taberna.Salida;
import taberna.bus.EventBus;
import taberna.eventos.PedidoRealizadoEvent;
import taberna.modelo.ItemPedido;

public final class SistemaDePedidos {
    private final EventBus bus;
    private final Salida salida;

    public SistemaDePedidos(EventBus bus, Salida salida) {
        this.bus = bus;
        this.salida = salida;
    }

    public void realizarPedido(int mesaId, int pedidoId, List<ItemPedido> items) {
        PedidoRealizadoEvent evento = new PedidoRealizadoEvent(mesaId, pedidoId, items);
        salida.mostrar("PEDIDOS", "Pedido " + pedidoId + " de la mesa " + mesaId);
        bus.publicar(evento);
    }
}
