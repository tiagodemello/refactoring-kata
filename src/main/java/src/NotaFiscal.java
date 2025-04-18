package src;

public class NotaFiscal {
    // Adicione atributos conforme necessário
    private int id;
    private String descricao;
    private String numero;
    private java.util.Date dataEmissao;
    private double valor;
    private String nomeCliente;
    private String cnpjCliente;
    private double valorICMS;
    private double valorIPI;
    private double valorPIS;
    private double valorCOFINS;
    private double valorTotalComImpostos;

    public NotaFiscal() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public java.util.Date getDataEmissao() { return dataEmissao; }
    public void setDataEmissao(java.util.Date dataEmissao) { this.dataEmissao = dataEmissao; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }

    public String getNomeCliente() { return nomeCliente; }
    public void setNomeCliente(String nomeCliente) { this.nomeCliente = nomeCliente; }

    public String getCnpjCliente() { return cnpjCliente; }
    public void setCnpjCliente(String cnpjCliente) { this.cnpjCliente = cnpjCliente; }

    public double getValorICMS() { return valorICMS; }
    public void setValorICMS(double valorICMS) { this.valorICMS = valorICMS; }

    public double getValorIPI() { return valorIPI; }
    public void setValorIPI(double valorIPI) { this.valorIPI = valorIPI; }

    public double getValorPIS() { return valorPIS; }
    public void setValorPIS(double valorPIS) { this.valorPIS = valorPIS; }

    public double getValorCOFINS() { return valorCOFINS; }
    public void setValorCOFINS(double valorCOFINS) { this.valorCOFINS = valorCOFINS; }

    public double getValorTotalComImpostos() { return valorTotalComImpostos; }
    public void setValorTotalComImpostos(double valorTotalComImpostos) { this.valorTotalComImpostos = valorTotalComImpostos; }
}
