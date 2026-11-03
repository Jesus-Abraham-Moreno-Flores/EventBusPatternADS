package taberna;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import taberna.bus.EventBus;
import taberna.componentes.*;
import taberna.modelo.ItemPedido;
import taberna.modelo.TipoItem;

/** Punto de composición: aquí se conectan los componentes, nunca entre sí. */
public final class Main {
    public static void main(String[] args) throws InterruptedException {
        Salida salida = new Salida(System.console() != null && !List.of(args).contains("--sin-color"));
        EventBus bus = new EventBus();
        ScheduledExecutorService planificador = Executors.newScheduledThreadPool(4);
        ScheduledFuture<?> musica = null;
        try {
            new PanelLED(bus, salida);
            new Barra(bus, planificador, salida);
            new Cocina(bus, planificador, salida);
            new SistemaDeSonido(bus, salida);
            SistemaDePedidos pedidos = new SistemaDePedidos(bus, salida);
            Banda banda = new Banda(bus, salida);

            salida.mostrar("TABERNA", "El Gólem Alquímico abre sus puertas");
            pedidos.realizarPedido(5, 123, List.of(
                    new ItemPedido("Cerveza", TipoItem.BEBIDA),
                    new ItemPedido("Hamburguesa", TipoItem.COMIDA)));
            musica = banda.iniciar(planificador);
            planificador.schedule(() -> pedidos.realizarPedido(2, 124, List.of(
                    new ItemPedido("Agua", TipoItem.BEBIDA),
                    new ItemPedido("Pizza", TipoItem.COMIDA))), 1, TimeUnit.SECONDS);
            TimeUnit.SECONDS.sleep(14);
        } finally {
            if (musica != null) {
                musica.cancel(false);
            }
            planificador.shutdown();
            if (!planificador.awaitTermination(3, TimeUnit.SECONDS)) {
                planificador.shutdownNow();
            }
            salida.mostrar("TABERNA", "Simulación finalizada");
        }
    }
}
