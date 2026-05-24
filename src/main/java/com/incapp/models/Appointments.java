package com.incapp.models;

import java.sql.Date;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class Appointments {
	private int id;
	private String doctor_email;
	private String user_email;
	private String name;
	private String status;
	
	private Date doc_booking_date; //Appointment Day
	private String doc_booking_time;
	
	private java.util.Date booking_date_time; //day of booking
	
}
