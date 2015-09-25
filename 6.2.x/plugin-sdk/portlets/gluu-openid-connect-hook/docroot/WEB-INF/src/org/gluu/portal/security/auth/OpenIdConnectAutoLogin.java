/*
 * Copyright (c) Gluu, Inc. All rights reserved.
 * 
 * http://www.gluu.org/
 */
package org.gluu.portal.security.auth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.gluu.openidconnect.util.OpenIdConnectProperties;
import org.gluu.openidconnect.util.OpenIdConnectUtil;
import org.gluu.openidconnect.util.OpenIdKeys;

import com.liferay.portal.NoSuchUserException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.model.User;
import com.liferay.portal.security.auth.AutoLogin;
import com.liferay.portal.security.auth.AutoLoginException;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;

/**
 * @author Rajesh (liferay.freelancer@gmail.com)
 * 
 *         This class inserted in Liferay Auth Pipeline, usually read userlogin
 *         parameter set by openid callback and auto login users in liferay
 */
public class OpenIdConnectAutoLogin implements AutoLogin {

	@Override
	public String[] handleException(HttpServletRequest request, HttpServletResponse response, Exception exception)
					throws AutoLoginException {

		HttpSession session = request.getSession();

		if (exception instanceof NoSuchUserException) {
			session.removeAttribute(OpenIdKeys.OPENID_LOGIN);
			session.removeAttribute(OpenIdKeys.OPENID_ID_TOKEN);
			session.setAttribute(OpenIdKeys.OPENID_NO_SUCH_USER_EXCEPTION, Boolean.TRUE);
		}

		_log.error(exception.getMessage(), exception);

		return null;
	}

	@Override
	public String[] login(HttpServletRequest request, HttpServletResponse response) throws AutoLoginException {
		String[] credentials = null;

		if (_log.isDebugEnabled())
			_log.debug("autologin - start");

		try {
			if (!OpenIdConnectUtil.isAuthEnabled()) {
				return credentials;
			}

			HttpSession session = request.getSession();

			String login = (String) session.getAttribute(OpenIdKeys.OPENID_LOGIN);

			if (Validator.isNull(login)) {
				return credentials;
			}

			session.removeAttribute(OpenIdKeys.OPENID_LOGIN);

			String redirect = ParamUtil.getString(request, "redirect",
							(String) session.getAttribute(WebKeys.REDIRECT));

			try {
				long companyId = PortalUtil.getCompanyId(request);

				User user = UserLocalServiceUtil.getUserByEmailAddress(companyId, login);

				if (_log.isDebugEnabled()) {
					_log.debug("autologin - user=" + user);
				}

				if (Validator.isNull(redirect)) {
					redirect = PortalUtil.getPortalURL(request) + "/c/portal/login";
				}

				if (_log.isDebugEnabled()) {
					_log.debug("autologin - redirect=" + redirect);
				}
				request.setAttribute(AutoLogin.AUTO_LOGIN_REDIRECT_AND_CONTINUE, redirect);

				credentials = new String[3];
				credentials[0] = String.valueOf(user.getUserId());
				credentials[1] = user.getPassword();
				credentials[2] = Boolean.FALSE.toString();

				if (_log.isDebugEnabled())
					_log.debug("autologin - end");

				return credentials;

			} catch (NoSuchUserException nsue) {
				_log.error("autologin error:" + nsue.getMessage());
				request.setAttribute(AutoLogin.AUTO_LOGIN_REDIRECT,
								OpenIdConnectProperties.NO_SUCH_USER_REDIRECT_URL);
				return credentials;
			}
		} catch (Exception e) {
			_log.error(e, e);
		}

		return credentials;
	}

	private static Log _log = LogFactoryUtil.getLog(OpenIdConnectAutoLogin.class);
}
