package com.orangetoolz.customer.app.service;

import com.orangetoolz.customer.app.entity.Customer;
import com.orangetoolz.customer.app.entity.InvalidCustomer;
import com.orangetoolz.customer.app.repository.CustomerRepository;
import com.orangetoolz.customer.app.repository.InvalidCustomerRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private InvalidCustomerRepository invalidCustomerRepository;
    Logger logger = LoggerFactory.getLogger(CustomerService.class);

    public static final Pattern EVRE = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    /***
     *
     * @throws Exception
     * insert csv file to database and csv file located into src/main/resources/1M-customers.txt
     */
    public void parseCSVFileFromDisk() throws Exception {
        try {

            File initialFile = new File("src/main/resources/1M-customers.txt");
            InputStream targetStream = new FileInputStream(initialFile);
            HashSet<String> mobileHasSet = new HashSet<String>();
            HashSet<String> emailHasSet = new HashSet<String>();
            List<Customer> customers = new ArrayList<Customer>();
            List<InvalidCustomer> invalidCustomers = new ArrayList<InvalidCustomer>();
            int loopCount=0,duplicateCount = 0,invalidCount = 0,validCount = 0;
            try (final BufferedReader br = new BufferedReader(new InputStreamReader(targetStream))) {
                String line;
                while ((line = br.readLine()) != null) {
                    loopCount++;
                    List<String> list = Arrays.asList(line.split(","));
                    List<String> data = new ArrayList<>(list);
                    if(data.size()<8){
                        for(int j = data.size()+1;j<=8;j++){
                            data.add("");
                        }
                    }

                    if(mobileHasSet.contains(data.get(5)) || emailHasSet.contains(data.get(6))){
                        duplicateCount++;
                        continue;
                    }

                    mobileHasSet.add(data.get(5));
                    emailHasSet.add(data.get(6));

                    if (isMobileValid(data.get(5)) && isEmailValid(data.get(6))){
                        Customer customer = new Customer();
                        customer.setFirstName(data.get(0));
                        customer.setLastName(data.get(1));
                        customer.setCity(data.get(2));
                        customer.setState(data.get(3));
                        customer.setCode(data.get(4));
                        customer.setMobile(data.get(5));
                        customer.setEmail(data.get(6));
                        customer.setIp(data.get(7));
                        customers.add(customer);
                        /*customerRepository.save(customer);*/
                        validCount++;
                    } else{
                        InvalidCustomer invalidCustomer = new InvalidCustomer();
                        invalidCustomer.setFirstName(data.get(0));
                        invalidCustomer.setLastName(data.get(1));
                        invalidCustomer.setCity(data.get(2));
                        invalidCustomer.setState(data.get(3));
                        invalidCustomer.setCode(data.get(4));
                        invalidCustomer.setMobile(data.get(5));
                        invalidCustomer.setEmail(data.get(6));
                        invalidCustomer.setIp(data.get(7));
                        invalidCustomers.add(invalidCustomer);
                        /*invalidCustomerRepository.save(invalidCustomer);*/
                        invalidCount++;
                    }

                    if(loopCount%2000==0){
                        customerRepository.saveAll(customers);
                        customers = new ArrayList<Customer>();
                        invalidCustomerRepository.saveAll(invalidCustomers);
                        invalidCustomers = new ArrayList<InvalidCustomer>();
                    }
                }

                if(customers.size()>0){
                    customerRepository.saveAll(customers);
                }
                if(invalidCustomers.size()>0){
                    invalidCustomerRepository.saveAll(invalidCustomers);
                }
                System.out.println("Total Line Read "+ loopCount + ", Duplicate Count "+ duplicateCount + ", Invalid Count "+ invalidCount + ", valid Count "+ validCount);
            }
        } catch (final IOException e) {
            logger.error("Parse error: ", e);
            throw new Exception("Failed to parse CSV file", e);
        }
    }



    /***
     *
     * @param mobileNumber
     * @return true if valid else false
     */
    private boolean isMobileValid(String mobileNumber){

        return mobileNumber.replaceAll("[\\s()-]", "").length()>=10;
    }

    /***
     *
     * @param emailAddress
     * @return true if valid else false
     */
    public static boolean isEmailValid(String emailAddress) {
        Matcher matcher = EVRE.matcher(emailAddress);
        return matcher.matches();
    }


    /***
     *
     * @return CompletableFuture all customer list
     */
    @Async
    public CompletableFuture<List<Customer>> getAllValidCustomers(){
        logger.info("Valid customer list by  "+Thread.currentThread().getName());
        List<Customer> customers=customerRepository.findAll();
        return CompletableFuture.completedFuture(customers);
    }


    public void deleteAllFromCustomer(){
        customerRepository.deleteAll();
    }

    /***
     *
     * @param customers is list of customer that will be exported
     * @param writer
     */
    public void writeCustomerToCsv(List<Customer> customers, Writer writer) {
        try {

            CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT);
            for (Customer customer : customers) {
                printer.printRecord(customer.getFirstName(), customer.getLastName(), customer.getCity(),customer.getState(),customer.getCode(),customer.getMobile(),customer.getEmail(),customer.getIp());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
