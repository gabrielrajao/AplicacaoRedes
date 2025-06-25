public class ClienteGUI {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            ClienteView view = new ClienteView();
            ClienteModel model = new ClienteModel();
            ClienteController controller = new ClienteController(view, model);
            view.setVisible(true);
        });
    }
}
