package com.orangetoolz.customer.app.entity;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "customer")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Customer {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "city")
    private String city;
    @Column(name = "state")
    private String state;
    @Column(name = "code")
    private String code;
    @Column(name = "mobile",unique=true)
    private String mobile;
    @Column(name = "email",unique=true)
    private String email;
    @Column(name = "ip")
    private String ip;


}
