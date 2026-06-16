package com.campusresolver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the AI-Powered Campus Issue Resolution System.
 *
 * Architecture Overview:
 * ┌──────────────────────────────────────────────────────┐
 * │  Client (Browser / Postman)                          │
 * └──────────────┬───────────────────────────────────────┘
 *                │ HTTP REST
 * ┌──────────────▼───────────────────────────────────────┐
 * │  Controller Layer  (REST endpoints)                  │
 * └──────────────┬───────────────────────────────────────┘
 *                │
 * ┌──────────────▼───────────────────────────────────────┐
 * │  Service Layer  (Business Logic)                     │
 * │  ┌────────────────┐  ┌──────────────────────────┐   │
 * │  │ ComplaintSvc   │  │ AIService (OpenRouter)   │   │
 * │  └────────────────┘  └──────────────────────────┘   │
 * └──────────────┬───────────────────────────────────────┘
 *                │
 * ┌──────────────▼───────────────────────────────────────┐
 * │  Repository Layer  (Spring Data JPA)                 │
 * └──────────────┬───────────────────────────────────────┘
 *                │
 * ┌──────────────▼───────────────────────────────────────┐
 * │  MySQL Database                                      │
 * └──────────────────────────────────────────────────────┘
 */
@SpringBootApplication
public class CampusResolverApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusResolverApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("  Campus Issue Resolution System");
        System.out.println("  Running at: http://localhost:8080");
        System.out.println("========================================\n");
    }
}
