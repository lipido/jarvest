/*

Copyright 2012 Daniel Gonzalez Peña


This file is part of the jARVEST Project. 

jARVEST Project is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

jARVEST Project is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser Public License for more details.

You should have received a copy of the GNU Lesser Public License
along with jARVEST Project.  If not, see <http://www.gnu.org/licenses/>.
*/
package es.uvigo.ei.sing.jarvest.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

@SuppressWarnings("serial")
public abstract class URLBasedTransformer extends AbstractTransformer {

	private boolean ajax = false;
	private String userAgent = "";
	private String headers = "{}";
	
	public boolean isAjax() {
		return ajax;
	}
	public void setAjax(boolean ajax) {
		this.ajax = ajax;
	}
	public String getHeaders() {
		return headers;
	}
	public void setHeaders(String headers) {
		this.headers = headers;
	}
	public String getUserAgent() {
		return userAgent;
	}
	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}
	
	public Map<String, String> getAdditionalHeaders(){
		Map<String, String> toret = new HashMap<String, String>();
		
		if (this.isAjax()){
			toret.put("X-Requested-With", "XMLHttpRequest");
		}
		if (!this.getUserAgent().isEmpty()){
			toret.put("User-Agent", this.getUserAgent());
		}
		JSONTokener tokener = new JSONTokener(this.getHeaders());
		try {
			JSONObject root = new JSONObject(tokener);
			Iterator<?> it = root.keys();
			while (it.hasNext()){
				String key = it.next().toString();				
				toret.put(key, root.getString(key));
			}
		} catch (JSONException e) {
			throw new IllegalArgumentException("unable to parse headers parameters (must be a json string)", e);
		}
		return toret;
	}
	
}
