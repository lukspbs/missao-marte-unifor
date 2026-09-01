package missao;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {

    private static final Path RANKING_PATH = Paths.get("ranking.json");

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();
        List<RankingEntry> ranking = loadRanking(RANKING_PATH);

        exibirBoasVindas();

        boolean rodando = true;
        while (rodando) {
            exibirMenu();
            String opcao = lerLinha(scanner, "Escolha uma opção: ", "1");

            switch (opcao) {
                case "1":
                    jogarPartida(scanner, random, ranking);
                    ranking = loadRanking(RANKING_PATH);
                    break;
                case "2":
                    exibirRankingCompleto(ranking);
                    break;
                case "3":
                    ranking = resetarRanking(scanner);
                    break;
                case "4":
                    rodando = false;
                    System.out.println("\nObrigado por jogar a Missão Marte Unifor!");
                    break;
                default:
                    System.out.println("Opção inválida. Tente novamente.");
            }
        }

        scanner.close();
    }

    private static void exibirBoasVindas() {
        System.out.println("================================================================");
        System.out.println("             MISSÃO MARTE UNIFOR - VERSÃO COMPLETA              ");
        System.out.println("================================================================");
    }

    private static void exibirMenu() {
        System.out.println("\n--- MENU PRINCIPAL ---");
        System.out.println("1. Iniciar Nova Missão");
        System.out.println("2. Visualizar Ranking Top 5");
        System.out.println("3. Resetar Histórico de Ranking");
        System.out.println("4. Sair do Jogo");
        System.out.println("----------------------");
    }

    private static void jogarPartida(Scanner scanner, Random random, List<RankingEntry> ranking) {
        String pilotoNome = lerLinha(scanner, "\nDigite o nome do piloto: ", "Piloto Anônimo");
        if (pilotoNome.isEmpty()) {
            pilotoNome = "Piloto Anônimo";
        }

        Dificuldade dificuldade = lerDificuldade(scanner);
        int tamanhoMapa = lerTamanhoMapa(scanner);
        int minX = -tamanhoMapa;
        int maxX = tamanhoMapa;
        int minY = -tamanhoMapa;
        int maxY = tamanhoMapa;

        System.out.println("\nDecolagem em 3... 2... 1... Pressione Enter para iniciar!");
        scanner.nextLine();

        Missao missao = criarNovaMissao(random, minX, maxX, minY, maxY, dificuldade);
        Nave nave = missao.getNave();
        int score = definirPontuacaoInicial(dificuldade);
        int movimentos = 0;
        boolean partidaAtiva = true;
        long tempoInicio = System.currentTimeMillis();

        while (partidaAtiva) {
            desenharMapa(missao, minX, maxX, minY, maxY, score, pilotoNome);
            System.out.printf("Nave em (%d,%d) | Pontos: %d | Vidas: %d | A bordo: %d/%d | Restantes: %d%n",
                    nave.getX(), nave.getY(), score, nave.getVidas(),
                    nave.getPassageiros().size(), nave.getCapacidade(),
                    missao.todosEmbarcados() ? 0 : missao.getPassageiros().size());

            String comandoStr = lerLinha(scanner, "Comando (w/s/a/d/c/q): ", "").toLowerCase();
            if (comandoStr.isEmpty()) continue;
            char cmd = comandoStr.charAt(0);

            if (cmd == 'q') {
                System.out.println("Missão abortada.");
                partidaAtiva = false;
                break;
            } else if (cmd == 'c') {
                Passageiro p = missao.passagemNaPosicao();
                if (p == null) {
                    System.out.println("Nenhum passageiro nesta posição.");
                } else {
                    boolean embarcou = missao.embarcarPassageiroNaPosicao();
                    if (embarcou) {
                        int bonus = p.getPontuacao();
                        score += bonus;
                        System.out.printf("Passageiro %s embarcado! +%d pontos!%n", p.getNome(), bonus);
                    } else {
                        System.out.println("Nave cheia! Não foi possível embarcar.");
                    }
                }
            } else if (cmd == 'w' || cmd == 's' || cmd == 'a' || cmd == 'd') {
                nave.moverComLimites(cmd, minX, maxX, minY, maxY);
                score--;
                movimentos++;
            } else {
                System.out.println("Comando desconhecido.");
                continue;
            }

            missao.moverInimigos(random, minX, maxX, minY, maxY);

            if (missao.verificaColisao()) {
                nave.perderVida();
                if (nave.getVidas() > 0) {
                    System.out.printf("Colisão! Vidas restantes: %d%n", nave.getVidas());
                } else {
                    System.out.println("GAME OVER! A nave foi destruída por completo.");
                    partidaAtiva = false;
                }
            }

            if (score <= 0) {
                System.out.println("Pontuação zerada. Fim de missão por falta de combustível.");
                partidaAtiva = false;
            }

            if (missao.todosEmbarcados() && partidaAtiva) {
                if (nave.getX() == 0 && nave.getY() == 0) {
                    long tempoFim = System.currentTimeMillis();
                    long tempoJogoSegundos = (tempoFim - tempoInicio) / 1000;

                    System.out.println("\n================================================================");
                    System.out.println("🚀 DECOLAGEM AUTORIZADA! Nave acoplada à plataforma em (0,0).");
                    System.out.println("Retornando à órbita marciana com todos os passageiros. Missão cumprida!");
                    System.out.println("================================================================");

                    exibirEstatisticas(score, movimentos, tempoJogoSegundos, nave.getPassageiros().size(), ranking);

                    if (score > 0 && isTopScore(ranking, score)) {
                        RankingEntry novaEntrada = new RankingEntry(
                                pilotoNome,
                                score,
                                dificuldade,
                                nave.getPassageiros().size(),
                                java.time.LocalDateTime.now().toString().substring(0, 19).replace('T', ' '),
                                tempoJogoSegundos
                        );
                        ranking.add(novaEntrada);
                        List<RankingEntry> rankingFiltrado = ranking.stream()
                                .sorted(Comparator.comparingInt((RankingEntry e) -> e.score).reversed())
                                .limit(5)
                                .collect(Collectors.toList());
                        saveRanking(RANKING_PATH, rankingFiltrado);
                        System.out.println("Parabéns! Novo registro salvo no ranking!");
                    }
                    partidaAtiva = false;
                } else {
                    System.out.println("✨ ALERTA: Todos os passageiros resgatados! Retorne para a Plataforma de Pouso 'L' em (0,0) para completar a missão.");
                }
            }
        }
    }

    private static void exibirEstatisticas(int score, int movimentos, long tempoSegundos, int passageiros, List<RankingEntry> ranking) {
        System.out.println("\n================ ESTATÍSTICAS ================");
        System.out.printf(" - Pontuação Concluída: %d pontos%n", score);
        System.out.printf(" - Movimentos Efetuados: %d%n", movimentos);
        System.out.printf(" - Duração da Partida: %d segundos%n", tempoSegundos);
        System.out.printf(" - Passageiros Coletados: %d%n", passageiros);

        int recorde = ranking.isEmpty() ? 0 : ranking.get(0).score;
        if (score > recorde && recorde > 0) {
            System.out.println("🏆 Novo Recorde do Servidor!");
        } else if (recorde > 0) {
            System.out.printf(" - Recorde atual: %d pontos (Piloto: %s)%n", recorde, ranking.get(0).name);
        }
        System.out.println("==============================================");
    }

    private static Dificuldade lerDificuldade(Scanner scanner) {
        System.out.print("Escolha a Dificuldade (facil/medio/dificil): ");
        String difStr = lerLinha(scanner, "", "medio");
        return Dificuldade.deString(difStr);
    }

    private static int lerTamanhoMapa(Scanner scanner) {
        try {
            int tamanho = Integer.parseInt(lerLinha(scanner, "Tamanho do mapa (ex: 5 para mapa de -5 a +5): ", "5"));
            return tamanho > 0 ? tamanho : 5;
        } catch (NumberFormatException e) {
            System.out.println("Entrada inválida. Usando mapa padrão (5).");
            return 5;
        }
    }

    private static int definirPontuacaoInicial(Dificuldade dificuldade) {
        switch (dificuldade) {
            case FACIL: return 30;
            case DIFICIL: return 15;
            default: return 20;
        }
    }

    private static Missao criarNovaMissao(Random random, int minX, int maxX, int minY, int maxY, Dificuldade dificuldade) {
        Nave nave = new Nave("A-1", 5);
        Missao missao = new Missao(nave);

        int qtdPassageiros = 5;
        int qtdAsteroides = 2;
        int qtdInimigos = 2;

        if (dificuldade == Dificuldade.FACIL) {
            qtdPassageiros = 4;
            qtdAsteroides = 1;
            qtdInimigos = 1;
        } else if (dificuldade == Dificuldade.DIFICIL) {
            qtdPassageiros = 5;
            qtdAsteroides = 3;
            qtdInimigos = 3;
        }

        while (missao.getPassageiros().size() < qtdPassageiros) {
            int x = random.nextInt(maxX - minX + 1) + minX;
            int y = random.nextInt(maxY - minY + 1) + minY;
            if (x == nave.getX() && y == nave.getY()) continue;
            if (posicaoOcupada(missao, x, y)) continue;
            int index = missao.getPassageiros().size();
            missao.addPassageiro(criarPassageiroPolimorfico(index, x, y));
        }

        while (missao.getAsteroides().size() < qtdAsteroides) {
            int x = random.nextInt(maxX - minX + 1) + minX;
            int y = random.nextInt(maxY - minY + 1) + minY;
            if (x == nave.getX() && y == nave.getY()) continue;
            if (posicaoOcupada(missao, x, y)) continue;
            missao.addAsteroide(new Asteroide(x, y));
        }

        while (missao.getInimigos().size() < qtdInimigos) {
            int x = random.nextInt(maxX - minX + 1) + minX;
            int y = random.nextInt(maxY - minY + 1) + minY;
            if (x == nave.getX() && y == nave.getY()) continue;
            if (posicaoOcupada(missao, x, y)) continue;
            missao.addInimigo(new Inimigo(x, y));
        }

        return missao;
    }

    private static Passageiro criarPassageiroPolimorfico(int indice, int x, int y) {
        switch (indice % 5) {
            case 0: return new Professor("Dr. Silva", x, y);
            case 1: return new Engenheiro("Eng. Rosa", x, y);
            case 2: return new Professor("Dr. Lima", x, y);
            case 3: return new Engenheiro("Eng. Carlos", x, y);
            default: return new Astronauta("Ast. Maria", x, y);
        }
    }

    private static boolean posicaoOcupada(Missao missao, int x, int y) {
        if (missao.getNave().getX() == x && missao.getNave().getY() == y) return true;

        for (Passageiro p : missao.getPassageiros()) {
            if (p.getX() == x && p.getY() == y) return true;
        }
        for (Asteroide a : missao.getAsteroides()) {
            if (a.getX() == x && a.getY() == y) return true;
        }
        for (Inimigo i : missao.getInimigos()) {
            if (i.getX() == x && i.getY() == y) return true;
        }
        return false;
    }

    private static void desenharMapa(Missao missao, int minX, int maxX, int minY, int maxY, int score, String pilotoNome) {
        System.out.println();
        System.out.printf("Mapa da Missão (Pontos: %d) - Piloto: %s%n", score, pilotoNome);

        System.out.print("    ");
        for (int x = minX; x <= maxX; x++) {
            System.out.printf(" %2d", x);
        }
        System.out.println();

        System.out.print("    ");
        for (int x = minX; x <= maxX; x++) {
            System.out.print(" __");
        }
        System.out.println();

        for (int y = minY; y <= maxY; y++) {
            System.out.printf("%3d|", y);
            for (int x = minX; x <= maxX; x++) {
                char symbol = '.';

                if (missao.getNave().getX() == x && missao.getNave().getY() == y) {
                    symbol = '@';
                } else {
                    for (Passageiro p : missao.getPassageiros()) {
                        if (p.getX() == x && p.getY() == y) {
                            if (p instanceof Engenheiro) {
                                symbol = 'E';
                            } else if (p instanceof Astronauta) {
                                symbol = 'T';
                            } else {
                                symbol = 'P';
                            }
                            break;
                        }
                    }
                    if (symbol == '.') {
                        for (Asteroide a : missao.getAsteroides()) {
                            if (a.getX() == x && a.getY() == y) {
                                symbol = '#';
                                break;
                            }
                        }
                    }
                    if (symbol == '.') {
                        for (Inimigo i : missao.getInimigos()) {
                            if (i.getX() == x && i.getY() == y) {
                                symbol = 'X';
                                break;
                            }
                        }
                    }
                    if (symbol == '.' && x == 0 && y == 0) {
                        symbol = 'L';
                    }
                }

                System.out.printf(" %2c", symbol);
            }
            System.out.println();
        }
        System.out.println("Legenda: @=Nave, P=Professor, E=Engenheiro, T=Astronauta, #=Asteroide, X=Inimigo, L=Plataforma de Pouso, .=Vazio");
        System.out.println("Comandos: w/s/a/d (mover), c (embarcar), q (sair)");
    }

    private static String lerLinha(Scanner scanner, String prompt, String fallback) {
        if (prompt != null && !prompt.isEmpty()) {
            System.out.print(prompt);
        }
        if (scanner.hasNextLine()) {
            return scanner.nextLine().trim();
        }
        return fallback;
    }

    private static void exibirRankingCompleto(List<RankingEntry> ranking) {
        System.out.println("\n====== RANKING TOP 5 PILOTOS ======");
        if (ranking.isEmpty()) {
            System.out.println(" - Nenhum registro encontrado. Seja o primeiro a jogar!");
        } else {
            int pos = 1;
            for (RankingEntry entry : ranking) {
                System.out.printf("%d. %s - %d pts | Dificuldade: %s | Coletados: %d | Tempo: %ds | %s%n",
                        pos++, entry.name, entry.score, entry.dificuldade, entry.passageirosColetados, entry.tempoJogo, entry.dataHora);
            }
        }
        System.out.println("===================================");
    }

    private static List<RankingEntry> resetarRanking(Scanner scanner) {
        System.out.print("Você realmente deseja limpar o histórico de ranking? (s/n): ");
        String confirmacao = lerLinha(scanner, "", "n").toLowerCase();
        if (confirmacao.equals("s") || confirmacao.equals("sim")) {
            try {
                Files.deleteIfExists(RANKING_PATH);
                System.out.println("Histórico de ranking resetado!");
            } catch (IOException e) {
                System.out.println("Erro ao deletar ranking: " + e.getMessage());
            }
            return new ArrayList<>();
        }
        System.out.println("Operação cancelada.");
        return loadRanking(RANKING_PATH);
    }

    private static boolean isTopScore(List<RankingEntry> ranking, int score) {
        if (ranking.size() < 5) {
            return true;
        }
        return score > ranking.get(ranking.size() - 1).score;
    }

    private static List<RankingEntry> loadRanking(Path path) {
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        try {
            String json = new String(Files.readAllBytes(path), StandardCharsets.UTF_8).trim();
            return parseRankingJson(json);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private static void saveRanking(Path path, List<RankingEntry> ranking) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < ranking.size(); i++) {
            RankingEntry entry = ranking.get(i);
            builder.append("{\"name\":\"")
                    .append(entry.name.replace("\"", "\\\""))
                    .append("\",\"score\":")
                    .append(entry.score)
                    .append(",\"dificuldade\":\"")
                    .append(entry.dificuldade.name())
                    .append("\",\"passageirosColetados\":")
                    .append(entry.passageirosColetados)
                    .append(",\"dataHora\":\"")
                    .append(entry.dataHora)
                    .append("\",\"tempoJogo\":")
                    .append(entry.tempoJogo)
                    .append("}");
            if (i < ranking.size() - 1) {
                builder.append(",");
            }
        }
        builder.append("]");
        try {
            Files.write(path, builder.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.out.println("Não foi possível salvar o ranking: " + e.getMessage());
        }
    }

    private static List<RankingEntry> parseRankingJson(String json) {
        List<RankingEntry> ranking = new ArrayList<>();
        if (json.isEmpty() || json.equals("[]")) {
            return ranking;
        }
        json = json.trim();
        if (json.startsWith("[")) {
            json = json.substring(1);
        }
        if (json.endsWith("]")) {
            json = json.substring(0, json.length() - 1);
        }

        int index = 0;
        while (index < json.length()) {
            int start = json.indexOf('{', index);
            if (start < 0) break;
            int end = json.indexOf('}', start);
            if (end < 0) break;
            String object = json.substring(start + 1, end);

            String name = null;
            Integer score = null;
            Dificuldade dificuldade = Dificuldade.MEDIO;
            Integer passageirosColetados = 0;
            String dataHora = "";
            long tempoJogo = 0;

            for (String part : object.split(",")) {
                String[] pair = part.split(":", 2);
                if (pair.length != 2) continue;
                String key = pair[0].trim().replaceAll("\"", "");
                String value = pair[1].trim();

                if (key.equals("name")) {
                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        name = value.substring(1, value.length() - 1).replace("\\\"", "\"");
                    }
                } else if (key.equals("score")) {
                    try {
                        score = Integer.parseInt(value);
                    } catch (NumberFormatException ignored) {}
                } else if (key.equals("dificuldade")) {
                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        dificuldade = Dificuldade.deString(value.substring(1, value.length() - 1));
                    }
                } else if (key.equals("passageirosColetados")) {
                    try {
                        passageirosColetados = Integer.parseInt(value);
                    } catch (NumberFormatException ignored) {}
                } else if (key.equals("dataHora")) {
                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        dataHora = value.substring(1, value.length() - 1);
                    }
                } else if (key.equals("tempoJogo")) {
                    try {
                        tempoJogo = Long.parseLong(value);
                    } catch (NumberFormatException ignored) {}
                }
            }

            if (name != null && score != null) {
                ranking.add(new RankingEntry(name, score, dificuldade, passageirosColetados, dataHora, tempoJogo));
            }
            index = end + 1;
        }

        ranking.sort(Comparator.comparingInt((RankingEntry e) -> e.score).reversed());
        return ranking;
    }

    private static class RankingEntry {
        private final String name;
        private final int score;
        private final Dificuldade dificuldade;
        private final int passageirosColetados;
        private final String dataHora;
        private final long tempoJogo;

        private RankingEntry(String name, int score, Dificuldade dificuldade, int passageirosColetados, String dataHora, long tempoJogo) {
            this.name = name;
            this.score = score;
            this.dificuldade = dificuldade;
            this.passageirosColetados = passageirosColetados;
            this.dataHora = dataHora;
            this.tempoJogo = tempoJogo;
        }
    }
}
