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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Select extends AbstractTransformer {

	/**
	 * Serial Version UID 
	 */
	private static final long serialVersionUID = 1L;

	
	private String selector=""; 
	private String ifNoMatch="--none--";
	private String attribute="";
	private boolean innerHTML=false;
	
	public Select(){
		
	}
	
	
	public String getSelector(){
		return selector;
	}
	
	public void setSelector(String xpath){
		this.selector = xpath;
		this.setChanged();
		this.notifyObservers();
		
	}
	
	public String getAttribute() {
		return attribute;
	}
	public void setAttribute(String attribute) {
		this.attribute = attribute;
		this.setChanged();
		this.notifyObservers();
	}
	
	public void setIfNoMatch(String ifNoMatch) {
		this.ifNoMatch = ifNoMatch;
		this.setChanged();
		this.notifyObservers();
	}
	public String getIfNoMatch() {
		return ifNoMatch;
	}
	
	public boolean isInnerHTML() {
		return innerHTML;
	}
	
	public void setInnerHTML(boolean innerHTML) {
		this.innerHTML = innerHTML;
		this.setChanged();
		this.notifyObservers();
	}
	@Override	
	protected String[] _apply(String[] source) {
		throw new UnsupportedOperationException("old model!");
	}
	

	@Override
	protected void _closeOneInput() {
		
		String currentString = super.getAndClearCurrentString();
		
		Document doc = Jsoup.parse(currentString);
		
		Elements elements = doc.select(this.getSelector());
		
		if (elements.size()==0 && !this.ifNoMatch.equals("--none--")){
			super.getOutputHandler().pushOutput(this.getIfNoMatch());			    
		    super.getOutputHandler().outputFinished();
		}
		for (Element element : elements){
			if (this.isInnerHTML()){
				super.getOutputHandler().pushOutput(element.html());
				super.getOutputHandler().outputFinished();
			}else if (!this.getAttribute().isEmpty()){
				super.getOutputHandler().pushOutput(element.attr(this.getAttribute()));			    
			    super.getOutputHandler().outputFinished();
			}else{
				super.getOutputHandler().pushOutput(element.text());			    
			    super.getOutputHandler().outputFinished();
			}
		}
	}
}
