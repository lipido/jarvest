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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Comparator extends AbstractTransformer {
	private static final long serialVersionUID = 1L;
	
	
	private String prefixIfGreater="_GREATER_";
	private String prefixIfLess="_LESS_";
	private String prefixIfEquals="_EQUALS_";
	private String prefixIfError="_ERROR_";
	private CompareAs compareAs=CompareAs.String;
	private String compareWith="";
	
	
	public Comparator(){
		
	}
	/**
	 * @param compareAs
	 * @param compareWith
	 */
	public Comparator(String compareWith, CompareAs compareAs) {
		super();
		this.compareAs = compareAs;
		this.compareWith = compareWith;
	}

	@Override
	protected String[] _apply(String[] source) {
		String[] toret = new String[source.length];
		int i =0;
		for (String inputString :source){
			String toadd=inputString;
			switch(compareAs){
			case Date:
				DateFormat formatter = new SimpleDateFormat("MM/dd/yy");
		        try {
						Date inputDate = (Date)formatter.parse(inputString);
						Date compareWithDate = (Date)formatter.parse(compareWith);
						
						if (inputDate.compareTo(compareWithDate)>0){
							toadd= prefixIfGreater+toadd;
						}else if (inputDate.compareTo(compareWithDate)<0){
							toadd= prefixIfLess+toadd;
						}else{
							toadd= prefixIfEquals+toadd;
						}
						
					} catch (ParseException e) {
						toadd = prefixIfError+toadd;
						
					}
				
				break;			
			case String:
				if (inputString.compareTo(compareWith)>0){
					toadd= prefixIfGreater+toadd;
				}else if (inputString.compareTo(compareWith)<0){
					toadd= prefixIfLess+toadd;
				}else{
					toadd= prefixIfEquals+toadd;
				}
				break;
			case Number:
				try {
					Double inputNumber = Double.parseDouble(inputString);
					Double compareWithNumber = Double.parseDouble(compareWith);
					
					if (inputNumber.compareTo(compareWithNumber)>0){
						toadd= prefixIfGreater+toadd;
					}else if (inputNumber.compareTo(compareWithNumber)<0){
						toadd= prefixIfLess+toadd;
					}else{
						toadd= prefixIfEquals+toadd;
					}
					
				} catch (NumberFormatException e) {
					toadd = prefixIfError+toadd;
					
				}
			
				break;
		
			}
			toret[i++]=toadd;
		}
		return toret;
	}

	/**
	 * @return the compareAs
	 */
	public CompareAs getCompareAs() {
		return compareAs;
	}

	/**
	 * @param compareAs the compareAs to set
	 */
	public void setCompareAs(CompareAs compareAs) {
		this.compareAs = compareAs;
	}


	/**
	 * @return the compareWith
	 */
	public String getCompareWith() {
		return compareWith;
	}

	/**
	 * @param compareWith the compareWith to set
	 */
	public void setCompareWith(String compareWith) {
		this.compareWith = compareWith;
	}

	/**
	 * @return the prefixIfGreater
	 */
	public String getPrefixIfGreater() {
		return prefixIfGreater;
	}

	/**
	 * @param prefixIfGreater the prefixIfGreater to set
	 */
	public void setPrefixIfGreater(String prefixIfGreater) {
		this.prefixIfGreater = prefixIfGreater;
	}

	/**
	 * @return the prefixIfLess
	 */
	public String getPrefixIfLess() {
		return prefixIfLess;
	}

	/**
	 * @param prefixIfLess the prefixIfLess to set
	 */
	public void setPrefixIfLess(String prefixIfLess) {
		this.prefixIfLess = prefixIfLess;
	}

	/**
	 * @return the prefixIfEquals
	 */
	public String getPrefixIfEquals() {
		return prefixIfEquals;
	}

	/**
	 * @param prefixIfEquals the prefixIfEquals to set
	 */
	public void setPrefixIfEquals(String prefixIfEquals) {
		this.prefixIfEquals = prefixIfEquals;
	}

	/**
	 * @return the prefixIfError
	 */
	public String getPrefixIfError() {
		return prefixIfError;
	}

	/**
	 * @param prefixIfError the prefixIfError to set
	 */
	public void setPrefixIfError(String prefixIfError) {
		this.prefixIfError = prefixIfError;
	}

	
	
	//NEW MODEL

	@Override
	protected void _closeOneInput() {
		String inputString = super.getAndClearCurrentString();
		String toadd=inputString;
			switch(compareAs){
			case Date:
				DateFormat formatter = new SimpleDateFormat("MM/dd/yy");
		        try {
						Date inputDate = (Date)formatter.parse(inputString);
						Date compareWithDate = (Date)formatter.parse(compareWith);
						
						if (inputDate.compareTo(compareWithDate)>0){
							toadd= prefixIfGreater+toadd;
						}else if (inputDate.compareTo(compareWithDate)<0){
							toadd= prefixIfLess+toadd;
						}else{
							toadd= prefixIfEquals+toadd;
						}
						
					} catch (ParseException e) {
						toadd = prefixIfError+toadd;
						
					}
				
				break;			
			case String:
				if (inputString.compareTo(compareWith)>0){
					toadd= prefixIfGreater+toadd;
				}else if (inputString.compareTo(compareWith)<0){
					toadd= prefixIfLess+toadd;
				}else{
					toadd= prefixIfEquals+toadd;
				}
				break;
			case Number:
				try {
					Double inputNumber = Double.parseDouble(inputString);
					Double compareWithNumber = Double.parseDouble(compareWith);
					
					if (inputNumber.compareTo(compareWithNumber)>0){
						toadd= prefixIfGreater+toadd;
					}else if (inputNumber.compareTo(compareWithNumber)<0){
						toadd= prefixIfLess+toadd;
					}else{
						toadd= prefixIfEquals+toadd;
					}
					
				} catch (NumberFormatException e) {
					toadd = prefixIfError+toadd;
					
				}
			
				break;
		
			}
			super.getOutputHandler().pushOutput(toadd);
			super.getOutputHandler().outputFinished();
		
	}

	

}
