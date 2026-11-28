SET NAMES utf8mb4;

DROP DATABASE IF EXISTS infratrace_v2;
CREATE DATABASE infratrace_v2
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;
USE infratrace_v2;

CREATE TABLE tipo_ativo (
    tpa_id        INT          NOT NULL AUTO_INCREMENT,
    tpa_nome      VARCHAR(128) NOT NULL,
    tpa_descricao VARCHAR(512),
    CONSTRAINT pk_tipo_ativo PRIMARY KEY (tpa_id),
    CONSTRAINT uq_tpa_nome   UNIQUE (tpa_nome)
);

CREATE TABLE tipo_cliente (
    tpc_id   INT          NOT NULL AUTO_INCREMENT,
    tpc_nome VARCHAR(128) NOT NULL,
    CONSTRAINT pk_tipo_cliente PRIMARY KEY (tpc_id),
    CONSTRAINT uq_tpc_nome     UNIQUE (tpc_nome)
);

CREATE TABLE usuario (
    usr_id                INT          NOT NULL AUTO_INCREMENT,
    usr_nome              VARCHAR(128) NOT NULL,
    usr_email             VARCHAR(128) NOT NULL,
    usr_senha_hash        VARCHAR(255) NOT NULL,          
    usr_perfil            VARCHAR(16)  NOT NULL,
    usr_ativo             BOOLEAN      NOT NULL DEFAULT TRUE,
    usr_token_recuperacao VARCHAR(255),
    usr_token_expiracao   DATETIME,
    usr_criado_em         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_usuario    PRIMARY KEY (usr_id),
    CONSTRAINT uq_usr_email  UNIQUE (usr_email),
    CONSTRAINT ck_usr_perfil CHECK (usr_perfil IN ('ADMIN', 'TECNICO', 'AUDITOR'))
);


CREATE TABLE sala (
    sal_id        INT          NOT NULL AUTO_INCREMENT,
    sal_nome      VARCHAR(128) NOT NULL,
    sal_descricao VARCHAR(512),
    CONSTRAINT pk_sala     PRIMARY KEY (sal_id),
    CONSTRAINT uq_sal_nome UNIQUE (sal_nome)
);

CREATE TABLE fileira (
    fil_id    INT         NOT NULL AUTO_INCREMENT,
    fil_nome  VARCHAR(10) NOT NULL,
    fk_sal_id INT         NOT NULL,
    CONSTRAINT pk_fileira       PRIMARY KEY (fil_id),
    CONSTRAINT uq_fil_sala_nome UNIQUE (fk_sal_id, fil_nome),
    CONSTRAINT fk_fil_sala      FOREIGN KEY (fk_sal_id) REFERENCES sala (sal_id)
);

CREATE TABLE rack (
    rac_id          INT          NOT NULL AUTO_INCREMENT,
    rac_nome        VARCHAR(128) NOT NULL,
    rac_total_slots INT          NOT NULL,
    fk_fil_id       INT          NOT NULL,
    CONSTRAINT pk_rack             PRIMARY KEY (rac_id),
    CONSTRAINT uq_rac_fileira_nome UNIQUE (fk_fil_id, rac_nome),
    CONSTRAINT ck_rac_total_slots  CHECK (rac_total_slots BETWEEN 1 AND 60),
    CONSTRAINT fk_rac_fileira      FOREIGN KEY (fk_fil_id) REFERENCES fileira (fil_id)
);

CREATE TABLE nota_fiscal (
    nff_id           INT           NOT NULL AUTO_INCREMENT,
    nff_num          INT           NOT NULL,
    nff_chave        VARCHAR(44),
    nff_valor        DECIMAL(10,2) NOT NULL,
    nff_volumes      INT,
    nff_razao_social VARCHAR(128)  NOT NULL,
    nff_cnpj         CHAR(14)      NOT NULL,
    nff_endereco     VARCHAR(128),
    nff_inscricao    VARCHAR(128),
    nff_descricao    VARCHAR(128),
    CONSTRAINT pk_nota_fiscal PRIMARY KEY (nff_id),
    CONSTRAINT uq_nff_chave   UNIQUE (nff_chave),
    CONSTRAINT ck_nff_valor   CHECK (nff_valor >= 0)
);

CREATE TABLE ativo (
    atv_id              INT           NOT NULL AUTO_INCREMENT,
    atv_nome            VARCHAR(128)  NOT NULL,
    atv_fabricante      VARCHAR(128)  NOT NULL,
    atv_modelo          VARCHAR(128)  NOT NULL,
    atv_num_serie       VARCHAR(64)   NOT NULL,
    atv_patrimonio      VARCHAR(32),
    atv_status          VARCHAR(32)   NOT NULL DEFAULT 'ATIVO',
    atv_data_aquisicao  DATE,
    atv_valor_compra    DECIMAL(10,2),
    atv_garantia        DATE,                              -- data de vencimento da garantia
    atv_slot_inicio     INT,
    atv_slot_fim        INT,
    atv_data_descarte   DATE,
    atv_motivo_descarte VARCHAR(512),
    atv_criado_em       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fk_tpa_id           INT           NOT NULL,
    fk_rac_id           INT,                               
    fk_nff_id           INT,
    CONSTRAINT pk_ativo          PRIMARY KEY (atv_id),
    CONSTRAINT uq_atv_num_serie  UNIQUE (atv_num_serie),
    CONSTRAINT uq_atv_patrimonio UNIQUE (atv_patrimonio),
    CONSTRAINT ck_atv_status CHECK (atv_status IN ('ATIVO', 'INATIVO', 'EM_MANUTENCAO',
                                                   'AGUARDANDO_DESCARTE', 'DESCARTADO')),
    CONSTRAINT ck_atv_valor_compra CHECK (atv_valor_compra >= 0),
    CONSTRAINT ck_atv_localizacao CHECK (
        (fk_rac_id IS NULL AND atv_slot_inicio IS NULL AND atv_slot_fim IS NULL)
        OR
        (fk_rac_id IS NOT NULL AND atv_slot_inicio IS NOT NULL AND atv_slot_fim IS NOT NULL
         AND atv_slot_inicio >= 1 AND atv_slot_fim >= atv_slot_inicio)
    ),
    CONSTRAINT ck_atv_descarte CHECK (
        atv_status <> 'DESCARTADO'
        OR (atv_data_descarte IS NOT NULL AND atv_motivo_descarte IS NOT NULL AND fk_rac_id IS NULL)
    ),
    CONSTRAINT fk_atv_tipo_ativo  FOREIGN KEY (fk_tpa_id) REFERENCES tipo_ativo (tpa_id),
    CONSTRAINT fk_atv_rack        FOREIGN KEY (fk_rac_id) REFERENCES rack (rac_id),
    CONSTRAINT fk_atv_nota_fiscal FOREIGN KEY (fk_nff_id) REFERENCES nota_fiscal (nff_id)
);

CREATE TABLE movimentacao (
    mov_id                  INT          NOT NULL AUTO_INCREMENT,
    mov_data                DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    mov_motivo              VARCHAR(512),
    mov_slot_inicio_origem  INT,
    mov_slot_fim_origem     INT,
    mov_slot_inicio_destino INT,
    mov_slot_fim_destino    INT,
    fk_rac_origem_id        INT,                           -- nulo = veio do estoque
    fk_rac_destino_id       INT,                           -- nulo = foi para o estoque
    fk_atv_id               INT          NOT NULL,
    fk_usr_id               INT          NOT NULL,
    CONSTRAINT pk_movimentacao PRIMARY KEY (mov_id),
    CONSTRAINT ck_mov_tem_rack CHECK (fk_rac_origem_id IS NOT NULL OR fk_rac_destino_id IS NOT NULL),
    CONSTRAINT ck_mov_origem CHECK (
        (fk_rac_origem_id IS NULL AND mov_slot_inicio_origem IS NULL AND mov_slot_fim_origem IS NULL)
        OR (fk_rac_origem_id IS NOT NULL
            AND mov_slot_inicio_origem IS NOT NULL AND mov_slot_fim_origem IS NOT NULL
            AND mov_slot_inicio_origem >= 1 AND mov_slot_fim_origem >= mov_slot_inicio_origem)
    ),
    CONSTRAINT ck_mov_destino CHECK (
        (fk_rac_destino_id IS NULL AND mov_slot_inicio_destino IS NULL AND mov_slot_fim_destino IS NULL)
        OR (fk_rac_destino_id IS NOT NULL
            AND mov_slot_inicio_destino IS NOT NULL AND mov_slot_fim_destino IS NOT NULL
            AND mov_slot_inicio_destino >= 1 AND mov_slot_fim_destino >= mov_slot_inicio_destino)
    ),
    CONSTRAINT fk_mov_rack_origem  FOREIGN KEY (fk_rac_origem_id)  REFERENCES rack (rac_id),
    CONSTRAINT fk_mov_rack_destino FOREIGN KEY (fk_rac_destino_id) REFERENCES rack (rac_id),
    CONSTRAINT fk_mov_ativo        FOREIGN KEY (fk_atv_id)         REFERENCES ativo (atv_id),
    CONSTRAINT fk_mov_usuario      FOREIGN KEY (fk_usr_id)         REFERENCES usuario (usr_id)
);

CREATE TABLE cliente (
    cli_id            INT          NOT NULL AUTO_INCREMENT,
    cli_nome          VARCHAR(200) NOT NULL,
    cli_cnpj          CHAR(14)     NOT NULL,               -- somente dígitos
    cli_representante VARCHAR(128) NOT NULL,
    cli_ativo         BOOLEAN      NOT NULL DEFAULT TRUE,
    fk_tpc_id         INT          NOT NULL,
    CONSTRAINT pk_cliente  PRIMARY KEY (cli_id),
    CONSTRAINT uq_cli_cnpj UNIQUE (cli_cnpj),
    CONSTRAINT ck_cli_cnpj CHECK (REGEXP_LIKE(cli_cnpj, '^[0-9]{14}$')),
    CONSTRAINT fk_cli_tipo_cliente FOREIGN KEY (fk_tpc_id) REFERENCES tipo_cliente (tpc_id)
);

CREATE TABLE aluguel (
    alu_id          INT           NOT NULL AUTO_INCREMENT,
    alu_data_inicio DATE          NOT NULL,
    alu_data_fim    DATE          NOT NULL,
    alu_valor       DECIMAL(10,2) NOT NULL,                -- valor mensal
    fk_cli_id       INT           NOT NULL,
    fk_rac_id       INT           NOT NULL,
    CONSTRAINT pk_aluguel   PRIMARY KEY (alu_id),
    CONSTRAINT ck_alu_valor CHECK (alu_valor >= 0),
    CONSTRAINT ck_alu_datas CHECK (alu_data_fim >= alu_data_inicio),
    CONSTRAINT fk_alu_cliente FOREIGN KEY (fk_cli_id) REFERENCES cliente (cli_id),
    CONSTRAINT fk_alu_rack    FOREIGN KEY (fk_rac_id) REFERENCES rack (rac_id)
);

CREATE INDEX ix_atv_status   ON ativo (atv_status);
CREATE INDEX ix_atv_garantia ON ativo (atv_garantia);
CREATE INDEX ix_mov_atv      ON movimentacao (fk_atv_id, mov_data);
CREATE INDEX ix_alu_data_fim ON aluguel (alu_data_fim);

-- Triggers 

DELIMITER $$

CREATE TRIGGER Trigger_ativo_valida_slots_ins
BEFORE INSERT ON ativo
FOR EACH ROW
BEGIN
    DECLARE v_capacidade INT;
    DECLARE v_conflitos  INT;

    IF NEW.fk_rac_id IS NOT NULL THEN

        SELECT rac_total_slots INTO v_capacidade
          FROM rack
         WHERE rac_id = NEW.fk_rac_id;

        IF NEW.atv_slot_fim > v_capacidade THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Slot final maior que a capacidade do rack';
        END IF;

        SELECT COUNT(*) INTO v_conflitos
          FROM ativo
         WHERE fk_rac_id = NEW.fk_rac_id
           AND atv_slot_inicio <= NEW.atv_slot_fim
           AND atv_slot_fim    >= NEW.atv_slot_inicio;

        IF v_conflitos > 0 THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Slots já ocupados por outro ativo neste rack';
        END IF;

    END IF;
END$$

-- Mesmo trigger para UPDATE;
CREATE TRIGGER Trigger_ativo_valida_slots_upd
BEFORE UPDATE ON ativo
FOR EACH ROW
BEGIN
    DECLARE v_capacidade INT;
    DECLARE v_conflitos  INT;

    IF NEW.fk_rac_id IS NOT NULL THEN

        SELECT rac_total_slots INTO v_capacidade
          FROM rack
         WHERE rac_id = NEW.fk_rac_id;

        IF NEW.atv_slot_fim > v_capacidade THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Slot final maior que a capacidade do rack';
        END IF;

        SELECT COUNT(*) INTO v_conflitos
          FROM ativo
         WHERE fk_rac_id = NEW.fk_rac_id
           AND atv_id <> NEW.atv_id
           AND atv_slot_inicio <= NEW.atv_slot_fim
           AND atv_slot_fim    >= NEW.atv_slot_inicio;

        IF v_conflitos > 0 THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Slots já ocupados por outro ativo neste rack';
        END IF;

    END IF;
END$$

-- A capacidade do rack não pode ficar menor que o maior slot ocupado nele
CREATE TRIGGER Trigger_rack_reducao_capacidade
BEFORE UPDATE ON rack
FOR EACH ROW
BEGIN
    DECLARE v_maior_slot INT;

    SELECT MAX(atv_slot_fim) INTO v_maior_slot
      FROM ativo
     WHERE fk_rac_id = NEW.rac_id;

    IF v_maior_slot > NEW.rac_total_slots THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Capacidade menor que o maior slot ocupado no rack';
    END IF;
END$$

-- Movimentação: o slot final de destino não pode passar da capacidade do rack
CREATE TRIGGER Trigger_movimentacao_capacidade_ins
BEFORE INSERT ON movimentacao
FOR EACH ROW
BEGIN
    DECLARE v_capacidade INT;

    IF NEW.fk_rac_destino_id IS NOT NULL THEN

        SELECT rac_total_slots INTO v_capacidade
          FROM rack
         WHERE rac_id = NEW.fk_rac_destino_id;

        IF NEW.mov_slot_fim_destino > v_capacidade THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Slot final de destino maior que a capacidade do rack';
        END IF;

    END IF;
END$$

CREATE TRIGGER Trigger_movimentacao_capacidade_upd
BEFORE UPDATE ON movimentacao
FOR EACH ROW
BEGIN
    DECLARE v_capacidade INT;

    IF NEW.fk_rac_destino_id IS NOT NULL THEN

        SELECT rac_total_slots INTO v_capacidade
          FROM rack
         WHERE rac_id = NEW.fk_rac_destino_id;

        IF NEW.mov_slot_fim_destino > v_capacidade THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'Slot final de destino maior que a capacidade do rack';
        END IF;

    END IF;
END$$

-- O mesmo rack não pode ter aluguéis com períodos sobrepostos

CREATE TRIGGER Trigger_aluguel_periodo_ins
BEFORE INSERT ON aluguel
FOR EACH ROW
BEGIN
    DECLARE v_conflitos INT;

    SELECT COUNT(*) INTO v_conflitos
      FROM aluguel
     WHERE fk_rac_id = NEW.fk_rac_id
       AND alu_data_inicio <= NEW.alu_data_fim
       AND alu_data_fim    >= NEW.alu_data_inicio;

    IF v_conflitos > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Já existe aluguel deste rack nesse período';
    END IF;
END$$

CREATE TRIGGER Trigger_aluguel_periodo_upd
BEFORE UPDATE ON aluguel
FOR EACH ROW
BEGIN
    DECLARE v_conflitos INT;

    SELECT COUNT(*) INTO v_conflitos
      FROM aluguel
     WHERE fk_rac_id = NEW.fk_rac_id
       AND alu_id <> NEW.alu_id
       AND alu_data_inicio <= NEW.alu_data_fim
       AND alu_data_fim    >= NEW.alu_data_inicio;

    IF v_conflitos > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Já existe aluguel deste rack nesse período';
    END IF;
END$$

DELIMITER ;

-- Views. Lidas pela API Python (Relatórios).

CREATE VIEW View_ocupacao_rack AS
SELECT r.rac_id,
       r.rac_nome,
       f.fil_id,
       f.fil_nome,
       s.sal_id,
       s.sal_nome,
       r.rac_total_slots,
       COUNT(a.atv_id)                                              AS qtd_ativos,
       COALESCE(SUM(a.atv_slot_fim - a.atv_slot_inicio + 1), 0)     AS slots_ocupados,
       r.rac_total_slots
           - COALESCE(SUM(a.atv_slot_fim - a.atv_slot_inicio + 1), 0) AS slots_livres,
       ROUND(COALESCE(SUM(a.atv_slot_fim - a.atv_slot_inicio + 1), 0) * 100
             / r.rac_total_slots, 1)                                AS percentual_ocupacao
  FROM rack r
  JOIN fileira f    ON f.fil_id = r.fk_fil_id
  JOIN sala s       ON s.sal_id = f.fk_sal_id
  LEFT JOIN ativo a ON a.fk_rac_id = r.rac_id
 GROUP BY r.rac_id, r.rac_nome, f.fil_id, f.fil_nome, s.sal_id, s.sal_nome, r.rac_total_slots;

CREATE VIEW View_garantia_ativo AS
SELECT a.atv_id,
       a.atv_nome,
       a.atv_patrimonio,
       a.atv_status,
       a.atv_garantia,
       DATEDIFF(a.atv_garantia, CURDATE()) AS dias_restantes,
       t.tpa_nome,
       r.rac_id,
       r.rac_nome,
       f.fil_nome,
       s.sal_nome,
       a.atv_slot_inicio,
       a.atv_slot_fim
  FROM ativo a
  JOIN tipo_ativo t   ON t.tpa_id = a.fk_tpa_id
  LEFT JOIN rack r    ON r.rac_id = a.fk_rac_id
  LEFT JOIN fileira f ON f.fil_id = r.fk_fil_id
  LEFT JOIN sala s    ON s.sal_id = f.fk_sal_id
 WHERE a.atv_garantia IS NOT NULL
   AND a.atv_status <> 'DESCARTADO';

-- Procedures
DELIMITER $$

CREATE PROCEDURE Procedure_ativos_do_rack(IN p_rac_id INT)
BEGIN
    SELECT atv_id, atv_nome, atv_status, atv_slot_inicio, atv_slot_fim
      FROM ativo
     WHERE fk_rac_id = p_rac_id
     ORDER BY atv_slot_inicio;
END$$

-- Movimentação de ativo: TRANSAÇÃO feita pelo MySQL.
CREATE PROCEDURE Procedure_movimentar_ativo(
    IN  p_atv_id         INT,
    IN  p_rac_destino_id INT,
    IN  p_slot_inicio    INT,
    IN  p_slot_fim       INT,
    IN  p_motivo         VARCHAR(512),
    IN  p_usr_id         INT,
    OUT p_mov_id         INT
)
BEGIN
    DECLARE v_existe        INT;
    DECLARE v_status        VARCHAR(32);
    DECLARE v_rac_origem    INT;
    DECLARE v_inicio_origem INT;
    DECLARE v_fim_origem    INT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    -- o ativo precisa existir
    SELECT COUNT(*) INTO v_existe FROM ativo WHERE atv_id = p_atv_id;
    IF v_existe = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Ativo não encontrado';
    END IF;


    SELECT atv_status, fk_rac_id, atv_slot_inicio, atv_slot_fim
      INTO v_status, v_rac_origem, v_inicio_origem, v_fim_origem
      FROM ativo
     WHERE atv_id = p_atv_id
       FOR UPDATE;

    IF v_status = 'DESCARTADO' THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Ativo descartado não pode ser movimentado';
    END IF;

    IF p_rac_destino_id IS NOT NULL THEN
        SELECT COUNT(*) INTO v_existe
          FROM rack
         WHERE rac_id = p_rac_destino_id
           FOR UPDATE;
        IF v_existe = 0 THEN
            SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Rack de destino não encontrado';
        END IF;
    END IF;

    INSERT INTO movimentacao (mov_motivo,
                              mov_slot_inicio_origem, mov_slot_fim_origem,
                              mov_slot_inicio_destino, mov_slot_fim_destino,
                              fk_rac_origem_id, fk_rac_destino_id, fk_atv_id, fk_usr_id)
    VALUES (p_motivo,
            v_inicio_origem, v_fim_origem,
            p_slot_inicio, p_slot_fim,
            v_rac_origem, p_rac_destino_id, p_atv_id, p_usr_id);

    SET p_mov_id = LAST_INSERT_ID();

    UPDATE ativo
       SET fk_rac_id       = p_rac_destino_id,
           atv_slot_inicio = p_slot_inicio,
           atv_slot_fim    = p_slot_fim
     WHERE atv_id = p_atv_id;

    COMMIT;
END$$

DELIMITER ;

-- ---------------------------------------------------------------------
INSERT INTO tipo_ativo (tpa_nome) VALUES
('Servidor'), ('Switch'), ('Roteador'), ('Storage'), ('Firewall'), ('PDU'), ('Patch Panel');

INSERT INTO tipo_cliente (tpc_nome) VALUES
('Corporativo'), ('Provedor'), ('Governo');

INSERT INTO usuario (usr_nome, usr_email, usr_senha_hash, usr_perfil) VALUES
('Administrador', 'admin@infratrace.local',
 'pbkdf2_sha256$600000$0dQN+p2sCSPGFAsp2mST9w==$aHUVQUr9JoZMhFJFBbaSTVovlMdXABe+GAunVFLZug4=',
 'ADMIN');
