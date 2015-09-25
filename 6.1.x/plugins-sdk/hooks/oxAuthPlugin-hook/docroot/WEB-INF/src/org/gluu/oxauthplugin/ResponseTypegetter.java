package org.gluu.oxauthplugin;

import com.liferay.portal.kernel.util.PrefsPropsUtil;

/**
 * ResponseTypegetter class gets the ResponseType
 *
 * @author Reda Zerrad Date: 04.02.2012
 * @author Arcko Duan
 */
public class ResponseTypegetter {

	public static String getResponseType() throws Exception {

		String type = PrefsPropsUtil.getString(OXPropsKeys.OXAUTH_SSO_RESPONSETYPE);

		if (type.contains("implicit")) {

			String responseType ="response_type=token+id_token";
			return responseType;
		}

		else if (type.contains("code")) {

			String responseType ="response_type=code+id_token";
			return responseType;
		}

		String responseType ="response_type=token+id_token";
		return responseType;

	}

}