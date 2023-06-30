<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>FAIL</title>
</head>
<body>
<div class="container">
	<c:if test="${de ne null }">
		<h3>로그인 처리 실패</h3>
		<div class="alert alert-warning">
			<div class="btn-area">
				<button type="button" onclick="history.back();">뒤로가기</button>
			</div>
		</div>
	</c:if>
	
</div>
</body>
</html>