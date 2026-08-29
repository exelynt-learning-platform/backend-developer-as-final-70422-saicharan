package com.exelynt.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.exelynt.booking.entity.Resource;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
}
