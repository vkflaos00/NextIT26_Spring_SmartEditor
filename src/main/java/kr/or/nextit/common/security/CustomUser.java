package kr.or.nextit.common.security;

import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import kr.or.nextit.member.vo.MemberVO;

public class CustomUser extends User {
	
	
	public CustomUser(MemberVO member) {
		super(
				member.getMemId()
				,member.getMemPass()
				,member.getRoleList().stream()
					.map(role -> new SimpleGrantedAuthority(role))
					.collect(Collectors.toList())
				);
		
		System.out.println("CustomUser : "+ member.getRoleList().stream().map(role -> new SimpleGrantedAuthority(role)).collect(Collectors.toList()));
		
	}
}
