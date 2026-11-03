package taberna.bus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/** Distribuye cada evento a los suscriptores de su clase exacta. */
public final class EventBus {
    private final Map<Class<?>, List<Registro<?>>> suscriptores = new HashMap<>();

    public synchronized <T> void suscribir(Class<T> tipo, Consumer<T> suscriptor) {
        Objects.requireNonNull(tipo);
        Objects.requireNonNull(suscriptor);
        List<Registro<?>> registros = suscriptores.computeIfAbsent(tipo, clave -> new ArrayList<>());
        if (registros.stream().noneMatch(registro -> registro.suscriptor().equals(suscriptor))) {
            registros.add(new Registro<>(tipo, suscriptor));
        }
    }

    /** Se debe pasar la misma instancia de Consumer que se registró. */
    public synchronized <T> void desuscribir(Class<T> tipo, Consumer<T> suscriptor) {
        Objects.requireNonNull(tipo);
        Objects.requireNonNull(suscriptor);
        List<Registro<?>> registros = suscriptores.get(tipo);
        if (registros != null) {
            registros.removeIf(registro -> registro.suscriptor().equals(suscriptor));
            if (registros.isEmpty()) {
                suscriptores.remove(tipo);
            }
        }
    }

    public void publicar(Object evento) {
        Objects.requireNonNull(evento);
        List<Registro<?>> destinatarios;
        synchronized (this) {
            destinatarios = List.copyOf(suscriptores.getOrDefault(evento.getClass(), List.of()));
        }
        // Las altas y bajas afectan publicaciones futuras, no el reparto en curso.
        // No retenemos el bloqueo mientras se ejecuta código de los suscriptores.
        for (Registro<?> registro : destinatarios) {
            try {
                registro.notificar(evento);
            } catch (RuntimeException error) {
                System.err.printf("[BUS] Falló un suscriptor de %s: %s%n",
                        evento.getClass().getSimpleName(), error.getMessage());
            }
        }
    }

    private record Registro<T>(Class<T> tipo, Consumer<T> suscriptor) {
        void notificar(Object evento) {
            suscriptor.accept(tipo.cast(evento));
        }
    }
}
