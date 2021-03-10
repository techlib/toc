package cz.incad.ntk.toc_ntk;

import cz.incad.ntk.toc_ntk.index.Indexer;
import cz.incad.ntk.toc_ntk.index.KonspektReader;
import cz.incad.ntk.toc_ntk.index.PSHIndexer;
import cz.incad.ntk.toc_ntk.index.SolrService;
import cz.incad.ntk.toc_ntk.index.SolrTaggerAnalyzer;
import cz.incad.ntk.toc_ntk.index.TOCIndexer;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.json.JSONObject;

/**
 *
 * @author alberto
 */
@WebServlet(value = "/index/*")
public class IndexerServlet extends HttpServlet {

  public static final Logger LOGGER = Logger.getLogger(IndexerServlet.class.getName());
  public static final String ACTION_NAME = "action";

  /**
   * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
   * methods.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    response.setContentType("application/json;charset=UTF-8");
    response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
    response.setHeader("Pragma", "no-cache"); // HTTP 1.0
    response.setDateHeader("Expires", 0); // Proxies.
    PrintWriter out = response.getWriter();
    try {
      String actionNameParam = request.getPathInfo().substring(1);
      if (actionNameParam != null) {
        Actions actionToDo = Actions.valueOf(actionNameParam.toUpperCase());
        JSONObject json = actionToDo.doPerform(request, response);
        out.println(json.toString(2));
      } else {

        out.print("actionNameParam -> " + actionNameParam);
      }
    } catch (IOException e1) {
      LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e1.toString());
      out.print(e1.toString());
    } catch (SecurityException e1) {
      LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    } catch (Exception e1) {
      LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e1.toString());
      out.print(e1.toString());
    }

  }

  enum Actions {
    INDEX_PSH {
      @Override
      JSONObject doPerform(HttpServletRequest req, HttpServletResponse response) throws Exception {
        JSONObject json = new JSONObject();
        try {
          Indexer indexer = new Indexer();
          json.put("psh", indexer.indexPSH());

        } catch (Exception ex) {
          LOGGER.log(Level.SEVERE, null, ex);
          json.put("error", ex.toString());
        }
        return json;
      }
    },
    TOCS {
      @Override
      JSONObject doPerform(HttpServletRequest req, HttpServletResponse response) throws Exception {
        JSONObject json = new JSONObject();
        try {
          TOCIndexer indexer = new TOCIndexer();
          json.put("tocs", indexer.full());

        } catch (Exception ex) {
          LOGGER.log(Level.SEVERE, null, ex);
          json.put("error", ex.toString());
        }
        return json;
      }
    },
    PSH {
      @Override
      JSONObject doPerform(HttpServletRequest req, HttpServletResponse response) throws Exception {
        JSONObject json = new JSONObject();
        try {
          PSHIndexer indexer = new PSHIndexer();
          json.put("psh", indexer.full());

        } catch (Exception ex) {
          LOGGER.log(Level.SEVERE, null, ex);
          json.put("error", ex.toString());
        }
        return json;
      }
    },
    KONSPEKT {
      @Override
      JSONObject doPerform(HttpServletRequest req, HttpServletResponse response) throws Exception {
        JSONObject json = new JSONObject();
        try {
          KonspektReader k = new KonspektReader();
          try (SolrClient solr = new HttpSolrClient.Builder(Options.getInstance().getString("solr.host", "http://localhost:8983/solr/")).build()) {
            k.readFromTxt(Indexer.class.getResourceAsStream("konsp_uni.txt"), solr);
            solr.close();
          } catch (Exception ex) {
            json.put("error", ex);
            Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
          }
        } catch (Exception ex) {
          LOGGER.log(Level.SEVERE, null, ex);
          json.put("error", ex.toString());
        }
        return json;
      }
    },
    INDEX_KONSPEKT {
      @Override
      JSONObject doPerform(HttpServletRequest req, HttpServletResponse response) throws Exception {

        JSONObject json = new JSONObject();
        try {
          Indexer indexer = new Indexer();
          json.put("konspekt", indexer.indexKonspekt());

        } catch (Exception ex) {
          LOGGER.log(Level.SEVERE, null, ex);
          json.put("error", ex.toString());
        }
        return json;
      }
    },
    INDEX_SIMPLE_KEYWORDS {
      @Override
      JSONObject doPerform(HttpServletRequest req, HttpServletResponse response) throws Exception {

        JSONObject json = new JSONObject();
        try {
          Indexer indexer = new Indexer();
          json.put("keywords", indexer.indexKeywords());

        } catch (Exception ex) {
          LOGGER.log(Level.SEVERE, null, ex);
          json.put("error", ex.toString());
        }
        return json;
      }
    },
    FIND {
      @Override
      JSONObject doPerform(HttpServletRequest req, HttpServletResponse response) throws Exception {
        JSONObject json = new JSONObject();
        try {
          String text = req.getParameter("text");
          json.put("results", SolrService.findInDictionariesAsJSON(text));

        } catch (Exception ex) {
          LOGGER.log(Level.SEVERE, null, ex);
          json.put("error", ex.toString());
        }
        return json;
      }
    },
    TAG {
      @Override
      JSONObject doPerform(HttpServletRequest req, HttpServletResponse response) throws Exception {
        JSONObject json = new JSONObject();
        try {
          String sysno = req.getParameter("sysno");
          json = SolrTaggerAnalyzer.getTags(sysno);
        } catch (Exception ex) {
          LOGGER.log(Level.SEVERE, null, ex);
          json.put("error", ex.toString());
        }
        return json;
      }
    },
    TAGRAW {
      @Override
      JSONObject doPerform(HttpServletRequest req, HttpServletResponse response) throws Exception {
        JSONObject json = new JSONObject();
        try {
          String sysno = req.getParameter("sysno");
          json = SolrTaggerAnalyzer.getTagsJSON(sysno, req.getParameter("field"));
        } catch (Exception ex) {
          LOGGER.log(Level.SEVERE, null, ex);
          json.put("error", ex.toString());
        }
        return json;
      }
    };

    abstract JSONObject doPerform(HttpServletRequest request, HttpServletResponse response) throws Exception;
  }

  // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
  /**
   * Handles the HTTP <code>GET</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    processRequest(request, response);
  }

  /**
   * Handles the HTTP <code>POST</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
    processRequest(request, response);
  }

  /**
   * Returns a short description of the servlet.
   *
   * @return a String containing servlet description
   */
  @Override
  public String getServletInfo() {
    return "Short description";
  }// </editor-fold>

}
