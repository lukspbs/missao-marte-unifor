package missao;

import java.util.Random;

public class Inimigo {

    private int x;
    private int y;

    public Inimigo(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    // Movimenta o inimigo aleatoriamente em uma das 4 direcoes, respeitando os limites do mapa
    public void mover(Random random, int minX, int maxX, int minY, int maxY) {
        int direcao = random.nextInt(4);
        switch (direcao) {
            case 0:
                if (y - 1 >= minY) y--;
                break;
            case 1:
                if (y + 1 <= maxY) y++;
                break;
            case 2:
                if (x - 1 >= minX) x--;
                break;
            case 3:
                if (x + 1 <= maxX) x++;
                break;
        }
    }

    public boolean colideCom(Nave n) {
        return n.getX() == x && n.getY() == y;
    }
}
