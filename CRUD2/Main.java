public class Main {

    public static void main(String[] args) {
        InterfaceSistema sistema = null;

        try {
            sistema = new InterfaceSistema();
            sistema.executar();
        } catch (Exception e) {
            System.err.println("Erro ao executar o sistema: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (sistema != null) {
                try {
                    sistema.fechar();
                } catch (Exception e) {
                    System.err.println("Erro ao fechar os arquivos do sistema: " + e.getMessage());
                }
            }
        }
    }
}
