package com.orangetoolz.customer.app.service;

import com.orangetoolz.customer.app.entity.InvalidCustomer;
import com.orangetoolz.customer.app.repository.InvalidCustomerRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class InvalidCustomerService {
    @Autowired
    InvalidCustomerRepository invalidCustomerRepository;

    Logger logger = LoggerFactory.getLogger(InvalidCustomerService.class);

    /***
     *
     * @return CompletableFuture all invalidCustomer list
     */
    @Async
    public CompletableFuture<List<InvalidCustomer>> getAllInvalidCustomers(){
        logger.info("Invalid customer list by  "+Thread.currentThread().getName());
        List<InvalidCustomer> invalidCustomers =invalidCustomerRepository.findAll();
        return CompletableFuture.completedFuture(invalidCustomers);
    }

    /***
     *
     * @param invalidCustomers is list of invalid customer that will be exported
     * @param writer
     */
    public void writeInvalidCustomerToCsv(List<InvalidCustomer> invalidCustomers, Writer writer) {
        try {

            CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT);
            for (InvalidCustomer invalidCustomer : invalidCustomers) {
                printer.printRecord(invalidCustomer.getFirstName(), invalidCustomer.getLastName(), invalidCustomer.getCity(),invalidCustomer.getState(),invalidCustomer.getCode(),invalidCustomer.getMobile(),invalidCustomer.getEmail(),invalidCustomer.getIp());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
