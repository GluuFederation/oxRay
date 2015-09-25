package org.gluu.oxauthplugin;

import com.liferay.portal.kernel.util.PrefsPropsUtil;

import org.xdi.oxauth.client.TokenClient;
import org.xdi.oxauth.client.TokenResponse;

/**
 * AccessTokengetter the class responsible of getting the AccessToken
 *
 * @author Reda Zerrad Date: 03.28.2012
 * @author Arcko Duan
 *
 */

public class AccessTokengetter {

	public static String getAccessToken(String authorizationCode, String portalRootURL) throws Exception {

		String redirectUri = (new StringBuilder()).append(portalRootURL).append("/web/guest/home?").toString();
		String tokenUrl = PrefsPropsUtil.getString(OXPropsKeys.OXAUTH_SSO_SP_URL);
		tokenUrl = (new StringBuilder()).append(tokenUrl).append("seam/resource/restv1/oxauth/token").toString();
		//String clientCredentials = PrefsPropsUtil.getString(OXPropsKeys.OXAUTH_SSO_CLIENTCREDENTIAL);

		TokenClient tokenClient1 = new TokenClient(tokenUrl);
		System.out.println("Code : " + authorizationCode );
		String clientId = getCredentials()[0];
		String clientSecret = getCredentials()[1];
		TokenResponse tokenResponse = tokenClient1.execAuthorizationCode(authorizationCode, redirectUri, clientId, clientSecret);

		int status = tokenResponse.getStatus();

		if (status == 200) {
			String accessToken = tokenResponse.getAccessToken();
			return accessToken;
		}
		else {

		return "NOT_FOUND";
		}
	}

public static String[] getCredentials() throws Exception {

		String credentials = PrefsPropsUtil.getString(OXPropsKeys.OXAUTH_SSO_CLIENTCREDENTIAL);
		String str[]=credentials.split(":");
		return str;

	}

}