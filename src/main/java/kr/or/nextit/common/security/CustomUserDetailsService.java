package kr.or.nextit.common.security;

import java.util.List;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import kr.or.nextit.member.mapper.IMemberMapper;
import kr.or.nextit.member.vo.MemberVO;

public class CustomUserDetailsService implements UserDetailsService{

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Inject
	private IMemberMapper memMapper;
	
	@Autowired
	private HttpServletRequest request;
	
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		// TODO Auto-generated method stub
		logger.info("CustomUserDetailsService loadUserByUsername username: "
				+ username);
		
		MemberVO member = memMapper.getMember(username);
		
		if(member == null) {
			return null;
		}
		
		List<String> roleList  = memMapper.getUserRoleListByUserId(username);
		member.setRoleList(roleList);
		
		HttpSession session = request.getSession();
		member.setUserRoleList(memMapper.getUserRole(member));
		member.setRememberMe(request.getParameter("rememberMe"));
		session.setAttribute("memberVO", member);
		
		//return null;
		return new CustomUser(member);  //[AD, ME, ST]
		
	}

}
