# Missão Marte Unifor — Extensão em Java (POO)

**Disciplina:** Projeto e Arquitetura de Sistemas — UNIFOR
**Grupo:** [PREENCHER: nome do grupo]
**Integrantes:**

| Nome | Matrícula | Username Git |
|---|---|---|
| [PREENCHER] | [PREENCHER] | [PREENCHER] |
| [PREENCHER] | [PREENCHER] | [PREENCHER] |

**Link do repositório:** [PREENCHER: URL do GitHub]

---

## Sobre

Extensão do jogo em console "Missão Marte Unifor", desenvolvida sobre a base fornecida na disciplina,
implementando os exercícios dos Níveis 1 a 4 do tutorial:

- **Nível 1:** subclasse `Astronauta`, capacidade da nave ajustada, símbolos customizados no mapa (`@`, `#`, `L`).
- **Nível 2:** pontuação polimórfica por tipo de passageiro (`getPontuacao()` sobrescrito), sistema de vidas na `Nave`,
  mapa com tamanho configurável pelo jogador.
- **Nível 3:** entidade `Inimigo` com movimentação aleatória, `enum Dificuldade` (FACIL/MEDIO/DIFICIL) ajustando
  recursos e pontuação inicial, `ranking.json` expandido (dificuldade, passageiros resgatados, data/hora, tempo de jogo).
- **Nível 4 (Desafio Final):** condição de vitória por pouso na Plataforma `(0,0)` após resgatar todos os passageiros,
  menu principal interativo (jogar / ver ranking / resetar ranking / sair), estatísticas de fim de partida
  (tempo, movimentos, recorde), refatoração do `Main` em métodos coesos.

## Estrutura

```
oo-console/
├── src/
│   └── missao/
│       ├── Main.java
│       ├── Missao.java
│       ├── Nave.java
│       ├── Passageiro.java
│       ├── Professor.java
│       ├── Engenheiro.java
│       ├── Astronauta.java
│       ├── Asteroide.java
│       ├── Inimigo.java
│       └── Dificuldade.java
├── ranking.json         (gerado em tempo de execução)
├── .gitignore
└── README.md
```

## Como compilar e executar

Requer JDK 11+ instalado (`javac -version` para conferir).

```bash
# a partir da pasta oo-console/
javac -d out -encoding UTF-8 src/missao/*.java
java -cp out missao.Main
```

> Execute o terminal com locale UTF-8 para exibir acentos corretamente
> (no Windows, `chcp 65001` antes de rodar; a maioria dos terminais Linux/Mac já usa UTF-8).

## Comandos do jogo

| Tecla | Ação |
|---|---|
| `w` / `a` / `s` / `d` | Move a nave (cada movimento custa 1 ponto) |
| `c` | Embarca o passageiro na posição atual |
| `q` | Aborta a missão em andamento |

**Objetivo:** resgatar todos os passageiros e retornar com a nave até a Plataforma de Pouso `L`, na coordenada `(0,0)`.

## Legenda do mapa

`@` Nave · `P` Professor · `E` Engenheiro · `T` Astronauta · `#` Asteroide · `X` Inimigo · `L` Plataforma de Pouso · `.` Vazio

## Distribuição de tarefas

| Integrante | Exercícios / Funcionalidades desenvolvidas |
|---|---|
| [Davi Socoloski] | [Main.java] |
| [Lucas Pinheiro] | [Classes] |

*(preencher conforme a distribuição real dos commits de cada integrante)*
