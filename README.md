# Post-contenido Unidad 7: Patrones Arquitectónicos I

Nicolás Andrés Sánchez Villamizar · Patrones de Diseño de Software · UDES

## Descripción

Backend de un sistema de multas de biblioteca universitaria hecho con Spring Boot. La biblioteca registra una multa cuando un estudiante devuelve un libro tarde, el sistema calcula el monto y luego la multa se marca como pagada. Esta primera entrega cubre la Parte 1: la API REST organizada en arquitectura en capas sobre H2.

## Parte 1: Arquitectura en capas

```
sanchez-post1-u7-patrones/
└── multas-biblioteca-api/
    ├── pom.xml
    └── src/main/java/com/example/multas/
        ├── controller/     Presentación: MultaController, GenerarMultaRequest, GlobalExceptionHandler
        ├── service/        Aplicación: MultaService
        ├── model/          Dominio: Multa, EstadoMulta y excepciones de negocio
        ├── repository/     Infraestructura: MultaRepository
        └── MultasApplication.java
```

El controlador solo conoce a `MultaService`, el servicio solo conoce al repositorio y al modelo, y el modelo no conoce a nadie. Las dos reglas de negocio quedaron así:

- El cálculo del monto (500 por día con tope de 15000) vive en la entidad: [`Multa.calcularMonto`](https://github.com/NicolasSnchz/sanchez-post1-u7-patrones/blob/main/multas-biblioteca-api/src/main/java/com/example/multas/model/Multa.java#L53).
- El tope de 3 multas pendientes lo decide el servicio en [`MultaService.generar`](https://github.com/NicolasSnchz/sanchez-post1-u7-patrones/blob/main/multas-biblioteca-api/src/main/java/com/example/multas/service/MultaService.java#L38), usando la consulta [`countByEstudianteIdAndEstado`](https://github.com/NicolasSnchz/sanchez-post1-u7-patrones/blob/main/multas-biblioteca-api/src/main/java/com/example/multas/repository/MultaRepository.java#L15).

## Endpoints

| Método | Ruta | Respuesta |
|---|---|---|
| GET | `/api/multas` | 200 con la lista |
| GET | `/api/multas/{id}` | 200, o 404 si no existe |
| GET | `/api/multas/estudiante/{estudianteId}` | 200 con las multas del estudiante |
| POST | `/api/multas` | 201, 400 si hay datos inválidos, 409 si supera el tope |
| PATCH | `/api/multas/{id}/pagar` | 200, o 409 si ya estaba pagada |

## Cómo ejecutar

```
cd multas-biblioteca-api
mvn clean package
mvn spring-boot:run
```

La API queda en `http://localhost:8080/api/multas` y la consola de H2 en `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:multas_biblioteca_db`, usuario `sa`).

## Capturas

![GET vacío](docs/capturas/p1-01-listar-vacio.png)
![POST 201](docs/capturas/p1-02-generar-201.png)
![POST 400](docs/capturas/p1-03-validacion-400.png)
![Tope 409](docs/capturas/p1-04-limite-409.png)
![GET 404](docs/capturas/p1-05-no-encontrada-404.png)
![PATCH pagar](docs/capturas/p1-06-pagar-ventanilla.png)
![Navegador](docs/capturas/p1-07-navegador-estudiante.png)

El README se completa con la Parte 2 y las decisiones de diseño en la entrega final.
