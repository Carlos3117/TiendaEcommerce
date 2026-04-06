package com.example.unimagdalena.TiendaEcommerce.services;

import com.example.unimagdalena.TiendaEcommerce.entities.*;
import com.example.unimagdalena.TiendaEcommerce.enums.CustomerStatus;
import com.example.unimagdalena.TiendaEcommerce.enums.OrderStatus;
import com.example.unimagdalena.TiendaEcommerce.exceptions.BusinessException;
import com.example.unimagdalena.TiendaEcommerce.exceptions.ResourceNotFoundException;
import com.example.unimagdalena.TiendaEcommerce.exceptions.ValidationException;
import com.example.unimagdalena.TiendaEcommerce.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;

    @Override
    public Order createOrder(Long customerId, Long addressId, List<OrderItem> items) {

        if (customerId == null) {
            throw new ValidationException("El cliente es obligatorio");
        }

        if (addressId == null) {
            throw new ValidationException("La dirección es obligatoria");
        }

        if (items == null || items.isEmpty()) {
            throw new BusinessException("El pedido debe contener al menos un ítem");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new BusinessException("El cliente no está activo");
        }

        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Dirección no encontrada"));

        if (!address.getCustomer().getId().equals(customer.getId())) {
            throw new BusinessException("La dirección no pertenece al cliente");
        }

        Order order = new Order();
        order.setCustomer(customer);
        order.setAddress(address);
        order.setStatus(OrderStatus.CREATED);

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItem item : items) {
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new ValidationException("Cantidad inválida");
            }

            if (item.getProduct() == null || item.getProduct().getId() == null) {
                throw new ValidationException("El producto es obligatorio");
            }

            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

            if (!product.getActive()) {
                throw new BusinessException("Producto inactivo: " + product.getName());
            }

            if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Producto con precio inválido: " + product.getName());
            }

            BigDecimal unitPrice = product.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

            OrderItem newItem = new OrderItem();
            newItem.setOrder(order);
            newItem.setProduct(product);
            newItem.setQuantity(item.getQuantity());
            newItem.setUnitPrice(unitPrice);
            newItem.setSubtotal(subtotal);

            total = total.add(subtotal);
            orderItems.add(newItem);
        }

        order.setItems(orderItems);
        order.setTotal(total);

        Order savedOrder = orderRepository.save(order);

        saveHistory(savedOrder, OrderStatus.CREATED);

        return savedOrder;
    }


    @Override
    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        return getOrderOrThrow(id);
    }


    @Override
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }


    @Override
    public void payOrder(Long orderId) {

        Order order = getOrderOrThrow(orderId);

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new BusinessException("Solo pedidos en estado CREATED pueden pagarse");
        }

        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new BusinessException("El pedido no tiene ítems");
        }


        for (OrderItem item : order.getItems()) {

            if (item.getProduct() == null) {
                throw new BusinessException("Ítem sin producto asociado");
            }

            if (!item.getProduct().getActive()) {
                throw new BusinessException("Producto inactivo: " + item.getProduct().getName());
            }

            Inventory inventory = item.getProduct().getInventory();

            if (inventory == null) {
                throw new BusinessException("Producto sin inventario asociado");
            }

            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new BusinessException("Cantidad inválida en ítems");
            }

            if (inventory.getStock() < item.getQuantity()) {
                throw new BusinessException(
                        "Stock insuficiente para el producto: " + item.getProduct().getName()
                );
            }
        }

        for (OrderItem item : order.getItems()) {
            Inventory inventory = item.getProduct().getInventory();
            inventory.setStock(inventory.getStock() - item.getQuantity());
            inventoryRepository.save(inventory);
        }


        BigDecimal total = BigDecimal.ZERO;

        for (OrderItem item : order.getItems()) {

            BigDecimal unitPrice = item.getProduct().getPrice();
            item.setUnitPrice(unitPrice);

            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            item.setSubtotal(subtotal);

            total = total.add(subtotal);
        }

        order.setTotal(total);


        order.setStatus(OrderStatus.PAID);

        orderRepository.save(order);

        saveHistory(order, OrderStatus.PAID);
    }


    @Override
    public void shipOrder(Long orderId) {

        Order order = getOrderOrThrow(orderId);

        if (order.getStatus() != OrderStatus.PAID) {
            throw new BusinessException("Solo pedidos en estado PAID pueden enviarse");
        }

        order.setStatus(OrderStatus.SHIPPED);
        orderRepository.save(order);

        saveHistory(order, OrderStatus.SHIPPED);
    }

    @Override
    public void deliverOrder(Long orderId) {

        Order order = getOrderOrThrow(orderId);

        if (order.getStatus() != OrderStatus.SHIPPED) {
            throw new BusinessException("Solo pedidos enviados pueden marcarse como entregados");
        }

        order.setStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);

        saveHistory(order, OrderStatus.DELIVERED);
    }


    @Override
    public void cancelOrder(Long orderId) {

        Order order = getOrderOrThrow(orderId);

        switch (order.getStatus()) {

            case CREATED:
                order.setStatus(OrderStatus.CANCELLED);
                break;

            case PAID:

                for (OrderItem item : order.getItems()) {

                    if (item.getProduct() == null || item.getProduct().getInventory() == null) {
                        throw new BusinessException("Producto sin inventario asociado");
                    }
                    Inventory inventory = item.getProduct().getInventory();
                    inventory.setStock(inventory.getStock() + item.getQuantity());
                    inventoryRepository.save(inventory);
                }

                order.setStatus(OrderStatus.CANCELLED);
                break;

            case SHIPPED:
                throw new BusinessException("No se puede cancelar un pedido ya enviado");

            case DELIVERED:
                throw new BusinessException("No se puede cancelar un pedido entregado");

            case CANCELLED:
                throw new BusinessException("El pedido ya está cancelado");
        }

        orderRepository.save(order);

        saveHistory(order, OrderStatus.CANCELLED);
    }


    private Order getOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
    }


    private void saveHistory(Order order, OrderStatus status) {
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setStatus(status);
        historyRepository.save(history);
    }
}