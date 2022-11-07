package com.poc.boot.rabbitmq.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.poc.boot.rabbitmq.model.Order;

public interface OrderMessageSender {

    void sendOrder(Order order) throws JsonProcessingException;
}
