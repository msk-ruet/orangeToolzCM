package com.orangetoolz.customer.app.repository;

import com.orangetoolz.customer.app.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer,Integer> {

}
