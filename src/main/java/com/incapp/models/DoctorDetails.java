package com.incapp.models;

import java.sql.Date;

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
public class DoctorDetails {
	private int id;
	
	private String phone;
	private Date dob;
	private String qualification;
	private int experience;
	private String gender;
    private byte[] photo;

}
