package com.firatdeneme.order_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Value("${server.port}")
    private String serverport;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private EntityManager entityManager;

    @GetMapping("/call-customer")
    public String callCustomerService(){
        String url = "http://customer-service/customer/hello";
        return restTemplate.getForObject(url, String.class);
    }

    @GetMapping("/status")
    public String sendOrder(HttpServletRequest request) {
        if("8083".equals(serverport)){
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return "Order processed successfully by Order Service (Port: " + serverport + ").";
    }


    @GetMapping("/send-order")
    public ModelAndView showOrderForm() {
        return new ModelAndView("postOrder");
    }

    @GetMapping("/show-orders")
    public List<Map<String, Object>> getAllOrders() {
        List<Map<String, Object>> ordersList = new ArrayList<>();
        try {
            Query query = entityManager.createNativeQuery(
                    "SELECT ID, NAME, EMAIL, ORDER_ID, ITEM_NAME FROM CUSTOMERS WHERE ORDER_ID IS NOT NULL"
            );
            List<?> results = query.getResultList();

            for (Object result : results) {
                Object[] row = (Object[]) result;
                Map<String, Object> orderMap = new HashMap<>();
                orderMap.put("customerId", row[0]);
                orderMap.put("name", row[1]);
                orderMap.put("email", row[2]);
                orderMap.put("orderId", row[3]);
                orderMap.put("itemName", row[4]);
                ordersList.add(orderMap);
            }
        } catch (Exception e) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("error", "Database error: " + e.getMessage());
            return List.of(errorMap);
        }
        return ordersList;
    }

    @PostMapping("/send-order")
    public ModelAndView processOrder(@RequestParam("name") String name,
                                     @RequestParam("email") String email,
                                     @RequestParam("itemName") String itemName) {
        ModelAndView modelAndView = new ModelAndView("postOrder");
        try {
            long generatedOrderId = (long) (Math.random() * 100000);

            String orderPayload = String.format(
                    "{\"name\":\"%s\", \"email\":\"%s\", \"orderId\":%d, \"itemName\":\"%s\"}",
                    name, email, generatedOrderId, itemName
            );

            kafkaTemplate.send("order-topic", orderPayload);

            modelAndView.addObject("success", "Order request sent to Kafka! Assigned Order ID: " + generatedOrderId);
        } catch (Exception e) {
            modelAndView.addObject("error", "Failed to publish event: " + e.getMessage());
        }
        return modelAndView;
    }
}
