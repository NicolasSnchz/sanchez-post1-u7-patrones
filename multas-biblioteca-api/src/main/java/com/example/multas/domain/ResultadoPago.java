package com.example.multas.domain;

// Tipo de dominio: ninguna pasarela concreta se expone hacia MultaService, solo este contrato comun.
public record ResultadoPago(
        String proveedor,
        boolean exitoso,
        String referenciaExterna,
        String mensaje
) {
}
