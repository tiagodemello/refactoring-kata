package src;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.*;

public class ListarNotasFiscaisAction extends Action {
    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response) throws Exception {
        // Aqui você pode acessar o NotaFiscalManager e setar atributos para o JSP
        // Exemplo: request.setAttribute("notas", NotaFiscalManager.getNotas());
        return mapping.findForward("success");
    }
}
