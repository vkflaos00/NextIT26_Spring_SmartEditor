package kr.or.nextit.login;

import java.util.List;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import kr.or.nextit.code.service.CommCodeServiceImpl;
import kr.or.nextit.code.service.ICommCodeService;
import kr.or.nextit.code.vo.CodeVO;
import kr.or.nextit.member.service.IMemberService;
import kr.or.nextit.member.service.MailSendService;
import kr.or.nextit.member.service.MemberServiceImpl;
import kr.or.nextit.member.vo.MailAuthVO;
import kr.or.nextit.member.vo.MemberVO;

@Controller
public class LoginController {

		
	private final Logger logger
		= LoggerFactory.getLogger(this.getClass());
	
	//@Autowired
	//@Inject
	@Resource(name="codeService")
	private ICommCodeService codeService;
	
	
	@Autowired
	private IMemberService memberService;
	
	
	
	//@RequestMapping("/login")
	//@RequestMapping( value="/login", method = RequestMethod.POST )
	//@RequestMapping( value="/login", method = RequestMethod.GET)
	@GetMapping("/login")
	public String login(HttpServletRequest request) {
		//System.out.println("LoginController login");
		logger.info("LoginController login");
		
		Cookie[] cookies =  request.getCookies();
		if( cookies != null && cookies.length >0){
			for(int i=0; i< cookies.length ; i++){
				if(cookies[i].getName().equals("rememberMe")){
					System.out.println( cookies[i].getName() +" : "+ cookies[i].getValue());
					request.setAttribute("checkBox", "checked");
					request.setAttribute("memId", cookies[i].getValue());
				}
			}
		}
		
		return "/login/login";
	}
	
	
	@GetMapping("/login/{msg}")
	public String loginMsg (@PathVariable String msg, Model model) {
		//System.out.println("LoginController loginMsg");
		//System.out.println("msg : "+ msg);
		logger.info("LoginController loginMsg");
		logger.info("msg : "+ msg);
		
		
		model.addAttribute("msg", msg);
		return "/login/login";
	}
	
	/*@RequestMapping("/login/join")
	public String join(Model model) {
		System.out.println("LoginController join");
	
		List<CodeVO> jobList = codeService.getCodeListByParent("JB00");
		model.addAttribute("jobList", jobList);

		List<CodeVO> hobbyList = codeService.getCodeListByParent("HB00");
		model.addAttribute("hobbyList", hobbyList);
		
		return "/login/join";
	}*/
	
	
	@RequestMapping("/login/join")
	public ModelAndView join(ModelAndView mav
		, @ModelAttribute("member") MemberVO member	 ) {
		//System.out.println("LoginController join");
		logger.info("LoginController join");
		
		
		List<CodeVO> jobList = codeService.getCodeListByParent("JB00");
		mav.addObject("jobList", jobList);

		List<CodeVO> hobbyList = codeService.getCodeListByParent("HB00");
		mav.addObject("hobbyList", hobbyList);
		
		mav.setViewName("/login/join");
		return mav;
	}
	
	
	@RequestMapping("/login/loginCheck")
	public String loginCheck(@ModelAttribute MemberVO member
			, HttpServletRequest request
			, HttpServletResponse response) {
		//System.out.println("LoginController loginCheck");
		logger.info("LoginController loginCheck");
		
		//System.out.println("loginCheck member.toString(): "+ member.toString());
		logger.info("loginCheck member.toString(): "+ member.toString());
		
		
		try{
			boolean loginCheck = memberService.loginCheck(member, request, response);
			if(loginCheck) {
				return "redirect:/home";
			}else {
				return "redirect:/login/fail";
			}
		}catch(Exception e){
			e.printStackTrace();
			return "redirect:/login/error";
		}
		
	}
	
	@RequestMapping("/home")
	public String home(HttpSession session, HttpServletResponse response) {
		logger.info("LoginController home");
		
		MemberVO member =  (MemberVO) session.getAttribute("memberVO");
		
		String rememberMe = member.getRememberMe();
		if (rememberMe != null && rememberMe.equals("Y")) {
			System.out.println("rememberMe is Y");
			Cookie cookie= new Cookie("rememberMe", member.getMemId());
			cookie.setMaxAge(60*60*24); 
			cookie.setHttpOnly(true);
			//cookie.setSecure(true);
			response.addCookie(cookie);
		}else{
			Cookie cookie= new Cookie("rememberMe", "");
			cookie.setMaxAge(0);
			response.addCookie(cookie);
		}
		
		
		
		return "/home/home";
	}
	
	@RequestMapping("/login/logout")
	public String logout(HttpServletRequest request) {
		//System.out.println("LoginController logout");
		logger.info("LoginController logout");
		
		request.getSession().removeAttribute("memberVO");
		return "redirect:/login";
	}

	
	@RequestMapping("/join/idCheck")
	@ResponseBody
	//public String idCheck(@ModelAttribute("member") MemberVO member) {
	public boolean idCheck(@ModelAttribute("member") MemberVO member) {
		logger.info("LoginController idCheck member.getMemId(): "
				+ member.getMemId());
		
		boolean result = memberService.idCheck(member.getMemId());
		
		
		//return "/login/join";
		return result;
	}
	
	
	/*@RequestMapping("/join/mailAuth")
	public String mailAuth(@ModelAttribute("member") MemberVO member) {
		return "/login/join";
	}*/
	
	
	
	@Inject
	private MailSendService mailSendService;
	
	@RequestMapping("/join/mailAuth")
	@ResponseBody
	public boolean mailAuth(@RequestParam(required = true) String mail) {
		logger.info("mail : "+ mail);
		
		 String authKey= mailSendService.sendAuthMail(mail);
		 
		 boolean result = false;

		 if(!authKey.equals("false")) {
			 memberService.registerMailAut(mail, authKey);
			 result = true;
		 }
		
		return result;
	}
	
	
	@RequestMapping("/join/mailWindow")
	public String mailWindow() {
		return "/login/mailWindow";
	}
	
	
	@RequestMapping("/join/authKeyCompare")
	@ResponseBody
	public boolean authKeyCompare(@ModelAttribute MailAuthVO mailAuthVO) {
		logger.info("mailAuthVO.toString() : "+ mailAuthVO.toString());
		boolean result = memberService.authKeyCompare(mailAuthVO);
		return result; 
	}
	
}
