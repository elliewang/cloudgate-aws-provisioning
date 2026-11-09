package com.example.portal;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
  @GetMapping("/healthz") public Map<String,String> health() {
    return Map.of("status", "UP", "mode", "DEMO_NO_AWS");
  }
}
