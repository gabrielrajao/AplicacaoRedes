package Servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ServidorTCP implements Runnable {
    private static final int portaServidorTCP = 6789;
    private static final List<PrintWriter> clientes = new ArrayList<>(); // Lista de clientes conectados
    private static final List<String> clientesIdentifiers = new ArrayList<>();

    private void broadcastUsuariosOnline() {
        synchronized (clientes) {
            StringBuilder mensagem = new StringBuilder();
            if (clientesIdentifiers.size() > 0) {
                mensagem.append("1\u001F");
                boolean fazVirgula = false;
                for (String id : clientesIdentifiers) {
                    if (fazVirgula)
                        mensagem.append(",");
                    fazVirgula = true;
                    mensagem.append(id);
                }
            } else {
                mensagem.append("0");
            }
            for (PrintWriter cliente : clientes) {
                cliente.println(mensagem.toString());
            }
        }
    }

    public void run() {
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(portaServidorTCP);
            System.out.println("Servidor TCP iniciado na porta " + portaServidorTCP);
            while (true) {
                Socket conexao = socket.accept();
                System.out.println("Novo cliente conectado: " + conexao.getInetAddress().getHostAddress() + ":"
                        + conexao.getPort());

                synchronized (clientes) {
                    clientes.add(new PrintWriter(conexao.getOutputStream(), true));
                    clientesIdentifiers.add(conexao.getInetAddress().getHostAddress() + ":" + conexao.getPort());
                    broadcastUsuariosOnline();
                }

                // Cria uma nova thread para o cliente
                ClienteHandler handler = new ClienteHandler(conexao);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.out.println("Erro ao iniciar comunicação: " + e.getMessage());
        } finally {
            try {
                if (socket != null)
                    socket.close();
            } catch (IOException e) {
                System.out.println("Erro ao fechar a conexão: " + e.getMessage());
            }
        }
    }

    // Essa classe trata a comunicação com o cliente
    static class ClienteHandler implements Runnable {
        private Socket conexao;
        private BufferedReader entrada;
        private PrintWriter saida;

        public ClienteHandler(Socket conexao) throws IOException {
            this.conexao = conexao;
            this.entrada = new BufferedReader(new InputStreamReader(conexao.getInputStream()));
            this.saida = new PrintWriter(conexao.getOutputStream(), true);
            String currentIdentifier = conexao.getInetAddress().getHostAddress() + ":" + conexao.getPort();
            synchronized (clientes) {
                int index = 0;
                for (PrintWriter cliente : clientes) {
                    if (clientesIdentifiers.get(index++).equals(currentIdentifier) == false) { // Não envia
                                                                                                // para o
                                                                                                // próprio
                                                                                                // cliente
                        cliente.println("3\u001F"+currentIdentifier);
                    }
                }
            }
        }

        public void run() {
            try {
                String str;

                while ((str = entrada.readLine()) != null) // Enquanto o cliente mandar algo
                {
                    String currentIdentifier = conexao.getInetAddress().getHostAddress() + ":" + conexao.getPort();
                    System.out.println("Recebido de " + currentIdentifier + ": " + str);
                    synchronized (clientes) {
                        int index = 0;
                        for (PrintWriter cliente : clientes) {
                            if (clientesIdentifiers.get(index++).equals(currentIdentifier) == false) { // Não envia
                                                                                                       // para o
                                                                                                       // próprio
                                                                                                       // cliente
                                cliente.println("2\u001F"+currentIdentifier + "\u001F" + str);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Erro na comunicação com o cliente: " + e.getMessage());
            } finally {
                String clientId = conexao.getInetAddress().getHostAddress() + ":" + conexao.getPort();
                try {
                    conexao.close();
                } catch (IOException e) {
                    System.out.println("Erro ao fechar a conexão: " + e.getMessage());
                }
                synchronized (clientes) {
                    int idx = clientesIdentifiers.indexOf(clientId);
                    if (idx >= 0) {
                        clientesIdentifiers.remove(idx);
                        clientes.remove(idx);
                        new ServidorTCP().broadcastUsuariosOnline();
                    }
                }
                System.out.println("Cliente desconectado: " + conexao.getInetAddress().getHostAddress());
            }
        }
    }
}
