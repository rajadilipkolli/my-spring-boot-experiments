package com.poc.boot.rabbitmq.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.poc.boot.rabbitmq.model.Order;
import com.poc.boot.rabbitmq.service.OrderMessageSender;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class MessageController {

    private final OrderMessageSender orderMessageSender;

    @PostMapping("/sendMsg")
    public String handleMessage(
            @ModelAttribute Order order, RedirectAttributes redirectAttributes) {
        try {
            this.orderMessageSender.sendOrder(order);
            redirectAttributes.addFlashAttribute("message", "Order message sent successfully");
            return "redirect:/";
        } catch (JsonProcessingException exc) {
            // Spring 5 + way of throwing exception.
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR, "Unable To Parse " + order.toString(), exc);
        }
    }
}
