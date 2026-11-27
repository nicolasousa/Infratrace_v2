package Model;

import java.util.List;

public class RelatorioGarantias {

    private String geradoEm;
    private int limiteDias;
    private int total;
    private int vencidas;
    private int aVencer;
    private List<Item> itens;

    public String getGeradoEm() {
        return geradoEm;
    }

    public int getLimiteDias() {
        return limiteDias;
    }

    public int getTotal() {
        return total;
    }

    public int getVencidas() {
        return vencidas;
    }

    public int getAVencer() {
        return aVencer;
    }

    public List<Item> getItens() {
        return itens == null ? List.of() : itens;
    }

    public static class Item {

        private int id;
        private String nome;
        private String tipo;
        private String patrimonio;
        private String status;
        private String garantia;
        private int diasRestantes;
        private String situacao;
        private String local;

        public int getId() {
            return id;
        }

        public String getNome() {
            return nome;
        }

        public String getTipo() {
            return tipo;
        }

        public String getPatrimonio() {
            return patrimonio;
        }

        public String getStatus() {
            return status;
        }

        public String getGarantia() {
            return garantia;
        }

        public int getDiasRestantes() {
            return diasRestantes;
        }

        public String getSituacao() {
            return situacao;
        }

        public String getLocal() {
            return local;
        }
    }
}
