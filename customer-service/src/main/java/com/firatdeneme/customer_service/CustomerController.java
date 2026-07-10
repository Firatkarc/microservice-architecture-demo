package com.firatdeneme.customer_service;


import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/customer")
@RefreshScope
public class CustomerController {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${deneme}")
    private String denemeConfig;

    @GetMapping("/refresh-deneme")
    public String getConfigurationValue() {
        return "Current configuration value:  " + denemeConfig;
    }


    @GetMapping("/status")
    public String getCustomerServiceStatus(){return "Customer Service is running successfully.";}



    @GetMapping("/whoami")
    public String getAuthenticatedUser(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        return "Authenticated user: " + userId;
    }

    @GetMapping("/order-status")
    @Retry(name = "orderServiceRetry", fallbackMethod = "handleOrderServiceFailure")
    @CircuitBreaker(name = "orderServiceCB", fallbackMethod = "handleOrderServiceFailure")
    public String callOrderService() {

            String url = "http://order-service/order/ordered";
            return restTemplate.getForObject(url, String.class);

    }
    public String handleOrderServiceFailure(Throwable throwable) {
        return "Order services are down at the moment.Please try again later.";
    }
}
