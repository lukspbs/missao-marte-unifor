package missao;

import java.util.ArrayList;
import java.util.List;

public class Nave {

    private String id;
    private int x;
    private int y;
    private int capacidade;
    private int vidas;
    private List<Passageiro> passageiros = new ArrayList<>();

    public Nave(String id, int capacidade) {
        this.id = id;
        this.capacidade = capacidade;
        this.x = 0;
        this.y = 0;
        this.vidas = 3;
    }

    public String getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public int getVidas() {
        return vidas;
    }

    public List<Passageiro> getPassageiros() {
        return passageiros;
    }

    // Movimentos livres (sem checagem de limite)
    public void moveUp() {
        y--;
    }

    public void moveDown() {
        y++;
    }

    public void moveLeft() {
        x--;
    }

    public void moveRight() {
        x++;
    }

    // Movimento respeitando os limites do mapa configurável (Exercicio 6 - Nivel 2)
    public void moverComLimites(char comando, int minX, int maxX, int minY, int maxY) {
        switch (comando) {
            case 'w':
                if (y - 1 >= minY) y--;
                break;
            case 's':
                if (y + 1 <= maxY) y++;
                break;
            case 'a':
                if (x - 1 >= minX) x--;
                break;
            case 'd':
                if (x + 1 <= maxX) x++;
                break;
        }
    }

    // Sistema de vidas (Exercicio 5 - Nivel 2)
    public void perderVida() {
        if (vidas > 0) {
            vidas--;
        }
    }

    public boolean embarcar(Passageiro p) {
        if (passageiros.size() < capacidade) {
            passageiros.add(p);
            return true;
        }
        return false;
    }
}
