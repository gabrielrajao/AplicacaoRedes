import Servidor.ServidorTCP;
import Servidor.ServidorUDP;

class Servidor {
    public static void main(String argv[]) {
        ServidorTCP serverTCP = new ServidorTCP();
        ServidorUDP serverUDP = new ServidorUDP();

        new Thread(serverTCP).start();
        new Thread(serverUDP).start();
    }
}
