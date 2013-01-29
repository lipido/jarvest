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

/**
 *
 * @author lipido
 */
public enum TransformerType {
	APPENDER("Appender", Appender.class),
    DECORATOR("Decorator", Decorator.class), 
    REPLACER("Replacer", Replacer.class), 
    URL_RETRIEVER("URL Retriever", URLRetriever.class), 
    URL_DOWNLOAD("URL Download", URLDownload.class),
    HTTPPOST("HTTP Post", HTTPPost.class),
    PATTERN_MATCHER("Pattern Matcher", PatternMatcher.class), 
    HTML_MATCHER("HTMLMatcher", HTMLMatcher.class),
    MERGER("Merger", Merger.class), 
    SIMPLE_TRANSFORMER("Simple Transformer", SimpleTransformer.class), 
    COMPARATOR("Comparator", Comparator.class);
    
    private final String name;
    private final Class<?> transformer;
    TransformerType(String name, Class<?> transformer) {
        this.name = name;
        this.transformer = transformer;
    }
    
    public Class<?> getTransformer() {
        return this.transformer;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
