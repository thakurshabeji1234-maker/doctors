package com.incapp.controllers;

import java.util.Date;
import java.util.List;

import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import com.incapp.models.Appointments;
import com.incapp.models.Doctor;
import com.incapp.models.User;

import jakarta.servlet.http.HttpSession;


@Controller
@RequestMapping("/appointment")
public class AppointmentController {
	
	String URL="http://localhost:7071/appointment";
	RestTemplate restTemplate=new RestTemplate();
	
	@PostMapping("/addAppointment")
	public String addAppointment(@ModelAttribute Appointments	appointment,HttpSession session,ModelMap model) {
		appointment.setUser_email(((User)session.getAttribute("user")).getEmail());
		appointment.setBooking_date_time(new java.sql.Date(new Date().getTime()));
		String API="/addAppointment";
		String result=restTemplate.postForObject(URL+API, appointment, String.class);
		model.addAttribute("msg",result);

		API="/getByUserEmail/"+((User)session.getAttribute("user")).getEmail();
		List<Appointments> appointments=restTemplate.getForObject(URL+API,List.class);
		model.addAttribute("apts",appointments);
		
		return "UserAppointments";
	}
	@GetMapping("/getByUserEmail")
	public String getByUserEmail(HttpSession session,ModelMap model) {
		String API="/getByUserEmail/"+((User)session.getAttribute("user")).getEmail();
		List<Appointments> appointments=restTemplate.getForObject(URL+API,List.class);
		model.addAttribute("apts",appointments);
		
		return "UserAppointments";
	}
	@GetMapping("/getByDoctorEmail")
	public String getByDoctorEmail(HttpSession session,ModelMap model) {
		String API="/getByDoctorEmail/"+((Doctor)session.getAttribute("doctor")).getEmail();
		List<Appointments> appointments=restTemplate.getForObject(URL+API,List.class);
		model.addAttribute("apts",appointments);
		
		return "DoctorAppointments";
	}
	@PostMapping("/statusUpdate")
	public String statusUpdate(@RequestParam String role,@RequestParam int id,@RequestParam String status) {
		String API="/updateAppointmentStatus/"+id+"/"+status;
		ResponseEntity<Boolean> result= restTemplate.exchange(URL+API, HttpMethod.PUT, null,Boolean.class);
		if(role.equalsIgnoreCase("user"))
			return "redirect:/user/UserAppointments";
		else
			return "redirect:/doctor/DoctorAppointments";
	}
	
}
