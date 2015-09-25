package org.gluu.oxauthsignin;



import com.liferay.portal.util.PortalUtil;
import org.xdi.oxauth.client.model.common.ResponseType;

import com.liferay.portal.kernel.util.PrefsPropsUtil;


/**
 * ResponseTypeGen class responsible of getting the ResponseType
 *
 * @author Reda Zerrad Date: 04.03.2012
 */

public class ResponseTypeGen {
	
public static ResponseType getResponseType() throws Exception{

        long companyId = PortalUtil.getDefaultCompanyId();  //TODO: support non default companyId
		String type = PrefsPropsUtil.getString(companyId, "oxauth.sso.responsetype");
		
		if(type.contains("implicit")){
			
		
			return ResponseType.TOKEN;
		}
		
		else if(type.contains("code")){
			
			
			return ResponseType.CODE;
		}
			
		
		return ResponseType.TOKEN;
		
	}

}
