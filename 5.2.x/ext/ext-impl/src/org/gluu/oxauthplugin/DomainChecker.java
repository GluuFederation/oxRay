package org.gluu.oxauthplugin;


import com.liferay.portal.util.PortalUtil;
import org.xdi.oxauth.client.ValidateTokenClient;
import org.xdi.oxauth.client.ValidateTokenResponse;

import com.liferay.portal.kernel.util.PrefsPropsUtil;

/**
 * Main class of the domains checker
 *
 * @author Reda Zerrad Date: 03.27.2012
 */

public class DomainChecker {

// the method that checks if the source of the access_token is trusted 
public static boolean isDomainTrusted(String access_token) throws Exception	{
	
	String domains[] = getDomains();
	
	for(int i=0;i< domains.length;i++){
		
		String validateUrl = domains[i];
		validateUrl = (new StringBuilder()).append(validateUrl).append("seam/resource/restv1/oxauth/validate").toString();
		
		ValidateTokenClient validateTokenClient = new ValidateTokenClient(validateUrl);
		ValidateTokenResponse response = validateTokenClient.execValidateToken(access_token);
		
		boolean isValid = false;
		isValid = response.isValid();
		
		if(isValid == true){
			
			return true;
		}
		
		
	}
	
    return false;
	
}
	

// the method that gets the list of trusted domains from the properties
public static String[] getDomains() throws Exception{

    long companyId = PortalUtil.getDefaultCompanyId();  //TODO: support non default companyId
	String domains = PrefsPropsUtil.getString(companyId, OXPropsKeys.OXAUTH_SSO_TRUSTEDDOMAINS);
	String str[]=domains.split(";");
	return str;
	
}
	
		

}
