import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simulador local de las dos pasarelas para probar el pago en linea sin servicios reales.
 * No hace parte del build de Maven. Se ejecuta en otra terminal con:
 *   java simulador/SimuladorPasarelas.java
 *
 * PagosUDES (puerto 9001): recibe {estudianteId, monto} y responde {idTransaccion, estadoTransaccion}.
 * Wompi     (puerto 9002): recibe {reference, amountInCents} y responde {reference, status}.
 * Regla de prueba: aprueba pagos hasta 10000 pesos y rechaza los mayores (limite por transaccion).
 */
public class SimuladorPasarelas {

    private static final BigDecimal LIMITE_PESOS = new BigDecimal("10000");
    private static final AtomicInteger SECUENCIA = new AtomicInteger(1000);

    public static void main(String[] args) throws IOException {
        HttpServer pagosUdes = HttpServer.create(new InetSocketAddress(9001), 0);
        pagosUdes.createContext("/pagosudes/transacciones", SimuladorPasarelas::pagosUdes);
        pagosUdes.start();

        HttpServer wompi = HttpServer.create(new InetSocketAddress(9002), 0);
        wompi.createContext("/wompi/transactions", SimuladorPasarelas::wompi);
        wompi.start();

        System.out.println("Simulador PagosUDES en http://localhost:9001/pagosudes/transacciones");
        System.out.println("Simulador Wompi en http://localhost:9002/wompi/transactions");
    }

    private static void pagosUdes(HttpExchange ex) throws IOException {
        String body = leer(ex);
        BigDecimal monto = new BigDecimal(campo(body, "\"monto\"\\s*:\\s*([0-9.]+)"));
        String estado = monto.compareTo(LIMITE_PESOS) <= 0 ? "APROBADA" : "RECHAZADA";
        String id = "PU-" + SECUENCIA.incrementAndGet();
        System.out.println("[PagosUDES] recibido " + body + " -> " + estado);
        responder(ex, "{\"idTransaccion\":\"" + id + "\",\"estadoTransaccion\":\"" + estado + "\"}");
    }

    private static void wompi(HttpExchange ex) throws IOException {
        String body = leer(ex);
        long centavos = Long.parseLong(campo(body, "\"amountInCents\"\\s*:\\s*([0-9]+)"));
        String referencia = campo(body, "\"reference\"\\s*:\\s*\"([^\"]*)\"");
        String estado = centavos <= LIMITE_PESOS.longValue() * 100 ? "APPROVED" : "DECLINED";
        System.out.println("[Wompi] recibido " + body + " -> " + estado);
        responder(ex, "{\"reference\":\"" + referencia + "\",\"status\":\"" + estado + "\"}");
    }

    private static String leer(HttpExchange ex) throws IOException {
        return new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static String campo(String json, String regex) {
        Matcher m = Pattern.compile(regex).matcher(json);
        if (!m.find()) {
            throw new IllegalArgumentException("Campo no encontrado en " + json);
        }
        return m.group(1);
    }

    private static void responder(HttpExchange ex, String json) throws IOException {
        byte[] out = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json");
        ex.sendResponseHeaders(200, out.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(out);
        }
    }
}
