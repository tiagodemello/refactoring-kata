package src;

import org.apache.struts.action.ActionForm;
import java.util.Date;
import java.text.ParseException;

public class NotaFiscalForm extends ActionForm {
    // Adicione campos conforme necessário para o formulário
    private String exemploCampo;
    private int id;
    private String numero;
    private Date dataEmissao;
    private double valor;
    private String nomeCliente;
    private String cnpjCliente;

    public String getExemploCampo() {
        return exemploCampo;
    }

    public void setExemploCampo(String exemploCampo) {
        this.exemploCampo = exemploCampo;
    }

    public int getId() { 
        return id; 
    }
    public void setId(int id) { 
        this.id = id; 
    }
    public String getNumero() { 
        return numero; 
    }
    public void setNumero(String numero) { 
        this.numero = numero; 
    }
    public Date getDataEmissao() { 
        return dataEmissao; 
    }
    public void setDataEmissao(Date dataEmissao) { 
        this.dataEmissao = dataEmissao; 
    }
    public double getValor() { 
        return valor; 
    }
    public void setValor(double valor) { 
        this.valor = valor; 
    }
    public String getNomeCliente() { 
        return nomeCliente; 
    }
    public void setNomeCliente(String nomeCliente) { 
        this.nomeCliente = nomeCliente; 
    }
    public String getCnpjCliente() { 
        return cnpjCliente; 
    }
    public void setCnpjCliente(String cnpjCliente) { 
        this.cnpjCliente = cnpjCliente; 
    }
    public String getDataEmissaoString() {
        return dataEmissao != null ? new java.text.SimpleDateFormat("dd/MM/yyyy").format(dataEmissao) : null;
    }
    public void setDataEmissaoString(String data) throws ParseException {
        this.dataEmissao = data != null ? new java.text.SimpleDateFormat("dd/MM/yyyy").parse(data) : null;
    }
}
