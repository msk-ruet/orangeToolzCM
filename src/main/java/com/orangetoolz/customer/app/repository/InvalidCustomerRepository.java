package com.orangetoolz.customer.app.repository;

import com.orangetoolz.customer.app.entity.InvalidCustomer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvalidCustomerRepository extends JpaRepository<InvalidCustomer,Integer> {
}
