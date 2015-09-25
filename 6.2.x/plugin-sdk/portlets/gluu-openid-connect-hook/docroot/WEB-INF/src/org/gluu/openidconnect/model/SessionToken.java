/**
 * Copyright (c) Gluu, Inc. All rights reserved.
 */

package org.gluu.openidconnect.model;

/**
 * @author Rajesh (liferay.freelancer@gmail.com)
 *
 */
public class SessionToken {

	String accessToken = null;

	String idToken = null;

	/**
	 * @return the accessToken
	 */
	public String getAccessToken() {
		return accessToken;
	}

	/**
	 * @param accessToken
	 *                the accessToken to set
	 */
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	/**
	 * @return the idToken
	 */
	public String getIdToken() {
		return idToken;
	}

	/**
	 * @param idToken
	 *                the idToken to set
	 */
	public void setIdToken(String idToken) {
		this.idToken = idToken;
	}

}
