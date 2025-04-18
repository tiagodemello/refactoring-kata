<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ page import="java.util.*" %>
<%@ page import="com.notafiscal.model.*" %>
<%@ page import="com.notafiscal.gerenciamento.*" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Lista de Notas Fiscais</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 8px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background-color: #f2f2f2; }
        .button {
            background-color: #4CAF50;
            color: white;
            padding: 8px 16px;
            border: none;
            cursor: pointer;
            border-radius: 4px;
        }
        .button.delete { background-color: #f44336; }
        .button.edit { background-color: #2196F3; }
    </style>
</head>
<body>
    <%
        // Lógica de negócio dentro da JSP
        String filtroCliente = request.getParameter("filtroCliente");
        String filtroData = request.getParameter("filtroData");

        List<NotaFiscal> notasFiscais = new ArrayList<NotaFiscal>();
        NotaFiscalManager manager = NotaFiscalManager.getInstance();

        // Verificando autenticação
        String usuario = (String) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        // Verificando autorização
        if (!manager.autorizar(usuario, "LISTAR_NOTAS")) {
            response.sendRedirect("sem_permissao.jsp");
            return;
        }

        // Carregando todas as notas fiscais
        for (int i = 0; i < manager.getNotasFiscais().size(); i++) {
            notasFiscais.add((NotaFiscal) manager.getNotasFiscais().get(i));
        }

        // Aplicando filtros
        if (filtroCliente != null && !filtroCliente.trim().isEmpty()) {
            List<NotaFiscal> notasFiltradas = new ArrayList<NotaFiscal>();
            for (NotaFiscal nota : notasFiscais) {
                if (nota.getNomeCliente().toLowerCase().contains(filtroCliente.toLowerCase())) {
                    notasFiltradas.add(nota);
                }
            }
            notasFiscais = notasFiltradas;
        }

        if (filtroData != null && !filtroData.trim().isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                Date data = sdf.parse(filtroData);

                List<NotaFiscal> notasFiltradas = new ArrayList<NotaFiscal>();
                for (NotaFiscal nota : notasFiscais) {
                    if (nota.getDataEmissao().equals(data)) {
                        notasFiltradas.add(nota);
                    }
                }
                notasFiscais = notasFiltradas;
            } catch (Exception e) {
                // Ignorando erro de parse
            }
        }

        // Ordenando as notas por data (mais recente primeiro)
        Collections.sort(notasFiscais, new Comparator<NotaFiscal>() {
            @Override
            public int compare(NotaFiscal n1, NotaFiscal n2) {
                return n2.getDataEmissao().compareTo(n1.getDataEmissao());
            }
        });

        // Calculando totais
        double valorTotal = 0.0;
        for (NotaFiscal nota : notasFiscais) {
            valorTotal += nota.getValor();
        }

        // Configurando atributo para uso nas tags Struts
        request.setAttribute("notasFiscais", notasFiscais);
    %>

    <h1>Notas Fiscais</h1>

    <div>
        <!-- Formulário de filtro -->
        <form method="get" action="listar_notas_fiscais.jsp">
            <label for="filtroCliente">Cliente:</label>
            <input type="text" id="filtroCliente" name="filtroCliente" value="<%= filtroCliente != null ? filtroCliente : "" %>">

            <label for="filtroData">Data (dd/mm/aaaa):</label>
            <input type="text" id="filtroData" name="filtroData" value="<%= filtroData != null ? filtroData : "" %>">

            <input type="submit" value="Filtrar" class="button">
        </form>

        <!-- Link para adicionar nova nota fiscal -->
        <p>
            <a href="<%=request.getContextPath()%>/notafiscal.do?op=novo" class="button">Nova Nota Fiscal</a>
        </p>
    </div>

    <p>Total de notas fiscais: <%= notasFiscais.size() %> | Valor total: R$ <%= String.format("%.2f", valorTotal) %></p>

    <table>
        <thead>
            <tr>
                <th>ID</th>
                <th>Número</th>
                <th>Data Emissão</th>
                <th>Cliente</th>
                <th>CNPJ</th>
                <th>Valor (R$)</th>
                <th>Ações</th>
            </tr>
        </thead>
        <tbody>
            <logic:iterate name="notasFiscais" id="nota">
                <tr>
                    <td><bean:write name="nota" property="id"/></td>
                    <td><bean:write name="nota" property="numero"/></td>
                    <td>
                        <%
                            NotaFiscal nota = (NotaFiscal) pageContext.getAttribute("nota");
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                            out.print(sdf.format(nota.getDataEmissao()));
                        %>
                    </td>
                    <td><bean:write name="nota" property="nomeCliente"/></td>
                    <td><bean:write name="nota" property="cnpjCliente"/></td>
                    <td>
                        <%
                            out.print(String.format("%.2f", nota.getValor()));
                        %>
                    </td>
                    <td>
                        <a href="<%=request.getContextPath()%>/notafiscal.do?op=editar&id=<bean:write name="nota" property="id"/>" class="button edit">Editar</a>
                        <a href="<%=request.getContextPath()%>/notafiscal.do?op=excluir&id=<bean:write name="nota" property="id"/>" class="button delete" onclick="return confirm('Deseja realmente excluir esta nota fiscal?');">Excluir</a>
                    </td>
                </tr>
            </logic:iterate>
        </tbody>
    </table>

    <div>
        <h3>Relatórios</h3>
        <p>
            <a href="<%=request.getContextPath()%>/notafiscal.do?op=relatorio&tipo=resumido" target="_blank" class="button">Relatório Resumido</a>
            <a href="<%=request.getContextPath()%>/notafiscal.do?op=relatorio&tipo=analitico" target="_blank" class="button">Relatório Analítico</a>
            <a href="<%=request.getContextPath()%>/notafiscal.do?op=relatorio&tipo=completo" target="_blank" class="button">Relatório Completo</a>
        </p>
    </div>
</body>
</html>