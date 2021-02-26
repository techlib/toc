/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.ntk.toc_ntk.index;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;

/**
 *
 * @author alberto
 */
public class PSHReader {

  public static final Logger LOGGER = Logger.getLogger(PSHReader.class.getName());
  SolrClient client;

  public void readFromXML(InputStream is, SolrClient client) throws XMLStreamException, IOException, SolrServerException {
    this.client = client;
            
    XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    XMLStreamReader reader = null;
    try {
      reader = inputFactory.createXMLStreamReader(is);
      readDocument(reader);
    } finally {
      if (reader != null) {
        reader.close();
      }
    }

  }

  private void readDocument(XMLStreamReader reader) throws XMLStreamException, IOException, SolrServerException {
    while (reader.hasNext()) {
      int eventType = reader.next();
      switch (eventType) {
        case XMLStreamReader.START_ELEMENT:

          String elementName = reader.getLocalName();
//          LOGGER.log(Level.INFO, "START_ELEMENT: {0}", elementName);
          if (elementName.equals("RDF")) {
            readPSHConcepts(reader);
          }
          break;
        case XMLStreamReader.END_ELEMENT:
          break;
      }
    }
    //throw new XMLStreamException("Premature end of file");
  }

  private void readPSHConcepts(XMLStreamReader reader) throws XMLStreamException, IOException, SolrServerException {
int i = 0;
    while (reader.hasNext()) {
      int eventType = reader.next();
      switch (eventType) {
        case XMLStreamReader.START_ELEMENT:
          String elementName = reader.getLocalName();
          //LOGGER.log(Level.INFO, "eventType: {0}, elementName: {1}", new Object[]{eventType, elementName});
          if (elementName.equals("Concept")) {
            PSHConcept pshC = readPSHConcept(reader);
            if (pshC.broader != null) {
              // Get path
              // reader.
              // System.out.println(pshC.id + " -> " + pshC.broader);
            }
            client.addBean(pshC, 10);
          } else {
            skipElement(reader, elementName);
          }
          break;
        case XMLStreamReader.END_ELEMENT:
          return;
      }
    }
    throw new XMLStreamException("Premature end of PSHConcepts");
  }

  private PSHConcept readPSHConcept(XMLStreamReader reader) throws XMLStreamException {
    PSHConcept pshC = new PSHConcept();
    pshC.setUri(reader.getAttributeValue(0));

    while (reader.hasNext()) {
      int eventType = reader.next();
      switch (eventType) {
        case XMLStreamReader.START_ELEMENT:
          String elementName = reader.getLocalName();
          //LOGGER.log(Level.INFO, "eventType: {0}, elementName: {1}", new Object[]{eventType, elementName});
          if (elementName.equals("identifier")) {
            pshC.setId(reader.getElementText());
          } else if (elementName.equals("altLabel")) {
            pshC.addAltLabel(reader.getAttributeValue(0), reader.getElementText());
          } else if (elementName.equals("prefLabel")) {
            pshC.addPrefLabel(reader.getAttributeValue(0), reader.getElementText());
          } else if (elementName.equals("narrower")) {
            pshC.narrower.add(reader.getAttributeValue(0).substring(reader.getAttributeValue(0).lastIndexOf("/")+1));
          } else if (elementName.equals("broader")) {
            pshC.broader = reader.getAttributeValue(0).substring(reader.getAttributeValue(0).lastIndexOf("/")+1);
          }
        case XMLStreamReader.END_ELEMENT:
          elementName = reader.getLocalName();
          if (elementName.equals("Concept")) {
            return pshC;
          }
      }
    }
      
    throw new XMLStreamException("Premature end of PSHConcept");
  }

  private void skipElement(XMLStreamReader reader, String name) throws XMLStreamException {

    while (reader.hasNext()) {
      int eventType = reader.next();
      switch (eventType) {
        case XMLStreamReader.END_ELEMENT:
          String elementName = reader.getLocalName();
          if (elementName.equals(name)) {
            //LOGGER.log(Level.INFO, "eventType: {0}, elementName: {1}", new Object[]{eventType, elementName});
            return;
          }
      }
    }
//    throw new XMLStreamException("Premature end of file");
  }

}
