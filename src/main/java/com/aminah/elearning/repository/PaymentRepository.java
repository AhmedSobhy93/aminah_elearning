package com.aminah.elearning.repository;

import com.aminah.elearning.model.Payment;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.mongodb.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByGatewayAndGatewayOrderId(String gateway, String gatewayOrderId);
}
