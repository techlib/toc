
package cz.incad.ntk.toc_ntk;

import cz.incad.ntk.toc_ntk.index.Indexer;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONObject;

/**
 *
 * @author alberto
 */
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
    try {

      String actionNameParam = request.getParameter(ACTION_NAME);
      if (actionNameParam != null) {
        
        Actions actionToDo = Actions.valueOf(actionNameParam.toUpperCase());
        actionToDo.doPerform(request, response);
        
          
      } else {
        PrintWriter out = response.getWriter();
        out.print("Action missing");
      }
    } catch (IOException e1) {
      LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e1.toString());
      PrintWriter out = response.getWriter();
      out.print(e1.toString());
    } catch (SecurityException e1) {
      LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    } catch (Exception e1) {
      LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
      response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      PrintWriter out = response.getWriter();
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e1.toString());
      out.print(e1.toString());
    }

  }
  
  enum Actions {
    INDEX_PSH {
      @Override
      void doPerform(HttpServletRequest req, HttpServletResponse response) throws Exception {

        response.setContentType("application/json;charset=UTF-8");

        PrintWriter out = response.getWriter();
        JSONObject json = new JSONObject();
        try {
          Indexer indexer = new Indexer();
            json.put("psh", indexer.indexPSH());

        } catch (Exception ex) {
      LOGGER.log(Level.SEVERE, null, ex);
          json.put("error", ex.toString());
        }
        out.println(json.toString(2));
      }
    },
    INDEX_KONSPEKT {
      @Override
      void doPerform(HttpServletRequest req, HttpServletResponse response) throws Exception {

        response.setContentType("application/json;charset=UTF-8");

        PrintWriter out = response.getWriter();
        JSONObject json = new JSONObject();
        try {
          Indexer indexer = new Indexer();
            json.put("konspekt", indexer.indexKonspekt());

        } catch (Exception ex) {
      LOGGER.log(Level.SEVERE, null, ex);
          json.put("error", ex.toString());
        }
        out.println(json.toString(2));
      }
    },
    INDEX_653 {
      @Override
      void doPerform(HttpServletRequest req, HttpServletResponse response) throws Exception {

        response.setContentType("application/json;charset=UTF-8");

        PrintWriter out = response.getWriter();
        JSONObject json = new JSONObject();
        try {
          Indexer indexer = new Indexer();
            json.put("653", indexer.index653());

        } catch (Exception ex) {
      LOGGER.log(Level.SEVERE, null, ex);
          json.put("error", ex.toString());
        }
        out.println(json.toString(2));
      }
    };

    abstract void doPerform(HttpServletRequest request, HttpServletResponse response) throws Exception;
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
