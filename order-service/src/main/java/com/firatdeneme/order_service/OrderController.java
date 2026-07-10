package com.firatdeneme.order_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Value("${server.port}")
    private String serverport;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/call-customer")
    public String callCustomerService(){
        String url = "http://customer-service/customer/hello";

        return restTemplate.getForObject(url,String.class);
    }

    @GetMapping("/status")
    public String sendOrder(HttpServletRequest request)
    {   if("8082".equals(serverport)){
        try{
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
        return "Order processed successfully by Order Service (Port: " + serverport + ").";}
}
