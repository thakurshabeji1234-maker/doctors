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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;





@Controller
@RequestMapping("/user")
public class UserController {
	
	private RestTemplate restTemplate=new RestTemplate();
	private String URL="http://localhost:7071/user";
	
	@Autowired
	BCryptPasswordEncoder passwordEncoder;
	
	@GetMapping("/oauth2success")
	public String googleLoginSuccess(@AuthenticationPrincipal OAuth2User principal, HttpSession session) {
	    String email = principal.getAttribute("email");
	    String name = principal.getAttribute("name");
	    
	    String API="/getUser/"+email;
		ResponseEntity<User> result= restTemplate.exchange(URL+API,HttpMethod.GET, null, User.class);
		User user=result.getBody();
		if(user!=null) {
			session.setAttribute("user", user);
		}else {
			user = new User();
			user.setName(name);
		    user.setEmail(email);
		    user.setPassword(passwordEncoder.encode("jggJHGH@jgjhgjU%465"));
		    API="/register";
			HttpEntity<User> requestEntity=new HttpEntity<User>(user);
			restTemplate.exchange(URL+API,HttpMethod.POST,requestEntity,Boolean.class);
			session.setAttribute("user", user);
		}
	    return "redirect:/user/UserHome";
	}
	
	
	@PostMapping("/login")
	public String login(@RequestParam String email,@RequestParam String password, HttpSession session,  ModelMap m) {
		String API="/getUser/"+email;
		ResponseEntity<User> result= restTemplate.exchange(URL+API,HttpMethod.GET, null, User.class);
		User user=result.getBody();
		if(user!=null && passwordEncoder.matches(password, user.getPassword())) {
			session.setAttribute("user", user);
			return "redirect:/user/UserHome";
		}else {
			m.addAttribute("msg","Invalid Credentials!");
			session.setAttribute("message", "Invalid Credentials!");
			return "login-signup";
		}
	}
	
	@GetMapping("/FindDoctor")
	public String findDoctor() {
		return "FindDoctor";
	}
	@GetMapping("/UserAppointments")
	public String UserAppointments(HttpSession session,ModelMap model) {
		String URL="http://localhost:7071/appointment";
		String API="/getByUserEmail/"+((User)session.getAttribute("user")).getEmail();
		List<Appointments> appointments=restTemplate.getForObject(URL+API,List.class);
		model.addAttribute("apts",appointments);
		return "UserAppointments";
	}
	
	@GetMapping("/UserProfile")
	public String userProfile() {
		return "UserProfile";
	}
	@GetMapping("/UserHome")
	public String userHome() {
		return "UserHome";
	}

	
	@PostMapping("/updatePassword")
	public String updatePassword(@RequestParam String email,@RequestParam String oldpassword,@RequestParam(value = "newpassword") String newPassword, HttpSession session, ModelMap m) {
		String API="/getUser/"+email;
		ResponseEntity<User> result= restTemplate.exchange(URL+API,HttpMethod.GET, null, User.class);
		User user=result.getBody();
		if(user!=null && passwordEncoder.matches(oldpassword, user.getPassword())) {
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
		return "UserProfile";
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
	public String register(@ModelAttribute User user,HttpSession session, ModelMap m) {
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		String API="/register";
		HttpEntity<User> requestEntity=new HttpEntity<User>(user);
		ResponseEntity<Boolean> result= restTemplate.exchange(URL+API,HttpMethod.POST,requestEntity,Boolean.class);
		if(result.getBody()) {
			session.setAttribute("user", user);
			return "redirect:/user/UserHome";
		}else {
			m.addAttribute("msg","Email ID Already Exist!");
			return "login-signup";
		}
	}
	@GetMapping("/getPhoto")
	public void getPhoto(HttpSession session,ServletResponse response) throws IOException {
		User user=(User)session.getAttribute("user");
		String API="/getPhoto/"+user.getEmail();
		ResponseEntity<byte[]> result=restTemplate.exchange(URL+API, HttpMethod.GET, null, byte[].class);
		byte image[]=result.getBody();
		if(image==null || image.length==0 ) {
			InputStream is = this.getClass().getClassLoader().getResourceAsStream("static/images/person.png");
			image=is.readAllBytes();
		}
		response.getOutputStream().write(image);
	}
	@PostMapping("/updatePhoto")
	public String updatePhoto(HttpSession session,@RequestPart("photo") MultipartFile photo,ModelMap m) throws IOException {
		User user=(User)session.getAttribute("user");
		String API="/updatePhoto/"+user.getEmail();
		HttpEntity<byte[]> requestEntity=new HttpEntity<>(photo.getBytes());
		restTemplate.put(URL+API, requestEntity);
		m.addAttribute("msg","Photo updated successfully!");
		API="/getUser/"+user.getEmail();
		ResponseEntity<User> result=restTemplate.exchange(URL+API,HttpMethod.GET,null,User.class);
		user=result.getBody();
		session.setAttribute("user", user);
		return "redirect:/user/UserHome";
	}
	
	@PostMapping("/updateUser")
	public String updateUser(HttpSession session,@ModelAttribute User user, ModelMap m) {
		String API="/updateUser";
		HttpEntity<User> requestEntity=new HttpEntity<User>(user);
		ResponseEntity<User> result=restTemplate.exchange(URL+API,HttpMethod.PUT,requestEntity,User.class);
		if(result.getBody()!=null) {
			session.setAttribute("user", result.getBody());
			m.addAttribute("msg","Updation Success!");
		}else {
			m.addAttribute("msg","Updation Failed!");
		}
		return "UserHome";
	}
	@GetMapping("/videoCall")
	public String videoCall(HttpSession session,@RequestParam String email,ModelMap model) throws IOException {
		String URL="http://localhost:7071/doctor";
		String API="/getDoctorOnline/"+email;
		ResponseEntity<DoctorOnline> result=restTemplate.exchange(URL+API,HttpMethod.GET,null,DoctorOnline.class);
		DoctorOnline doctorOnline=result.getBody();
		if(doctorOnline==null) {
			API="/getDoctor/"+email;
			ResponseEntity<Doctor> r=restTemplate.exchange(URL+API, HttpMethod.GET, null, Doctor.class);
			Doctor doctor=r.getBody();
			model.addAttribute("doctor",doctor);
			model.addAttribute("msg", "Doctor Not Available for Video Call.");
			return "DoctorDetails";
		}else {
			String roomID=doctorOnline.getRoomId();
			User user=(User)session.getAttribute("user");
			String userName=user.getName();
			model.addAttribute("roomID", roomID);
	        model.addAttribute("userName", userName);
			return "videocall";
		}
	}
	
}
