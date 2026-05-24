package com.incapp.controllers;

import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import com.incapp.DoctorsApplication;
import com.incapp.models.Doctor;
import com.incapp.models.User;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import jakarta.servlet.http.HttpSession;


@Controller
public class CommonController {
	
	
	@GetMapping("/")
	public String home() {
		return "index";
	}
	@GetMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/login-signup";
	}
	@GetMapping("/login-signup")
	public String loginsignup() {
		return "login-signup";
	}
	
	@Value("${api.key}") // Injecting the value of api.key from application.properties
    private String key;
	
	@PostMapping("/symptomChecker")
	public String symptomChecker(@RequestParam String symptoms,ModelMap model)  {
		try {
			HttpResponse<String> response = Unirest.post("https://ai-doctor-api-ai-medical-chatbot-healthcare-ai-assistant.p.rapidapi.com/chat?noqueue=1")
					
					.header("x-rapidapi-key", key)
					.header("x-rapidapi-host", "ai-doctor-api-ai-medical-chatbot-healthcare-ai-assistant.p.rapidapi.com")
					.header("Content-Type", "application/json")
					
					.body("{\"message\":\""+symptoms+"\",\"specialization\":\"general\",\"language\":\"en\"}")
					.asString();
		
			JSONObject jobj=new JSONObject(response.getBody());
			String m=jobj.getString("message");
			if(m.contains("exceeded")) {
				model.addAttribute("msg", "You have exceeded the MONTHLY quota for Requests on your current plan.");
			}else {
				JSONObject jo=jobj.getJSONObject("result").getJSONObject("response");
				JSONObject j=new JSONObject(response.getBody()).getJSONObject("result").getJSONObject("metadata");
				HashMap<String, Object> dataSet=new HashMap<>();
				dataSet.put("message",jo.getString("message"));
				dataSet.put("warnings",jo.getJSONArray("warnings"));
				dataSet.put("recommendations",jo.getJSONArray("recommendations"));
				dataSet.put("requiresPhysicianConsult",j.getBoolean("requiresPhysicianConsult"));
				dataSet.put("emergencyLevel",j.getString("emergencyLevel"));
				dataSet.put("topRelatedSpecialties",j.getJSONArray("topRelatedSpecialties"));
				model.addAttribute("dataSet", dataSet);
			}
		} catch (Exception e) {
			model.addAttribute("msg", "Wrong Key.");
		}
		return "index";
	}
	@PostMapping("/searchDoctor")
	public String searchDoctor(HttpSession session, @RequestParam String state,@RequestParam String city,@RequestParam String speciality,ModelMap model) {
		RestTemplate restTemplate=new RestTemplate();
		String URL="http://localhost:7071/doctor";
		String API="/getDoctors/"+state+"/"+city+"/"+speciality;
		ResponseEntity<List> result=restTemplate.exchange(URL+API, HttpMethod.GET, null, List.class);
		List<Doctor> doctors=result.getBody();
		model.addAttribute("doctors",doctors);
		User user=(User)session.getAttribute("user");
		if(user==null) {
			return "index";
		}else {
			return "FindDoctor";
		}
	}

	@PostMapping("/SearchDoctorSpeciality")
	public String SearchDoctorSpeciality(HttpSession session,@RequestParam String speciality,ModelMap model) {
		RestTemplate restTemplate=new RestTemplate();
		String URL="http://localhost:7071/doctor";
		String API="/getDoctorsBySpeciality/"+speciality;
		ResponseEntity<List> result=restTemplate.exchange(URL+API, HttpMethod.GET, null, List.class);
		List<Doctor> doctors=result.getBody();
		model.addAttribute("doctors",doctors);
		User user=(User)session.getAttribute("user");
		if(user==null) {
			return "index";
		}else {
			return "FindDoctor";
		}
	}
	@PostMapping("/DoctorDetails")
	public String DoctorDetails(HttpSession session,@RequestParam String docEmail,ModelMap model) {
		User user=(User)session.getAttribute("user");
		if(user==null) {
			return "login-signup";
		}else {
			RestTemplate restTemplate=new RestTemplate();
			String URL="http://localhost:7071/doctor";
			String API="/getDoctor/"+docEmail;
			ResponseEntity<Doctor> result=restTemplate.exchange(URL+API, HttpMethod.GET, null, Doctor.class);
			Doctor doctor=result.getBody();
			model.addAttribute("doctor",doctor);
			return "DoctorDetails";
		}
	}
}
