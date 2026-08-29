package com.exelynt.booking.dto;

import java.math.BigDecimal;

import com.exelynt.booking.entity.ResourceType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceResponse {

    private Long id;
    private String name;
    private String description;
    private ResourceType type;
    private BigDecimal price;
    private boolean available;
}
