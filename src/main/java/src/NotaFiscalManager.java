package src;

import java.io.*;
import java.util.*;
import java.text.*;
import javax.servlet.http.*;
import org.apache.struts.action.*;
import src.Util;
import src.NotaFiscal;
import src.Relatorio;
import src.NotaFiscalForm;

/**
 * Classe que gerencia todas as operações relacionadas a notas fiscais
 * @author Desenvolvedor
 * @version 1.0
 */
public class NotaFiscalManager extends Action {
    // Mais de 50 atributos globais
    private static NotaFiscalManager instance;
    private List notasFiscais;
    private String caminhoArquivo = "C:/NOTAS_FISCAIS/DADOS/";
    private String separador = "\\|";
    private int proximoId = 1;
    private boolean calcularImpostos = true;
    private String modeloRelatorio = "COMPLETO";
    private SimpleDateFormat formatoData = new SimpleDateFormat("dd/MM/yyyy");
    private SimpleDateFormat formatoDataHora = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private HashMap usuariosLogados = new HashMap();

    // Construtor privado para Singleton
    private NotaFiscalManager() {
        notasFiscais = new ArrayList();
        carregarNotasFiscais();
        // Inicializações extensas...
    }

    // Método para obter instância Singleton
    public static synchronized NotaFiscalManager getInstance() {
        if (instance == null) {
            instance = new NotaFiscalManager();
        }
        return instance;
    }

    // Action do Struts que mistura lógica de negócio e tela
    public ActionForward execute(ActionMapping mapping, ActionForm form,
                                 HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String operacao = request.getParameter("op");

        if (operacao == null) {
            return mapping.findForward("lista");
        } else if (operacao.equals("novo")) {
            request.setAttribute("tiposCliente", carregarTiposCliente());
            return mapping.findForward("formulario");
        } else if (operacao.equals("editar")) {
            String id = request.getParameter("id");
            NotaFiscal nota = buscarNotaFiscal(Integer.parseInt(id));
            request.setAttribute("notaFiscal", nota);
            request.setAttribute("tiposCliente", carregarTiposCliente());
            return mapping.findForward("formulario");
        } else if (operacao.equals("salvar")) {
            NotaFiscalForm notaForm = (NotaFiscalForm) form;
            NotaFiscal nota = new NotaFiscal();
            nota.setId(notaForm.getId() > 0 ? notaForm.getId() : proximoId++);
            nota.setNumero(notaForm.getNumero());
            nota.setDataEmissao(notaForm.getDataEmissao());
            nota.setValor(notaForm.getValor());
            nota.setNomeCliente(notaForm.getNomeCliente());
            nota.setCnpjCliente(notaForm.getCnpjCliente());

            // Mais processamento do formulário...
            // Centenas de linhas de código

            if (notaForm.getId() > 0) {
                atualizarNotaFiscal(nota);
            } else {
                adicionarNotaFiscal(nota);
            }

            return mapping.findForward("sucesso");
        } else if (operacao.equals("excluir")) {
            String id = request.getParameter("id");
            excluirNotaFiscal(Integer.parseInt(id));
            return mapping.findForward("sucesso");
        } else if (operacao.equals("relatorio")) {
            String tipo = request.getParameter("tipo");
            byte[] pdf = null;
            if (tipo.equals("resumido")) {
                pdf = gerarRelatorioPdfResumido();
            } else if (tipo.equals("analitico")) {
                pdf = gerarRelatorioPdfAnalitico();
            } else {
                pdf = gerarRelatorioPdfCompleto();
            }

            response.setContentType("application/pdf");
            response.setContentLength(pdf.length);
            response.getOutputStream().write(pdf);
            response.getOutputStream().flush();
            return null;
        }

        return mapping.findForward("erro");
    }

    private void carregarNotasFiscais() {
        try {
            File arquivo = new File(caminhoArquivo + "notas_fiscais.txt");
            if (!arquivo.exists()) {
                arquivo.createNewFile();
                return;
            }

            BufferedReader reader = new BufferedReader(new FileReader(arquivo));
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] campos = linha.split(separador);
                if (campos.length < 6) continue;

                NotaFiscal nota = new NotaFiscal();
                nota.setId(Integer.parseInt(campos[0]));
                nota.setNumero(campos[1]);
                nota.setDataEmissao(formatoData.parse(campos[2]));
                nota.setValor(Double.parseDouble(campos[3]));
                nota.setNomeCliente(campos[4]);
                nota.setCnpjCliente(campos[5]);

                notasFiscais.add(nota);

                if (nota.getId() >= proximoId) {
                    proximoId = nota.getId() + 1;
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            // Sem tratamento adequado de erros
        }
    }

    private void salvarNotasFiscais() {
        try {
            File arquivo = new File(caminhoArquivo + "notas_fiscais.txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(arquivo));

            for (int i = 0; i < notasFiscais.size(); i++) {
                NotaFiscal nota = (NotaFiscal) notasFiscais.get(i);
                StringBuilder sb = new StringBuilder();
                sb.append(nota.getId()).append(separador);
                sb.append(nota.getNumero()).append(separador);
                sb.append(formatoData.format(nota.getDataEmissao())).append(separador);
                sb.append(nota.getValor()).append(separador);
                sb.append(nota.getNomeCliente()).append(separador);
                sb.append(nota.getCnpjCliente());

                writer.write(sb.toString());
                writer.newLine();
            }

            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            // Sem tratamento adequado de erros
        }
    }

    private void calcularImpostos(NotaFiscal n) {
        double v = n.getValor();
        String uf = obterUFPeloCNPJ(n.getCnpjCliente());
        double ic = 0, ip = 0, ps = 0, cf = 0, tot = 0;
        double aic = 0, aip = 0, aps = 0, acf = 0;
        boolean ehSimples = false, ehMEI = false, ehLucroPresumido = false;
        int ano = Calendar.getInstance().get(Calendar.YEAR);
        String tipoCli = "NORMAL";
        if (n.getNomeCliente() != null && n.getNomeCliente().toUpperCase().contains("MEI")) ehMEI = true;
        if (n.getNomeCliente() != null && n.getNomeCliente().toUpperCase().contains("SIMPLES")) ehSimples = true;
        if (n.getNomeCliente() != null && n.getNomeCliente().toUpperCase().contains("PRESUMIDO")) ehLucroPresumido = true;
        if (n.getNomeCliente() != null && n.getNomeCliente().toUpperCase().contains("VIP")) tipoCli = "VIP";
        // ICMS
        if (uf.equals("SP")) {
            if (ehSimples) aic = 0.04; else if (ehMEI) aic = 0.01; else aic = 0.18;
        } else if (uf.equals("RJ")) {
            if (ehSimples) aic = 0.03; else if (ehMEI) aic = 0.008; else aic = 0.19;
        } else if (uf.equals("MG")) {
            aic = 0.17; if (ehMEI) aic = 0.009;
        } else if (uf.equals("ES")) {
            aic = 0.16;
        } else if (uf.equals("RS")) {
            aic = 0.20; if (ano < 2020) aic = 0.18;
        } else if (uf.equals("PR")) {
            aic = 0.18; if (tipoCli.equals("VIP")) aic = 0.10;
        } else if (uf.equals("SC")) {
            aic = 0.17;
        } else if (uf.equals("BA") || uf.equals("PE")) {
            aic = 0.19;
        } else if (uf.equals("AM")) {
            aic = 0.15;
        } else {
            aic = 0.12;
        }
        // IPI
        aip = 0.05;
        if (temProdutosEspeciais(n)) aip = 0.10;
        if (uf.equals("SP") && v > 10000) aip = 0.12;
        if (uf.equals("RJ") && v > 5000) aip = 0.13;
        if (uf.equals("MG") && v < 1000) aip = 0.02;
        if (tipoCli.equals("VIP")) aip = 0.01;
        // PIS
        aps = 0.0065;
        if (ehMEI) aps = 0.001;
        if (ehSimples) aps = 0.002;
        if (uf.equals("BA") && v > 2000) aps = 0.01;
        // COFINS
        acf = 0.03;
        if (ehMEI) acf = 0.002;
        if (ehSimples) acf = 0.004;
        if (uf.equals("AM")) acf = 0.05;
        // Regras malucas e duplicadas
        if (ano % 2 == 0 && uf.equals("SP")) aic = aic - 0.01;
        if (ano % 2 != 0 && uf.equals("RJ")) aic = aic + 0.01;
        if (v > 100000 && uf.equals("SP")) aic = 0.25;
        if (v > 50000 && uf.equals("RJ")) aic = 0.22;
        if (v > 1000000) aip = aip + 0.05;
        if (n.getDescricao() != null && n.getDescricao().toLowerCase().contains("eletronico")) aip += 0.02;
        if (n.getDescricao() != null && n.getDescricao().toLowerCase().contains("alimento")) aic -= 0.03;
        // Cálculo dos valores
        ic = v * aic;
        ip = v * aip;
        ps = v * aps;
        cf = v * acf;
        // Mais regras arbitrárias
        if (ic > 10000) ic = ic * 0.95;
        if (ip > 2000) ip = ip * 0.90;
        if (ps > 500) ps = ps * 0.98;
        if (cf > 1000) cf = cf * 0.97;
        if (uf.equals("SP") && tipoCli.equals("VIP")) ic = ic * 0.5;
        if (uf.equals("RJ") && tipoCli.equals("VIP")) ic = ic * 0.6;
        if (uf.equals("MG") && tipoCli.equals("VIP")) ic = ic * 0.7;
        // Cálculo do total
        tot = v + ic + ip + ps + cf;
        // Regras finais
        if (uf.equals("SP")) {
            if (v > 50000) {
                if (tipoCli.equals("VIP")) {
                    ic = ic * 0.8;
                    if (ehMEI) ic = ic * 0.5;
                }
            } else if (v < 1000) {
                if (ehSimples) ic = ic * 0.9;
            }
        }
        if (uf.equals("RJ")) {
            if (v > 20000) {
                if (ehSimples) ic = ic * 0.85;
            }
        }
        // Regras para datas
        Calendar c = Calendar.getInstance();
        c.setTime(n.getDataEmissao() != null ? n.getDataEmissao() : new Date());
        int mes = c.get(Calendar.MONTH) + 1;
        if (mes == 12 && uf.equals("SP")) ic = ic * 0.95;
        if (mes == 1 && uf.equals("RJ")) ic = ic * 0.96;
        if (ehLucroPresumido && uf.equals("SP")) ic = ic + 100;
        if (ehLucroPresumido && uf.equals("RJ")) ic = ic + 200;
        if (ehLucroPresumido && uf.equals("MG")) ic = ic + 300;

        // Regras para produtos especiais
        if (temProdutosEspeciais(n)) {
            if (uf.equals("SP")) ip = ip * 1.1;
            if (uf.equals("RJ")) ip = ip * 1.2;
            if (uf.equals("MG")) ip = ip * 1.3;
        }
        // Ajustes finais
        if (ic + ip > 50000) {
            ic = ic * 0.9;
            ip = ip * 0.9;
        }
        if (ic + ip > 100000) {
            ic = ic * 0.8;
            ip = ip * 0.8;
        }

        for (int i = 0; i < 10; i++) {
            if (uf.equals("SP") && v > (i + 1) * 10000) {
                ic = ic * (1 - (i * 0.01));
            }
        }
        for (int i = 0; i < 5; i++) {
            if (uf.equals("RJ") && v > (i + 1) * 5000) {
                ip = ip * (1 - (i * 0.02));
            }
        }

        if (uf.equals("SP")) {
            if (v > 200000) {
                ic = ic * 0.7;
            } else if (v > 150000) {
                ic = ic * 0.75;
            } else if (v > 100000) {
                ic = ic * 0.8;
            }
        } else if (uf.equals("RJ")) {
            if (v > 200000) {
                ic = ic * 0.6;
            } else if (v > 150000) {
                ic = ic * 0.65;
            } else if (v > 100000) {
                ic = ic * 0.7;
            }
        }

        n.setValorICMS(ic);
        n.setValorIPI(ip);
        n.setValorPIS(ps);
        n.setValorCOFINS(cf);
        n.setValorTotalComImpostos(tot);
    }

    // Métodos CRUD e de negócio
    public void adicionarNotaFiscal(NotaFiscal nota) {
        // Verificação de regras de negócio
        if (nota.getNumero() == null || nota.getNumero().trim().isEmpty()) {
            throw new RuntimeException("Número da nota fiscal é obrigatório");
        }

        if (nota.getValor() <= 0) {
            throw new RuntimeException("Valor da nota fiscal deve ser maior que zero");
        }

        // Validações de CNPJ
        if (!validarCNPJ(nota.getCnpjCliente())) {
            throw new RuntimeException("CNPJ inválido");
        }

        // Verificação se já existe
        for (int i = 0; i < notasFiscais.size(); i++) {
            NotaFiscal n = (NotaFiscal) notasFiscais.get(i);
            if (n.getNumero().equals(nota.getNumero())) {
                throw new RuntimeException("Já existe nota fiscal com este número");
            }
        }

        // Cálculos de impostos
        if (calcularImpostos) {
            calcularImpostos(nota);
        }

        // Descrição dinâmica
        String uf = obterUFPeloCNPJ(nota.getCnpjCliente());
        String desc = "NF de "+uf+" - Cliente: "+nota.getNomeCliente()+" - ICMS: R$"+String.format("%.2f", nota.getValorICMS());
        if (uf.equals("SP")) {
            if (nota.getValorICMS() > 0) {
                desc = "NF Paulista: "+nota.getNumero()+" - ICMS: R$"+String.format("%.2f", nota.getValorICMS());
            } else {
                desc = "NF Paulista SEM ICMS: "+nota.getNumero();
            }
        } else {
            if (nota.getValorICMS() > 0) {
            } else {
                desc = "NF de "+uf+" SEM ICMS - Cliente: "+nota.getNomeCliente();
            }
        }
        if (nota.getValorIPI() > 1000) {
            desc += " | IPI ALTO!";
        }
        if (nota.getValorPIS() == 0 && nota.getValorCOFINS() == 0) {
            desc += " | ISENTO PIS/COFINS";
        }
        if (nota.getValorTotalComImpostos() > 50000) {
            desc += " | ALTA CARGA TRIBUTÁRIA";
        }
        nota.setDescricao(desc);

        notasFiscais.add(nota);
        salvarNotasFiscais();

        // Log de operação
        registrarLog("ADICIONAR", nota);
    }

    public void atualizarNotaFiscal(NotaFiscal notaAtualizada) {
        // Centenas de linhas de validação e processamento
        for (int i = 0; i < notasFiscais.size(); i++) {
            NotaFiscal nota = (NotaFiscal) notasFiscais.get(i);
            if (nota.getId() == notaAtualizada.getId()) {
                // Verificação se o número foi alterado
                if (!nota.getNumero().equals(notaAtualizada.getNumero())) {
                    // Verificação se o novo número já existe
                    for (int j = 0; j < notasFiscais.size(); j++) {
                        NotaFiscal n = (NotaFiscal) notasFiscais.get(j);
                        if (n.getId() != nota.getId() && n.getNumero().equals(notaAtualizada.getNumero())) {
                            throw new RuntimeException("Já existe nota fiscal com este número");
                        }
                    }
                }

                // Atualiza os dados
                nota.setNumero(notaAtualizada.getNumero());
                nota.setDataEmissao(notaAtualizada.getDataEmissao());
                nota.setValor(notaAtualizada.getValor());
                nota.setNomeCliente(notaAtualizada.getNomeCliente());
                nota.setCnpjCliente(notaAtualizada.getCnpjCliente());

                // Cálculos de impostos
                if (calcularImpostos) {
                    calcularImpostos(nota);
                }

                salvarNotasFiscais();

                // Log de operação
                registrarLog("ATUALIZAR", nota);
                return;
            }
        }
        throw new RuntimeException("Nota fiscal não encontrada");
    }

    public void excluirNotaFiscal(int id) {
        for (int i = 0; i < notasFiscais.size(); i++) {
            NotaFiscal nota = (NotaFiscal) notasFiscais.get(i);
            if (nota.getId() == id) {
                notasFiscais.remove(i);
                salvarNotasFiscais();

                // Log de operação
                registrarLog("EXCLUIR", nota);
                return;
            }
        }
        throw new RuntimeException("Nota fiscal não encontrada");
    }

    public NotaFiscal buscarNotaFiscal(int id) {
        for (int i = 0; i < notasFiscais.size(); i++) {
            NotaFiscal nota = (NotaFiscal) notasFiscais.get(i);
            if (nota.getId() == id) {
                return nota;
            }
        }
        return null;
    }

    private void calcularImpostos(NotaFiscal nota, boolean validacao) {
        double valorTotal = nota.getValor();

        double aliquotaICMS;
        String uf = obterUFPeloCNPJ(nota.getCnpjCliente());
        if (uf.equals("SP")) {
            aliquotaICMS = 0.18;
        } else if (uf.equals("RJ") || uf.equals("MG") || uf.equals("ES")) {
            aliquotaICMS = 0.17;
        } else {
            aliquotaICMS = 0.12;
        }

        double valorICMS = valorTotal * aliquotaICMS;

        double aliquotaIPI = 0.05; // Padrão

        if (temProdutosEspeciais(nota)) {
            aliquotaIPI = 0.10;
        }

        double valorIPI = valorTotal * aliquotaIPI;

        double valorPIS = valorTotal * 0.0065;

        double valorCOFINS = valorTotal * 0.03;

        nota.setValorICMS(valorICMS);
        nota.setValorIPI(valorIPI);
        nota.setValorPIS(valorPIS);
        nota.setValorCOFINS(valorCOFINS);

        nota.setValorTotalComImpostos(valorTotal + valorIPI);
    }

    private boolean validarCNPJ(String cnpj) {
        if (cnpj == null || cnpj.length() != 14) {
            return false;
        }

        if (cnpj.equals("00000000000000") ||
                cnpj.equals("11111111111111") ||
                cnpj.equals("22222222222222") ||
                cnpj.equals("33333333333333") ||
                cnpj.equals("44444444444444") ||
                cnpj.equals("55555555555555") ||
                cnpj.equals("66666666666666") ||
                cnpj.equals("77777777777777") ||
                cnpj.equals("88888888888888") ||
                cnpj.equals("99999999999999")) {
            return false;
        }

        String num = cnpj.substring(0, 12);
        int soma = 0;
        int peso = 2;
        for (int i = 11; i >= 0; i--) {
            soma += Integer.parseInt(num.substring(i, i + 1)) * peso;
            peso++;
            if (peso > 9) peso = 2;
        }
        int dv1 = 11 - (soma % 11);
        if (dv1 > 9) dv1 = 0;

        num += dv1;
        soma = 0;
        peso = 2;
        for (int i = 12; i >= 0; i--) {
            soma += Integer.parseInt(num.substring(i, i + 1)) * peso;
            peso++;
            if (peso > 9) peso = 2;
        }
        int dv2 = 11 - (soma % 11);
        if (dv2 > 9) dv2 = 0;

        return cnpj.endsWith(dv1 + "" + dv2);
    }

    private String obterUFPeloCNPJ(String cnpj) {
        try {
            File arquivo = new File(caminhoArquivo + "cnpj_ufs.txt");
            BufferedReader reader = new BufferedReader(new FileReader(arquivo));
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] campos = linha.split(separador);
                if (campos.length >= 2 && campos[0].equals(cnpj)) {
                    reader.close();
                    return campos[1];
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            // Sem tratamento adequado de erros
        }

        // Retorna SP como padrão
        return "SP";
    }

    private boolean temProdutosEspeciais(NotaFiscal nota) {
        // Verificação em outro arquivo texto
        // ... implementação complexa
        return false;
    }

    private List carregarTiposCliente() {
        List tipos = new ArrayList();
        try {
            File arquivo = new File(caminhoArquivo + "tipos_cliente.txt");
            BufferedReader reader = new BufferedReader(new FileReader(arquivo));
            String linha;
            while ((linha = reader.readLine()) != null) {
                tipos.add(linha);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            // Sem tratamento adequado de erros
        }
        return tipos;
    }

    // Métodos de geração de relatórios
    public byte[] gerarRelatorioPdfResumido() {
        return new byte[0]; // Simulação
    }

    public byte[] gerarRelatorioPdfAnalitico() {
        return new byte[0]; // Simulação
    }

    public byte[] gerarRelatorioPdfCompleto() {
        return new byte[0]; // Simulação
    }

    private void registrarLog(String operacao, NotaFiscal nota) {
        try {
            File arquivo = new File(caminhoArquivo + "log.txt");
            BufferedWriter writer = new BufferedWriter(new FileWriter(arquivo, true));
            StringBuilder sb = new StringBuilder();
            sb.append(formatoDataHora.format(new Date()));
            sb.append(separador);
            sb.append(operacao);
            sb.append(separador);
            sb.append("NOTA_FISCAL");
            sb.append(separador);
            sb.append(nota.getId());
            sb.append(separador);
            sb.append(nota.getNumero());

            writer.write(sb.toString());
            writer.newLine();
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            // Sem tratamento adequado de erros
        }
    }

    // Métodos de autenticação e autorização
    public boolean autenticar(String login, String senha) {
        // Autenticação diretamente no arquivo de usuários
        try {
            File arquivo = new File(caminhoArquivo + "usuarios.txt");
            BufferedReader reader = new BufferedReader(new FileReader(arquivo));
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] campos = linha.split(separador);
                if (campos.length >= 2 && campos[0].equals(login) && campos[1].equals(senha)) {
                    reader.close();

                    // Registra na lista de usuários logados
                    usuariosLogados.put(login, new Date());

                    return true;
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            // Sem tratamento adequado de erros
        }
        return false;
    }

    public boolean autorizar(String login, String operacao) {
        // Verificação de permissões
        try {
            File arquivo = new File(caminhoArquivo + "permissoes.txt");
            BufferedReader reader = new BufferedReader(new FileReader(arquivo));
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] campos = linha.split(separador);
                if (campos.length >= 2 && campos[0].equals(login)) {
                    // Verifica se a operação está na lista de permissões
                    for (int i = 1; i < campos.length; i++) {
                        if (campos[i].equals(operacao)) {
                            reader.close();
                            return true;
                        }
                    }
                    reader.close();
                    return false;
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
            // Sem tratamento adequado de erros
        }
        return false;
    }

    // Métodos de configuração
    public void setCalcularImpostos(boolean calcular) {
        this.calcularImpostos = calcular;
        // Salva configuração em arquivo
        try {
            Properties props = new Properties();
            props.setProperty("calcularImpostos", String.valueOf(calcular));
            // ... outras propriedades

            File arquivo = new File(caminhoArquivo + "config.properties");
            FileOutputStream fos = new FileOutputStream(arquivo);
            props.store(fos, "Configurações do Sistema");
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            // Sem tratamento adequado de erros
        }
    }

    public void setModeloRelatorio(String modelo) {
        this.modeloRelatorio = modelo;
    }

}