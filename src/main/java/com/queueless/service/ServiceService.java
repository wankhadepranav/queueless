package com.queueless.service;

import com.queueless.entity.Service;
import com.queueless.repository.ServiceRepository;

import java.util.List;

@org.springframework.stereotype.Service
public class ServiceService {

    private final ServiceRepository serviceRepository;

    public ServiceService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }
    

    public List<Service> getAllServices() {
        return serviceRepository.findAll();
    }

    public Service saveService(Service service) {
        return serviceRepository.save(service);
    }
    public void deleteService(Long id) {
    serviceRepository.deleteById(id);
}
}