<%
/**
 * Copyright (c) 2000-2012 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
%>



<%@ taglib uri="http://liferay.com/tld/util" prefix="liferay-util" %>

    <script type="text/javascript">
    
   
    
    if (self.document.location.hash.length != 0){
    	
    	 
    var hash = self.document.location.hash.substring(1);
    hash = unescape(hash.replace(/\?/g, " ")); 
    var tkURI = new String();
    var host = new String();
    var protocol = new String();
   
    tkURI = hash;
    host = window.location.host;
    protocol = window.location.protocol;
    window.location = protocol +"//" + host + "/web/guest/home?" + tkURI;
    
    }
   
    </script>