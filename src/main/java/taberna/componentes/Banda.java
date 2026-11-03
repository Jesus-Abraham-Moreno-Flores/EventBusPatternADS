package taberna.componentes;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import taberna.Salida;
import taberna.bus.EventBus;
import taberna.eventos.BandaTocandoEvent;

public final class Banda {
    private final EventBus bus;
    private final Salida salida;

    public Banda(EventBus bus, Salida salida) {
        this.bus = bus;
        this.salida = salida;
    }

    /** La demo acelera el intervalo; la duración del evento conserva el dato musical. */
    public ScheduledFuture<?> iniciar(ScheduledExecutorService planificador) {
        List<BandaTocandoEvent> repertorio = List.of(
                new BandaTocandoEvent("Los Seguidores de Dijkstra", "El Algoritmo del Amor", 180),
                new BandaTocandoEvent("Los Seguidores de Dijkstra", "Through the Fire and Flames", 440));
        return planificador.scheduleAtFixedRate(new Runnable() {
            private int siguiente;

            @Override
            public void run() {
                BandaTocandoEvent evento = repertorio.get(siguiente);
                siguiente = (siguiente + 1) % repertorio.size();
                salida.mostrar("BANDA", "Tocando: " + evento.nombreCancion());
                bus.publicar(evento);
            }
        }, 0, 4, TimeUnit.SECONDS);
    }
}
