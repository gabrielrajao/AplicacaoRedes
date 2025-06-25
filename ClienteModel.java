public class ClienteModel {
    private String usuario;
    private boolean conectado;
    private String[][] usuariosOnline;

    public ClienteModel() {
        this.usuario = "";
        this.conectado = false;
        this.usuariosOnline = new String[0][1];
    }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public boolean isConectado() { return conectado; }
    public void setConectado(boolean conectado) { this.conectado = conectado; }

    public String[][] getUsuariosOnline() { return usuariosOnline; }
    public void setUsuariosOnline(String[][] usuariosOnline) { this.usuariosOnline = usuariosOnline; }
}
