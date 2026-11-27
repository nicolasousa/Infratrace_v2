package Model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Equipamento do data center. Espelha a tabela `ativo`:
 *
 *   atv_id              INT           -> id
 *   atv_nome            VARCHAR(128)  -> nome            (obrigatório)
 *   atv_fabricante      VARCHAR(128)  -> fabricante      (obrigatório)
 *   atv_modelo          VARCHAR(128)  -> modelo          (obrigatório)
 *   atv_num_serie       VARCHAR(64)   -> numSerie        (obrigatório e único)
 *   atv_patrimonio      VARCHAR(32)   -> patrimonio      (opcional, mas único quando preenchido)
 *   atv_status          VARCHAR(32)   -> status          (enum StatusAtivo)
 *   atv_data_aquisicao  DATE          -> dataAquisicao   (LocalDate: só a data, sem hora)
 *   atv_valor_compra    DECIMAL(10,2) -> valorCompra     (BigDecimal: dinheiro nunca usa double)
 *   atv_garantia        DATE          -> garantia        (data em que a garantia vence)
 *   atv_slot_inicio     INT           -> slotInicio      (posição no rack; null = fora de rack)
 *   atv_slot_fim        INT           -> slotFim
 *   atv_data_descarte   DATE          -> dataDescarte    (preenchidos só no descarte)
 *   atv_motivo_descarte VARCHAR(512)  -> motivoDescarte
 *   fk_tpa_id           INT           -> tipoId          (obrigatório)
 *   fk_rac_id           INT           -> rackId          (null = no estoque)
 *
 * Colunas que esta tela não altera (o UPDATE não mexe nelas, então ficam como estão):
 *   atv_criado_em (o banco preenche sozinho) e fk_nff_id (nota fiscal, sem tela nesta versão).
 *
 * Campos só para exibição (vêm dos JOINs da consulta): tipoNome, rackNome, fileiraNome, salaNome.
 */
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

    /** true se o ativo está instalado em um rack (false = estoque ou descartado). */
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
