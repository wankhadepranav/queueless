package com.queueless.controller;

import com.queueless.entity.Service;
import com.queueless.service.ServiceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final ServiceService serviceService;

    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @GetMapping
    public List<Service> getAllServices() {
        return serviceService.getAllServices();
    }

    @PostMapping
    public Service createService(@RequestBody Service service) {
        return serviceService.saveService(service);
    }
    @DeleteMapping("/{id}")
public void deleteService(@PathVariable Long id) {
    serviceService.deleteService(id);
}
}