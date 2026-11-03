package taberna.eventos;

import java.util.Objects;

public record BebidaServidaEvent(int mesaId, String bebida) {
    public BebidaServidaEvent {
        Objects.requireNonNull(bebida);
        if (mesaId <= 0 || bebida.isBlank()) {
            throw new IllegalArgumentException("Mesa o bebida inválida");
        }
    }
}
