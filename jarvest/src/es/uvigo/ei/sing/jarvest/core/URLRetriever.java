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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

public class URLRetriever extends AbstractTransformer{

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = 1L;
	private static final String DEFAULT_CHARSET = Charset.defaultCharset().name();

	@Override
	protected String[] _apply(String[] source) {
		
		String[] toret = new String[source.length];
		
		int counter = 0;
		for (String string : source){
			InputStream input = null;
			try {
			
				String contentString="";
			
				
				contentString = HTTPUtils.getURLBodyAsString(string);
				
				toret[counter++] = contentString;
			} catch (Exception e){
				e.printStackTrace();
				toret[counter++] = "<exception>";
			}
			finally{
				if (input!=null)
					try {
						input.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		}
		
		return toret;
		
	}

	
	// NEW MODEL
	@Override
	protected void _closeOneInput() {
		Reader input = null;
		InputStream is = null;
		try {
			String current = this.getAndClearCurrentString();
			//System.err.println("trying getting: "+current);
			StringBuffer charsetb = new StringBuffer();
			
			//System.out.println("getting url body "+current);
			is = HTTPUtils.getURLBody(current, charsetb );
			String charset = validCharset(charsetb.toString());
			//System.out.println(charset);
			input = new InputStreamReader(is, Charset.forName(charset));
			char[] bytes = new char[1024];
			int readed = 0;
			
			
			
			while ((readed=input.read(bytes))!=-1 && !this.isStopped()){
				
				this.getOutputHandler().pushOutput(new String(bytes,0,readed));
			
			}
			
			/*String res = HTTPUtils.getURLBodyAsString(current);
			this.getOutputHandler().pushOutput(res);*/
		} catch (Exception e){
			e.printStackTrace();
			this.getOutputHandler().pushOutput("<exception> " +e);
			
		}
		finally{
			if (is!=null)
				try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		this.getOutputHandler().outputFinished();
		
	}


	private String validCharset(String string) {
		
		String input = string.toUpperCase();
		if (Charset.availableCharsets().containsKey(input)){ return input;
		}else return DEFAULT_CHARSET;
	}

	
}
