package org.gluu.oxauthplugin;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.model.User;
import com.liferay.portal.security.auth.AutoLogin;
import com.liferay.portal.security.auth.AutoLoginException;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * AutoLogin class
 *
 * @author Reda Zerrad Date: 03.21.2012
 * @author Arcko Duan
 *
 */

public class OxAuthAutoLogin implements AutoLogin {

	public OxAuthAutoLogin() {

	}

	private static Log log = LogFactoryUtil.getLog(OxAuthAutoLogin.class);

	//@Override
	public String[] login(HttpServletRequest request, HttpServletResponse response)
			throws AutoLoginException {

		String credentials[];
		HttpSession session = request.getSession();
		String userName = (String)session.getAttribute("oxauth.sso.username");

		if (userName == null)
			return null;

		log.info((new StringBuilder()).append("OxAuthAutoLogin: doing an autologin for user = ").append(userName).toString());
		try
		{
			long companyId = PortalUtil.getCompany(request).getCompanyId();
			User user = UserLocalServiceUtil.getUserByScreenName(companyId, userName);
			long userId = user.getUserId();
			String password = user.getPassword();
			credentials = new String[3];
			credentials[0] = Long.toString(userId);
			credentials[1] = password;
			credentials[2] = Boolean.TRUE.toString();
		}

		catch(Exception e)
		{
			throw new AutoLoginException(e);
		}

		return credentials;
	}

	}