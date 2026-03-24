package com.invoicemanager.server.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.invoicemanager.server.model.Invoice;

public interface InvoiceRepository extends MongoRepository<Invoice, String> {
    List<Invoice> findByUserId(String userId);

    Optional<Invoice> findByIdAndUserId(String id, String userId);

    Optional<Invoice> findByRazorpayOrderId(String razorpayOrderId);
}
