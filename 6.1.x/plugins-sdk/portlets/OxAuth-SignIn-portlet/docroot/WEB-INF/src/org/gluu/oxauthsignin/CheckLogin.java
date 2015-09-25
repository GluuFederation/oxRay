package org.gluu.oxauthsignin;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


import org.xdi.oxauth.client.AuthorizationRequest;
import org.xdi.oxauth.client.model.common.ResponseType;

import com.liferay.portal.kernel.util.PrefsPropsUtil;

/**
 * CheckLogin Class the class responsible of forwarding the request to oxAuth server
 *
 * @author Reda Zerrad Date: 04.02.2012
 */

public class CheckLogin extends HttpServlet {
	
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request,
            HttpServletResponse response)
throws ServletException{
		
		String userName = request.getParameter("username");
		String passWord = request.getParameter("password");
		
		try{
		if (userName == null || passWord == null) {
			URL reconstructedURL;
			reconstructedURL = new URL(request.getScheme(), request.getServerName(), request.getServerPort(), "");
		    String portalRootURL = reconstructedURL.toString();
		    String redirectUri = (new StringBuilder()).append(portalRootURL).append("/web/guest/home").toString();
		    response.sendRedirect(redirectUri);
		    return;
        }
		
		else{ 
		HttpSession session;
		session = request.getSession();
		List<ResponseType> responseTypes = new ArrayList<ResponseType>();
		
        responseTypes.add(ResponseTypeGen.getResponseType());
        responseTypes.add(ResponseType.ID_TOKEN);
        List<String> scopes = new ArrayList<String>();
        scopes.add("openid");
        scopes.add("profile");
        scopes.add("address");
        scopes.add("email");
        String nonce = "n-0S6_WzA2Mj";
        String state = "af0ifjsldkj";
        URL reconstructedURL;
	    reconstructedURL = new URL(request.getScheme(), request.getServerName(), request.getServerPort(), "");
	    String portalRootURL = reconstructedURL.toString();
		String redirectUri = (new StringBuilder()).append(portalRootURL).append("/web/guest/home?").toString();
		session.setAttribute("oxauth.sso.username", userName);
		String clientID =  PrefsPropsUtil.getString("oxauth.sso.clientid");
		String serverURL =  PrefsPropsUtil.getString("oxauth.sso.sp.url");
		
		AuthorizationRequest authRequest = new AuthorizationRequest(responseTypes, clientID, scopes, redirectUri, nonce);
        authRequest.setState(state);
        authRequest.setAuthUsername(userName);
        authRequest.setAuthUsername(userName);
        
        String requestURL = serverURL;
        requestURL = (new StringBuilder()).append(requestURL).append("seam/resource/restv1/oxauth/authorize?").append(authRequest.getQueryString()).toString();
        response.sendRedirect(requestURL);
        return;
		}
	}
	catch(Exception e){
		
	    return;
		
	}

}

}
