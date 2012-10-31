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

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternMatcher extends AbstractTransformer {

	/**
	 * Serial Version UID 
	 */
	private static final long serialVersionUID = 1L;

	
	private Pattern pattern=Pattern.compile("", Pattern.DOTALL); 
	private boolean dotAll=true;
	private String ifNoMatch="--none--";
	public PatternMatcher(){
	
		
		
	}
	
	
	public String getPattern(){
		return pattern.toString();
	}
	
	public void setPattern(String pattern){
		this.pattern = Pattern.compile(pattern, Pattern.DOTALL);
		this.setChanged();
		this.notifyObservers();
		
	}
	
	public void setDotAll(boolean useDotAll){
		//System.err.println("setted dot all to: "+useDotAll);
		this.dotAll = useDotAll;
		if(this.pattern !=null){
			if (useDotAll) this.pattern = Pattern.compile(pattern.toString(), Pattern.DOTALL );
			else this.pattern = Pattern.compile(pattern.toString());
		}
		this.setChanged();
		this.notifyObservers();
	}
	
	public boolean getDotAll(){
		//System.err.println("getting dotall: "+this.dotAll);
		return this.dotAll;
	}
	
	public void setIfNoMatch(String ifNoMatch) {
		this.ifNoMatch = ifNoMatch;
	}
	public String getIfNoMatch() {
		return ifNoMatch;
	}
	
	
	
	@Override	
	protected String[] _apply(String[] source) {
		Vector<String> toretV = new Vector<String>();
		
		for (String string : source){
			Matcher matcher = pattern.matcher(string.subSequence(0, string.length()));
			
			while(matcher.find()){
		
				for (int i = 0; i< matcher.groupCount(); i++){
					
					toretV.add(matcher.group(i+1));
				}
			}
		}
		
		return toretV.toArray(new String[]{});
	}
	
	public String toString(){
		String toret="";
		toret+="Pattern Matcher " +
				"pattern: "+pattern.toString();
		return toret;
			
	}


	// NEW MODEL
	@Override
	protected void _closeOneInput() {
		
		String currentString = super.getAndClearCurrentString();
		Matcher matcher = pattern.matcher(currentString.subSequence(0, currentString.length()));
		//System.err.println(currentString);
		boolean hasMatches = false;
		while(matcher.find()){
			hasMatches = true;
			for (int i = 0; i< matcher.groupCount(); i++){
					
				super.getOutputHandler().pushOutput(matcher.group(i+1));
				super.getOutputHandler().outputFinished();
			}
		}
		
		if (!hasMatches && !this.getIfNoMatch().equals("--none--")){
			super.getOutputHandler().pushOutput(this.getIfNoMatch());
			super.getOutputHandler().outputFinished();
		}
		
	}

}
