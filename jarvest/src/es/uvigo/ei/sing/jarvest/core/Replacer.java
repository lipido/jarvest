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

public class Replacer extends AbstractTransformer{

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = 1L;
	private String sourceRE="", dest="";
	public Replacer(String sourceRE, String dest){
		this.sourceRE = sourceRE;
		this.dest = dest;
	}
	public Replacer(){
		
	}
	@Override
	protected String[] _apply(String[] source) {
		String[] toret = new String[source.length];
		
		int counter = 0;
		for (String string : source){
			toret[counter++] = string.replaceAll(sourceRE, super.restoreScapes(dest));
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
			this.getOutputHandler().pushOutput(super.getAndClearCurrentString().replaceAll(sourceRE, super.restoreScapes(dest)));
			this.getOutputHandler().outputFinished();	
	}
	
}
