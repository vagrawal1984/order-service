package com.va.os.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.va.os.api.common.Payment;
import com.va.os.api.common.TransactionRequest;
import com.va.os.api.common.TransactionResponse;
import com.va.os.api.entity.Order;
import com.va.os.api.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OrderService {

    @Autowired
    private OrderRepository repository;

    @Autowired
    private RestTemplate restTemplate;

    private Logger log = LoggerFactory.getLogger(OrderService.class);

    public TransactionResponse saveOrder(TransactionRequest request) throws JsonProcessingException {
        Order order = request.getOrder();
        Payment payment = request.getPayment();
        payment.setOrderId(order.getId());
        payment.setAmount(order.getPrice());
        //rest call
        log.info("Order Service request : {}",new ObjectMapper().writeValueAsString(request));
       Payment paymentResponse = restTemplate.postForObject("http://PAYMENT-SERVICE/payment/doPayment",payment,Payment.class);

        log.info("Payment Service response from Order service : {}",new ObjectMapper().writeValueAsString(paymentResponse));
       String response = paymentResponse.getPaymentStatus().equals("success")?"Order Placed Successfully":"Issue in Payment Service";

       repository.save(order);
        return new TransactionResponse(order,paymentResponse.getAmount(),paymentResponse.getTransactionId(),response);
    }


}
