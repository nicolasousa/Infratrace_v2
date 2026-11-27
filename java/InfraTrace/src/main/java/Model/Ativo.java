package Model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Ativo {

    private Integer id;
    private String nome;
    private String fabricante;
    private String modelo;
    private String numSerie;
    private String patrimonio;
    private StatusAtivo status;
    private LocalDate dataAquisicao;
    private BigDecimal valorCompra;
    private LocalDate garantia;
    private Integer slotInicio;
    private Integer slotFim;
    private LocalDate dataDescarte;
    private String motivoDescarte;
    private Integer tipoId;
    private Integer rackId;

    private String tipoNome;
    private String rackNome;
    private String fileiraNome;
    private String salaNome;

    public boolean estaEmRack() {
        return rackId != null;
    }

    public boolean isDescartado() {
        return status == StatusAtivo.DESCARTADO;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getFabricante() {
        return fabricante;
    }

    public void setFabricante(String fabricante) {
        this.fabricante = fabricante;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getNumSerie() {
        return numSerie;
    }

    public void setNumSerie(String numSerie) {
        this.numSerie = numSerie;
    }

    public String getPatrimonio() {
        return patrimonio;
    }

    public void setPatrimonio(String patrimonio) {
        this.patrimonio = patrimonio;
    }

    public StatusAtivo getStatus() {
        return status;
    }

    public void setStatus(StatusAtivo status) {
        this.status = status;
    }

    public LocalDate getDataAquisicao() {
        return dataAquisicao;
    }

    public void setDataAquisicao(LocalDate dataAquisicao) {
        this.dataAquisicao = dataAquisicao;
    }

    public BigDecimal getValorCompra() {
        return valorCompra;
    }

    public void setValorCompra(BigDecimal valorCompra) {
        this.valorCompra = valorCompra;
    }

    public LocalDate getGarantia() {
        return garantia;
    }

    public void setGarantia(LocalDate garantia) {
        this.garantia = garantia;
    }

    public Integer getSlotInicio() {
        return slotInicio;
    }

    public void setSlotInicio(Integer slotInicio) {
        this.slotInicio = slotInicio;
    }

    public Integer getSlotFim() {
        return slotFim;
    }

    public void setSlotFim(Integer slotFim) {
        this.slotFim = slotFim;
    }

    public LocalDate getDataDescarte() {
        return dataDescarte;
    }

    public void setDataDescarte(LocalDate dataDescarte) {
        this.dataDescarte = dataDescarte;
    }

    public String getMotivoDescarte() {
        return motivoDescarte;
    }

    public void setMotivoDescarte(String motivoDescarte) {
        this.motivoDescarte = motivoDescarte;
    }

    public Integer getTipoId() {
        return tipoId;
    }

    public void setTipoId(Integer tipoId) {
        this.tipoId = tipoId;
    }

    public Integer getRackId() {
        return rackId;
    }

    public void setRackId(Integer rackId) {
        this.rackId = rackId;
    }

    public String getTipoNome() {
        return tipoNome;
    }

    public void setTipoNome(String tipoNome) {
        this.tipoNome = tipoNome;
    }

    public String getRackNome() {
        return rackNome;
    }

    public void setRackNome(String rackNome) {
        this.rackNome = rackNome;
    }

    public String getFileiraNome() {
        return fileiraNome;
    }

    public void setFileiraNome(String fileiraNome) {
        this.fileiraNome = fileiraNome;
    }

    public String getSalaNome() {
        return salaNome;
    }

    public void setSalaNome(String salaNome) {
        this.salaNome = salaNome;
    }

    @Override
    public String toString() {
        return nome;
    }
}
