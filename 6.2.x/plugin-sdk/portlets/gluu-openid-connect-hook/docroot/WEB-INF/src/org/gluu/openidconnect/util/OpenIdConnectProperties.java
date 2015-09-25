/*
 * Copyright (c) Gluu, Inc. All rights reserved.
 * 
 * http://www.gluu.org/
 */
package org.gluu.openidconnect.util;

import com.liferay.portal.kernel.util.PropsUtil;

/**
 * @author Rajesh (liferay.freelancer@gmail.com)
 * 
 */
public class OpenIdConnectProperties {

	public static final String OPEN_ID_DISCOVERY_URL = PropsUtil.get("gluu.openidconnect.url.discovery");;

	public static final String AUTHORIZE_URL = PropsUtil.get("gluu.openidconnect.authorize.url");

	public static final String CLIENT_ID = PropsUtil.get("gluu.openidconnect.client.id");

	public static final String CLIENT_SECRET = PropsUtil.get("gluu.openidconnect.client.secret");

	public static final String REDIRECT_URL = PropsUtil.get("gluu.openidconnect.client.redirect.url");

	public static final String NO_SUCH_USER_REDIRECT_URL = PropsUtil
					.get("gluu.openidconnect.no.such.user.redirect.url");

	public static final String OPEN_ID_AUTH_ENABLED = PropsUtil.get("gluu.openidconnect.auth.enabled");

}
