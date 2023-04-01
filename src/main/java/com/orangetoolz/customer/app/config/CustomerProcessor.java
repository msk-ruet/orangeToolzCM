package com.orangetoolz.customer.app.config;

import com.orangetoolz.customer.app.entity.Customer;
import org.springframework.batch.item.ItemProcessor;


public class CustomerProcessor implements ItemProcessor<Customer,Customer> {

    @Override
    public Customer process(Customer customer) throws Exception {
        return customer;
    }
}
