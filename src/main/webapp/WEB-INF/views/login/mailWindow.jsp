<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%> 
<%@ taglib prefix="c"  uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>NextIT</title>
<script  src="http://code.jquery.com/jquery-latest.min.js"></script>
<style type="text/css">
body{
    background: lightgray;
    text-align: center;
}
input{
    margin-top: 60px;
    height: 25px;
    border-radius: 10px;
}
button{
	border-radius: 10px;
}
</style>

</head>
<body>

인증번호 <input type="text" id="authKey" name="authKey"	value="" >
<button type="button" id="authKeyCompare">인증키 확인</button>

<script type="text/javascript">
$(document).ready(function(){
	$("#authKeyCompare").on("click", function(){
		
		let mail = opener.document.getElementById("memMail").value;
		let authKey = $("input[name='authKey']").val();
		
		$.ajax({
			url: "<c:url value='/join/authKeyCompare'/>"
			,data : {"mail":  mail , "authKey": authKey }
			,success: function(data){
				//alert(data)
				if(data){
					alert("인증되었습니다.");
					window.close();
				}else{
					alert("인증키가 틀립니다. 다시 확인해 주세요");
				}
			}
			,error : function(e){
				alert("메일인증 처리도중 문제가 발생하였습니다. 전산실에 문의 부탁드립니다.042-719-8850");
			}
		});
		
	});
});
</script>
</body>
</html>