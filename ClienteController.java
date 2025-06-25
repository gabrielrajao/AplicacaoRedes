import javax.swing.*;
import java.awt.event.*;
import java.awt.Color;
import java.io.*;
import java.net.*;
import java.util.concurrent.TimeoutException;

public class ClienteController {
    private final ClienteView view;
    private final ClienteModel model;
    private Socket socket;
    private BufferedReader entradaServidor;
    private DataOutputStream saidaServidor;
    private Thread recebendoMensagens;
    private static final int portaServidorTCP = 6789;
    private static final int portaServidorUDP = 9876;

    public ClienteController(ClienteView view, ClienteModel model) {
        this.view = view;
        this.model = model;
        initController();
    }

    private void initController() {
        view.getConectarButton().addActionListener(e -> conectar());
        view.getEnviarButton().addActionListener(e -> enviarMensagem());
        view.getMenuSair().addActionListener(e -> sair());
        view.getMensagemField().addActionListener(e -> enviarMensagem());
    }

    private void conectar() {
        String usuario = view.getUsuarioField().getText().trim();
        if (usuario.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Digite um nome de usuário.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            socket = new Socket();
            socket.bind(new InetSocketAddress(InetAddress.getLocalHost(), 0));
            sendMessageUDP("1\u001F" + usuario + "\u001F" + InetAddress.getLocalHost().getHostAddress() +":"+ socket.getLocalPort() + "\u001F");
            socket.connect(new InetSocketAddress(InetAddress.getLocalHost(), portaServidorTCP));
            entradaServidor = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            saidaServidor = new DataOutputStream(socket.getOutputStream());
            model.setUsuario(usuario);
            model.setConectado(true);
            view.getStatusLabel().setText("Conectado como: " + usuario);
            view.getStatusLabel().setForeground(Color.WHITE);
            view.getConectarButton().setEnabled(false);
            view.getUsuarioField().setEnabled(false);
            view.habilitarEnvioMensagem(true); // Habilita painel de mensagem
            startRecebendoMensagens();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view, "Erro ao conectar: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void enviarMensagem() {
        if (!model.isConectado()) {
            JOptionPane.showMessageDialog(view, "Conecte-se primeiro.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String msg = view.getMensagemField().getText().trim();
        if (msg.isEmpty()) return;
        try {
            saidaServidor.writeBytes(msg + '\n');
            view.getChatArea().append("Você: " + msg + "\n");
            view.getMensagemField().setText("");
        } catch (IOException e) {
            mostrarMensagemSistema("Erro ao enviar mensagem: " + e.getMessage());
        }
    }

    private void startRecebendoMensagens() {
        recebendoMensagens = new Thread(() -> {
            try {
                String mensagem;
                while ((mensagem = entradaServidor.readLine()) != null) {
                    String[] splittedMsg = mensagem.split("\u001F");
                    switch (splittedMsg[0]) {
                        case "0":
                            mostrarMensagemSistema("Você está sozinho");
                            break;
                        case "1":
                            if (splittedMsg.length < 2) break;
                            atualizarUsuariosOnline(splittedMsg[1].split(","));
                            break;
                        case "2":
                            if (splittedMsg.length < 3) break;
                            String remetente = translateUser(splittedMsg[1]);
                            SwingUtilities.invokeLater(() -> view.getChatArea().append(remetente + " disse: " + splittedMsg[2] + "\n"));
                            break;
                        case "3":
                            if (splittedMsg.length < 2) break;
                            mostrarMensagemSistema("Usuário " + translateUser(splittedMsg[1]) + " se conectou");
                            break;
                        default:
                            mostrarMensagemSistema("Mensagem desconhecida recebida.");
                    }
                }
            } catch (IOException e) {
                mostrarMensagemSistema("Erro ao receber mensagem: " + e.getMessage());
            }
        });
        recebendoMensagens.start();
    }

    private void sair() {
        int op = JOptionPane.showConfirmDialog(view, "Deseja realmente sair?", "Sair", JOptionPane.YES_NO_OPTION);
        if (op == JOptionPane.YES_OPTION) {
            try {
                if (socket != null && !socket.isClosed()) socket.close();
            } catch (IOException ignored) {}
            System.exit(0);
        }
    }

    public void atualizarUsuariosOnline(String[] usuarios) {
        String[][] data = new String[usuarios.length][1];
        for (int i = 0; i < usuarios.length; i++) {
            data[i][0] = translateUser(usuarios[i]);
        }
        model.setUsuariosOnline(data);
        view.getUsuariosTable().setModel(new javax.swing.table.DefaultTableModel(data, new String[]{"Usuários Online"}));
    }

    public void mostrarMensagemSistema(String msg) {
        SwingUtilities.invokeLater(() -> view.getChatArea().append("[Sistema]: " + msg + "\n"));
    }

    // Métodos UDP reutilizados do Cliente.java
    public String sendMessageUDP(String message) throws TimeoutException {
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
                shouldRetry = !splittedResponseString[0].equals("OK");
                if (!shouldRetry)
                    result = splittedResponseString[1];
                else retryCounter++;
            }
            if(shouldRetry && retryCounter >= 10) throw new TimeoutException("Não foi possível obter resposta do servidor UDP");
        } catch (IOException e) {
            mostrarMensagemSistema("Não foi possível alcançar o servidor UDP: " + e.getMessage());
        }
        return result;
    }

    public String translateUser(String user) {
        try {
            return sendMessageUDP("0\u001F" + user + "\u001F");
        } catch (TimeoutException e) {
            return user;
        }
    }
}
