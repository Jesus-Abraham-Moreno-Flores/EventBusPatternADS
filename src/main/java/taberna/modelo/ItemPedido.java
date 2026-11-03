package taberna.modelo;

import java.util.Objects;

public record ItemPedido(String nombre, TipoItem tipo) {
    public ItemPedido {
        Objects.requireNonNull(nombre);
        Objects.requireNonNull(tipo);
        if (nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto no puede estar vacío");
        }
    }
}
