package kr.or.nextit.member;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import kr.or.nextit.attach.vo.AttachVO;
import kr.or.nextit.code.service.CommCodeServiceImpl;
import kr.or.nextit.code.service.ICommCodeService;
import kr.or.nextit.code.vo.CodeVO;
import kr.or.nextit.common.util.NextITFileUpload;
import kr.or.nextit.common.valid.MemberModify;
import kr.or.nextit.common.valid.MemberRegister;
import kr.or.nextit.common.vo.ResultMessageVO;
import kr.or.nextit.common.vo.RoleInfoVO;
import kr.or.nextit.exception.BizDuplicateKeyException;
import kr.or.nextit.exception.BizMailAuthException;
import kr.or.nextit.exception.BizNotEffectedException;
import kr.or.nextit.exception.BizNotFoundException;
import kr.or.nextit.exception.BizPasswordNotMatchedException;
import kr.or.nextit.exception.DaoException;
import kr.or.nextit.member.service.IMemberService;
import kr.or.nextit.member.service.MemberServiceImpl;
import kr.or.nextit.member.vo.MemberSearchVO;
import kr.or.nextit.member.vo.MemberVO;

@Controller
public class MemberController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	
	@Autowired
	private ICommCodeService codeService;
	
	@Resource(name="memberService")
	private IMemberService memberService;
	
	@Autowired
	private NextITFileUpload nextITFileUpload;
	
	@ModelAttribute("jobList")
	public List<CodeVO> jobList(){
		return codeService.getCodeListByParent("JB00");
	}
	@ModelAttribute("hobbyList")
	public List<CodeVO> hobbyList(){
		return codeService.getCodeListByParent("HB00");
	}
	
	@RequestMapping(value = "/member/memberRegister", method = RequestMethod.POST)
	public String memberRegister(
			@Validated(value = MemberRegister.class) @ModelAttribute("member") MemberVO member
			,BindingResult error
			,Model model
			,ResultMessageVO resultMessageVO
			,@RequestParam(required = false)MultipartFile[] profilePhoto
			) {
		System.out.println("MemberController memberRegister member.toString(): "
			+ member.toString());
		
		if(error.hasErrors()) {
			return "/login/join";
		}
		
		boolean fileuploadFlag = true;
		if(profilePhoto != null) {
			 try {
				List<AttachVO>  attachList 
					= nextITFileUpload.fileUpload(profilePhoto
							, "PROFILEPHOTO", "profilePhoto");
				if(attachList.size()>0) {
					member.setAttachList(attachList);
				}
			} catch (IllegalStateException | IOException e) {
				e.printStackTrace();
				fileuploadFlag = false;
			}
		}
		
		
		try{
			if(member.getMemId() != null && ! member.getMemId().equals("")) {
				memberService.registerMember(member);
			}else {
				throw new Exception();
			}
			//return "redirect:/login/sign";
			if(fileuploadFlag) {
				return "redirect:/login/sign";
			}else {
				resultMessageVO.failSetting(false
						, "회원등록 성공, 프로필 업로드 실패"
						, "회원 등록되었으나 프로필은 업로드 되지 못하였습니다. 전산실에 문의 부탁드립니다. 042-719-8850");
			}
		}catch(BizDuplicateKeyException bde){
			bde.printStackTrace();
			resultMessageVO.failSetting(false
					, "회원등록실패"
					,  "이미 사용중인 아이디 입니다. 다른 아이드를 사용해주세요");
		}catch(BizNotEffectedException bne){
			bne.printStackTrace();
			resultMessageVO.failSetting(false
					, "회원등록실패"
					,  "회원등록에 실패하였습니다. 전산실에 문의부탁드립니다. 042-719-8850");
		}catch(BizMailAuthException bmae){
			bmae.printStackTrace();
			resultMessageVO.failSetting(false
					, "회원등록실패"
					,  "메일 인증이 되지 않았습니다. 메일 인증 버튼으로 인증해주세요");
		
		}catch(Exception de){
			de.printStackTrace();
			resultMessageVO.failSetting(false
					, "회원등록실패"
					,  "회원등록에 실패하였습니다. 전산실에 문의부탁드립니다. 042-719-8850");
		}
		
		model.addAttribute("resultMessageVO", resultMessageVO);
		return "/common/message";
	}
	
	
	@RequestMapping("/member/memberView")
	public String memberView(@RequestParam String memId
			, Model model) {
		System.out.println("MemberController memberView memId : "+ memId);
		try{
			MemberVO member = memberService.getMember(memId);
			member.setMemPass("");
			model.addAttribute("member", member);
		}catch(BizNotEffectedException bne){
			model.addAttribute("bne", bne);
			bne.printStackTrace();
		}catch(Exception de){
			model.addAttribute("de", de);
			de.printStackTrace();
		}
		return "member.memberView";
	}
	
	
	@RequestMapping("/member/memberEdit")
	public String memberEdit(@RequestParam String memId
			,Model model) {
		System.out.println("MemberController memberEdit memId :"+ memId);
		try{
			MemberVO member = memberService.getMember(memId);
			member.setMemPass("");
			model.addAttribute("member", member);
		}catch(BizNotEffectedException bne){
			model.addAttribute("bne", bne);
			bne.printStackTrace();
		}catch(Exception de){
			model.addAttribute("de", de);
			de.printStackTrace();
		}
		return "member.memberEdit";
	}
	
	@RequestMapping(value = "/member/memberModify", method = RequestMethod.POST)
	public String memberModify(
			@Validated(value = MemberModify.class) @ModelAttribute("member") MemberVO member
			,BindingResult error
			, Model model
			, ResultMessageVO resultMessageVO
			, @RequestParam(required=false)MultipartFile[] profilePhoto
			) {
		System.out.println("MemberController memberModify member.toStirng(): "
				+ member.toString());

		if(error.hasErrors()) {
			return "member.memberEdit";
		}
		
		
		boolean fileUploadflag = true;
		if(profilePhoto !=null) {
			try {
				List<AttachVO> attachList = nextITFileUpload.fileUpload(profilePhoto, "PROFILEPHOTO", "profilePhoto");
				member.setAttachList(attachList);
			} catch (IllegalStateException | IOException e) {
				// TODO Auto-generated catch block
				fileUploadflag = false;
				e.printStackTrace();
			}
		}
		
		try{
			if(member.getMemId() !=null && ! member.getMemId().equals("") ) {
				memberService.modifyMember(member);
			}else {
				throw new Exception();
			}
			if(fileUploadflag) {
				return "redirect:/member/memberView?memId="+member.getMemId();
			}else {
				resultMessageVO.failSetting(false, "파일 업로드 실패", "회원정보는 수정되었으나 파일이 업로드 되지 못하였습니다. 전산실에 문의 부탁드립니다. 042-719-8850");
			}
			
		}catch(BizNotEffectedException bne){
			resultMessageVO.failSetting(false
					, "회원정보 수정 실패"
					, "회원정보 수정 실패 하였습니다. 전산실에 문의 부탁드립니다. 042-719-8850");
			bne.printStackTrace();
		}catch(BizPasswordNotMatchedException bpn){
			resultMessageVO.failSetting(false
					, "회원정보 수정 실패"
					, "입력하신 패스워드가 일치하지 않습니다. 다시 입력 해주세요");
			bpn.printStackTrace();
		}catch(BizNotFoundException bnf){
			resultMessageVO.failSetting(false
					, "회원정보 수정 실패"
					, "회원정보 수정 실패 하였습니다. 전산실에 문의 부탁드립니다. 042-719-8850");
			bnf.printStackTrace();
		}catch(Exception de){
			resultMessageVO.failSetting(false
					, "회원정보 수정 실패"
					, "회원정보 수정 실패 하였습니다. 전산실에 문의 부탁드립니다. 042-719-8850");
			de.printStackTrace();
		}
		
		model.addAttribute("resultMessageVO", resultMessageVO);
		return "/common/message";
	}
	

	@RequestMapping(value = "/member/memberDelete", method = RequestMethod.POST)
	public String memberDelete(@ModelAttribute MemberVO member
			, HttpServletRequest request
			, Model model
			, ResultMessageVO resultMessageVO) {
		System.out.println("MemberController memberDelete member.toString() :"
				+ member.toString());
		
		try{
			if(member.getMemId() !=null &&  ! member.getMemId().equals("")) {
				memberService.removeMember(member);
			}else {
				throw new Exception();
			}
			HttpSession session = request.getSession();
			session.removeAttribute("memberVO");
			return "redirect:/login/quit";
			
		}catch(BizNotFoundException bnf){
			//model.addAttribute("bnf", bnf);
			resultMessageVO.failSetting(false
					, "회원탈퇴 실패"
					, "회원탈퇴 실패하였습니다. 전산실에 문의 부탁드립니다. 042-719-8850");
			bnf.printStackTrace();
		}catch(BizPasswordNotMatchedException bpn){
			//model.addAttribute("bpn", bpn);
			resultMessageVO.failSetting(false
					, "회원정보 수정 실패"
					, "입력하신 패스워드가 일치하지 않습니다. 다시 입력 해주세요");
			bpn.printStackTrace();
		}catch(BizNotEffectedException bne){
			//model.addAttribute("bne", bne);
			resultMessageVO.failSetting(false
					, "회원탈퇴 실패"
					, "회원탈퇴 실패하였습니다. 전산실에 문의 부탁드립니다. 042-719-8850");
			bne.printStackTrace();
		}catch(Exception de){
			//model.addAttribute("de", de);
			resultMessageVO.failSetting(false
					, "회원탈퇴 실패"
					, "회원탈퇴 실패하였습니다. 전산실에 문의 부탁드립니다. 042-719-8850");
			de.printStackTrace();
		}
		//return "/member/memberDelete";
		model.addAttribute("resultMessageVO", resultMessageVO);
		return "/common/message";
	}


	@RequestMapping("/member/memberList")
	public String memberList(@ModelAttribute("searchVO") MemberSearchVO searchVO
			, Model model) {
		System.out.println("MemberController memberList ");
		
		try{
			List<MemberVO> memberList = memberService.getMemberList(searchVO);
			model.addAttribute("memberList", memberList);
		}catch(BizNotFoundException bnf){
			model.addAttribute("bnf", bnf);
			bnf.printStackTrace();
		}catch(Exception de){
			model.addAttribute("de", de);
			de.printStackTrace();
		}
		return "member.memberList";
	}

	
	@RequestMapping(value="/member/memberMultiDelete", method=RequestMethod.POST)
	public String memberMultiDelete(@RequestParam String memMultiId
			, Model model
			, ResultMessageVO resultMessageVO) {
		System.out.println("MemberController memberMultiDelete memMultiId: "
				+ memMultiId);
		try{
			if( memMultiId !=null && memMultiId.length() >2 ) {
				memberService.removeMultiMember(memMultiId);
			}else {
				throw new Exception();
			}
			return "redirect:/member/memberList";
			
		}catch(BizNotEffectedException bne){
			bne.printStackTrace();
			//model.addAttribute("bne", bne);
			resultMessageVO.failSetting(false
					, "회원삭제 실패"
					, "회원삭제 실패하였습니다. 전산실에 문의 부탁드립니다. 042-719-8850");
		}catch(Exception de){
			de.printStackTrace();
			//model.addAttribute("de", de);
			resultMessageVO.failSetting(false
					, "회원삭제 실패"
					, "회원삭제 실패하였습니다. 전산실에 문의 부탁드립니다. 042-719-8850");
		}
		//return "/member/memberMultiDelete";
		
		model.addAttribute("resultMessageVO", resultMessageVO);
		return "/common/message";
	}


	@RequestMapping("/member/memberRole")
	public String memberRole(@RequestParam String memId
			, Model model) {
		System.out.println("MemberController memberRole memId :" + memId);
		
		try{
			MemberVO member = null;
			if( memId != null && ! memId.equals("")) {
				member	= memberService.getMemberRole(memId);
			}else {
				throw new Exception();
			}
			model.addAttribute("member", member);
		 	List<RoleInfoVO> roleInfoList = memberService.getRoleInfo();
		 	model.addAttribute("roleInfoList", roleInfoList);
		}catch(BizNotEffectedException bne){
			bne.printStackTrace();
			model.addAttribute("bne", bne);
		}catch(Exception de){
			de.printStackTrace();
			model.addAttribute("de", de);
		}
		return "member.memberRole"; 
		
	}

	@RequestMapping(value = "/member/memberRoleUpdate", method=RequestMethod.POST)
	public String memberRoleUpdate(@RequestParam String memId
			, @RequestParam(required = false, name="userRole") String[] roles
			, Model model
			, ResultMessageVO resultMessageVO) {
		System.out.println("MemberController memberRoleUpdate memId : "
				+ memId 
				+ ", roles : "+ Arrays.toString(roles));
		try{
			if(memId !=null && ! memId.equals("")) {
				memberService.updateUserRole(memId, roles);
			}else {
				throw new Exception();
			}
			return "redirect:/member/memberList";
		/*}catch(BizNotEffectedException bne){
			//model.addAttribute("bne", bne);
			resultMessageVO.failSetting(false
					, "회원 권한 수정 실패"
					, "회원 권한 수정 실패 하였습니다. 전산실에 문의 부탁드립니다. 042-719-8850");
			bne.printStackTrace();*/
		}catch(Exception de){
			//model.addAttribute("de", de);
			resultMessageVO.failSetting(false
					, "회원 권한 수정 실패"
					, "회원 권한 수정 실패 하였습니다. 전산실에 문의 부탁드립니다. 042-719-8850");
			de.printStackTrace();
		}
		
		//return "/member/memberRoleUpdate";
		model.addAttribute("resultMessageVO", resultMessageVO);
		return "/common/message";
	}

	
	@RequestMapping(value="/member/memberExcelUpload", method = RequestMethod.POST)
	public String memberExcelUpload(
			@RequestParam(required = true) MultipartFile memberExcelUpload
			,ResultMessageVO resultMessageVO
			,Model model
			) {
		
		String path1 = "/home/ssam/upload/excel";
		String path2 = UUID.randomUUID().toString();
		String fileName = memberExcelUpload.getOriginalFilename();
		
		File saveFile = new File(path1 + File.separator + path2 + File.separator + fileName);
		
		if(!saveFile.getParentFile().exists()) {
			saveFile.getParentFile().mkdirs();
		}
		
		try {
			memberExcelUpload.transferTo(saveFile);
			
			//파일 업로드 했고 데이터를 가져와서 디비에 넣기
			File excel = new File(path1 + File.separator + path2 + File.separator + fileName);
			FileInputStream fis = new FileInputStream(excel);
			
			Workbook workbook = null;
			if(fileName.endsWith(".xls")) {
				workbook = new HSSFWorkbook(fis);
			}else if(fileName.endsWith(".xlsx")) {
				workbook = new XSSFWorkbook(fis);
			}
			
			if(workbook != null) {
				//시트수
				int sheets = workbook.getNumberOfSheets();
				getSheet(workbook, sheets);
			}
			
			return "redirect:/member/memberList";
		} catch (Exception e) {
			e.printStackTrace();
			resultMessageVO.failSetting(false
					, "회원엑셀등록 실패"
					, "회원엑셀 등록 실패 하였습니다. 전산실에 문의 부탁드립니다. 042-719-8850");
		}
		model.addAttribute("resultMessageVO", resultMessageVO);
		
		return "/common/message";
	}
	
	//각 시트에서 첫번재 행 과 마지막 행을 취득하려고 
	private void getSheet(Workbook workbook, int sheets) throws BizNotEffectedException {
		// TODO Auto-generated method stub
		
		for(int i =0; i<sheets ; i++) {
			Sheet sheet = workbook.getSheetAt(i);
			int startRow = sheet.getFirstRowNum();
			int endRow = sheet.getLastRowNum();
			getRow(sheet, startRow, endRow);
		}
	}
	
	//각 행에 셀 개수 취득
	private void getRow(Sheet sheet, int startRow, int endRow) throws BizNotEffectedException {
		// TODO Auto-generated method stub
		
		int cellCnt = 0;
		for(int i= startRow; i<= endRow ; i++) {
			Row row = sheet.getRow(i);
			if(row !=null) {
				if(i== startRow) {
					cellCnt = row.getLastCellNum();  //해당 셀개수 취득
				}else {
					getCellValue(row, cellCnt);
				}
			}
		}
	}
	private void getCellValue(Row row, int cellCnt) throws BizNotEffectedException {
		// TODO Auto-generated method stub
		
		String[] excelData = new String[cellCnt];
		
		for(int i=0; i<cellCnt; i++) {
			Cell cell = row.getCell(i);
			if(cell != null) {
				switch (cell.getCellType()) {
				case BLANK:
					excelData[i] = "";
					break;
				case STRING:
					excelData[i] = cell.getStringCellValue();
					break;		
				case NUMERIC:
					//숫자인데 형식이 날짜일때
					if(DateUtil.isCellDateFormatted(cell)) {
						excelData[i] = new SimpleDateFormat("yyyy-MM-dd")
								.format(cell.getDateCellValue());
					}else {
						//숫자일때
						excelData[i] = cell.getNumericCellValue()+"";	
					}
					break;	
					
				case ERROR:
					excelData[i] ="";
					break;
					
				default:
					excelData[i] ="";
					break;
				}
			}
		}
		
		MemberVO member = new MemberVO();
		for(int i=0; i< excelData.length; i++) {
			if(i==0) {
				member.setMemId(excelData[i]);
			}else if(i==1) {
				member.setMemPass(excelData[i]);
			}else if(i==2) {
				member.setMemName(excelData[i]);
			}else if(i==3) {
				member.setMemBir(excelData[i]);
			}else if(i==4) {
				member.setMemZip(excelData[i]);
			}else if(i==5) {
				member.setMemAdd1(excelData[i]);
			}else if(i==6) {
				member.setMemAdd2(excelData[i]);
			}else if(i==7) {
				member.setMemHp(excelData[i]);
			}else if(i==8) {
				member.setMemMail(excelData[i]);
			}else if(i==9) {
				member.setMemJob(excelData[i]);
			}else if(i==10) {
				member.setMemHobby(excelData[i]);
			}else if(i==11) {
				//member.setMemMileage((int)Double.parseDouble(excelData[i]));	//마일리지가 0.0 이므로 더블로 받고 int로 형변환
			}else if(i==12) {
				member.setMemDelYn(excelData[i]);
			}else if(i==13) {
				member.setMemJoinDate(excelData[i]);
			}else if(i==14) {
				member.setMemEditDate(excelData[i]);
			}
		}
		
		if(member !=null) {
			memberService.memberExcelUpload(member);
		}
	}

	
	/*@RequestMapping("/member/memberExcelDownload")
	public void memberExcelDownload(HttpServletResponse response) throws IOException {
		
		XSSFWorkbook wb = null;
		Sheet sheet = null;
		Row row = null;
		Cell cell = null;
		
		wb = new XSSFWorkbook();
		sheet = wb.createSheet("NextIT 회원목록");
		
		//row 생성
		row = sheet.createRow(0); //0번째  
		cell = row.createCell(0);
		cell.setCellValue("순번");
		cell = row.createCell(1);
		cell.setCellValue("아이디");
		cell = row.createCell(2);
		cell.setCellValue("이름");
		
		//row 생성
		row = sheet.createRow(1);	//1번째행
		cell = row.createCell(0);
		cell.setCellValue("1");
		cell = row.createCell(1);
		cell.setCellValue("nextit11");
		cell = row.createCell(2);
		cell.setCellValue("넥스트아이티11");
		
		//row 생성
		row = sheet.createRow(2);	//2번째행
		cell = row.createCell(0);
		cell.setCellValue("2");
		cell = row.createCell(1);
		cell.setCellValue("nextit12");
		cell = row.createCell(2);
		cell.setCellValue("넥스트아이티12");
		
		response.setContentType("ms-vnd/excel");
		response.setHeader("Content-Disposition"
				, "attachment; filename=memberList.xlsx");
		wb.write(response.getOutputStream());
		wb.close();
	}*/

	
	@RequestMapping("/member/memberExcelDownload")
	public void memberExcelDownload(
			HttpServletResponse response
			, @ModelAttribute MemberSearchVO searchVO) throws IOException {
		
		logger.info("memberExcelDownload searchVO.toString() : "
				+searchVO.toString());
		
		try {
			List<MemberVO> memberList = memberService.getMemberList(searchVO);
		
			if(memberList != null) {
				XSSFWorkbook wb = null;
				Sheet sheet = null;
				Row row = null;
				Cell cell = null;
				
				wb = new XSSFWorkbook();
				sheet = wb.createSheet("NextIT 회원목록");
				
				int cellCount = 0;
				
				row = sheet.createRow(0);
				
				cell = row.createCell(cellCount++);
				cell.setCellValue("순번");
				cell = row.createCell(cellCount++);
				cell.setCellValue("ID");
				cell = row.createCell(cellCount++);
				cell.setCellValue("회원명");
				cell = row.createCell(cellCount++);
				cell.setCellValue("HP");
				cell = row.createCell(cellCount++);
				cell.setCellValue("생일");
				cell = row.createCell(cellCount++);
				cell.setCellValue("직업");
				cell = row.createCell(cellCount++);
				cell.setCellValue("취미");
				cell = row.createCell(cellCount++);
				cell.setCellValue("마일리지");
				
				for(int i=0; i<memberList.size(); i++) {
					row = sheet.createRow(i+1);
					cellCount = 0;
					
					cell = row.createCell(cellCount++);
					cell.setCellValue(memberList.get(i).getRnum());	
					cell = row.createCell(cellCount++);
					cell.setCellValue(memberList.get(i).getMemId());
					cell = row.createCell(cellCount++);
					cell.setCellValue(memberList.get(i).getMemName());
					cell = row.createCell(cellCount++);
					cell.setCellValue(memberList.get(i).getMemHp());
					cell = row.createCell(cellCount++);
					cell.setCellValue(memberList.get(i).getMemBir());
					cell = row.createCell(cellCount++);
					cell.setCellValue(memberList.get(i).getMemJob());
					cell = row.createCell(cellCount++);
					cell.setCellValue(memberList.get(i).getMemHobby());
					cell = row.createCell(cellCount++);
					cell.setCellValue(memberList.get(i).getMemMileage());
				}
				
				response.setContentType("ms-vnd/excel");
				response.setHeader("Content-Disposition"
						, "attachment; filename=memberList.xlsx");
				wb.write(response.getOutputStream());
				wb.close();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@RequestMapping("/member/memberGrid")
	@ResponseBody
	public MemberSearchVO memberGrid(@ModelAttribute MemberSearchVO searchVO) {
		logger.info("memberGrid searchVO.toString():" +searchVO.toString());
		
		try {
			List<MemberVO> memberList = memberService.getMemberList(searchVO);
			searchVO.setMemberList(memberList);
		} catch (BizNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//return "member.memberList";
		return searchVO;
	}
	
	
	@RequestMapping("/member/memberGridUpdate")
	@ResponseBody
	public boolean memberGridUpdate(@RequestBody Map<String, Object> map) {
		logger.info("memberGridUpdate memId : "+ map.get("memId"));
		
		String memId =  (String) map.get("memId");
		String memName =   (String) map.get("memName");
		String memHp = (String) map.get("memHp");
		String memJob = (String) map.get("memJob");
		String memHobby = (String) map.get("memHobby");
		int memMileage = (int) map.get("memMileage");
		
		MemberVO member = new MemberVO();
		member.setMemId(memId);
		member.setMemName(memName);
		member.setMemHp(memHp);
		member.setMemJob(memJob);
		member.setMemHobby(memHobby);
		member.setMemMileage(memMileage);
		
		boolean result = memberService.memberGridUpdate(member);
		
		//return "member.memberList";
		return result;
	}
	
	@RequestMapping("/member/memberGridMultiDelete")
	@ResponseBody
	public boolean memberGridMultiDelete(
			@RequestParam(name="memIdArr[]")List<String> memId_arr ) {
		logger.info("memId_arr : " +  memId_arr);
		
		boolean result = false; 
		if(memId_arr.size()>0) {
			result  = memberService.memberGridMultiDelete(memId_arr);
		}

		//return "member.memberList";
		return result;
	}
	
}


