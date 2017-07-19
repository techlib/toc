package cz.incad.ntk.toc_ntk;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

/**
 *
 * @author alberto
 */
public class MorphoDiTaServlet extends HttpServlet {

  public static final Logger LOGGER = Logger.getLogger(MorphoDiTaServlet.class.getName());
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

        Actions actionToDo = Actions.valueOf("LOAD");
        actionToDo.doPerform(request, response);
//        PrintWriter out = response.getWriter();
//        out.print("Action missing");
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

    PROCESS_PHRASE {
      @Override
      void doPerform(HttpServletRequest request, HttpServletResponse response) throws Exception {

        response.setContentType("text/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        JSONObject ret = new JSONObject();

        String data = request.getParameter("data");

        ret.put("phrase", data);

//        TocAnalizer t = new TocAnalizer();
//        for (MorphoToken token : t.analyzeLine(data)) {
//          ret.append("tokens (part of speech) (Case)",
//                  token.getToken() + " (" + token.getTag().getPosHuman() + ")"
//                  + " (" + token.getTag().getCase() + ")");
//        }

        out.print(ret.toString(2));
      }
    },
    PROCESS_FILE {
      @Override
      void doPerform(HttpServletRequest request, HttpServletResponse response) throws Exception {

        response.setContentType("text/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        JSONObject ret = new JSONObject();

        String filename = request.getParameter("filename");

        ret.put("filename", filename);

        TocAnalizer t = new TocAnalizer();
        List<TocLine> lines = t.getLines(new File(filename));
        for (TocLine line : lines) {

          JSONObject l = new JSONObject();
          l.put("phrase", line.text);
          l.put("deep", line.deep);
          if (line.text.trim().length() > 0) {
            for (MorphoToken token : line.mtokens) {
              l.append("tokens (part of speech) (Case) (Lemma)",
                      token.getToken() + " (" + token.getTag().getPosHuman() + ")"
                      + " (" + token.getTag().getCaseHuman() + " - " + token.getTag().getCase() + ") (" + token.getLemma() + ")");
            }
            for (Candidate c : t.findCandidates(line.mtokens)) {
              String str = c.text;
              if (c.isMatched) {
                str += " ('" + c.matched_text + "' in dictionary: " + c.dictionary + ")";
                if (c.text.split(" ").length > 1) {
                  str += "!!!";
                }
              }
              l.append("candidates", str);
            }
          }
          ret.append("lines", l);

        }

        out.print(ret.toString(2));
      }
    },
    ANALYZE_FILE {
      @Override
      void doPerform(HttpServletRequest request, HttpServletResponse response) throws Exception {

        response.setContentType("text/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        JSONObject ret = new JSONObject();

        String filename = request.getParameter("filename");

        ret.put("filename", filename);

        TocAnalizer t = new TocAnalizer();
        Map<String, Candidate> cs = new HashMap<>();
        t.analyze(new File(filename), cs);

        List<Candidate> sorted = new ArrayList<>();
        for (String key : cs.keySet()) {
          Candidate c = cs.get(key);
          sorted.add(c);
        }
        Collections.sort(sorted, new Comparator<Candidate>() {
          @Override
          public int compare(Candidate lhs, Candidate rhs) {
            // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
            return rhs.found - lhs.found;
          }
        });
        //sorted.sort(c.found);
        for (Candidate c : sorted) {
          String str = c.text;
          if (c.isMatched) {
            str += " ('" + c.matched_text + "' in dictionary: " + c.dictionary + ")";
            if (c.text.split(" ").length > 1) {
              str += "!!!";
            }
          }
          ret.append("candidates", c.found + ".- " + str);
        }

        out.print(ret.toString(2));
      }
    },
    ANALYZE_FOLDER {
      @Override
      void doPerform(HttpServletRequest request, HttpServletResponse response) throws Exception {

        response.setContentType("text/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        JSONObject ret = new JSONObject();

        String foldername = request.getParameter("foldername");

        ret.put("foldername", foldername);

        TocAnalizer t = new TocAnalizer();
        Map<String, Candidate> cs = t.analyzeFolder(foldername);

        List<Candidate> sorted = new ArrayList<>();
        for (String key : cs.keySet()) {
          Candidate c = cs.get(key);
          sorted.add(c);
        }
        Collections.sort(sorted, new Comparator<Candidate>() {
          @Override
          public int compare(Candidate lhs, Candidate rhs) {
            // -1 - less than, 1 - greater than, 0 - equal, all inversed for descending
            return rhs.found - lhs.found;
          }
        });
        //sorted.sort(c.found);
        for (Candidate c : sorted) {
          String str = c.text;
          if (c.isMatched) {
            str += " ('" + c.matched_text + "' in dictionary: " + c.dictionary + ")";
            if (c.text.split(" ").length > 1) {
              str += "!!!";
            }
            ret.append("candidates in dictionary", c.found + ".- " + str);
          }
          ret.append("candidates", c.found + ".- " + str);

        }

        out.print(ret.toString(2));
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
