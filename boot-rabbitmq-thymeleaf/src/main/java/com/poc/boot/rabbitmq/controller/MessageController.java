package com.poc.boot.rabbitmq.controller;

import com.poc.boot.rabbitmq.model.Order;
import com.poc.boot.rabbitmq.service.OrderMessageSender;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
class MessageController {

    private final OrderMessageSender orderMessageSender;

    MessageController(OrderMessageSender orderMessageSender) {
        this.orderMessageSender = orderMessageSender;
    }

    @PostMapping("/sendMsg")
    String handleMessage(@ModelAttribute Order order, RedirectAttributes redirectAttributes) {
        this.orderMessageSender.sendOrder(order);
        redirectAttributes.addFlashAttribute("message", "Order message sent successfully");
        return "redirect:/";
    }
}
