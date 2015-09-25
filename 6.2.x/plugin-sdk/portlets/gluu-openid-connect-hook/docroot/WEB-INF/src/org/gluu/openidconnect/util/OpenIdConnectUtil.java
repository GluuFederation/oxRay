/*
 * Copyright (c) Gluu, Inc. All rights reserved.
 * 
 * http://www.gluu.org/
 */
package org.gluu.openidconnect.util;

import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpStatus;
import org.gluu.openidconnect.exception.OpenIdConnectExpection;
import org.gluu.openidconnect.model.SessionToken;
import org.xdi.oxauth.client.AuthorizationRequest;
import org.xdi.oxauth.client.OpenIdConfigurationClient;
import org.xdi.oxauth.client.OpenIdConfigurationResponse;
import org.xdi.oxauth.client.TokenClient;
import org.xdi.oxauth.client.TokenRequest;
import org.xdi.oxauth.client.TokenResponse;
import org.xdi.oxauth.client.UserInfoClient;
import org.xdi.oxauth.client.UserInfoRequest;
import org.xdi.oxauth.client.UserInfoResponse;
import org.xdi.oxauth.model.common.AuthenticationMethod;
import org.xdi.oxauth.model.common.AuthorizationMethod;
import org.xdi.oxauth.model.common.GrantType;
import org.xdi.oxauth.model.common.ResponseType;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.GetterUtil;

/**
 * @author Rajesh (liferay.freelancer@gmail.com)
 * 
 *         Utility class to handle and invoke the OpenID OXauth API
 */
public class OpenIdConnectUtil {

	private static String _authorizationEndpoint = null;
	private static String _tokenEndpoint = null;
	private static String _userInfoEndpoint = null;
	private static String _endsessionEndpoint = null;

	public static boolean isAuthEnabled() throws SystemException {

		return GetterUtil.getBoolean(OpenIdConnectProperties.OPEN_ID_AUTH_ENABLED);
	}

	public static void initOpenIdDiscovery() {

		_log.info("initOpenIdDiscovery - initialize all endpoints...");

		OpenIdConfigurationClient client = new OpenIdConfigurationClient(
						OpenIdConnectProperties.OPEN_ID_DISCOVERY_URL);
		OpenIdConfigurationResponse response = client.execOpenIdConfiguration();

		int status = response.getStatus();

		_log.info("initOpenIdDiscovery - status=" + status);

		//String issuer = response.getIssuer();

		_authorizationEndpoint = response.getAuthorizationEndpoint();
		_tokenEndpoint = response.getTokenEndpoint();
		_userInfoEndpoint = response.getUserInfoEndpoint();
		_endsessionEndpoint = response.getEndSessionEndpoint();
	}

	public static String getAuthorizeURL() {

		if (_authorizationEndpoint == null) {
			initOpenIdDiscovery();
		}

		List<ResponseType> responseTypes = Arrays.asList(ResponseType.CODE);

		List<String> scopes = Arrays.asList("openid", "profile", "email");

		AuthorizationRequest authorizationRequest = new AuthorizationRequest(responseTypes,
						OpenIdConnectProperties.CLIENT_ID, scopes,
						OpenIdConnectProperties.REDIRECT_URL, null);

		String request = _authorizationEndpoint + "?" + authorizationRequest.getQueryString();

		return request;
	}

	public static SessionToken getAccessToken(String code) throws OpenIdConnectExpection {

		if (_tokenEndpoint == null) {
			initOpenIdDiscovery();
		}

		// Call the service
		TokenRequest tokenRequest = new TokenRequest(GrantType.AUTHORIZATION_CODE);
		tokenRequest.setCode(code);
		tokenRequest.setRedirectUri(OpenIdConnectProperties.REDIRECT_URL);
		tokenRequest.setAuthUsername(OpenIdConnectProperties.CLIENT_ID);
		tokenRequest.setAuthPassword(OpenIdConnectProperties.CLIENT_SECRET);
		tokenRequest.setAuthenticationMethod(AuthenticationMethod.CLIENT_SECRET_BASIC);

		TokenClient tokenClient = new TokenClient(_tokenEndpoint);
		tokenClient.setRequest(tokenRequest);

		if (_log.isDebugEnabled()) {
			_log.debug("getAccessToken - tokenRequest=" + tokenClient.getRequestAsString());
		}
		
		// Handle response
		TokenResponse tokenResponse = tokenClient.exec();
		int status = tokenResponse.getStatus(); // 200 if succeed
		
		if (_log.isDebugEnabled()) {
			_log.debug("autologin - tokenResponse=" + tokenClient.getResponseAsString());
		}

		if (status != HttpStatus.SC_OK) {
			throw new OpenIdConnectExpection(tokenResponse.getErrorDescription());
		}

		String accessToken = tokenResponse.getAccessToken(); // 16973cbc-3a6d-4d42-a5c6-6bbc1da1f06f

		SessionToken sessionToken = new SessionToken();
		sessionToken.setAccessToken(accessToken);
		sessionToken.setIdToken(tokenResponse.getIdToken());

		return sessionToken;
	}

	public static String getUserAuth(String accessToken) throws OpenIdConnectExpection {

		UserInfoRequest userInfoRequest = new UserInfoRequest(accessToken);
		userInfoRequest.setAuthorizationMethod(AuthorizationMethod.AUTHORIZATION_REQUEST_HEADER_FIELD);

		UserInfoClient userInfoClient = new UserInfoClient(_userInfoEndpoint);
		userInfoClient.setRequest(userInfoRequest);
		userInfoClient.exec();

		if (_log.isDebugEnabled())
			_log.debug("getAccessToken - userInfoRequest=" + userInfoClient.getRequestAsString());

		UserInfoResponse userInfoResponse = userInfoClient.getResponse();

		if (_log.isDebugEnabled())
			_log.debug("getAccessToken - userInfoResponse=" + userInfoClient.getResponseAsString());

		// Handle response
		int status = userInfoResponse.getStatus(); // 200 if succeed

		if (status != HttpStatus.SC_OK) {
			throw new OpenIdConnectExpection(userInfoResponse.getErrorDescription());
		}

		List<String> authList = userInfoResponse.getClaim("email");
		if (authList != null && authList.size() > 0) {
			return authList.get(0);
		}

		throw new OpenIdConnectExpection("email attribute not found for this users in idp");
	}

	public static String endSession(String id_token_hint, String post_logout_redirect_uri) {

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(_endsessionEndpoint);
		stringBuilder.append("?id_token_hint=");
		stringBuilder.append(id_token_hint);
		stringBuilder.append("&post_logout_redirect_uri=");
		stringBuilder.append(post_logout_redirect_uri);

		return stringBuilder.toString();

	}

	private static Log _log = LogFactoryUtil.getLog(OpenIdConnectUtil.class);

	/**
	 * @param sessionToken
	 */

}
