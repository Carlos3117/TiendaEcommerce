# E-commerce - Tienda Universitaria

## Descripción del proyecto

Este proyecto corresponde al desarrollo del backend de una tienda universitaria, enfocado en la implementación de la capa de persistencia (Repository) y la lógica de negocio (Service).

El sistema permite gestionar clientes, direcciones, categorías, productos, inventario, pedidos y reportes, siguiendo las reglas de negocio definidas en el enunciado del taller.

La implementación se basa en una arquitectura por capas utilizando Java 21, Spring Boot y JPA.

---

## Arquitectura del sistema

El proyecto sigue una arquitectura en capas:

### Capas implementadas:

- Entities: Representación del modelo entidad-relación
- Repositories: Acceso a datos con Spring Data JPA
- Services: Implementación de reglas de negocio
- DTOs: Objetos de transferencia (request/response)
- Mappers: Conversión entre entidades y DTOs (MapStruct)
- Exceptions: Manejo de errores de negocio

Nota: La capa Controller no fue implementada en esta fase del proyecto.

---

## Modelo de datos

Se diseñó un modelo entidad-relación compuesto por las siguientes entidades:

- Customer
- Address
- Category
- Product
- Inventory
- Order
- OrderItem
- OrderStatusHistory

### Relaciones principales:

- Un cliente tiene múltiples direcciones y pedidos
- Un producto pertenece a una categoría
- Un producto tiene un inventario asociado
- Un pedido contiene múltiples ítems
- Un pedido registra su historial de estados

---

## Reglas de negocio implementadas

### Productos e inventario

- El SKU es único dentro del sistema
- El precio del producto debe ser mayor que cero
- El stock y el stock mínimo no pueden ser negativos
- No se puede desactivar un producto con pedidos activos

---

### Creación de pedidos

Implementado en `OrderServiceImpl`:

- El pedido debe contener al menos un ítem
- El cliente debe existir y estar activo
- La dirección debe pertenecer al cliente
- Cada producto debe existir y estar activo
- El subtotal se calcula como cantidad por precio unitario
- El total del pedido se calcula automáticamente
- El estado inicial del pedido es `CREATED`

---

### Pago de pedidos

- Solo pedidos en estado `CREATED` pueden pagarse
- Se valida que exista stock suficiente para todos los ítems
- Si un ítem no tiene stock suficiente, se rechaza todo el proceso
- Al pagar, se descuenta el inventario correspondiente

---

### Envío y entrega

- Solo pedidos en estado `PAID` pueden enviarse
- Solo pedidos en estado `SHIPPED` pueden entregarse

---

### Cancelación de pedidos

- Pedidos en estado `CREATED` se cancelan sin afectar inventario
- Pedidos en estado `PAID` revierten el stock
- Pedidos en estado `SHIPPED` o `DELIVERED` no pueden cancelarse

---

### Reportes

Implementados en `ReportServiceImpl`:

- Productos con bajo stock
- Pedidos filtrados por múltiples criterios
- Productos más vendidos
- Ingresos mensuales
- Clientes con mayor facturación

---

## Persistencia y consultas

Se implementaron repositorios con:

- Consultas derivadas (Query Methods)
- Consultas JPQL
- Filtros compuestos

### Ejemplos:

- Búsqueda de productos por SKU
- Productos activos por categoría
- Pedidos con filtros combinados
- Productos con stock insuficiente
- Ingresos mensuales agrupados
- Clientes con mayor facturación

---

## Pruebas automatizadas

Se implementaron pruebas utilizando:

- JUnit 5
- Testcontainers con PostgreSQL
- Repository Integration Tests

### Se validan:

- Consultas por filtros
- Productos con bajo stock
- Agregaciones (ventas, ingresos, clientes)

### Validación de lógica en Services

### Se prueban:

- Creación de pedidos
- Validación de stock
- Transiciones de estado
- Reversión de inventario

Las pruebas se ejecutan contra una base de datos PostgreSQL real levantada mediante contenedores.

---

## Tecnologías utilizadas

- Java 21
- Spring Boot
- Spring Data JPA
- PostgreSQL (usado en pruebas con Testcontainers)
- JUnit 5
- Mockito
- MapStruct
- Lombok

---

## Ejecución del proyecto

### Requisitos

Para ejecutar correctamente el proyecto es necesario contar con los siguientes componentes instalados:

- Java 21 correctamente configurado en el sistema
- Maven como herramienta de gestión de dependencias
- Docker instalado y en ejecución (requerido para Testcontainers)

El proyecto fue desarrollado y probado utilizando IntelliJ IDEA, por lo que se recomienda este entorno para facilitar la ejecución y depuración.

---

### Ejecución en IntelliJ IDEA

- Abrir IntelliJ IDEA
- Seleccionar "Open" y cargar la carpeta raíz del proyecto
- Esperar a que IntelliJ importe automáticamente el proyecto Maven
- Verificar que el SDK esté configurado en Java 21
- File → Project Structure → Project SDK

---

### Compilación del proyecto

Para compilar el proyecto desde IntelliJ IDEA:

- Ir al panel lateral de Maven (Maven Tool Window)
- Navegar a: Lifecycle
- Ejecutar la fase install

Esto permitirá:

- Descargar todas las dependencias necesarias
- Compilar el código fuente
- Ejecutar validaciones de compilación
- Generar los artefactos del proyecto

---

### Ejecución de pruebas

Las pruebas pueden ejecutarse desde IntelliJ IDEA de dos formas:

#### Opción 1: Desde Maven

- En el panel de Maven → Lifecycle → ejecutar test

#### Opción 2: Desde las clases de prueba

- Hacer clic derecho sobre una clase de test
- Seleccionar Run 'NombreDelTest'

Nota:  
Las pruebas utilizan Testcontainers, por lo que es necesario que Docker esté en ejecución. Durante la ejecución, se levantará automáticamente un contenedor con PostgreSQL.

---

## Manejo de excepciones

Se implementa un manejador global de excepciones que controla:

- ResourceNotFoundException
- ConflictException
- ValidationException
- BusinessException

Esto permite centralizar la gestión de errores y mantener consistencia en las respuestas.

---

## Decisiones de diseño

- Uso de DTOs para desacoplar la lógica de negocio del modelo de datos
- Uso de MapStruct para automatizar la conversión entre entidades y DTOs
- Separación clara de responsabilidades por capas
- Implementación de reglas de negocio en la capa Service
- Uso de Testcontainers para pruebas realistas con base de datos  