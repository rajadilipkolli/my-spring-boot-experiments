package com.example.demo.listener;

public record NotificationEvent(String channel, String message, Integer pid) {}
