package com.orangetoolz.customer.app.controller;

import com.orangetoolz.customer.app.entity.Customer;
import com.orangetoolz.customer.app.entity.InvalidCustomer;
import com.orangetoolz.customer.app.service.CustomerService;
import com.orangetoolz.customer.app.service.InvalidCustomerService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@RestController
@RequestMapping("/otzApi")
public class CustomerController {

    @Autowired
    private CustomerService customerService;
    @Autowired
    private InvalidCustomerService invalidCustomerService;
    @Autowired
    private JobLauncher jobLauncher;
    @Autowired
    private Job job;

    @PostMapping(value = "/addCustomers", produces = "application/json")
    public ResponseEntity saveCustomerFromDisk() throws Exception {
        long start = System.currentTimeMillis();
        customerService.parseCSVFileFromDisk();
        long end = System.currentTimeMillis();
        System.out.println("Total Execution time is : " + ((end - start)/1000)+" Second(s)");
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @GetMapping(value = "/exportValidCustomers", produces = "application/json")
    public  void getCustomers(HttpServletResponse response) throws Exception{
        long start = System.currentTimeMillis();
        CompletableFuture<List<Customer>> comCustomer=customerService.getAllValidCustomers();
        long end = System.currentTimeMillis();
        System.out.println("Total Execution time is : "+ ((end - start)/1000)+"Second(s)");
        response.setContentType("text/csv");
        response.addHeader("Content-Disposition", "attachment; filename=\"valid-customer.txt\"");
        customerService.writeCustomerToCsv(comCustomer.get(),response.getWriter());
    }


    @GetMapping(value = "/exportInvalidCustomers", produces = "application/json")
    public  void getInvalidCustomers(HttpServletResponse response) throws Exception{
        long start = System.currentTimeMillis();
        CompletableFuture<List<InvalidCustomer>> comInvalidCustomer=invalidCustomerService.getAllInvalidCustomers();
        long end = System.currentTimeMillis();
        System.out.println("Total Execution time is : "+ ((end - start)/1000)+"Second(s)");
        response.setContentType("text/csv");
        response.addHeader("Content-Disposition", "attachment; filename=\"invalid-valid-customer.txt\"");
        invalidCustomerService.writeInvalidCustomerToCsv(comInvalidCustomer.get(),response.getWriter());
    }



    @PostMapping(value = "/addValidCustomer", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE}, produces = "application/json")
    public void importCsvCustomer(@RequestParam(value = "file") MultipartFile multipartFile) throws Exception {
        try {
            File file = new File("src/main/resources/"+System.currentTimeMillis()+"_"+multipartFile.getOriginalFilename());
            file.createNewFile();
            FileOutputStream output = new FileOutputStream(file);
            output.write(multipartFile.getBytes());

            //Delete ALL from customer table first
            customerService.deleteAllFromCustomer();

            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("Started at ", System.currentTimeMillis())
                    .addString("filePath",file.getAbsolutePath())
                    .toJobParameters();

            jobLauncher.run(job, jobParameters);
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
