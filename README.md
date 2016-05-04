**Note:** this project has not been maintained. In order to get SSO to your Magento site using the Gluu Server, we recommend using our [oxD Magento Extension](https://gluu.org/docs-oxd/plugin/php/cms/magento/). 

# oxRay
LifeRay OpenID Connect plugin for oxAuth

## Overview
oxAuth Liferay plugin is used to authenticate and auto-log users from Gluu Server into Liferay with same credentails. It is built on top of oxAuth, the OpenID Connect provider by Gluu. 
oxAuth plugin intercepts any attempt to login from anywhere in the Liferay and redirects the request and the user to an oxAuth server where the identification takes place. If the user has authorized the server to share some of his basic information with oxAuth plugin , the user will be redirected back to the Liferay CMS automatically logged in.
 
The goal of this project is to use the LifeRay CMS as the basis for an organizational personal data store service.

Note: This plugin doesn't support auto-user creation from information supplied from oxAuth Plugin. But it can be implemented by extending plugin.

## What is OpenID Connect?
[OpenID Connect 1.0](http://openid.net/connect) is a simple identity layer on top of the OAuth 2.0 protocol. It allows Clients to verify the identity of the End-User based on the authentication performed by an Authorization Server, as well as to obtain basic profile information about the End-User in an interoperable and REST-like manner.

OpenID Connect allows clients of all types, including Web-based, mobile, and JavaScript clients, to request and receive information about authenticated sessions and end-users. The specification suite is extensible, allowing participants to use optional features such as encryption of identity data, discovery of OpenID Providers, and session management, when it makes sense for them.

## Gluu plugin for LifeRay
## How to deploy oxAuth Liferay plugin 

Plugin is provided in 2 variant maven and ant. You can either use maven or liferay-plugin-sdk to build and deploy as standard liferay hot deployable war.

### Creating plugin deployable WAR files using Maven

Prerequisite: Make sure you have maven installed on your system to build this plugin.
 
1. Checkout maven source from [oxRay Repository](https://github.com/Gluufederation/oxRay/6.2.x/maven)

2. Open pom.xml in gluu-openid-connect-hook and update your local liferay tomcat bundle path. This is required for building `war` file and deploying to liferay tomcat bundle.  
![configure_pom_xml](https://raw.githubusercontent.com/GluuFederation/oxRay/master/img/configure_pom_xml.jpg)

3. Goto command prompt - under gluu\6.2.x\maven\gluu-openid-connect-hook directory and Run
     `mvn clean install package liferay:deploy` 

This will take few seconds to download the dependency `jar` files and generate liferay compiled deployable `war` file which will be placed within your <liferay-bundle-folder>/deploy directory and hot deployable process will start.

### Using Liferay Plugin SDK with ant

Prerequisite: Assume you have plugin sdk installed and configured with liferay bundle.

1.  checkout the gluu-openid-connect-hook plugin src from repository and place in your local plugin-sdk under `\liferay-plugins-sdk-6.2.0-ce-ga1\hooks` directory. 

2. Goto command prompt inside `\liferay-plugins-sdk-6.2.0-ce-ga1\hooks\gluu-openid-connect-hook` and run following command 

    `ant clean deploy`


### Using binary from Repository

You can download already compiled binary in standard liferay deployable war from following location. 

[oxRay Liferay Deployable War File](https://github.com/Gluufederation/oxRay/6.2.x/binary/gluu-openid-connect-hook-6.2.0.1.war)

Copy and paste this war in your liferay bundle `\liferay-portal-6.2.0-ce-ga1\deploy` directory.

Once plugin is deployed as war using either maven or ant, you will see following success message in your liferay tomcat server.

![deploy_success](https://raw.githubusercontent.com/GluuFederation/oxRay/master/img/deploy_success.jpg)

### Client Registration

Liferay application need to be register with Authorization server before initiating authentication request/response with oAuth IDP server.
 
Following steps are necessary, to obtain client id and client secret that will be used with liferay portal-ext.properties.

1. Go to `https://seed.gluu.org/oxauth-rp/home.seam`
2. You will see Dynamic Client Registration Section
3. Enter the Registration Endpoint Url eg: `https://idp.example.org/oxauth/seam/resource/restv1/oxauth/authorize`
	* You can get this url from your idp auto-discovery url
`https://<Your IDP Server Domain>/.well-known/openid-configuration`
	* You can search for registration_endpoint and copy paste that url here.
4. Enter the Redirect URI's as http://localhost:8080/openidconnect/callback
	* Replace your domain name with localhost:8080
	* This would be your liferay handler for autologging user to liferay, when redirect comes back from oAuth server. 
5. Select the Response Types: CODE
6. Select the Application Type: WEB
7. For development purpose use : NATIVE (if your testing on local machine with localhost:8080 domain)
8. Enter Client Name: Liferay App
	* You can choose any name here. 
9. All other options can be left as DEFAULT
Please see the attached screenshot..  
![client_registration](https://raw.githubusercontent.com/GluuFederation/oxRay/master/img/dynamic_client_registration_screen1.jpg)

10. Click `Submit` and the following `Registration Request` and `Registration Response` will appear.
![json-request-response](https://raw.githubusercontent.com/GluuFederation/oxRay/master/img/json-request-response.jpg)

11. Save the Registration Response to your local system. Parameters `client_id` and `client_secret` is used in Liferay when configuring `portal-ext.properties`. 
#### Modifying portal.properties

It is necessary to modify `portal-ext.properties` file to reflect oxAuth server client credentials and server's URL. It can accomplished by navigating into `liferay-portal-6.2.0-ce-ga1\` folder , where `portal-ext.properties` resides.

Note: To activate or deactivate oxAuth plugin put `true` to activate and `false` to deactivate.
    
`gluu.openidconnect.auth.enabled=true`
     
* oxAuth client ID and Client Secret.
```
gluu.openidconnect.client.id=@!1111!0008!51CE.1E59
gluu.openidconnect.client.secret=65777eb7-87a8-4d60-9dbc-d31d43971f2b
```
* oAuth Server Domain  
`gluu.openidconnect.idp.domain=https://idp.gluu.org`

* oAuth Server Auto discovery url
`gluu.openidconnect.url.discovery=https://idp.gluu.org/.well-known/openid-configuration`

* Your oAuth Server Logout URL (Typically this will be used to logout user from oAuth when user logout in liferay)
`gluu.openidconnect.idp.logout=https://idp.gluu.org/identity/logout`
     
* Liferay server callback url that will be handling response by oAuth Server after authentication:
	* Replace the localhost:8080 with your liferay domain name.
`gluu.openidconnect.client.redirect.url=http://localhost:8080/openidconnect/callback`
This page would be invoked when the user does not exist in liferay database but it getting authenticated from oAuth Server.

* Typical create a liferay page with /no-such-user-found or Redirect to liferay registration page url
`gluu.openidconnect.no.such.user.redirect.url=http://localhost:8080/no-such-user-found `

Restart Liferay Server after editing `portal-ext.properties` 

### Login using Liferay Front End

* Server Bootup
	* Once liferay server is restarted. Open browser and hit 
`http://localhost:8080`
 
* Login URL
	* Once liferay page successfully loaded paste following url in the browser and hit `enter`.
`http://localhost:8080/openidconnect/login`
 
Note: You can edit the theme code and link the login url as http://localhost:8080/openidconnect/login, so user will always redirect to oAuth Server for authentication.
 
* oAuth Authentication
	* Above liferay login url will redirect users to oAuth IDP server for user authentication, internally passing oAuth client id as following screen 

![oauth-login](https://raw.githubusercontent.com/GluuFederation/oxRay/master/img/oauth_login.jpg)

* Request for Permission
	* This screen is configurable depending upon your oAuth Server Implementation.

![oauth_info_confirm](https://raw.githubusercontent.com/GluuFederation/oxRay/master/img/oauth_info_confirm.jpg)

* oAuth Callback (User auto-login to liferay)
	* After successful authentication with oAuth server, IDP will send callback to liferay with code as parameter
 
`http://localhost:8080/openidconnect/callback?code=xxx`
 
This will intercepted by our oxAuth Liferay plugin and upon validation of token with Gluu IDP internally, it will auto login user to Liferay and users will be redirected to respective home page.

![liferay_success_login](https://raw.githubusercontent.com/GluuFederation/oxRay/master/img/liferay_success_login.jpg)
