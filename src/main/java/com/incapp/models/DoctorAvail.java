package com.incapp.models;

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
public class DoctorAvail {
	private int id;
	
	private String mon_mor;
	private String mon_eve;
	private String tue_mor;
	private String tue_eve;
	private String wed_mor;
	private String wed_eve;
	private String thu_mor;
	private String thu_eve;
	private String fri_mor;
	private String fri_eve;
	private String sat_mor;
	private String sat_eve;
	private String sun_mor;
	private String sun_eve;
	private int max_mor_apmt;
	private int max_eve_apmt;
	
	
}
