/*
 * Copyright (c) Gluu, Inc. All rights reserved.
 * 
 * http://www.gluu.org/
 */
package org.gluu.openidconnect.filter;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.gluu.openidconnect.exception.OpenIdConnectExpection;
import org.gluu.openidconnect.model.SessionToken;
import org.gluu.openidconnect.util.OpenIdConnectUtil;
import org.gluu.openidconnect.util.OpenIdKeys;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.BaseFilter;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.util.PortalUtil;

/**
 * @author Rajesh (liferay.freelancer@gmail.com)
 * 
 *         This class handle the openidconnect login url generation and handle
 *         the openid callback to set parameter for user autologin in liferay
 */
public class OpenIdConnectFilter extends BaseFilter {

	@Override
	protected void processFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
					throws Exception {

		String request_uri = request.getRequestURI();

		HttpSession session = request.getSession();

		// make sure user is redirect to same page before login
		String redirect_session_param = (String) session.getAttribute(WebKeys.REDIRECT);
		String redirect = ParamUtil.getString(request, "redirect", redirect_session_param);
		if (redirect != null) {
			session.setAttribute(WebKeys.REDIRECT, redirect);
		}

		// redirect if user is already logged in
		if (request_uri.equals("/c/portal/logout")) {
			String oxAuthIdToken = (String) session.getAttribute(OpenIdKeys.OPENID_ID_TOKEN);
			if (_log.isDebugEnabled()) {
				_log.debug("processFilter - oxAuthIdToken=" + oxAuthIdToken);
			}
			if (oxAuthIdToken != null) {
				String endSession = OpenIdConnectUtil.endSession(oxAuthIdToken,
								PortalUtil.getPortalURL(request) + "/c/portal/logout");
				session.removeAttribute(OpenIdKeys.OPENID_ID_TOKEN);
				response.sendRedirect(endSession);
				return;
			}
		}
		// construct openid connect login url and redirect to OP
		else if (request_uri.equals("/openidconnect/login")) {

			if (Validator.isNotNull(session.getAttribute(OpenIdKeys.OPENID_ID_TOKEN))) {
				if (Validator.isNull(redirect)) {
					redirect = PortalUtil.getHomeURL(request);
				}
				response.sendRedirect(redirect);
				return;
			}

			String authorizeLoginURL = OpenIdConnectUtil.getAuthorizeURL();
			if (_log.isDebugEnabled()) {
				_log.debug("processFilter - authorizeLoginURL=" + authorizeLoginURL);
			}
			response.sendRedirect(authorizeLoginURL);
			return;
		}
		// on logout clear the session
		else if (request_uri.equals("/openidconnect/callback")) {
			try {
				//intercept the code and sessionId and construct the autologin attributes;
				processCallback(request, response);
			} catch (OpenIdConnectExpection e) {
				_log.error(e.getMessage());
				response.getWriter().write(e.getMessage());
				response.flushBuffer();
				return;
			}
		}

		processFilter(OpenIdConnectFilter.class, request, response, filterChain);

	}

	/**
	 * @param request
	 * @throws OpenIdConnectExpection
	 */
	private void processCallback(HttpServletRequest request, HttpServletResponse response)
					throws OpenIdConnectExpection {

		HttpSession session = request.getSession();

		// SUCCESS Reponse from OP
		String code = ParamUtil.getString(request, "code", null);
		String session_id = ParamUtil.getString(request, "session_id", null);

		if (_log.isDebugEnabled()) {
			_log.debug("processFilter - code=" + code + ", session_id=" + session_id);
		}

		if (Validator.isNotNull(code)) {

			SessionToken sessionToken = OpenIdConnectUtil.getAccessToken(code);
			String o_login = OpenIdConnectUtil.getUserAuth(sessionToken.getAccessToken());

			session.setAttribute(OpenIdKeys.OPENID_ID_TOKEN, sessionToken.getIdToken());
			session.setAttribute(OpenIdKeys.OPENID_LOGIN, o_login);

		}
	}

	@Override
	public boolean isFilterEnabled() {

		return true;
	}

	@Override
	public void init(FilterConfig filterConfig) {

		super.init(filterConfig);
	}

	@Override
	protected Log getLog() {

		return _log;
	}

	@Override
	public void destroy() {

		super.destroy();
	}

	private static Log _log = LogFactoryUtil.getLog(OpenIdConnectFilter.class);

}
