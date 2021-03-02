package cz.incad.ntk.toc_ntk;

import cz.incad.ntk.toc_ntk.index.SolrService;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author alberto
 */

@WebServlet(value = "/candidates")
public class CandidatesServlet extends HttpServlet {

    public static final Logger LOGGER = Logger.getLogger(CandidatesServlet.class.getName());
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

    public static String encodeEntities(String text) {
        return text.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;");
    }

    enum Actions {

        EXPORT {
            @Override
            void doPerform(HttpServletRequest request, HttpServletResponse response) throws Exception {

                response.setContentType("application/json;charset=UTF-8");
                PrintWriter out = response.getWriter();

                String csStr = request.getParameter("candidates");

                out.print(new JSONArray(csStr).toString(2));

            }
        },
        ADD_TO_BLACKLIST {
            @Override
            void doPerform(HttpServletRequest request, HttpServletResponse response) throws Exception {

                response.setContentType("application/json;charset=UTF-8");
                PrintWriter out = response.getWriter();
                String text = request.getParameter("key");
                JSONObject ret = new JSONObject();
                try {
                    SolrService.addToBlackList(text);
                    out.print(ret.put("code", 0).toString());
                } catch (Exception ex) {
                    out.print(ret.put("code", 1).put("error", ex).toString());
                }

            }
        },
        REMOVE_FROM_BLACKLIST {
            @Override
            void doPerform(HttpServletRequest request, HttpServletResponse response) throws Exception {

                response.setContentType("application/json;charset=UTF-8");
                PrintWriter out = response.getWriter();
                String text = request.getParameter("key");
                JSONObject ret = new JSONObject();
                try {
                    SolrService.removeFromBlackList(text);
                    out.print(ret.put("code", 0).toString());
                } catch (Exception ex) {
                    out.print(ret.put("code", 1).put("error", ex).toString());
                }
            }
        },
        ADD_TO_NERIZENE {
            @Override
            void doPerform(HttpServletRequest request, HttpServletResponse response) throws Exception {
              

                response.setContentType("application/json;charset=UTF-8");
                PrintWriter out = response.getWriter();
                String[] text = request.getParameterValues("key");
                JSONObject ret = new JSONObject();
                try {
                    SolrService.addToNerizene(text);
                    out.print(ret.put("code", 0).toString());
                } catch (Exception ex) {
                    out.print(ret.put("code", 1).put("error", ex).toString());
                }

            
            }
        },
        SAVE_TOC {
            @Override
            void doPerform(HttpServletRequest request, HttpServletResponse response) throws Exception {

                response.setContentType("application/json;charset=UTF-8");
                PrintWriter out = response.getWriter();
                String sysno = request.getParameter("sysno");
                //String json = request.getParameter("toc");
                String json = request.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                LOGGER.log(Level.FINE, "sysno: {0}, json: {1}", new Object[]{sysno, json});
                JSONObject ret = new JSONObject();
                try {
//                    SolrService.saveToc(sysno, json);
                    FileService.save(sysno, json);
                    Options.resetInstance();
                    out.print(ret.put("code", 0).toString());
                } catch (Exception ex) {
                    out.print(ret.put("code", 1).put("error", ex).toString());
                }

            
            }
        },
        ADD_TO_DICTIONARY {
            @Override
            void doPerform(HttpServletRequest request, HttpServletResponse response) throws Exception {

                response.setContentType("application/json;charset=UTF-8");
                PrintWriter out = response.getWriter();
                String text = request.getParameter("key");
                JSONObject ret = new JSONObject();
                try {
                    SolrService.addToBlackList(text);
                    out.print(ret.put("code", 0).toString());
                } catch (Exception ex) {
                    out.print(ret.put("code", 1).put("error", ex).toString());
                }

            }
        },
        BALICKY {
            @Override
            void doPerform(HttpServletRequest request, HttpServletResponse response) throws Exception {
                response.setContentType("application/json;charset=UTF-8");
                PrintWriter out = response.getWriter();
                if (request.getParameter("update") != null) {
                    Options.resetInstance();
                }
                
                JSONObject ret = Options.getInstance().getFolders();
                out.print(ret.toString(2));
            }
        },
        GET_TOC {
            @Override
            void doPerform(HttpServletRequest request, HttpServletResponse response) throws Exception {
                response.setContentType("text/plain;charset=UTF-8");
                PrintWriter out = response.getWriter();
                String text = FileService.getToc(request.getParameter("sysno"));
                out.print(text);
            }
        },
        GET_RAW_TOC {
            @Override
            void doPerform(HttpServletRequest request, HttpServletResponse response) throws Exception {
                response.setContentType("text/plain;charset=UTF-8");
                PrintWriter out = response.getWriter();
                String text = FileService.getRawToc(request.getParameter("sysno"));
                out.print(text);
            }
        },
        XSERVER{
            @Override
            void doPerform(HttpServletRequest request, HttpServletResponse response) throws Exception {

                response.setContentType("application/json;charset=UTF-8");
                PrintWriter out = response.getWriter();
                JSONObject ret = new JSONObject();
                try {
                    
                    String sysno = request.getParameter("sysno");

                        ret.put("info", XServer.find(sysno));
                } catch (Exception ex) {
                    ret.put("error", ex.toString());
                }
                out.print(ret.toString(2));
            }
        },
        XSERVERFIELD{
            @Override
            void doPerform(HttpServletRequest request, HttpServletResponse response) throws Exception {

                response.setContentType("application/json;charset=UTF-8");
                PrintWriter out = response.getWriter();
                JSONObject ret = new JSONObject();
                try {
                  ret.put("info", XServer.find(request.getParameter("field"), request.getParameter("val")));
                } catch (Exception ex) {
                    ret.put("error", ex.toString());
                }
                out.print(ret.toString(2));
            }
        },
        FIND {
            @Override
            void doPerform(HttpServletRequest request, HttpServletResponse response) throws Exception {

                response.setContentType("application/json;charset=UTF-8");
                PrintWriter out = response.getWriter();
                JSONObject ret = new JSONObject();
                try {
                    String foldername = request.getParameter("foldername");
                    boolean solrTagger = Boolean.parseBoolean(request.getParameter("solrTagger"));

                    String sysno = request.getParameter("sysno");
//                    String toc = SolrService.getToc(sysno);
                    String toc = FileService.getToc(sysno);
                    if(toc != null){
                      out.print(toc);
                      return;
                    }
                    JSONObject jfolders = Options.getInstance().getFolders();
                    if (jfolders.has(sysno)) {
                        foldername = Options.getInstance().getString("balicky_dir", "~/.ntk/balicky/") + jfolders.getJSONObject(sysno).optString("name");
                    } else {
                        //Mozna pridan pozdeji
                        Options.resetInstance();
                        jfolders = Options.getInstance().getFolders();
                        if (jfolders.has(sysno)) {
                            foldername = Options.getInstance().getString("balicky_dir", "~/.ntk/balicky/") + jfolders.getJSONObject(sysno).optString("name");
                        } else {
                            ret.put("error", "balik neexistuje");
                        }
                    }

                    if (foldername == null || !new File(foldername).exists()) {
                        ret.put("error", "balik neexistuje");
                    } else {

                        ret.put("info", XServer.find(sysno));
                        ret.put("foldername", foldername);

                        TocAnalizer t = new TocAnalizer();
                        Map<String, Candidate> cs = t.analyzeFolder(sysno, foldername, ret.getJSONObject("info"), solrTagger);

                        List<Candidate> sorted = new ArrayList<>();
                        for (String key : cs.keySet()) {
                            Candidate c = cs.get(key);
                            sorted.add(c);
                        }

                        final ScoreConfig sc = new ScoreConfig();
                        String scStr = request.getParameter("scoreconfig");

                        if (scStr != null) {
                            sc.fromJSON(new JSONObject(scStr));
                        }
                        for (Candidate c : sorted) {
                            c.score(sc);
                            ret.append("candidates", c.toJSON());

                        }
                    }
                } catch (Exception ex) {
                  LOGGER.log(Level.SEVERE, null, ex);
                    ret.put("error", ex.toString());
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
