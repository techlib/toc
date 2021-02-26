/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.incad.ntk.toc_ntk.index;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.apache.solr.client.solrj.SolrClient;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author alberto
 */
public class PSHIndexer {
  public static final Logger LOGGER = Logger.getLogger(PSHIndexer.class.getName());
  
  Document xmlDocument;
  JSONObject ret = new JSONObject();
  
  public JSONObject full() {
    
    try {
      InputStream is = PSHIndexer.class.getResourceAsStream("psh-skos.rdf");
      DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
      builderFactory.setNamespaceAware(true);
      DocumentBuilder builder = builderFactory.newDocumentBuilder();
      xmlDocument = builder.parse(is);
      getTopNodes();
    } catch (ParserConfigurationException | SAXException | IOException ex) {
      LOGGER.log(Level.SEVERE, null, ex);
    } 
    return ret;
  }
  
  private NamespaceContext setCtx() {
    return new NamespaceContext() {
        @Override
        public Iterator getPrefixes(String arg0) {
            return null;
        }
        @Override
        public String getPrefix(String arg0) {
            return null;
        }
        @Override
        public String getNamespaceURI(String arg0) {
            if ("skos".equals(arg0)) {
                return "http://www.w3.org/2004/02/skos/core#";
            } else if ("rdf".equals(arg0)) {
                return "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
            }
            return null;
        }
    };
  }
  
  private void getTopNodes() {
    try {
      XPath xPath = XPathFactory.newInstance().newXPath();
      
      xPath.setNamespaceContext(setCtx());
      String expression = "//skos:hasTopConcept";
      NodeList nodes = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
      System.out.println(nodes.getLength());
      for (int i =0; i<nodes.getLength(); i++) {
        Node node = nodes.item(i);
        String id = node.getAttributes().getNamedItem("rdf:resource").getNodeValue();
        
        ret.append("top", id.substring(id.lastIndexOf("/")+1));
      }
    } catch (Exception ex) {
      LOGGER.log(Level.SEVERE, null, ex);
      
    } 
  }
  
  public PSHConcept getConcept(String id) {
    try {
      XPath xPath = XPathFactory.newInstance().newXPath();
      String expression = "/Tutorials/Tutorial[@tutId=" + "'" + id + "'" + "]";
      PSHConcept pshC = new PSHConcept();
      Node node = (Node) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODE);
      // pshC.setUri(node.getAttributeValue(0));
      return pshC;
    } catch (Exception ex) {
      LOGGER.log(Level.SEVERE, null, ex);
      return null;
    } 
  }
  
}
