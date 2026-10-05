package com.example.multas.domain.port;

import com.example.multas.domain.ResultadoPago;
import com.example.multas.domain.SolicitudPago;

public interface PasarelaPagoPort {
    ResultadoPago procesar(SolicitudPago solicitud);
}
