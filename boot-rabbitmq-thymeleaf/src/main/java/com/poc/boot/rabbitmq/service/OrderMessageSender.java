package com.poc.boot.rabbitmq.service;

import com.poc.boot.rabbitmq.model.Order;

public interface OrderMessageSender {

    void sendOrder(Order order);
}
