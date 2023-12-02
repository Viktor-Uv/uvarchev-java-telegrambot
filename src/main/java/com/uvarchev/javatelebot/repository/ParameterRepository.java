package com.uvarchev.javatelebot.repository;

import com.uvarchev.javatelebot.entity.Parameter;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParameterRepository extends CrudRepository<Parameter, Long> {
}
