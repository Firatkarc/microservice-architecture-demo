package com.firatdeneme.customer_service.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderConsumerServiceTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query selectQuery;

    @Mock
    private Query updateOrInsertQuery;

    @InjectMocks
    private OrderConsumerService orderConsumerService;

    @Test
    @DisplayName("Should update existing customer row when ORDER_ID is NULL")
    void shouldUpdateExistingCustomerRow() {
        // 1. ARRANGE
        String jsonMessage = "{"
                + "\"name\":\"Firat\","
                + "\"email\":\"firat@example.com\","
                + "\"orderId\":101,"
                + "\"itemName\":\"MacBook Air M1\""
                + "}";

        // Mock the SELECT query to return an existing customer ID (e.g. 1L)
        when(entityManager.createNativeQuery(contains("SELECT"))).thenReturn(selectQuery);
        when(selectQuery.getResultList()).thenReturn(List.of(1L));

        // Mock the UPDATE query execution
        when(entityManager.createNativeQuery(contains("UPDATE"))).thenReturn(updateOrInsertQuery);
        when(updateOrInsertQuery.executeUpdate()).thenReturn(1);

        // =================================================================
        // 2. ACT
        // =================================================================
        orderConsumerService.consumeOrder(jsonMessage);

        // 3. ASSERT
        verify(selectQuery).setParameter(1, "Firat");
        verify(selectQuery).setParameter(2, "firat@example.com");

        verify(updateOrInsertQuery).setParameter(1, 101);
        verify(updateOrInsertQuery).setParameter(2, "MacBook Air M1");
        verify(updateOrInsertQuery).setParameter(3, 1L);
        verify(updateOrInsertQuery, times(1)).executeUpdate();
    }



    @Test
    @DisplayName("Should handle invalid JSON payload gracefully without throwing exception")
    void shouldHandleInvalidJsonGracefully() {
        // 1. ARRANGE
        String malformedJson = "invalid-json-string";

        // =================================================================
        // 2. ACT
        // =================================================================
        orderConsumerService.consumeOrder(malformedJson);

        // 3. ASSERT
        // Verify that native queries are never executed if JSON parsing fails
        verify(entityManager, never()).createNativeQuery(anyString());
    }
}