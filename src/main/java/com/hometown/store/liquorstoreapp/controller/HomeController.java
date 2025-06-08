package com.hometown.store.liquorstoreapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "forward:/index.html";
    }

    @GetMapping("/api/health")
    @ResponseBody
    public String health() {
        return "{ \"status\": \"UP\", \"message\": \"Liquor Store App is running!\" }";
    }

    @GetMapping("/admin")
    @ResponseBody
    public String admin() {
        return "<h1>Admin Panel - Liquor Store App</h1>" +
               "<p>Application is running successfully.</p>" +
               "<ul>" +
               "<li><a href='/h2-console'>H2 Database Console</a></li>" +
               "<li><a href='/api/health'>Health Check</a></li>" +
               "<li><a href='/'>Main Application</a></li>" +
               "</ul>";
    }
}