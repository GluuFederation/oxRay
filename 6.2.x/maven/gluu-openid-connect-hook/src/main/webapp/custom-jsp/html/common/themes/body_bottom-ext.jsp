<%--
/*
 * Copyright (c) Gluu, Inc. All rights reserved.
 * 
 * http://www.gluu.org/
 */
--%>

<%@ include file="/html/common/themes/init.jsp" %>

<%-- <%
	String open_id_cookie_name = "OPENID_SESSION_STATE";
	String open_id_session_id = getCookie(request, open_id_cookie_name);
	boolean isUserSignIn =  themeDisplay.getPermissionChecker().isSignedIn();

	//System.out.println("body_bottom-ext.jsp open_id_session_id=" + open_id_session_id + ", isUserSignIn=" + isUserSignIn);
	
	if(!isUserSignIn){
				
		if(Validator.isNotNull(open_id_session_id)){
			String idpLogoutURL = PropsUtil.get("gluu.openidconnect.idp.logout");
			%>
				<!--   insert invisible iframe for idp logout -->
				<iframe id="idp-logout-iframe" src="<%=idpLogoutURL%>" width="1" height="1"></iframe>
			<%
			//remove the cookie
			Cookie cookie = new Cookie(open_id_cookie_name, null);
			cookie.setMaxAge(0);
			cookie.setPath("/");
			response.addCookie(cookie);
		}
		
		return;
	}
	
	/* String client_id = PropsUtil.get("gluu.openidconnect.client.id");
	String idp_origin = PropsUtil.get("gluu.openidconnect.idp.domain");
	String rp_origin = PortalUtil.getPortalURL(request);
	
	System.out.println("body_bottom-ext.jsp client_id=" + client_id + ", open_id_session_id=" 
						+ open_id_session_id + ", rp_origin=" + rp_origin + ", idp_origin=" + idp_origin); */
%>


<script src="http://crypto-js.googlecode.com/svn/tags/3.0.2/build/rollups/sha256.js"></script>

<iframe id="idpIframe" src="<%=idp_origin%>/oxauth/opiframe.seam" width="1" height="1"></iframe>
	
<script type="text/javascript">
   
	   var rpOrigin = "<%=rp_origin%>"; // "https://localhost:8443";
	   var opOrigin = "<%=idp_origin%>"; // "https://localhost:8443";
	   var clientId = null;
	   var opbs = null;
	   var mes = null;
	   var timerId = null;
	
	   function checkSession() {
	       var win = window.document.getElementById("idpIframe").contentWindow;
	       
	       alert('mes=' + mes + ", oporigin=" + opOrigin);
	       
	       win.postMessage(mes, opOrigin);
	   }
	
	   function setTimer() {
	       clearTimer();
	       checkSession();
	       timerId = setInterval("checkSession()", 5 * 1000);
	   }
	
	   function clearTimer() {
	       if (timerId) {
	           window.clearInterval(timerId);
	           timerId = null;
	       }
	   }
	
	   window.addEventListener("message", receiveMessage, false);
	
	   function receiveMessage(e) {
		   
		   alert("e.origin=" + e.origin +", opOrigin=" + opOrigin);
		   
	       if (e.origin !== opOrigin) {
	           return;
	       }
	       alert("Session State: " + e.data);
	       
	       //console.log("Session State: " + e.data);
	       if (e.data == "changed") {
	           clearTimer();
	           alert("Session State has changed");
	       }
	   }
	   
	   function updateMes() {
	       opbs = "<%=open_id_session_id%>";
	       clientId = "<%=client_id%>";
	
	       var salt = CryptoJS.lib.WordArray.random(128 / 8);
	       mes = clientId + ' ' + CryptoJS.SHA256(clientId + ' ' + rpOrigin + ' ' + opbs + ' ' + salt) + "." + salt;

	       setTimer();
	   }
	   
	   AUI().ready(
		    function() {
				 updateMes();
		    }
		);
	   
</script>

<%!
	public String getCookie(HttpServletRequest _request, String cookieName){
		for(Cookie cookie : _request.getCookies()){
			if(cookie.getName().equals(cookieName)){
				return cookie.getValue();
			}
		}	
		
		return null;
	}
%> --%>

