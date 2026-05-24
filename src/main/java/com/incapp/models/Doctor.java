package com.incapp.models;


import org.springframework.beans.factory.annotation.Autowired;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

//----lombok
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {
	private String email;
	private String password;
	private String name;
	private String state;
	private String city;
	private String area;
	private String speciality;
	@Autowired
	private DoctorDetails doctorDetails;
	@Autowired
	private DoctorAvail doctorAvail;
	
}
