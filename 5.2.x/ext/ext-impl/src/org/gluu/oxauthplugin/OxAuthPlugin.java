package org.gluu.oxauthplugin;


import java.net.URL;
import java.util.*;


import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.liferay.portal.kernel.util.*;
import com.liferay.portal.model.*;
import com.liferay.portal.service.*;
import org.xdi.oxauth.client.UserInfoClient;
import org.xdi.oxauth.client.UserInfoResponse;
import org.xdi.oxauth.client.model.userinfo.Schema;

import com.liferay.portal.NoSuchUserException;
import com.liferay.portal.PortalException;
import com.liferay.portal.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.BaseFilter;
import com.liferay.portal.util.PortalUtil;

/**
 * Main class of the Plugin
 *
 * @author Reda Zerrad Date: 03.21.2012
 */

public class OxAuthPlugin extends BaseFilter {
	
	
	@Override
	public boolean isFilterEnabled(){
		return true;
	}
	
	
    private static Log _log = LogFactoryUtil.getLog(OxAuthPlugin.class);

   
 
    // processFilter method , the filter's start point
    protected void processFilter( HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws Exception
	{
		HttpSession session;
        String requestURI;
        session = request.getSession();
        requestURI = GetterUtil.getString(request.getRequestURI());
        long companyId;
        boolean enabled;
        String OxAuthServiceProvierURL;
        companyId = PortalUtil.getCompanyId(request);
        String isenabled;

			isenabled = PrefsPropsUtil.getString(companyId, OXPropsKeys.OXAUTH_SSO_AUTH_ENABLED);
			OxAuthServiceProvierURL = PrefsPropsUtil.getString(companyId, OXPropsKeys.OXAUTH_SSO_SP_URL);
	
        enabled = isenabled !=null;
        
        if(!enabled || Validator.isNull(OxAuthServiceProvierURL))
        {     
				processFilter(OxAuthPlugin.class,request, response, filterChain);
                 return;
        }
        
        String portalRootURL;
        boolean authenticated;
        URL reconstructedURL;
	    reconstructedURL = new URL(request.getScheme(), request.getServerName(), request.getServerPort(), "");
        portalRootURL = reconstructedURL.toString();
        _log.info((new StringBuilder()).append("OxAuth - SSO Filter: enabled = ").append(enabled).append(", portal root URL = ").append(portalRootURL).toString());
        String userNameSessionAttribute = (String)request.getSession().getAttribute("oxauth.username");
        authenticated = userNameSessionAttribute != null;
        
        if(requestURI.endsWith("/web/guest/loginpage"))
        {
            session.setAttribute("oxauth.login.page.queryString", request.getQueryString());
            
				processFilter(OxAuthPlugin.class,request, response, filterChain);
                return;
        }
        
        
if(request.getParameter("code") != null){
        	
        	String authorizationCode = request.getParameter("code");
        	
        	String accessToken = AccessTokengetter.getAccessToken(authorizationCode, portalRootURL);
        	
        	if(accessToken != "NOT_FOUND"){
        		
        		boolean domainIsValid = false;
            	domainIsValid = DomainChecker.isDomainTrusted(accessToken);
            	
            	
            	if(!domainIsValid){
            		
            		String targetURL = (new StringBuilder()).append(portalRootURL).append("/web/guest/home").toString();
	                response.sendRedirect(targetURL);
	                return;
            	} else {
        	
        	 session.setAttribute("access_token",accessToken);
        	 System.out.println("Access Token : " + session.getAttribute("access_token").toString() );
        	 processLogin(request, response, filterChain, session, companyId, authenticated);
        	 return;
        	 }
        	
        	}
        	
        	else {
        	
        	String targetURL = (new StringBuilder()).append(portalRootURL).append("/web/guest/home").toString();
            response.sendRedirect(targetURL);
            return;
            }
        
 
        }
        
        
        try
        {
        	String access_token = request.getParameter("access_token");
        	
        	
        	
            if(access_token != null && !authenticated ){
            	
        
            	
            	boolean domainIsValid = false;
            	domainIsValid = DomainChecker.isDomainTrusted(access_token);
            	
            	
            	if(!domainIsValid){
            		
            		String targetURL = (new StringBuilder()).append(portalRootURL).append("/web/guest/home").toString();
	                response.sendRedirect(targetURL);
	                return;
            	}else{	
            		session.setAttribute("access_token",access_token);
             processLogin(request, response, filterChain, session, companyId, authenticated);
             return;
            	}
            }
            	
            
        	
            else if(requestURI.endsWith("/web/guest/home") && !authenticated)
            {
                String p_p_id = request.getParameter("p_p_id");
                String _58_struts_action = request.getParameter("_58_struts_action");
                if(p_p_id != null && p_p_id.equals("58") && _58_struts_action != null && _58_struts_action.equals("/login/login"))
                {
                    String redirectPage = request.getParameter("_58_redirect");
                    response.sendRedirect((new StringBuilder()).append(portalRootURL).append("/c/portal/login?_58_redirect=").append(redirectPage).toString());
                    return;
                }
            }
           _log.info((new StringBuilder()).append("OxAuth - SSO Filter: OxAuthServiceProvierURL = ").append(OxAuthServiceProvierURL).toString());
           _log.info((new StringBuilder()).append("OxAuth - SSO Filter: Request URI = ").append(requestURI).toString());
           if(requestURI.endsWith("/portal/logout")){
                processLogout(request, response, session, companyId);
                return;
                
           }
            else
            if(requestURI.endsWith("/portal/login")){
                processLogin(request, response, filterChain, session, companyId, authenticated);
                return;
                }
            else{
            	processFilter(OxAuthPlugin.class,request, response, filterChain);
                
            	return;}
        }
        catch(Exception e)
        {
            _log.error(e, e);
        }
        return;
	} // processFilter

	
    // The method that processes login
	private void processLogin(HttpServletRequest request,
			HttpServletResponse response,FilterChain filterChain,
			HttpSession session, long companyId, boolean authenticated) throws Exception {
		


		 
		 boolean ssoTokenAvailable = false;
	        URL reconstructedURL = new URL(request.getScheme(), request.getServerName(), request.getServerPort(), "");
	        String portalRootURL = reconstructedURL.toString();
	        String ssoRequestLoginToken = null;
	        try
	        {
	        	
	        	if(session.getAttribute("access_token") != null){	
	            ssoRequestLoginToken = session.getAttribute("access_token").toString(); }
	        	
	            ssoTokenAvailable = ssoRequestLoginToken != null && ssoRequestLoginToken.length() > 0;
	            
	        }
	        catch(Exception exception)
	        {
	          _log.error("Failed to check if user is authenticated and get Access token", exception);
	            processFilter(OxAuthPlugin.class,request, response, filterChain);
	            return;
	        }
	        
	        if(!authenticated && ssoTokenAvailable)
	        {
	           _log.info("User is not authenticated and SSO token is available ...");
	          
	        
	           
	            String oldSubjectName = (String)session.getAttribute("oxauth.sso.username");
	            String targetURL = (String)session.getAttribute("oxauth.sso.targeturl");
	            
	            
	            String redirectURL = PrefsPropsUtil.getString(companyId, OXPropsKeys.OXAUTH_SSO_SP_URL);
	            redirectURL = (new StringBuilder()).append(redirectURL).append("seam/resource/restv1/oxauth/userinfo").toString();
	            
	            UserInfoClient userInfoClient = new UserInfoClient(redirectURL);
	            UserInfoResponse attributes = userInfoClient.execUserInfo(ssoRequestLoginToken, Schema.OPEN_ID);
	            String newSubjectName =  removeSpaces(attributes.getUserId().toString());
	            
	            
	            
	            if(oldSubjectName != null){
	            if(!newSubjectName.equals(oldSubjectName))
	            {
	                session.invalidate();
	                session = request.getSession();
	            }
	            
	            }
	            
	            if(targetURL == null ){
	            	
	            	String freshtargetURL = portalRootURL;
	            	freshtargetURL = (new StringBuilder()).append(freshtargetURL).append("/web/guest/home").toString();
	            	targetURL = freshtargetURL;
	            }
	            
	            session.setAttribute("oxauth.sso.username", newSubjectName);
	            String access_token = ssoRequestLoginToken;
	             
	            
	            processAssertionAttributes(session, companyId,newSubjectName,access_token );
	            
	            if(targetURL.endsWith("/web/guest/home") || targetURL.endsWith("/web/guest"))
	            	
	            	
	            	targetURL = (new StringBuilder()).append(portalRootURL).append("/user/").append(newSubjectName).append("/home").toString();
	                response.sendRedirect(targetURL);
	                return;
	        } else
	        {
	           _log.info("oxAuth Connect - SSO Filter: User is not authenticated - initiate oxAuth login process ...");
	            String redirectPage = request.getParameter("_58_redirect");
	            String pageToOpenAfterLogin = portalRootURL;
	            if(redirectPage != null && !redirectPage.isEmpty())
	                pageToOpenAfterLogin = (new StringBuilder()).append(pageToOpenAfterLogin).append(redirectPage).toString();
	            else
	                pageToOpenAfterLogin = (new StringBuilder()).append(pageToOpenAfterLogin).append("/web/guest/home").toString();
	            launchOxauthLoginProcess(companyId, request, response, pageToOpenAfterLogin);
	        }
	        
		
	} // processLogin
	
	
	
     // the method that launches the authentication process
	private void launchOxauthLoginProcess(Long companyId,
			HttpServletRequest request, HttpServletResponse response,
			String pageToOpenAfterLogin) throws Exception {

		HttpSession session = request.getSession();
		String portalRootURL;
		URL reconstructedURL = null;
		reconstructedURL = new URL(request.getScheme(), request.getServerName(), request.getServerPort(), "");
		portalRootURL = reconstructedURL.toString();
		String loginpage = portalRootURL;
		loginpage = (new StringBuilder()).append(loginpage).append("/c/portal/login").toString();
		
		
		
		String response_type = ResponseTypegetter.getResponseType();
		String client_id = PrefsPropsUtil.getString(companyId, OXPropsKeys.OXAUTH_SSO_CLIENTID);
		session.setAttribute("client_id", client_id);
		session.setAttribute("oxauth.sso.targeturl", pageToOpenAfterLogin);
		String redirectURL = PrefsPropsUtil.getString(companyId, OXPropsKeys.OXAUTH_SSO_SP_URL);
		_log.info((new StringBuilder()).append("oxauth Service Provider URL: ").append(redirectURL).toString());
		String state ="state=af0ifjsldkj";
		String nonce = "nonce=n-0S6_WzA2Mj";
		String scope = "scope=openid+profile+address+email";
        redirectURL = (new StringBuilder()).append(redirectURL).append("authorize.seam?").append(response_type).append("&").append("client_id=").append(client_id).append("&").append(scope).append("&").append("redirect_uri=").append(pageToOpenAfterLogin).append("?").append("&").append(nonce).append("&").append(state).toString();
        response.sendRedirect(redirectURL);
        return;
        	
        
	}// launchOxauthLoginProcess
	
	
     // a method to request the userinfo from the oxAuth Server
	
	private void processAssertionAttributes(HttpSession session,
			long companyId, String newSubjectName, String access_token) throws Exception {
		
		String redirectURL = PrefsPropsUtil.getString(companyId, OXPropsKeys.OXAUTH_SSO_SP_URL);
        redirectURL = (new StringBuilder()).append(redirectURL).append("seam/resource/restv1/oxauth/userinfo").toString();
        
        UserInfoClient userInfoClient = new UserInfoClient(redirectURL);
        UserInfoResponse attributes = userInfoClient.execUserInfo(access_token, Schema.OPEN_ID);
        String user_id = removeSpaces(attributes.getUserId().toString());
        String email = removeSpaces(attributes.getEmail().toString());
        String firstName = removeSpaces(attributes.getGivenName().toString());
        String lastName = removeSpaces(attributes.getFamilyName().toString());
        String fullName = (new StringBuilder()).append(firstName).append(" ").append(lastName).toString();
       
        session.setAttribute("oxauth.user_id", user_id);
        session.setAttribute("oxauth.email", email);
        session.setAttribute("oxauth.firstName", firstName);
        session.setAttribute("oxauth.lastName", lastName);
        session.setAttribute("oxauth.fullName", fullName);
        
       
        
        
        createOrUpdateLiferayUser(user_id, email, firstName, lastName, fullName);

		
	}// processAssertionAttributes

	
	// creating a new LifeRay user if the user does not exist or updating it if it exists
	private void createOrUpdateLiferayUser(String user_id, String email,
			String firstName, String lastName, String fullName) throws Exception {

		try{
			
			long companyId = PortalUtil.getDefaultCompanyId();
            String languageId = "en_US";
            User lifeRayUser = null;
            
           try
            {
                lifeRayUser = UserLocalServiceUtil.getUserByEmailAddress(companyId, email);
            }
            catch(NoSuchUserException noSuchUserException)
            {
               _log.debug((new StringBuilder()).append("User ").append(email).append(" is not found").toString());
            }
            
            if(lifeRayUser == null)
            {
               _log.debug((new StringBuilder()).append("Create new LifeRay user object for ").append(email).toString());
               
               ServiceContext serviceContext = new ServiceContext();
               serviceContext.setCompanyId(companyId);

                long creatorUserId = UserLocalServiceUtil.getDefaultUser(companyId).getUserId();
                boolean autoPassword = false;
                String password1 = StringPool.BLANK;
                String password2 = password1;
                boolean autoScreenName = false;
                String screenName = user_id;
                String emailAddress = email;
                String openId = StringPool.BLANK;
                Locale locale = Locale.getDefault();
                String middleName = StringPool.BLANK;
                int prefixId = 0;
                int suffixId = 0;
                boolean male = true;
                int birthdayMonth = Calendar.JANUARY;
                int birthdayDay = 1;
                int birthdayYear = 1970;
                String jobTitle = StringPool.BLANK;

                Group guestGroup = GroupLocalServiceUtil.getGroup(
                        companyId, GroupConstants.GUEST);

                long[] groupIds = new long[] {guestGroup.getGroupId()};

                Organization sevenCogsOrganization =
                        OrganizationLocalServiceUtil.getOrganization(
                                companyId, "7Cogs, Inc.");

                long[] organizationIds = new long[] {
                        sevenCogsOrganization.getOrganizationId()
                };

                long[] roleIds = null;

                long[] userGroupIds = null;
                boolean sendEmail = false;

                lifeRayUser = UserLocalServiceUtil.addUser(
                        creatorUserId, companyId, autoPassword, password1, password2,
                        autoScreenName, screenName, emailAddress, openId, locale, firstName,
                        middleName, lastName, prefixId, suffixId, male, birthdayMonth,
                        birthdayDay, birthdayYear, jobTitle, groupIds, organizationIds,
                        roleIds, userGroupIds, sendEmail, serviceContext);
                
                String Question = randomString();
                String Answer = randomString();
               
               
                lifeRayUser.setAgreedToTermsOfUse(true);
                lifeRayUser.setPasswordReset(false);
                lifeRayUser.setReminderQueryQuestion(Question);
                lifeRayUser.setReminderQueryAnswer(Answer);
                lifeRayUser.setPasswordEncrypted(false);
                lifeRayUser.setCreateDate(new Date());
                
                
            }
            
            String timeZoneId = "America/Eastern";
           _log.debug((new StringBuilder()).append("Updating TimeZone to = ").append(timeZoneId).toString());
            lifeRayUser.setTimeZoneId(timeZoneId);
           _log.debug((new StringBuilder()).append("Updating Language to = ").append(languageId).toString());
            lifeRayUser.setLanguageId(languageId);
           _log.debug((new StringBuilder()).append("Welcome ").append(lifeRayUser.getFullName()).append(", your screenName = ").append(lifeRayUser.getScreenName()).toString());
           _log.debug((new StringBuilder()).append("Language = ").append(lifeRayUser.getLanguageId()).toString());
            String jobTitle = "User";
            if(jobTitle != null)
            {
               _log.debug((new StringBuilder()).append("Updating Job Title to = ").append(jobTitle).toString());
                Contact contact = lifeRayUser.getContact();
                contact.setJobTitle(jobTitle);
                ContactLocalServiceUtil.updateContact(contact);
            }
            UserLocalServiceUtil.updateUser(lifeRayUser);
			
		}
		
		catch(SystemException systemException)
        {
            _log.error((new StringBuilder()).append("SystemException: ").append(email).append(" - ").append(user_id).append(": ").append(systemException.getClass().getName()).append("\n    firstName = ").append(firstName).append("\n    lastName  = ").append(lastName).toString());
        }
        catch(PortalException portalException)
        {
           _log.error((new StringBuilder()).append("PortalException: ").append(email).append(" - ").append(user_id).append(": ").append(portalException.getClass().getName()).append("\n    firstName = ").append(firstName).append("\n    lastName  = ").append(lastName).toString());
        }
        catch(Exception exception)
        {
           _log.error(exception);
        }
	}// createOrUpdateLiferayUser

	// Login out method
	private void processLogout(HttpServletRequest request,
			HttpServletResponse response, HttpSession session, long companyId) throws Exception {
		
		  URL reconstructedURL = new URL(request.getScheme(), request.getServerName(), request.getServerPort(), "");
	      String portalRootURL = reconstructedURL.toString();
	     
	      String islogoutOnRequest = PrefsPropsUtil.getString(companyId, OXPropsKeys.OXAUTH_SSO_LOGOUT_ON_REQUEST);
          boolean logoutOnRequest = false;
          
          if(islogoutOnRequest == "enabled"){
        	  
        	  logoutOnRequest = true;	  
          }
	     
	    
			  if(logoutOnRequest)
	            {
				  _log.info("Initiate oxAuth SSO logout on request process ...");
	                session.removeAttribute("oxauth.sso.username");
	                session.invalidate();
	                response.sendRedirect((new StringBuilder()).append(portalRootURL).append("/web/guest/home").toString());
	                return;
	            }
	      
	      else
	        {
	          _log.info("Initiate oxAuth SSO logout process ...");
	            
	            String pageToOpenAfterLogout = portalRootURL;
	            pageToOpenAfterLogout = (new StringBuilder()).append(pageToOpenAfterLogout).append("/web/guest/home?").toString();
	    		session.setAttribute("oxauth.sso.logouturl", pageToOpenAfterLogout);
	    		String redirectURL = PrefsPropsUtil.getString(companyId, OXPropsKeys.OXAUTH_SSO_SP_URL);
	    		_log.info((new StringBuilder()).append("oxauth Service Provider URL: ").append(redirectURL).toString());
	    		String state ="state=af0ifjsldkj";
	    		String access_token = session.getAttribute("access_token").toString();
	            redirectURL = (new StringBuilder()).append(redirectURL).append("seam/resource/restv1/oxauth/end_session?").append("access_token=").append(access_token).append("&").append("redirect_uri=").append(pageToOpenAfterLogout).append("&").append(state).toString();
	    		session.removeAttribute("oxauth.sso.username");
		        session.invalidate();
	            response.sendRedirect(redirectURL);
	            return;
	        }
		
		
		
	}// createOrUpdateLiferayUser



	@Override
	protected Log getLog() {
		// TODO Auto-generated method stub
		 Log _log = LogFactoryUtil.getLog(OxAuthPlugin.class);
		 
		 return _log;
	}

	// This method removes spaces from strings
	protected String removeSpaces(String s) {
		  StringTokenizer st = new StringTokenizer(s," ",false);
		  String t="";
		  while (st.hasMoreElements()) t += st.nextElement();
		  return t;
		}
    
	// This method generates a random alphabetic String
	protected String randomString() 
	{
		
		String AB = "abcdefghijklmnopqrstuvwyz";
		Random rnd = new Random();
	    StringBuilder sb = new StringBuilder( 8 );
	   for( int i = 0; i < 8 ; i++ ) 
	      sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
	   return sb.toString();
	}
   
	 

}
