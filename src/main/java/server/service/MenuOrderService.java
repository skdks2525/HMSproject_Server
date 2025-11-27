package server.service;

import server.model.MenuOrder;
import server.repository.MenuOrderRepository;
import java.util.List;

public class MenuOrderService {
    private final MenuOrderRepository orderRepository;

    public MenuOrderService() {
        this.orderRepository = new MenuOrderRepository();
    }

    public List<MenuOrder> getAllOrders() {
        return orderRepository.findAll();
    }

    public void saveOrder(MenuOrder order) {
        orderRepository.save(order);
    }
}
