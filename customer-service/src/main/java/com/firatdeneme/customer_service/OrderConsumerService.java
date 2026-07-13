package com.firatdeneme.customer_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.List;
import java.util.Map;

@Service
public class OrderConsumerService {

    @Autowired
    private EntityManager entityManager;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "order-topic", groupId = "customer-group")
    @Transactional
    public void consumeOrder(String message) {
        try {
            // Parse the incoming JSON message string into a map
            Map<String, Object> orderData = objectMapper.readValue(message, Map.class);
            String name = (String) orderData.get("name");
            String email = (String) orderData.get("email");
            Integer orderId = (Integer) orderData.get("orderId");
            String itemName = (String) orderData.get("itemName");

            // Execute database check and update/insert logic locally inside customer-service
            Query findNullOrderQuery = entityManager.createNativeQuery(
                    "SELECT ID FROM CUSTOMERS WHERE NAME = ?1 AND EMAIL = ?2 AND ORDER_ID IS NULL"
            );
            findNullOrderQuery.setParameter(1, name);
            findNullOrderQuery.setParameter(2, email);

            List<?> nullOrderResults = findNullOrderQuery.getResultList();

            if (!nullOrderResults.isEmpty()) {
                Long customerId = ((Number) nullOrderResults.get(0)).longValue();
                Query updateQuery = entityManager.createNativeQuery(
                        "UPDATE CUSTOMERS SET ORDER_ID = ?1, ITEM_NAME = ?2 WHERE ID = ?3"
                );
                updateQuery.setParameter(1, orderId);
                updateQuery.setParameter(2, itemName);
                updateQuery.setParameter(3, customerId);
                updateQuery.executeUpdate();
                System.out.println("Kafka Event Handled: Updated existing customer row.");
            } else {
                Query insertQuery = entityManager.createNativeQuery(
                        "INSERT INTO CUSTOMERS (NAME, EMAIL, ORDER_ID, ITEM_NAME) VALUES (?1, ?2, ?3, ?4)"
                );
                insertQuery.setParameter(1, name);
                insertQuery.setParameter(2, email);
                insertQuery.setParameter(3, orderId);
                insertQuery.setParameter(4, itemName);
                insertQuery.executeUpdate();
                System.out.println("Kafka Event Handled: Inserted new order row.");
            }
        } catch (Exception e) {
            System.err.println("Error processing Kafka message: " + e.getMessage());
        }
    }
}