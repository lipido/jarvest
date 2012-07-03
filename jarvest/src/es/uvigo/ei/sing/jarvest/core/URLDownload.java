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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class URLDownload extends AbstractTransformer{

	private static int fileCounter=0;
	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = 1L;

	private String downloadPath = System.getProperty("user.dir");
	
	private boolean overwrite = false;
	/**
	 * @return the overwrite
	 */
	public boolean getOverwrite() {
		return overwrite;
	}
	/**
	 * @param overwrite the overwrite to set
	 */
	public void setOverwrite(boolean overwrite) {
		System.err.println("Setting overwrite "+overwrite);
		this.overwrite = overwrite;
	}
	@Override
	protected String[] _apply(String[] source) {
		
		String[] toret = new String[source.length];
		
		int counter = 0;
		File destiny = new File(downloadPath);
		if (destiny.exists()){
			for (String string : source){
				try {
						URL url = new URL(string);
						
						//Infer a suitable file name
						String unnamed="unnamedDownload_"+(fileCounter++);
						String fileName = unnamed;
						String urlFile = url.getFile();
						if (urlFile.length()>0){
							fileName = url.getFile();
							if (fileName.lastIndexOf('/')!=-1){
								fileName = fileName.substring(fileName.lastIndexOf('/'));
							}
							if (fileName.length()==0){
								fileName = unnamed;
							}
							
						}
						File destinyFile = new File(destiny.getAbsolutePath()+fileName);
						if (!overwrite && destinyFile.exists()){
							//dont overwrite
							destinyFile = new File(destinyFile.getAbsolutePath()+"_"+System.currentTimeMillis());
						}
						FileOutputStream output = new FileOutputStream(destinyFile);
						InputStream input =  url.openConnection().getInputStream();
						byte[] bytes = new byte[1024];
						int readed = 0;
						while ((readed=input.read(bytes))!=-1){
							output.write(bytes, 0 , readed);							
						}
						output.flush();
						output.close();
				} catch (Exception e){
					e.printStackTrace();
					toret[counter++] = "<exception>";
				}
			}	
		}
		
		return source;
		
	}
	/**
	 * @return the downloadPath
	 */
	public String getDownloadPath() {
		return downloadPath;
	}
	/**
	 * @param downloadPath the downloadPath to set
	 */
	public void setDownloadPath(String downloadPath) {
		this.downloadPath = downloadPath;
	}
	
	// NEW MODEL
	@Override
	protected void _closeOneInput() {
		InputStream input = null;
		try {
			URL url = new URL(super.getAndClearCurrentString());
			
			//Infer a suitable file name
			String unnamed="unnamedDownload_"+(fileCounter++);
			String fileName = unnamed;
			String urlFile = url.getFile();
			if (urlFile.length()>0){
				fileName = url.getFile();
				if (fileName.lastIndexOf('/')!=-1){
					fileName = fileName.substring(fileName.lastIndexOf('/'));
				}
				if (fileName.length()==0){
					fileName = unnamed;
				}
				
			}
			File destiny = new File(downloadPath);
			File destinyFile = new File(destiny.getAbsolutePath()+fileName);
			if (!overwrite && destinyFile.exists()){
				//dont overwrite
				destinyFile = new File(destinyFile.getAbsolutePath()+"_"+System.currentTimeMillis());
			}
			FileOutputStream output = new FileOutputStream(destinyFile);
			input =  url.openConnection().getInputStream();
			byte[] bytes = new byte[1024];
			int readed = 0;
			while ((readed=input.read(bytes))!=-1){
				output.write(bytes, 0 , readed);							
			}
			output.flush();
			output.close();
		} catch (Exception e){
			e.printStackTrace();
			
		}finally{
			if (input!=null)
				try {
					input.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		this.getOutputHandler().outputFinished();
		
	}

	
}
