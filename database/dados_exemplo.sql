
USE infratrace_v2;

INSERT INTO usuario (usr_id, usr_nome, usr_email, usr_senha_hash, usr_perfil) VALUES
(2, 'Carlos Técnico', 'tecnico@infratrace.local',
    'pbkdf2_sha256$600000$BFIKJGkkD5pulFo8eTKmsA==$KR57tgZp2bD6p87Ix4PJMi+5c24EN7fqCNbwolmuxMU=', 'TECNICO'),
(3, 'Ana Auditora',   'auditor@infratrace.local',
    'pbkdf2_sha256$600000$jJrwp3wP8dgQSRGClQO5oA==$QTap0SIipyf0ueOsZrdtECKWRPGE17sokJ4z2WGnQQg=', 'AUDITOR');

INSERT INTO sala (sal_id, sal_nome, sal_descricao) VALUES
(1, 'Sala 1 - Principal', 'Sala principal do data center'),
(2, 'Sala 2 - Expansão',  'Sala nova, ainda com pouco uso');

INSERT INTO fileira (fil_id, fil_nome, fk_sal_id) VALUES
(1, 'A', 1),
(2, 'B', 1),
(3, 'A', 2);

INSERT INTO rack (rac_id, rac_nome, rac_total_slots, fk_fil_id) VALUES
(1, 'R01', 42, 1),
(2, 'R02', 42, 1),
(3, 'R03', 24, 2),
(4, 'R04', 42, 3),   -- fica VAZIO de propósito: mostra o LEFT JOIN da View_ocupacao_rack
(5, 'R05', 12, 2);   

-- ---------------------------------------------------------------------
-- Notas fiscais de compra (dados fictícios; sem tela nesta versão,
-- servem para mostrar a FK ativo.fk_nff_id funcionando)
-- ---------------------------------------------------------------------
INSERT INTO nota_fiscal (nff_id, nff_num, nff_valor, nff_volumes, nff_razao_social, nff_cnpj, nff_descricao) VALUES
(1, 1001, 75000.00, 3, 'Fornecedor Exemplo de Servidores Ltda', '12345678000195', 'Servidores'),
(2, 2002, 98000.00, 4, 'Distribuidora Exemplo de Redes Ltda',   '98765432000198', 'Rede e storage');

-- ---------------------------------------------------------------------
-- Ativos
-- tipo_ativo (script principal): 1 Servidor, 2 Switch, 3 Roteador, 4 Storage,
--                                5 Firewall, 6 PDU, 7 Patch Panel
-- ---------------------------------------------------------------------
INSERT INTO ativo (atv_id, atv_nome, atv_fabricante, atv_modelo, atv_num_serie, atv_patrimonio, atv_status,
                   atv_data_aquisicao, atv_valor_compra, atv_garantia,
                   atv_slot_inicio, atv_slot_fim, atv_data_descarte, atv_motivo_descarte,
                   fk_tpa_id, fk_rac_id, fk_nff_id) VALUES
-- R01 (42U): 5 slots ocupados
(1,  'SRV-01',  'Dell',     'PowerEdge R750', 'SN-SRV-01', 'PAT-0001', 'ATIVO',
     DATE_SUB(CURDATE(), INTERVAL 700 DAY), 25000.00, DATE_ADD(CURDATE(), INTERVAL 20 DAY),
     1, 2, NULL, NULL, 1, 1, 1),
(2,  'SRV-02',  'Dell',     'PowerEdge R750', 'SN-SRV-02', 'PAT-0002', 'EM_MANUTENCAO',
     DATE_SUB(CURDATE(), INTERVAL 700 DAY), 25000.00, DATE_ADD(CURDATE(), INTERVAL 45 DAY),
     3, 4, NULL, NULL, 1, 1, 1),
(3,  'SW-01',   'Cisco',    'Catalyst 9300',  'SN-SW-01',  'PAT-0003', 'ATIVO',
     DATE_SUB(CURDATE(), INTERVAL 300 DAY), 18000.00, DATE_ADD(CURDATE(), INTERVAL 400 DAY),
     42, 42, NULL, NULL, 2, 1, 2),
-- R02 (42U): 5 slots ocupados
(4,  'STG-01',  'NetApp',   'AFF A250',       'SN-STG-01', 'PAT-0004', 'ATIVO',
     DATE_SUB(CURDATE(), INTERVAL 1085 DAY), 60000.00, DATE_ADD(CURDATE(), INTERVAL 10 DAY),
     1, 4, NULL, NULL, 4, 2, 2),
(5,  'FW-01',   'Fortinet', 'FortiGate 100F', 'SN-FW-01',  'PAT-0005', 'ATIVO',
     DATE_SUB(CURDATE(), INTERVAL 760 DAY), 20000.00, DATE_SUB(CURDATE(), INTERVAL 30 DAY),  -- garantia JÁ VENCIDA
     10, 10, NULL, NULL, 5, 2, 2),
-- R03 (24U): 3 slots ocupados
(6,  'PDU-01',  'APC',      'AP8868',         'SN-PDU-01', 'PAT-0006', 'ATIVO',
     DATE_SUB(CURDATE(), INTERVAL 400 DAY), 4500.00, NULL,                                    -- sem garantia cadastrada
     1, 1, NULL, NULL, 6, 3, NULL),
(10, 'RT-01',   'Juniper',  'MX204',          'SN-RT-01',  'PAT-0010', 'ATIVO',
     DATE_SUB(CURDATE(), INTERVAL 165 DAY), 30000.00, DATE_ADD(CURDATE(), INTERVAL 200 DAY),
     10, 11, NULL, NULL, 3, 3, NULL),
-- R05 (12U): 10 slots ocupados
(9,  'PP-01',   'Furukawa', 'Patch Panel 48p', 'SN-PP-01', 'PAT-0009', 'ATIVO',
     DATE_SUB(CURDATE(), INTERVAL 500 DAY), 1200.00, NULL,
     1, 10, NULL, NULL, 7, 5, NULL),
-- No ESTOQUE (sem rack): aparece na View_garantia_ativo com local NULL
(7,  'SRV-03',  'HPE',      'ProLiant DL380', 'SN-SRV-03', 'PAT-0007', 'ATIVO',
     DATE_SUB(CURDATE(), INTERVAL 340 DAY), 28000.00, DATE_ADD(CURDATE(), INTERVAL 25 DAY),
     NULL, NULL, NULL, NULL, 1, NULL, 1),
-- DESCARTADO: a View_garantia_ativo não mostra (WHERE atv_status <> 'DESCARTADO')
(8,  'SRV-OLD', 'Dell',     'PowerEdge R720', 'SN-SRV-OLD', 'PAT-0008', 'DESCARTADO',
     DATE_SUB(CURDATE(), INTERVAL 2500 DAY), 15000.00, DATE_ADD(CURDATE(), INTERVAL 5 DAY),
     NULL, NULL, DATE_SUB(CURDATE(), INTERVAL 60 DAY), 'Fim de vida útil',
     1, NULL, NULL);

-- ---------------------------------------------------------------------
-- Histórico de movimentações (já refletido na posição atual dos ativos acima)
-- fk_usr_id = quem fez a movimentação
-- ---------------------------------------------------------------------
INSERT INTO movimentacao (mov_id, mov_data, mov_motivo,
                          mov_slot_inicio_origem, mov_slot_fim_origem,
                          mov_slot_inicio_destino, mov_slot_fim_destino,
                          fk_rac_origem_id, fk_rac_destino_id, fk_atv_id, fk_usr_id) VALUES
(1, DATE_SUB(NOW(), INTERVAL 10 DAY), 'Reorganização da fileira A', 10, 10, 10, 10, 1, 2, 5, 2),
(2, DATE_SUB(NOW(), INTERVAL 5 DAY),  'Retirado para upgrade de memória', 5, 6, NULL, NULL, 3, NULL, 7, 2),
(3, DATE_SUB(NOW(), INTERVAL 60 DAY), 'Descarte: Fim de vida útil', 20, 21, NULL, NULL, 2, NULL, 8, 1);

-- ---------------------------------------------------------------------
-- Clientes e aluguéis (tipo_cliente: 1 Corporativo, 2 Provedor, 3 Governo)
-- Os CNPJs abaixo passam na validação dos dígitos verificadores.
-- ---------------------------------------------------------------------
INSERT INTO cliente (cli_id, cli_nome, cli_cnpj, cli_representante, cli_ativo, fk_tpc_id) VALUES
(1, 'Alfa Tecnologia Ltda',      '11222333000181', 'Mariana Souza', TRUE,  1),
(2, 'Beta Provedor de Internet', '44555666000181', 'Roberto Lima',  TRUE,  2),
(3, 'Órgão Público Exemplo',     '77888999000181', 'Paulo Mendes',  FALSE, 3);   -- inativo

-- Um contrato em cada situação, para o módulo de Aluguéis e os relatórios:
INSERT INTO aluguel (alu_id, alu_data_inicio, alu_data_fim, alu_valor, fk_cli_id, fk_rac_id) VALUES
(1, DATE_SUB(CURDATE(), INTERVAL 180 DAY), DATE_ADD(CURDATE(), INTERVAL 10 DAY),  3000.00, 1, 3),  -- VIGENTE, vence em 10 dias
(2, DATE_SUB(CURDATE(), INTERVAL 30 DAY),  DATE_ADD(CURDATE(), INTERVAL 335 DAY), 1800.00, 2, 5),  -- VIGENTE
(3, DATE_SUB(CURDATE(), INTERVAL 400 DAY), DATE_SUB(CURDATE(), INTERVAL 40 DAY),  2500.00, 3, 1),  -- ENCERRADO (cliente hoje inativo)
(4, DATE_ADD(CURDATE(), INTERVAL 11 DAY),  DATE_ADD(CURDATE(), INTERVAL 376 DAY), 3200.00, 2, 3);  -- FUTURO: começa no dia seguinte ao fim do contrato 1, no mesmo rack (não sobrepõe)

-- Conferência rápida
SELECT * FROM View_ocupacao_rack;
SELECT * FROM View_garantia_ativo ORDER BY dias_restantes;
