package taberna;

/** Una línea por mensaje, incluso cuando publican varios hilos. */
public final class Salida {
    private final long inicio = System.nanoTime();
    private final boolean color;

    public Salida(boolean color) {
        this.color = color;
    }

    public synchronized void mostrar(String componente, String mensaje) {
        double segundos = (System.nanoTime() - inicio) / 1_000_000_000.0;
        String prefijo = color && componente.equals("PANEL LED") ? "\u001B[36m" : "";
        String fin = prefijo.isEmpty() ? "" : "\u001B[0m";
        System.out.printf(java.util.Locale.ROOT, "%s[%5.2f s] [%-10s] %s%s%n",
                prefijo, segundos, componente, mensaje, fin);
    }
}
