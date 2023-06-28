package kr.or.nextit.common.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/*요청으로 넘어오는 값을 변경하기위해서 HttpServletRequestWrapper
를 상속해서 처리해야 함*/
public class NextITXSSFilterRequestWrapper extends HttpServletRequestWrapper{

	public NextITXSSFilterRequestWrapper(HttpServletRequest request) {
		super(request);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String getParameter(String parameter) {
		// TODO Auto-generated method stub
		//return super.getParameter(name);
		
		String value =  super.getParameter(parameter);
		if(value == null) {
			return null;
		}
		
		StringBuffer strBuff = new StringBuffer();
		for(int i=0; i<value.length(); i++	) {
			char c = value.charAt(i);
			switch (c) {
			case '<':
				strBuff.append("&lt");
				break;
			case '>':
				strBuff.append("&gt");
				break;	
			case '&':
				strBuff.append("&amp");
				break;	
			case '"':
				strBuff.append("&quot");
				break;		
			case '\'':
				strBuff.append("&apos");
				break;		
				
			default:
				strBuff.append(c);
				break;
			}
		}
		
		value = strBuff.toString();
		return value;
	}
	
	@Override
	public String[] getParameterValues(String parameter) {
		// TODO Auto-generated method stub
		//return super.getParameterValues(parameter);
		
		String[] values =  super.getParameterValues(parameter);
		if(values == null) {
			return null;
		}
		
		for(int i=0; i<values.length; i++	) {
			if(values[i] != null) {
				StringBuffer strBuff = new StringBuffer();
				for(int j=0; j<values[i].length(); j++) {
					char c = values[i].charAt(j);
					switch (c) {
					case '<':
						strBuff.append("&lt");
						break;
					case '>':
						strBuff.append("&gt");
						break;	
					case '&':
						strBuff.append("&amp");
						break;	
					case '"':
						strBuff.append("&quot");
						break;		
					case '\'':
						strBuff.append("&apos");
						break;		
						
					default:
						strBuff.append(c);
						break;
					}
				}
				values[i] = strBuff.toString();
			}else {
				values[i] = null;
			}
		}
		return values;
	}

}
