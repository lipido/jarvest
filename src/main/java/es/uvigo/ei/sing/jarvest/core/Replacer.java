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

import java.util.regex.Pattern;

public class Replacer extends AbstractTransformer{

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = 1L;
	private String sourceRE="", dest="";
	private boolean dotAll = true;
	private Pattern pattern = Pattern.compile("", Pattern.DOTALL);
	public Replacer(String sourceRE, String dest){
		this.sourceRE = sourceRE;
		this.dest = dest;
	}
	public Replacer(){
		
	}
	
	public void setDotAll(boolean dotAll) {
		this.dotAll = dotAll;
		if (this.sourceRE!=null){
		
			if (this.isDotAll()){
				this.pattern = Pattern.compile(sourceRE, Pattern.DOTALL);
			}else{
				this.pattern = Pattern.compile(sourceRE);
			}
		}
		
	}
	public boolean isDotAll() {
		return dotAll;
		
	}
	@Override
	protected String[] _apply(String[] source) {
		String[] toret = new String[source.length];
		
		int counter = 0;
		for (String string : source){
			toret[counter++] = pattern.matcher(string).replaceAll(dest);
		}
		
		return toret;
	}
	
	public String toString(){
		String toret = "Replacer Transformer\n" +
				"Find: "+sourceRE+"\n" +
				"Replace with: "+dest;
		return toret;
	}
	/**
	 * @return the sourceRE
	 */
	public String getSourceRE() {
		return sourceRE;
	}
	/**
	 * @param sourceRE the sourceRE to set
	 */
	public void setSourceRE(String sourceRE) {
		this.sourceRE = sourceRE;		
		if (this.isDotAll()){
			this.pattern = Pattern.compile(sourceRE, Pattern.DOTALL);
		}else{
			this.pattern = Pattern.compile(sourceRE);
		}
	}
	/**
	 * @return the dest
	 */
	public String getDest() {
		return dest;
	}
	/**
	 * @param dest the dest to set
	 */
	public void setDest(String dest) {
		this.dest = dest;
	}
	
	
	// NEW MODEL
	@Override
	protected void _closeOneInput() {		
			this.getOutputHandler().pushOutput(pattern.matcher(super.getAndClearCurrentString()).replaceAll(super.restoreScapes(dest)));
			this.getOutputHandler().outputFinished();	
	}
	
}
