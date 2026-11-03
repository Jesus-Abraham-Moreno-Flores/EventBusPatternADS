package taberna.eventos;

import java.util.Objects;

/** duracion se expresa en segundos. */
public record BandaTocandoEvent(String nombreBanda, String nombreCancion, int duracion) {
    public BandaTocandoEvent {
        Objects.requireNonNull(nombreBanda);
        Objects.requireNonNull(nombreCancion);
        if (nombreBanda.isBlank() || nombreCancion.isBlank() || duracion <= 0) {
            throw new IllegalArgumentException("Los datos de la canción son inválidos");
        }
    }
}
