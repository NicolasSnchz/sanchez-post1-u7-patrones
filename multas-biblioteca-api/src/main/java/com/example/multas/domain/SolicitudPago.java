package com.example.multas.domain;

import java.math.BigDecimal;

// Datos que el dominio entrega a cualquier pasarela. Es Java puro para que el puerto
// no dependa de la entidad JPA Multa (que importa jakarta.persistence).
public record SolicitudPago(
        Long multaId,
        String estudianteId,
        BigDecimal monto
) {
}
