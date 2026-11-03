package taberna.eventos;

import java.util.List;
import taberna.modelo.ItemPedido;

public record PedidoRealizadoEvent(int mesaId, int pedidoId, List<ItemPedido> items) {
    public PedidoRealizadoEvent {
        if (mesaId <= 0 || pedidoId <= 0) {
            throw new IllegalArgumentException("La mesa y el pedido deben ser positivos");
        }
        items = List.copyOf(items);
        if (items.isEmpty()) {
            throw new IllegalArgumentException("El pedido debe incluir productos");
        }
    }
}
