# Guia do NetBeans — como trabalhar no InfraTrace v2

Passo a passo para abrir, executar, navegar e criar classes no projeto Java pelo NetBeans.
Vale para qualquer máquina do grupo (NetBeans 21 ou mais novo, rodando com JDK 21 ou mais novo).

## 1. Antes de abrir o NetBeans

| Item | Como conferir |
|---|---|
| MySQL ligado | Serviço **MySQL80** em execução (Workbench conecta normalmente) |
| Banco criado | No Workbench, `database/infratrace_v2.sql` e depois `database/dados_exemplo.sql` já foram executados (o banco `infratrace_v2` aparece em *Schemas*) |
| `.env` preenchido | Arquivo `infratrace_v2/.env` existe e `INFRATRACE_DB_PASSWORD=` tem a senha do **seu** MySQL |
| Python (só para o relatório de garantias) | Python 3.12+ instalado e a API configurada uma vez ([etapa-6.7.md, seção 6](etapa-6.7.md#6-como-rodar-a-api)). Sem ele, os cadastros funcionam normalmente |

Para editar o `.env`, use o Bloco de Notas ou, no NetBeans, *File → Open File...* e escolha
`infratrace_v2\.env`. Ele fica **fora** da pasta do projeto Java, por isso não aparece na janela
*Projects*. Isso é de propósito: a senha fica longe do código.

> O `.env` não vai para o Git (está no `.gitignore`). Nunca coloquem a senha em um arquivo `.java`.

## 2. Abrir o projeto

1. *File → Open Project...* (Ctrl+Shift+O).
2. Entrem em `infratrace_v2\java\` e selecionem a pasta **`InfraTrace`**: o ícone é o de projeto
   Maven (um "ma" pequeno). Cliquem em **Open Project**.
   - Não abram a pasta `infratrace_v2` inteira: ela não é um projeto do NetBeans, só a pasta do repositório.
3. Na primeira vez, o NetBeans lê o `pom.xml` e baixa o driver do MySQL e o Gson (barra de
   progresso embaixo, à direita). Esperem terminar.
4. Se o projeto aparecer com um **ponto de exclamação**, botão direito nele → *Resolve Problems...*,
   ou *Clean and Build* (Shift+F11) com a internet ligada.

**Dica: deixe o InfraTrace como projeto principal.** Se houver outros projetos abertos (de outras
disciplinas), o F6 pode executar o projeto errado. Use *Run → Set Main Project → InfraTrace*
ou feche os outros (botão direito → *Close*).

## 3. Executar

| Para | Faça | O que acontece |
|---|---|---|
| Abrir o sistema | **F6** (*Run Project*) ou o botão ▶ verde | Abre a janela do InfraTrace (classe `Infratrace.App`, definida em `exec.mainClass` no `pom.xml`) |
| Usar o relatório de garantias | Antes do F6, iniciar a API Python num terminal (seção 9) | O botão **Relatório de garantias** da aba Ativo passa a funcionar |
| Testar só a conexão | Botão direito em `TesteConexao.java` → *Run File* (Shift+F6) | Roda o teste da etapa 6.2 no console (aba *Output*) |
| Parar o programa | Fechar a janela do InfraTrace, ou o quadrado vermelho da aba *Output* | O Maven termina com `BUILD SUCCESS` |
| Recompilar tudo do zero | *Clean and Build* (Shift+F11) | Apaga a pasta `target/` e compila de novo |

Conferência rápida depois do F6: a janela abre com as abas **Sala, Fileira, Rack, Ativo e
Tipo Ativo**. A aba *Sala* mostra 2 salas e a aba *Fileira*, 3 fileiras.

Cuidado: cada F6 abre **mais uma** janela. Fechem a anterior antes de rodar de novo.

## 4. Onde está cada coisa (janela *Projects*)

```
InfraTrace
└─ Source Packages
   ├─ Infratrace                     App (abre o sistema)
   ├─ Configuracao                   Configuracao (lê o .env), Conexao (abre a conexão JDBC), Sessao (usuário atual) e TesteConexao (teste da 6.2)
   ├─ Model                          Sala, Fileira, Rack, Ativo, TipoAtivo... (os dados de uma linha da tabela)
   ├─ Dao                            SalaDAO, AtivoDAO... (o SQL; recebem a Connection)
   ├─ Services                       SalaService, AtivoService... (regras, validações e transações)
   ├─ Controllers                    SalaController, AtivoController... (ligam tela e service)
   ├─ Exceptions                     NegocioException e ErrosBanco (códigos do MySQL)
   ├─ View                           TelaPrincipal, PainelSalas, PainelAtivos... (Swing)
   ├─ br.com.infratrace.integracao   RelatorioApi (chama a API Python por HTTP)
   └─ br.com.infratrace.util         Formatos (datas dd/mm/aaaa e valores 25.000,00)
└─ Dependencies                      mysql-connector-j (driver JDBC) e gson (lê o JSON da API), baixados pelo Maven
└─ Project Files                     pom.xml
```

Atalhos que ajudam a ler o código:

| Atalho | O que faz |
|---|---|
| Ctrl + clique num nome | Vai para onde a classe ou o método foi escrito |
| Alt+F7 | *Find Usages*: mostra quem usa aquele método ou classe |
| Ctrl+7 | Janela *Navigator*: lista os métodos da classe aberta |
| Ctrl+O | Abre uma classe pelo nome (ex.: digite `FileiraDAO`) |
| Ctrl+Espaço | Completa o código (métodos, variáveis) |
| Ctrl+Shift+I | Corrige os `import` que faltam |
| Alt+Insert | Gera construtor, getters/setters e `toString` (ótimo para classes do `model`) |

## 5. Criar ou alterar classes

**Classe nova** (ex.: um `model` novo):

1. Botão direito no pacote certo (ex.: `Model`) → *New → Java Class...*
2. *Class Name*: o nome da classe, sem `.java` → **Finish**. O pacote já vem preenchido.
3. Escrevam os atributos e usem **Alt+Insert → Getter and Setter...** para gerar os métodos.
4. Sigam a ordem das camadas: `model` → `dao` → `service` → `controller` → `view`.
5. Para uma tela nova aparecer, acrescentem uma aba no construtor da `TelaPrincipal`
   (ex.: `abas.addTab("Nome da aba", new PainelNovo());`).

**Alterar uma classe existente** (ex.: o exercício da coluna "Livres (U)",
[etapa-6.5.md, seção 6](etapa-6.5.md#6-exercício-do-grupo-coluna-livres-u)): Ctrl+O → digitem
`RackDAO` → Enter, e usem Alt+Insert dentro do `Rack` para gerar o get/set do atributo novo.

Erro de compilação aparece sublinhado em vermelho, com um ícone na margem esquerda. Passar
o mouse em cima mostra a mensagem. A lâmpada amarela (Alt+Enter) sugere correções.

## 6. Depurar (ver o programa rodando linha por linha)

Muito útil para entender o caminho tela → controller → service → dao:

1. Cliquem no **número da linha** para marcar um *breakpoint* (quadrado vermelho). Exemplo:
   primeira linha do método `salvar` do `FileiraService`.
2. *Debug Project* (Ctrl+F5). O sistema abre normalmente.
3. Façam a ação na tela (ex.: salvar uma fileira). O NetBeans para na linha marcada.
4. **F8** executa a linha e passa para a próxima; **F7** entra no método chamado; **F5** continua.
5. A janela *Variables* mostra o valor de cada variável naquele momento (ex.: o objeto `fileira`).

## 7. Erros comuns

| Sintoma | Causa | Solução |
|---|---|---|
| Janela mostra "Configuração 'INFRATRACE_DB_HOST' não encontrada" | Falta o `.env` na raiz | Copiar `.env.example` para `.env` |
| "Usuário ou senha do MySQL incorretos" (1045) | Senha errada ou vazia no `.env` | Corrigir `INFRATRACE_DB_PASSWORD` e rodar de novo (não precisa recompilar) |
| "O banco de dados não existe" (1049) | Script do banco não foi executado | Rodar `database/infratrace_v2.sql` no Workbench |
| "Não foi possível conectar ao MySQL" | Serviço MySQL80 parado ou porta errada | Ligar o serviço / conferir `INFRATRACE_DB_PORT` |
| F6 abre outro projeto | Outro projeto é o principal ou está selecionado | *Run → Set Main Project → InfraTrace* |
| NetBeans pede *Select Main Class* | O `exec.mainClass` não foi lido | Escolher `Infratrace.App` |
| Relatório: "A API Python não está respondendo" | A API não foi iniciada | Seção 9: rodar `python app.py` e deixar o terminal aberto |
| `release version 21 not supported` | O NetBeans está usando um JDK antigo | *Tools → Java Platforms*: instalar/escolher JDK 21+; em *Properties → Build → Compile*, escolher esse JDK |
| Acentos estranhos na aba *Output* (`Expans�o`) | Codificação do console | Não afeta o banco nem as telas Swing |
| Mudei o código e nada mudou | Programa antigo ainda aberto | Fechar a janela antiga e rodar de novo |

## 8. O que **não** fazer pelo NetBeans (por enquanto)

- **Commit/push** pelo menu *Team*: o repositório Git será criado depois, com o grupo combinando
  o que entra no primeiro commit.
- **GUI Builder** (*New → JFrame Form*): as telas deste projeto são escritas em código, no padrão
  do `PainelBase`. Telas geradas pelo GUI Builder criam arquivos `.form` e código bloqueado
  para edição, e ficariam diferentes das outras.
- Arquivos que o NetBeans cria sozinho (`nbactions.xml`, `nb-configuration.xml`,
  `nbproject/private/`) e a pasta `target/` estão no `.gitignore`. Podem existir na máquina,
  só não vão para o repositório.

## 9. A parte em Python (pasta `python`)

O NetBeans **não executa Python** e a pasta `python` fica fora do projeto Java, então ela não
aparece em *Projects*. Mas dá para trabalhar com ela sem sair do NetBeans:

| Para | Faça |
|---|---|
| Ver e editar os `.py` | *Window → Favorites* (Ctrl+3) → botão direito → *Add to Favorites...* → escolher `infratrace_v2\python` → **Add**. A pasta fica na *Favorites* nas próximas vezes |
| Abrir um terminal | *Window → IDE Tools → Terminal* |
| Instalar (só na primeira vez) | No terminal: `cd infratrace_v2\python`, `python -m venv .venv`, `.venv\Scripts\activate`, `pip install -r requirements.txt` |
| Iniciar a API | `cd infratrace_v2\python`, `.venv\Scripts\activate`, `python app.py` (deixar aberto; Ctrl+C para parar) |
| Rodar os testes do Python | `python -m unittest -v` na pasta `python` (com o `.venv` ativado). Esperado: `Ran 8 tests ... OK` |
| Ver o JSON da API | Com a API rodando, abrir no navegador `http://127.0.0.1:5000/relatorios/garantias?dias=90` |

O NetBeans abre os `.py` como texto comum. Para cores, autocompletar e depurador de Python,
usem o VS Code com a extensão Python. Detalhes, testes e erros comuns:
[etapa-6.7.md](etapa-6.7.md).
