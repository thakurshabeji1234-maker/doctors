package com.incapp.models;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


//----lombok
@Getter
@Setter
@ToString
@NoArgsConstructor
public class DoctorOnline {
	private String docEmail;
	private String userEmail;
	private String speciality;
	private String roomId;
	
}
