package taberna;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import taberna.bus.EventBus;
import taberna.componentes.Barra;
import taberna.componentes.Cocina;
import taberna.componentes.SistemaDePedidos;
import taberna.eventos.BebidaServidaEvent;
import taberna.eventos.ComidaPreparadaEvent;
import taberna.eventos.PedidoRealizadoEvent;
import taberna.modelo.ItemPedido;
import taberna.modelo.TipoItem;

/** Pruebas sin bibliotecas adicionales; fallan con AssertionError. */
public final class Pruebas {
    public static void main(String[] args) throws Exception {
        suscripcionesYTipos();
        cambiosDurantePublicacion();
        fallosAislados();
        publicacionesConcurrentes();
        eventosInmutables();
        preparacionEnParalelo();
        System.out.println("OK: 6 pruebas completadas");
    }

    private static void verificar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new AssertionError(mensaje);
        }
    }

    private static void suscripcionesYTipos() {
        EventBus bus = new EventBus();
        AtomicInteger recibidos = new AtomicInteger();
        Consumer<String> receptor = evento -> recibidos.incrementAndGet();
        bus.publicar("sin suscriptores");
        bus.suscribir(String.class, receptor);
        bus.suscribir(String.class, receptor);
        bus.publicar(123);
        bus.publicar("uno");
        bus.desuscribir(String.class, receptor);
        bus.desuscribir(String.class, receptor);
        bus.publicar("dos");
        verificar(recibidos.get() == 1, "Filtrado, duplicados o desuscripción incorrectos");
    }

    private static void cambiosDurantePublicacion() {
        EventBus bus = new EventBus();
        List<String> recibidos = new ArrayList<>();
        Consumer<String> segundo = evento -> recibidos.add("segundo");
        Consumer<String> nuevo = evento -> recibidos.add("nuevo");
        bus.suscribir(String.class, evento -> {
            recibidos.add("primero");
            bus.desuscribir(String.class, segundo);
            bus.suscribir(String.class, nuevo);
            bus.publicar(1);
        });
        bus.suscribir(String.class, segundo);
        bus.suscribir(Integer.class, evento -> recibidos.add("anidado"));
        bus.publicar("uno");
        bus.publicar("dos");
        verificar(recibidos.equals(List.of("primero", "anidado", "segundo",
                "primero", "anidado", "nuevo")), "La instantánea o publicación anidada falló");
    }

    private static void fallosAislados() {
        EventBus bus = new EventBus();
        AtomicInteger recibidos = new AtomicInteger();
        bus.suscribir(String.class, evento -> { throw new IllegalStateException("fallo de prueba esperado"); });
        bus.suscribir(String.class, evento -> recibidos.incrementAndGet());
        bus.publicar("continuar");
        verificar(recibidos.get() == 1, "Un suscriptor impidió notificar a los demás");
    }

    private static void publicacionesConcurrentes() throws Exception {
        EventBus bus = new EventBus();
        AtomicInteger recibidos = new AtomicInteger();
        bus.suscribir(Integer.class, evento -> recibidos.incrementAndGet());
        try (var hilos = Executors.newFixedThreadPool(4)) {
            List<java.util.concurrent.Callable<Void>> tareas = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                tareas.add(() -> { bus.publicar(1); return null; });
            }
            for (var resultado : hilos.invokeAll(tareas)) {
                resultado.get();
            }
        }
        verificar(recibidos.get() == 100, "Se perdieron publicaciones concurrentes");
    }

    private static void eventosInmutables() {
        List<ItemPedido> items = new ArrayList<>();
        items.add(new ItemPedido("Agua", TipoItem.BEBIDA));
        PedidoRealizadoEvent evento = new PedidoRealizadoEvent(1, 1, items);
        items.clear();
        verificar(evento.items().size() == 1, "Falta una copia defensiva de los productos");
        try {
            evento.items().clear();
            throw new AssertionError("La lista del evento es modificable");
        } catch (UnsupportedOperationException esperado) {
            // El evento conserva una lista inmutable de productos inmutables.
        }
    }

    private static void preparacionEnParalelo() throws Exception {
        EventBus bus = new EventBus();
        Salida salida = new Salida(false);
        var planificador = Executors.newScheduledThreadPool(2);
        CountDownLatch terminados = new CountDownLatch(2);
        AtomicLong bebidaEn = new AtomicLong();
        AtomicLong comidaEn = new AtomicLong();
        long inicio = System.nanoTime();
        bus.suscribir(BebidaServidaEvent.class, evento -> {
            verificar(evento.mesaId() == 5 && evento.bebida().equals("Cerveza"), "Bebida incorrecta");
            bebidaEn.set(System.nanoTime() - inicio);
            terminados.countDown();
        });
        bus.suscribir(ComidaPreparadaEvent.class, evento -> {
            verificar(evento.pedidoId() == 123 && evento.plato().equals("Hamburguesa"), "Comida incorrecta");
            comidaEn.set(System.nanoTime() - inicio);
            terminados.countDown();
        });
        try {
            new Barra(bus, planificador, salida);
            new Cocina(bus, planificador, salida);
            new SistemaDePedidos(bus, salida).realizarPedido(5, 123, List.of(
                    new ItemPedido("Cerveza", TipoItem.BEBIDA),
                    new ItemPedido("Hamburguesa", TipoItem.COMIDA)));
            verificar(System.nanoTime() - inicio < TimeUnit.SECONDS.toNanos(1), "El pedido bloquea el hilo");
            verificar(terminados.await(8, TimeUnit.SECONDS), "No terminó la preparación");
            verificar(bebidaEn.get() >= TimeUnit.MILLISECONDS.toNanos(1900)
                    && bebidaEn.get() < TimeUnit.SECONDS.toNanos(4), "La bebida no tarda unos 2 segundos");
            verificar(comidaEn.get() >= TimeUnit.MILLISECONDS.toNanos(4900)
                    && comidaEn.get() < TimeUnit.MILLISECONDS.toNanos(6500), "La cocina no trabaja en paralelo");
        } finally {
            planificador.shutdownNow();
        }
    }
}
