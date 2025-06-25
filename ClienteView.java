import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicButtonUI;

public class ClienteView extends JFrame {
    private JTextField usuarioField;
    private JButton conectarButton;
    private JButton enviarButton;
    private JTextArea chatArea;
    private JTextField mensagemField;
    private JTable usuariosTable;
    private JLabel statusLabel;
    private JMenuBar menuBar;
    private JMenu menuArquivo;
    private JMenuItem menuSair;

    public ClienteView() {
        setTitle("Cliente Chat - Redes");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(700, 450));
        getContentPane().setBackground(Color.decode("#F7F7FA"));
        Color azulClaro = new Color(66, 133, 244);
        Color cinzaClaro = new Color(230, 230, 230);
        Color cinzaEscuro = new Color(60, 60, 60);
        // Painel de login
        JPanel loginPanel = new JPanel();
        loginPanel.setBackground(Color.WHITE);
        loginPanel.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, cinzaClaro),
            new EmptyBorder(10, 10, 10, 10)
        ));
        JLabel userLabel = new JLabel("Usuário:");
        userLabel.setForeground(cinzaEscuro);
        usuarioField = new JTextField(15);
        usuarioField.setBorder(new RoundedBorder(8));
        conectarButton = new JButton("Conectar");
        estilizarBotao(conectarButton, azulClaro);
        loginPanel.add(userLabel);
        loginPanel.add(usuarioField);
        loginPanel.add(conectarButton);
        // Painel de mensagens
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setBackground(Color.WHITE);
        chatArea.setForeground(cinzaEscuro);
        chatArea.setBorder(new EmptyBorder(8, 8, 8, 8));
        JScrollPane chatScroll = new JScrollPane(chatArea);
        // Painel de usuários
        usuariosTable = new JTable(new String[][]{}, new String[]{"Usuários Online"});
        usuariosTable.setBackground(Color.WHITE);
        usuariosTable.setBorder(new RoundedBorder(8));
        JScrollPane tableScroll = new JScrollPane(usuariosTable);
        // Painel de envio
        mensagemField = new JTextField();
        mensagemField.setBorder(new RoundedBorder(8));
        enviarButton = new JButton("Enviar");
        estilizarBotao(enviarButton, azulClaro);
        JPanel enviarPanel = new JPanel(new BorderLayout(5, 0));
        enviarPanel.setBackground(Color.WHITE);
        enviarPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        enviarPanel.add(mensagemField, BorderLayout.CENTER);
        enviarPanel.add(enviarButton, BorderLayout.EAST);
        // Barra de status
        statusLabel = new JLabel("Desconectado");
        statusLabel.setOpaque(true);
        statusLabel.setBackground(azulClaro);
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));
        statusLabel.setBorder(new EmptyBorder(6, 12, 6, 0));
        // Menu
        JMenuBar menuBar = new JMenuBar();
        JMenu menuArquivo = new JMenu("Arquivo");
        menuSair = new JMenuItem("Sair");
        menuArquivo.add(menuSair);
        menuBar.add(menuArquivo);
        setJMenuBar(menuBar);
        // Layout principal com GroupLayout
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        layout.setHorizontalGroup(
            layout.createParallelGroup()
                .addComponent(loginPanel)
                .addGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup()
                        .addComponent(chatScroll)
                        .addComponent(enviarPanel))
                    .addComponent(tableScroll, 180, 200, 250))
                .addComponent(statusLabel)
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addComponent(loginPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGroup(layout.createParallelGroup()
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(chatScroll)
                        .addComponent(enviarPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addComponent(tableScroll))
                .addComponent(statusLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        );
    }
    private void estilizarBotao(JButton botao, Color cor) {
        botao.setBackground(cor);
        botao.setForeground(Color.WHITE);
        botao.setFocusPainted(false);
        botao.setBorder(new RoundedBorder(8));
        botao.setUI(new BasicButtonUI());
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        botao.setContentAreaFilled(true);
        botao.setOpaque(true);
        botao.setMargin(new Insets(8, 16, 8, 16));
        botao.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                botao.setBackground(cor.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                botao.setBackground(cor);
            }
        });
    }
    static class RoundedBorder extends LineBorder {
        public RoundedBorder(int radius) {
            super(new Color(200,200,200), 1, true);
        }
    }

    // Getters para Controller
    public JTextField getUsuarioField() { return usuarioField; }
    public JButton getConectarButton() { return conectarButton; }
    public JButton getEnviarButton() { return enviarButton; }
    public JTextArea getChatArea() { return chatArea; }
    public JTextField getMensagemField() { return mensagemField; }
    public JTable getUsuariosTable() { return usuariosTable; }
    public JLabel getStatusLabel() { return statusLabel; }
    public JMenuItem getMenuSair() { return menuSair; }

    public void habilitarEnvioMensagem(boolean habilitar) {
        mensagemField.setEnabled(habilitar);
        enviarButton.setEnabled(habilitar);
    }
}
