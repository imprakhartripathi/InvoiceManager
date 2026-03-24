package com.invoicemanager.server.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.invoicemanager.server.model.Template;

public interface TemplateRepository extends MongoRepository<Template, String> {
    List<Template> findByUserId(String userId);

    Optional<Template> findByIdAndUserId(String id, String userId);
}
