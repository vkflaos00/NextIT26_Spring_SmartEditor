package kr.or.nextit.member.vo;

import java.util.List;

import kr.or.nextit.code.vo.CodeVO;
import kr.or.nextit.common.vo.PagingVO;

public class MemberSearchVO extends PagingVO{

	private String searchType;
	private String searchWord;
	private String searchJob;
	private String searchHobby;
	
	private List<MemberVO> memberList;
	 
	
	public List<MemberVO> getMemberList() {
		return memberList;
	}
	public void setMemberList(List<MemberVO> memberList) {
		this.memberList = memberList;
	}
	  
	
	
	public String getSearchType() {
		return searchType;
	}
	public void setSearchType(String searchType) {
		this.searchType = searchType;
	}
	public String getSearchWord() {
		return searchWord;
	}
	public void setSearchWord(String searchWord) {
		this.searchWord = searchWord;
	}
	public String getSearchJob() {
		return searchJob;
	}
	public void setSearchJob(String searchJob) {
		this.searchJob = searchJob;
	}
	public String getSearchHobby() {
		return searchHobby;
	}
	public void setSearchHobby(String searchHobby) {
		this.searchHobby = searchHobby;
	}
	@Override
	public String toString() {
		return "MemberSearchVO [searchType=" + searchType + ", searchWord=" + searchWord + ", searchJob=" + searchJob
				+ ", searchHobby=" + searchHobby + ", memberList=" + memberList + "]"
				+ super.toString();
	
	}

	 
	
	
	
	
	
}
