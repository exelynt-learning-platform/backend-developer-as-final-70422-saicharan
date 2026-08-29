package com.exelynt.booking.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.exelynt.booking.dto.ResourceRequest;
import com.exelynt.booking.dto.ResourceResponse;
import com.exelynt.booking.entity.Resource;
import com.exelynt.booking.exception.ResourceNotFoundException;
import com.exelynt.booking.repository.ResourceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;

    public List<ResourceResponse> getAll() {
        return resourceRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public ResourceResponse getById(Long id) {
        return toResponse(findResourceById(id));
    }

    public ResourceResponse create(ResourceRequest request) {
        Resource resource = Resource.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType())
                .price(request.getPrice())
                .available(request.isAvailable())
                .build();

        return toResponse(resourceRepository.save(resource));
    }

    public ResourceResponse update(Long id, ResourceRequest request) {
        Resource resource = findResourceById(id);
        resource.setName(request.getName());
        resource.setDescription(request.getDescription());
        resource.setType(request.getType());
        resource.setPrice(request.getPrice());
        resource.setAvailable(request.isAvailable());

        return toResponse(resourceRepository.save(resource));
    }

    public void delete(Long id) {
        resourceRepository.delete(findResourceById(id));
    }

    private Resource findResourceById(Long id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));
    }

    private ResourceResponse toResponse(Resource resource) {
        return ResourceResponse.builder()
                .id(resource.getId())
                .name(resource.getName())
                .description(resource.getDescription())
                .type(resource.getType())
                .price(resource.getPrice())
                .available(resource.isAvailable())
                .build();
    }
}
