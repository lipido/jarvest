package es.uvigo.ei.sing.jarvest.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Properties;

import org.junit.Test;

public class MetadataTest {

	@Test
	public void test() throws IOException {
		Properties properties = new Properties();
		properties.load(this.getClass().getResourceAsStream("/es/uvigo/ei/sing/jarvest/dsl/transformer.properties"));
		
		assertTrue(properties.containsKey("wget.description"));
	}

}
