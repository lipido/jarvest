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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class XMLInputOutput {

	private static final String packagePrefix = "es.uvigo.ei.sing.jarvest.core";

	private static final String XML_ROBOT_VERSION = "1.0";

	private XMLInputOutput() {
	};

	private Document doc;

	// ====================
	// INPUT
	// ====================
	private static String readStream(InputStream stream) {
		BufferedReader reader = new BufferedReader(
				new InputStreamReader(stream));
		String allFile = "";
		String line = "";
		try {
			while ((line = reader.readLine()) != null) {
				allFile += line;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return allFile;
	}

	public static Transformer loadTransformer(File file)
			throws FileNotFoundException {
		return loadTransformer(new FileInputStream(file));
	}

	public static Transformer loadTransformer(InputStream inputStream) {
		return loadTransformer(asDoc(inputStream));
	}

	public static Document asDoc(InputStream inputStream) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringElementContentWhitespace(true);
		factory.setIgnoringComments(true);
		String xml_description = readStream(inputStream);
		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			return builder.parse(new ByteArrayInputStream(xml_description
					.getBytes()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static Transformer loadTransformer(Document doc) {
		
		XMLInputOutput inputOutput = new XMLInputOutput();
		inputOutput.doc = doc;
		Transformer toret =  inputOutput.parseRobot();
		XMLInputOutput.writeTransformer(toret, System.err);
		return toret;
	}

	private Transformer parseRobot() {
		return parseTransformer(this.doc.getElementsByTagName("transformer")
				.item(0));

	}

	private Transformer parseTransformer(Node node) {
		Transformer toret = null;
		// System.out.println(node);
		String className = node.getAttributes().getNamedItem("class")
				.getNodeValue();

		Node _node = null;

		// Find transformer's class
		Class<?> clazz = null;
		try {
			clazz = Class.forName(className);
		} catch (ClassNotFoundException e) {
			if (className.indexOf('.') == -1) {
				// the class may be a Transformer of our package, but without
				// its prefix. We will try adding the prefix...
				try {
					clazz = Class.forName(packagePrefix + "." + className);
				} catch (ClassNotFoundException e1) {
					throw new RuntimeException("Transformer class not found: "
							+ packagePrefix + "." + className, e1);

				}
			} else {
				throw new RuntimeException("Transformer class not found: "
						+ className, e);
			}
		}

		// create and configure transformer
		try {

			toret = (Transformer) clazz.newInstance();

			// branchtype
			_node = node.getAttributes().getNamedItem("branchtype");
			if (_node != null) {
				String branchType = null;
				try {
					branchType = _node.getNodeValue();
					BranchType bType = BranchType.valueOf(branchType);
					toret.setBranchType(bType);
				} catch (IllegalArgumentException e) {
					throw new RuntimeException("branchtype unrecognized: "
							+ branchType);
				}
			} else {
				// no branchtype specified, taking default
				toret.setBranchType(Transformer.DEFAULT_BRANCH_TYPE);

			}

			// mergemode
			_node = node.getAttributes().getNamedItem("branchmergemode");
			if (_node != null) {
				String mergeMode = _node.getNodeValue();
				try {
					MergeMode mMode = MergeMode.valueOf(mergeMode);
					toret.setBranchMergeMode(mMode);
				} catch (IllegalArgumentException e) {
					throw new RuntimeException("branchmergemode unrecognized: "
							+ mergeMode);
				}
			} else {
				toret.setBranchMergeMode(Transformer.DEFAULT_BRANCH_MERGE_MODE);

			}

			// loop
			_node = node.getAttributes().getNamedItem("loop");
			if (_node != null) {
				String loop = _node.getNodeValue();
				if (loop.equalsIgnoreCase("yes")
						|| loop.equalsIgnoreCase("true")) {
					toret.setLoop(true);
				}
			} else {
				toret.setLoop(false);

			}

			// childs
			for (int i = 0; i < node.getChildNodes().getLength(); i++) {
				_node = node.getChildNodes().item(i);

				if (_node.getNodeName().equals("description")) {
					// System.out.println(_node.getTextContent());
					toret.setDescription(_node.getTextContent());
				} else if (_node.getNodeName().equals("transformer")) {
					toret.add(parseTransformer(_node));

				} else if (_node.getNodeName().equals("param")) {
					Node keyNode = _node.getAttributes().getNamedItem("key");
					if (_node != null) {
						String key = keyNode.getNodeValue();
						if (key.equals("stopped"))
							continue;

						BeanInfo info = java.beans.Introspector
								.getBeanInfo(toret.getClass());
						for (PropertyDescriptor desc : info
								.getPropertyDescriptors()) {

							// System.out.println(desc.getName());
							if (desc.getName().equals(key)) {
								if (desc.getWriteMethod() != null) {

									// only primitive types, wrappers and
									// strings allowed
									if (desc.getPropertyType() == Boolean.TYPE
											|| desc.getPropertyType().equals(
													Boolean.class)) {
										if (_node.getTextContent().trim()
												.equalsIgnoreCase("yes")
												|| _node.getTextContent()
														.trim()
														.equalsIgnoreCase(
																"true")) {
											desc.getWriteMethod().invoke(toret,
													new Object[] { true });
										} else {

											desc.getWriteMethod().invoke(toret,
													new Object[] { false });
										}
									} else if (desc.getPropertyType().equals(
											String.class)) {
										desc.getWriteMethod().invoke(
												toret,
												new Object[] { _node
														.getTextContent() });
									} else if (desc.getPropertyType() == Integer.TYPE
											|| desc.getPropertyType().equals(
													Integer.class)) {
										desc
												.getWriteMethod()
												.invoke(
														toret,
														new Object[] { Integer
																.parseInt(_node
																		.getTextContent()
																		.trim()) });

									} else if (desc.getPropertyType() == Long.TYPE
											|| desc.getPropertyType().equals(
													Long.class)) {
										desc
												.getWriteMethod()
												.invoke(
														toret,
														new Object[] { Long
																.parseLong(_node
																		.getTextContent()
																		.trim()) });

									} else if (desc.getPropertyType() == Float.TYPE
											|| desc.getPropertyType().equals(
													Float.class)) {
										desc
												.getWriteMethod()
												.invoke(
														toret,
														new Object[] { Float
																.parseFloat(_node
																		.getTextContent()
																		.trim()) });

									} else if (desc.getPropertyType() == Double.TYPE
											|| desc.getPropertyType().equals(
													Double.class)) {
										desc
												.getWriteMethod()
												.invoke(
														toret,
														new Object[] { Double
																.parseDouble(_node
																		.getTextContent()
																		.trim()) });

									} else if (desc.getPropertyType() == Integer.TYPE
											|| desc.getPropertyType().equals(
													Integer.class)) {
										desc
												.getWriteMethod()
												.invoke(
														toret,
														new Object[] { Integer
																.parseInt(_node
																		.getTextContent()
																		.trim()) });
									}
								}
							}
						}
					}
				}
			}
			// System.out.println("returning "+toret);
			return toret;
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (IntrospectionException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

	// ====================
	// INPUT
	// ====================

	public static void writeTransformer(Transformer t, OutputStream stream) {
		PrintStream out = new PrintStream(stream);

		new XMLInputOutput().writeRobot(t, out);

	}

	public static void writeTransformer(Transformer t, File f)
			throws FileNotFoundException {
		writeTransformer(t, new FileOutputStream(f));
	}

	private void writeRobot(Transformer t, PrintStream out) {
		out.println("<robot version=\"" + XML_ROBOT_VERSION + "\">");
		_writeTransformer(t, out, 1);
		out.println("</robot>");
	}

	private void _writeTransformer(Transformer t, PrintStream out, int level) {
		writeTransformerHeader(t, out, level);
		writeTransformerParams(t, out, level);
		out.println(doTabs(level) + "</transformer>");
	}

	private void writeTransformerHeader(Transformer t, PrintStream out,
			int level) {
		out.print(doTabs(level) + "<transformer ");

		// class
		String className = t.getClass().getName();
		if (t.getClass().getName().equals(
				packagePrefix + "." + t.getClass().getSimpleName())) {
			className = t.getClass().getSimpleName();
		}
		out.print("class=\"" + className + "\" ");

		// branchtype
		out.print("branchtype=\"" + t.getBranchType() + "\" ");

		// branchmergemode
		out.print("branchmergemode=\"" + t.getBranchMergeMode() + "\" ");

		// loop
		out.print("loop=\"" + t.isLoop() + "\"");
		out.println(">");
	}

	private void writeTransformerParams(Transformer t, PrintStream out,
			int level) {
		try {
			out.println(doTabs(level + 1) + "<description>"
					+ t.getDescription() + "</description>");

			// params
			BeanInfo info;
			info = java.beans.Introspector.getBeanInfo(t.getClass());
			for (PropertyDescriptor desc : info.getPropertyDescriptors()) {
				if (desc.getName().equals("loop"))
					continue;
				if (desc.getName().equals("stopped"))
					continue;
				if (isParamTypeAllowed(desc.getPropertyType())
						&& desc.getReadMethod() != null) {
					Object _obj = desc.getReadMethod().invoke(t,
							new Object[] {});
					String paramValue = "null";
					if (_obj != null) {
						paramValue = _obj.toString();
					}
					out.print(doTabs(level + 1) + "<param key=\""
							+ desc.getName() + "\">");

					// see if this param contains XML illegal characters
					if (paramValue.indexOf('<') != -1
							|| paramValue.indexOf('>') != -1
							|| paramValue.indexOf('&') != -1
							|| paramValue.indexOf('\'') != -1
							|| paramValue.indexOf('\"') != -1) {
						out.print("<![CDATA[");
						out.print(paramValue);
						out.print("]]>");
					} else {
						out.print(paramValue);
					}

					out.println("</param>");
				}

			}
			// childs
			for (Transformer child : t.getChilds()) {
				_writeTransformer(child, out, level + 1);
			}
		} catch (IntrospectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private String doTabs(int cty) {
		String toret = "";
		for (int i = 0; i < cty; i++) {
			toret += "\t";
		}
		return toret;
	}

	private boolean isParamTypeAllowed(Class<?> c) {
		return c == Boolean.TYPE || c.equals(Boolean.class)
				|| c.equals(String.class) || c.equals(Integer.class)
				|| c == Integer.TYPE || c.equals(Float.class)
				|| c == Float.TYPE || c.equals(Double.class)
				|| c == Double.TYPE || c.equals(Long.class) || c == Long.TYPE;

	}

	public static void main(String[] args) {

		try {
			Transformer t = loadTransformer(new FileInputStream(new File(
					"/home/lipido/Desktop/laregionourense.xml")));
			Transformer t_c = (Transformer) t.clone();
			t_c.setOutputHandler(new OutputHandler() {

				public void allFinished() {
					// TODO Auto-generated method stub

				}

				public void outputFinished() {
					System.out.println("");

				}

				public void pushOutput(String string) {
					System.out.print(string);

				}

			});
			// t_c.pushString("");
			// t_c.closeOneInput();
			t_c.closeAllInputs();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
