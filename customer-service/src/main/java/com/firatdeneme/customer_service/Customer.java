package com.firatdeneme.customer_service;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private Long orderId;
    private String itemName;

    public Customer() {}

    public Customer(String name, String email) {
        this.name = name;
        this.email = email;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Long getOrderId() {return orderId;}
    public void setOrderId(Long orderId) {this.orderId = orderId;}

    public String getItemName() {return itemName;}
    public void setItemName(String itemName) {this.itemName = itemName;}
}