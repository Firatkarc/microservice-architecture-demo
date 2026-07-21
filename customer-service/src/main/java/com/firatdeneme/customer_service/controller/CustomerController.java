package com.firatdeneme.customer_service.controller;


import com.firatdeneme.customer_service.model.Customer;
import com.firatdeneme.customer_service.repository.CustomerRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/customer")
@RefreshScope
public class CustomerController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CustomerRepository customerRepository;

    @GetMapping("/status")
    public String getCustomerServiceStatus(){return "Customer Service is running successfully.";}

    @PostMapping("/insert")
    public String insertCustomer(@RequestBody Customer customer) {
        Customer savedCustomer = customerRepository.save(customer);
        return "Customer successfully saved to H2 DB with ID: " + savedCustomer.getId();
    }



    @GetMapping("/whoami")
    public String getAuthenticatedUser(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        return "Authenticated user: " + userId;
    }

    @GetMapping("/order-status")
    @Retry(name = "orderServiceRetry", fallbackMethod = "handleOrderServiceFailure")
    @CircuitBreaker(name = "orderServiceCB", fallbackMethod = "handleOrderServiceFailure")
    public String callOrderService() {

            String url = "http://order-service/order/status";
            return restTemplate.getForObject(url, String.class);

    }
    public String handleOrderServiceFailure(Throwable throwable) {
        return "Order services are down at the moment.Please try again later.";
    }


}
