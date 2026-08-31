package com.aminah.elearning.repository;

import com.aminah.elearning.model.Payment;
import com.aminah.elearning.model.CourseEnrollment;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.mongodb.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByGatewayAndGatewayOrderId(String gateway, String gatewayOrderId);

    Optional<Payment> findByCourseEnrollment(CourseEnrollment enrollment);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.gateway = :gateway and p.gatewayOrderId = :gatewayOrderId")
    Optional<Payment> findForUpdateByGatewayOrder(
            @Param("gateway") String gateway,
            @Param("gatewayOrderId") String gatewayOrderId
    );
}
