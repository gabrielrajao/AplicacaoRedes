import java.io.*;
import java.net.*;
import java.util.concurrent.TimeoutException;

public class Cliente {
    private static int portaServidorTCP = 6789; // Porta do servidorTCP
    private static int portaServidorUDP = 9876; // Porta do servidorUDP

    public static void main(String[] args) throws Exception {

        Socket socket = new Socket();
        socket.bind(new InetSocketAddress(InetAddress.getLocalHost(), 0));
        // Fluxo de entrada Input de usuário
        BufferedReader entradaTeclado = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("Digite o seu nome de usuário: ");
        sendMessageUDP("1\u001F" + entradaTeclado.readLine() + "\u001F" + InetAddress.getLocalHost().getHostAddress() +":"+ socket.getLocalPort() + "\u001F");

        // Conecta-se ao servidor
        socket.connect(new InetSocketAddress(InetAddress.getLocalHost(), portaServidorTCP));
        System.out.println("Conectado ao servidor: " + InetAddress.getLocalHost());

        // Cria os fluxos de entrada e saída do servidor TCP
        BufferedReader entradaServidor = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        DataOutputStream saidaServidor = new DataOutputStream(socket.getOutputStream());

        // Thread para ler mensagens do servidor (mensagens de outros clientes)
        Thread recebendoMensagens = new Thread(() -> {
            try {
                String mensagem;
                while ((mensagem = entradaServidor.readLine()) != null) {
                    String[] splittedMsg = mensagem.split("\u001F");
                    switch (splittedMsg[0]) {
                        case "0":
                            System.out.println("Você está sozinho");
                            break;
                        case "1":
                            if (splittedMsg.length < 2)
                                throw new IOException("Parâmetros de mensagem inválidos");
                            System.out.println("Usuários online: " + translateUsers(splittedMsg[1]));
                            break;
                        case "2":
                            if (splittedMsg.length < 3)
                                throw new IOException("Parâmetros de mensagem inválidos");
                            System.out.println(translateUser(splittedMsg[1]) + " disse: " + splittedMsg[2]);
                            break;
                        case "3":
                            if (splittedMsg.length < 2)
                                throw new IOException("Parâmetros de mensagem inválidos");
                            System.out.println("Usuário " + translateUser(splittedMsg[1]) + " se conectou");
                            break;
                        default:
                            throw new IOException("Identificador de protocolo de mensagem indefinido");
                    }
                }
            } catch (IOException e) {
                System.out.println("Erro ao receber mensagem: " + e.getMessage());
            } catch (TimeoutException e){
                System.out.println("Tempo limite atingido: " + e.getMessage());
            }
        });

        // Inicia a thread de recebimento
        recebendoMensagens.start();

        String mensagem;

        while (true) {
            // Lê uma mensagem do usuário
            mensagem = entradaTeclado.readLine();

            if (mensagem.equalsIgnoreCase("sair")) {
                System.out.println("Desconectando do servidor...");
                break;
            }

            // Envia a mensagem para o servidor
            saidaServidor.writeBytes(mensagem + '\n');
        }

        // Fecha a conexão
        socket.close();
    }

    public static String sendMessageUDP(String message) throws TimeoutException{
        String result = "";

        try (DatagramSocket socket = new DatagramSocket()) {
            int retryCounter = 0;
            boolean shouldRetry = true;

            while (retryCounter < 10 && shouldRetry) {

                byte[] messageBuffer = message.getBytes();
                DatagramPacket pacoteEnvio = new DatagramPacket(messageBuffer, messageBuffer.length,
                        InetAddress.getByName(InetAddress.getLocalHost().getHostAddress()), portaServidorUDP);
                socket.send(pacoteEnvio);

                byte[] bufferResposta = new byte[1024];
                DatagramPacket pacoteRecebido = new DatagramPacket(bufferResposta, bufferResposta.length);
                socket.receive(pacoteRecebido);

                String responseString = new String(pacoteRecebido.getData(), 0, pacoteRecebido.getLength());
                String[] splittedResponseString = responseString.split("\u001F");

                shouldRetry = splittedResponseString[0].equals("OK") == false;
                if (shouldRetry == false)
                    result = splittedResponseString[1];
                else retryCounter++;
            }
            if(shouldRetry && retryCounter >= 10) throw new TimeoutException("Não foi possível obter resposta do servidor UDP");
        } catch (IOException e) {
            System.out.println("Não foi possível alcançar o servidor UDP: " + e.getMessage());
        }
        return result;
    }

    public static String translateUsers(String users) throws TimeoutException {
        String translatedUsers = "";
        boolean needsSeparation = false;

        String[] usersSplitted = users.split(",");

        for (String user : usersSplitted) {
            if (needsSeparation == true) {
                translatedUsers += ", ";
            }
            translatedUsers += translateUser(user);
            needsSeparation = true;
        }

        return translatedUsers;
    }

    public static String translateUser(String user) throws TimeoutException {
        return sendMessageUDP("0\u001F" + user + "\u001F" );
    }
}
