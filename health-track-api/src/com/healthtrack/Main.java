cat > src/com/healthtrack/Usuario.java

cat > src/com/healthtrack/Usuario.java
sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class Main {
    private static ServicoAPI servico = new ServicoAPI();
    
    public static void main(String[] args) throws IOException {
        int porta = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(porta), 0);
        
        System.out.println("🚀 Iniciando Health Track API...");
        System.out.println("📊 API rodando em: http://localhost:" + porta);
        System.out.println("📍 Endpoints disponíveis:");
        System.out.println("   POST /usuarios - Criar usuário");
        System.out.println("   GET  /usuarios - Listar usuários");
        System.out.println("   GET  /usuarios/{id} - Buscar usuário");
        System.out.println("   POST /tarefas - Criar tarefa");
        System.out.println("   GET  /tarefas/usuario/{id} - Listar tarefas");
        System.out.println("   PUT  /tarefas/{id}/concluir - Concluir tarefa");
        System.out.println("   POST /registros - Criar registro diário");
        System.out.println("   GET  /registros/usuario/{id} - Listar registros");
        
        // Configurar endpoints
        server.createContext("/usuarios", new UsuarioHandler());
        server.createContext("/tarefas", new TarefaHandler());
        server.createContext("/registros", new RegistroHandler());
        server.createContext("/", new InfoHandler());
        
        server.setExecutor(null);
        server.start();
        
        System.out.println("✅ API iniciada com sucesso!");
        System.out.println("⏹️  Pressione Ctrl+C para parar a API");
    }
    
    static class UsuarioHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String response = "";
            int statusCode = 200;
            
            try {
                if ("POST".equals(method) && path.equals("/usuarios")) {
                    String body = getRequestBody(exchange);
                    response = servico.criarUsuario(body);
                } else if ("GET".equals(method) && path.equals("/usuarios")) {
                    response = servico.listarUsuarios();
                } else if ("GET".equals(method) && path.startsWith("/usuarios/")) {
                    String[] parts = path.split("/");
                    if (parts.length == 3) {
                        Long id = Long.parseLong(parts[2]);
                        response = servico.buscarUsuario(id);
                    }
                } else {
                    statusCode = 404;
                    response = "{\"erro\": \"Endpoint não encontrado\"}";
                }
            } catch (Exception e) {
                statusCode = 400;
                response = "{\"erro\": \"" + e.getMessage() + "\"}";
            }
            
            sendResponse(exchange, response, statusCode);
        }
    }
    
    static class TarefaHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String response = "";
            int statusCode = 200;
            
            try {
                if ("POST".equals(method) && path.equals("/tarefas")) {
                    String body = getRequestBody(exchange);
                    response = servico.criarTarefa(body);
                } else if ("GET".equals(method) && path.startsWith("/tarefas/usuario/")) {
                    String[] parts = path.split("/");
                    if (parts.length == 4) {
                        Long usuarioId = Long.parseLong(parts[3]);
                        response = servico.listarTarefas(usuarioId);
                    }
                } else if ("PUT".equals(method) && path.contains("/concluir")) {
                    String[] parts = path.split("/");
                    if (parts.length == 4) {
                        Long tarefaId = Long.parseLong(parts[2]);
                        response = servico.concluirTarefa(tarefaId);
                    }
                } else {
                    statusCode = 404;
                    response = "{\"erro\": \"Endpoint não encontrado\"}";
                }
            } catch (Exception e) {
                statusCode = 400;
                response = "{\"erro\": \"" + e.getMessage() + "\"}";
            }
            
            sendResponse(exchange, response, statusCode);
        }
    }
    
    static class RegistroHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            String response = "";
            int statusCode = 200;
            
            try {
                if ("POST".equals(method) && path.equals("/registros")) {
                    String body = getRequestBody(exchange);
                    response = servico.criarRegistro(body);
                } else if ("GET".equals(method) && path.startsWith("/registros/usuario/")) {
                    String[] parts = path.split("/");
                    if (parts.length == 4) {
                        Long usuarioId = Long.parseLong(parts[3]);
                        response = servico.listarRegistros(usuarioId);
                    }
                } else {
                    statusCode = 404;
                    response = "{\"erro\": \"Endpoint não encontrado\"}";
                }
            } catch (Exception e) {
                statusCode = 400;
                response = "{\"erro\": \"" + e.getMessage() + "\"}";
            }
            
            sendResponse(exchange, response, statusCode);
        }
    }
    
    static class InfoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "{\"mensagem\": \"Health Track API - Use os endpoints /usuarios, /tarefas, /registros\"}";
            sendResponse(exchange, response, 200);
        }
    }
    
    private static String getRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody();
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
    
    private static void sendResponse(HttpExchange exchange, String response, int statusCode) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
        
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}