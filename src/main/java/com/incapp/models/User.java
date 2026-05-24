package com.incapp.models;


import java.sql.Date;
import java.util.Arrays;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 */

//----lombok
@Getter
@Setter
@ToString
@NoArgsConstructor
public class User {
	private String email;
	private String password;
	private String name;
	private String phone;
	private Date dob;
	private String gender;
    private byte[] photo;
	
}
