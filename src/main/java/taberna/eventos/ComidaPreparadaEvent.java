package taberna.eventos;

import java.util.Objects;

public record ComidaPreparadaEvent(int pedidoId, String plato) {
    public ComidaPreparadaEvent {
        Objects.requireNonNull(plato);
        if (pedidoId <= 0 || plato.isBlank()) {
            throw new IllegalArgumentException("Pedido o plato inválido");
        }
    }
}
