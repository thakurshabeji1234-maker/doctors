package com.incapp.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.incapp.models.Appointments;
import com.incapp.models.Doctor;
import com.incapp.models.DoctorAvail;
import com.incapp.models.DoctorDetails;
import com.incapp.models.DoctorNotAvail;
import com.incapp.models.DoctorOnline;
import com.incapp.models.User;

import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;





@Controller
@RequestMapping("/doctor")
public class DoctorController {
	
	private RestTemplate restTemplate=new RestTemplate();
	private String URL="http://localhost:7071/doctor";
	
	@Autowired
	BCryptPasswordEncoder passwordEncoder;
	
	@PostMapping("/login")
	public String login(@RequestParam String email,@RequestParam String password, HttpSession session,  ModelMap m) {
		String API="/getDoctor/"+email;
		ResponseEntity<Doctor> result= restTemplate.exchange(URL+API,HttpMethod.GET, null, Doctor.class);
		Doctor doctor=result.getBody();
		if(doctor!=null && passwordEncoder.matches(password, doctor.getPassword())) {
			session.setAttribute("doctor", doctor);
			
			API="/getDocNotAvail/"+email;
			List<DoctorNotAvail> dna=restTemplate.getForObject(URL+API, List.class);
			session.setAttribute("dna",dna);
	        session.setAttribute("onlineStatus", "offline");
			return "redirect:/doctor/DoctorHome";
		}else {
			m.addAttribute("msg","Invalid Credentials!");
			return "login-signup";
		}
	}
	@GetMapping("/DoctorAppointments")
	public String DoctorAppointments(HttpSession session,ModelMap model) {
		String URL="http://localhost:7071/appointment";
		String API="/getByDoctorEmail/"+((Doctor)session.getAttribute("doctor")).getEmail();
		List<Appointments> appointments=restTemplate.getForObject(URL+API,List.class);
		model.addAttribute("apts",appointments);
		return "DoctorAppointments";
	}
	@RequestMapping("/DoctorHome")
	public String DoctorHome() {
		return "DoctorHome";
	}
	
	@PostMapping("/updatePassword")
	public String updatePassword(@RequestParam String email,@RequestParam String oldpassword,@RequestParam(value = "newpassword") String newPassword, HttpSession session, ModelMap m) {
		String API="/getDoctor/"+email;
		ResponseEntity<Doctor> result= restTemplate.exchange(URL+API,HttpMethod.GET, null, Doctor.class);
		Doctor doctor=result.getBody();
		if(doctor!=null && passwordEncoder.matches(oldpassword, doctor.getPassword())) {
			newPassword=passwordEncoder.encode(newPassword);
			
			Map<String, String> data=new HashMap<>();
			data.put("email", email);
			data.put("newPassword", newPassword);
			
			HttpEntity<Map<String, String>> requestEntity=new HttpEntity<Map<String, String>>(data);
			API="/updatePassword";
			ResponseEntity<Boolean> r= restTemplate.exchange(URL+API,HttpMethod.PUT, requestEntity, Boolean.class);
			if(r.getBody()) {
				m.addAttribute("msg","Password Updation Success!");
			}else {
				session.invalidate();
				return "redirect:/login-signup";
			}
		}else {
			m.addAttribute("msg","Invalid OLD Password!");
		}
		return "DoctorHome";
	}
	
	@PostMapping("/forgetPassword")
	public String forgetPassword(@RequestParam String email,@RequestParam(value = "newpassword") String newPassword, ModelMap m) {
		newPassword=passwordEncoder.encode(newPassword);
		Map<String, String> data=new HashMap<>();
		data.put("email", email);
		data.put("newPassword", newPassword);
		HttpEntity<Map<String, String>> requestEntity=new HttpEntity<Map<String, String>>(data);
		String API="/updatePassword";
		ResponseEntity<Boolean> r= restTemplate.exchange(URL+API,HttpMethod.PUT, requestEntity, Boolean.class);
		if(r.getBody()) {
			m.addAttribute("msg","Success!");
		}else {
			m.addAttribute("msg","Id does not exist!");
		}
		return "login-signup";
	}
	
	@PostMapping("/register")
	public String register(@ModelAttribute Doctor doctor,HttpSession session, ModelMap m) {
		doctor.setDoctorDetails(new DoctorDetails());
		doctor.setDoctorAvail(new DoctorAvail());
		doctor.setPassword(passwordEncoder.encode(doctor.getPassword()));
		String API="/register";
		HttpEntity<Doctor> requestEntity=new HttpEntity<Doctor>(doctor);
		ResponseEntity<Boolean> result= restTemplate.exchange(URL+API,HttpMethod.POST,requestEntity,Boolean.class);
		if(result.getBody()) {
			session.setAttribute("doctor", doctor);
	        session.setAttribute("onlineStatus", "offline");
			return "redirect:/doctor/DoctorHome";
		}else {
			m.addAttribute("msg","Email ID Already Exist!");
			return "login-signup";
		}
	}
	@GetMapping("/getPhoto")
	public void getPhoto(@RequestParam String email,ServletResponse response) throws IOException {
		String API="/getDoctorPhoto/"+email;
		ResponseEntity<byte[]> result=restTemplate.exchange(URL+API, HttpMethod.GET, null, byte[].class);
		byte image[]=result.getBody();
		if(image==null || image.length==0 ) {
			InputStream is = this.getClass().getClassLoader().getResourceAsStream("static/images/doctor-dp.png");
			image=is.readAllBytes();
		}
		response.getOutputStream().write(image);
	}
	@PostMapping("/updatePhoto")
	public String updatePhoto(HttpSession session,@RequestPart("photo") MultipartFile photo,ModelMap m) throws IOException {
		Doctor doctor=(Doctor)session.getAttribute("doctor");
		String API="/updateDoctorPhoto/"+doctor.getEmail();
		HttpEntity<byte[]> requestEntity=new HttpEntity<>(photo.getBytes());
		restTemplate.put(URL+API, requestEntity);
		m.addAttribute("msg","Photo updated successfully!");
		API="/getDoctor/"+doctor.getEmail();
		ResponseEntity<Doctor> result=restTemplate.exchange(URL+API,HttpMethod.GET,null,Doctor.class);
		doctor=result.getBody();
		session.setAttribute("doctor", doctor);
		return "redirect:/doctor/DoctorHome";
	}
	
	@PostMapping("/updateDoctor")
	public String updateDoctor(HttpSession session,@ModelAttribute Doctor doctor,@ModelAttribute DoctorDetails doctorDetails, ModelMap m) {
		doctor.setDoctorDetails(doctorDetails);
		String API="/updateDoctor";
		HttpEntity<Doctor> requestEntity=new HttpEntity<Doctor>(doctor);
		ResponseEntity<Doctor> result=restTemplate.exchange(URL+API,HttpMethod.PUT,requestEntity,Doctor.class);
		if(result.getBody()!=null) {
			session.setAttribute("doctor", result.getBody());
			m.addAttribute("msg","Updation Success!");
		}else {
			m.addAttribute("msg","Updation Failed!");
		}
		return "DoctorHome";
	}
	
	@PostMapping("/updateDocAvail") 
	public String updateDocAvail(@ModelAttribute DoctorAvail doctorAvail,HttpSession session,ModelMap m) {
		String email=((Doctor)session.getAttribute("doctor")).getEmail();
		String API="/updateDocAvail/"+email;
		HttpEntity<DoctorAvail> requestEntity=new HttpEntity<DoctorAvail>(doctorAvail);
		ResponseEntity<Doctor> result=restTemplate.exchange(URL+API,HttpMethod.PUT,requestEntity,Doctor.class);
		if(result.getBody()!=null) {
			session.setAttribute("doctor", result.getBody());
			m.addAttribute("msg","Updation Success!");
		}else {
			m.addAttribute("msg","Updation Failed!");
		}
		return "DoctorHome";
	}
	
	@PostMapping("/addDocNotAvail")
	public String addDocNotAvail(HttpSession session,@ModelAttribute DoctorNotAvail doctorNotAvail ,ModelMap m) {
		String API="/addDocNotAvail";
		boolean result=restTemplate.postForObject(URL+API, doctorNotAvail, Boolean.class);
		if(result) {
			m.addAttribute("msg","Success!");
			API="/getDocNotAvail/"+doctorNotAvail.getEmail();
			List<DoctorNotAvail> dna=restTemplate.getForObject(URL+API, List.class);
			session.setAttribute("dna",dna);
		}else {
			m.addAttribute("msg","Already Exist!");
		}
		return "DoctorHome";
	}
	
	@GetMapping("/cancelDocNotAvail")
	public String cancelDocNotAvail(@RequestParam int id,HttpSession session,ModelMap m) {
		String API="/cancelDocNotAvail/"+id;
		ResponseEntity<Boolean> result= restTemplate.exchange(URL+API,HttpMethod.DELETE,null,Boolean.class);
		if(result.getBody()) {
			m.addAttribute("msg","Success!");
			String email=((Doctor)session.getAttribute("doctor")).getEmail();
			API="/getDocNotAvail/"+email;
			List<DoctorNotAvail> dna=restTemplate.getForObject(URL+API, List.class);
			session.setAttribute("dna",dna);
		}else {
			m.addAttribute("msg","Leave Does Not Exist!");
		}
		return "redirect:/doctor/DoctorHome";
	}
	
	@GetMapping("/DoctorOnline")
	public String doctorOnline(@RequestParam String status,HttpSession session,ModelMap model) {
		Doctor d=(Doctor)session.getAttribute("doctor");
		String email=d.getEmail();
		if(status.equalsIgnoreCase("online")) {
			String speciality=d.getSpeciality();
			String API="/doctorOnline/"+email+"/"+speciality;
			ResponseEntity<DoctorOnline> result=restTemplate.exchange(URL+API,HttpMethod.POST,null,DoctorOnline.class);
			DoctorOnline doctorOnline= result.getBody();
			String roomID=doctorOnline.getRoomId();
			String userName=d.getName();
			model.addAttribute("roomID", roomID);
	        model.addAttribute("userName", userName);
	        session.setAttribute("onlineStatus", "online");
			return "videocallDoctor";
		}else {
			String API="/doctorOffline/"+email;
			restTemplate.delete(URL+API);
	        session.setAttribute("onlineStatus", "offline");
			return "redirect:/doctor/DoctorHome";
		}
	}
	
}
