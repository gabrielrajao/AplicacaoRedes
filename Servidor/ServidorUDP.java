package Servidor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class ServidorUDP implements Runnable {

    private static final int PORTA = 9876;
    private static final char SEP = 0x1F;
    private static final HashMap<String, String> mapaUsuarios = new HashMap<>();

    public void run() {
        try (DatagramSocket socket = new DatagramSocket(PORTA)) {
            System.out.println("Servidor UDP rodando na porta " + PORTA);

            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket pacoteRecebido = new DatagramPacket(buffer, buffer.length);
                socket.receive(pacoteRecebido);

                // Cria uma thread por "cliente" UDP (na verdade, por pacote recebido)
                new Thread(new ClienteUDPHandler(socket, pacoteRecebido)).start();
            }

        } catch (IOException e) {
            System.err.println("Erro no servidor UDP: " + e.getMessage());
        }
    }

    private static synchronized void imprimirTabela() {
        System.out.println("\n📋 Tabela atual de registros:");
        if (mapaUsuarios.isEmpty()) {
            System.out.println("   (vazia)");
            return;
        }
        for (Map.Entry<String, String> entry : mapaUsuarios.entrySet()) {
            System.out.println("   " + entry.getKey() + " → " + entry.getValue());
        }
        System.out.println();
    }

    static class ClienteUDPHandler implements Runnable {
        private final DatagramSocket socket;
        private final DatagramPacket pacote;

        ClienteUDPHandler(DatagramSocket socket, DatagramPacket pacote) {
            this.socket = socket;
            this.pacote = pacote;
        }

        public void run() {
            try {
                String mensagem = new String(pacote.getData(), 0, pacote.getLength());
                InetAddress endereco = pacote.getAddress();
                int porta = pacote.getPort();
                String remetenteChave = endereco.getHostAddress() + ":" + porta;

                System.out.println("Recebido de " + remetenteChave + ": " + mensagem);

                String[] partes = mensagem.split(String.valueOf(SEP), 3);
                if (partes.length < 2) {
                    System.out.println("Mensagem mal formatada");
                    return;
                }

                String tipo = partes[0];
                String valor = partes[1];
                if(partes.length >= 3){
                    remetenteChave = partes[2].trim();
                }
                String resposta;

                switch (tipo) {
                    case "1" -> {
                        synchronized (mapaUsuarios) {
                            mapaUsuarios.put(remetenteChave, valor);
                            resposta = "OK\u001FRegistrado como: " + valor;
                            imprimirTabela();
                        }
                    }
                    case "0" -> {
                        synchronized (mapaUsuarios) {
                            String nome = mapaUsuarios.get(valor.trim());
                            System.out.println(nome);
                            resposta = (nome != null) ? "OK\u001F" + nome : "0\u001FDesconhecido";
                        }
                    }
                    case "2" -> {
                        synchronized (mapaUsuarios) {
                            if (mapaUsuarios.containsKey(valor)) {
                                mapaUsuarios.remove(valor);
                                resposta = "OK\u001FRemovido";
                                imprimirTabela();
                            } else {
                                resposta = "0\u001FRegistro não encontrado";
                            }
                        }
                    }
                    default -> {
                        resposta = "0\u001FTipo inválido";
                    }
                }

                byte[] bufferResposta = resposta.getBytes();
                DatagramPacket pacoteResposta = new DatagramPacket(
                        bufferResposta,
                        bufferResposta.length,
                        endereco,
                        porta
                );
                System.out.println("Resposta: " + resposta);
                socket.send(pacoteResposta);

            } catch (IOException e) {
                System.err.println("Erro ao tratar pacote UDP: " + e.getMessage());
            }
        }
    }
}