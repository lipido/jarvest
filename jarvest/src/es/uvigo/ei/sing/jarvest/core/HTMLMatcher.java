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

import java.util.LinkedList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.HtmlTagProvider;
import org.htmlcleaner.TagInfo;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class HTMLMatcher extends AbstractTransformer {

	/**
	 * Serial Version UID 
	 */
	private static final long serialVersionUID = 1L;

	
	private String xpath=""; 
	private boolean addTBody=true;
	private String ifNoMatch="--none--";
	
	public HTMLMatcher(){
		
	}
	
	
	public String getXPath(){
		return xpath;
	}
	
	public void setXPath(String xpath){
		this.xpath = xpath;
		this.setChanged();
		this.notifyObservers();
		
	}
	
	public void setAddTBody(boolean addTBody) {
		this.addTBody = addTBody;
		this.setChanged();
		this.notifyObservers();
	}
	
	public boolean isAddTBody() {
		return addTBody;
	}
	
	public void setIfNoMatch(String ifNoMatch) {
		this.ifNoMatch = ifNoMatch;
		this.setChanged();
		this.notifyObservers();
	}
	public String getIfNoMatch() {
		return ifNoMatch;
	}
	
	@Override	
	protected String[] _apply(String[] source) {
		LinkedList<String> outputList = new LinkedList<String>();
		
		for (String input : source){
			String currentString = input;
			
			//clean HTML
			
			final HtmlCleaner cleaner = new HtmlCleaner(currentString);
			try {
				cleaner.clean();
				Document document = cleaner.createDOM();
				
				XPathFactory factory = XPathFactory.newInstance();
				XPath xpath = factory.newXPath();
				XPathExpression expr = xpath.compile(this.xpath);
				Object result = expr.evaluate(document, XPathConstants.NODESET);
				NodeList nodes = (NodeList) result;
				for (int i = 0; i < nodes.getLength(); i++) {		
					String output = nodes.item(i).getNodeValue();
					outputList.add(output);			    
				    
				    
				}
			} catch (Exception e) {
				e.printStackTrace();
			}		
		}
		String[] toret = new String[outputList.size()];
		outputList.toArray(toret);
		return toret;
	}
	
	public String toString(){
		String toret="";
		toret+="HTML Matcher " +
				"xpath: "+xpath;
		return toret;
			
	}


	// NEW MODEL
	@Override
	protected void _closeOneInput() {
		
		String currentString = super.getAndClearCurrentString();
		
		@SuppressWarnings("serial")		
		class MyTagProvider extends HtmlTagProvider{
			MyTagProvider(){
				super();
				
				//not insert tbody before all tr (removing the +tbody rule)
				if (!isAddTBody()){
					super.addTag("tr", TagInfo.CONTENT_ALL, TagInfo.BODY, "!table,^thead,^tfoot,#td,#th,tr,td,th,caption,colgroup");
				}
				
			}
		};
		final HtmlCleaner cleaner = new HtmlCleaner(currentString,new MyTagProvider());
		
		try {
			cleaner.clean();
			Document document = cleaner.createDOM();
			//System.err.println(cleaner.getBrowserCompactXmlAsString());
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			XPathExpression expr = xpath.compile(this.xpath);
			Object result = expr.evaluate(document, XPathConstants.NODESET);
			NodeList nodes = (NodeList) result;
			
			if (nodes.getLength()==0 && !this.getIfNoMatch().equals("--none--")){
				super.getOutputHandler().pushOutput(this.getIfNoMatch());
				super.getOutputHandler().outputFinished();
			}
			for (int i = 0; i < nodes.getLength(); i++) {
				
				String output = nodes.item(i).getTextContent();
				
				super.getOutputHandler().pushOutput(output);			    
			    super.getOutputHandler().outputFinished();
			    
			}
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
}
