/**
 *
 */
package com.redhat.brq.integration.activemq.util;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;

/**
 * Class for conversion of jobs between XML and java representation.
 *
 * @author jknetl
 *
 */
public class XmlConverter {

	/**
	 * Converts object to XML using JAXB.
	 *
	 * @param clazz Class with JAXB annotation used for conversion
	 * @param object object to be converted
	 * @return String XML representation of the object
	 * @throws JAXBException
	 */
	public static String toXml(Class<?> clazz, Object object) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

		// output pretty printed
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		ByteArrayOutputStream os = new ByteArrayOutputStream();
		jaxbMarshaller.marshal(object, os);

		return os.toString();

	}

	/**
	 * Converts XML to object using JAXB
	 *
	 * @param clazz Class with JAXB annotation used for conversion
	 * @param xml xml representation of object
	 * @return Java object constructed from XML
	 * @throws JAXBException
	 */
	public static <T> T toObject(Class<T> clazz, String xml) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(clazz);

		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		T o = clazz.cast(jaxbUnmarshaller.unmarshal(new StringReader(xml)));
		return o;
	}

}
