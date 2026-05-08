package com.lld.elevator.app;

import com.lld.elevator.controller.ElevatorController;
import com.lld.elevator.dto.StatusDTO;
import com.lld.elevator.enums.Direction;
import com.lld.elevator.model.Elevator;
import com.lld.elevator.repository.ElevatorRepository;
import com.lld.elevator.service.ElevatorService;
import com.lld.elevator.service.NotificationService;
import com.lld.elevator.strategy.ScanStrategy;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class ElevatorHttpServer {
    private static final int PORT = 8080;
    private static final int TOTAL_FLOORS = 8;
    private static final int[] START_FLOORS = {1, 4, 8};
    private static final String[] LABELS = {"A", "B", "C"};

    private final Object lock = new Object();
    private ElevatorController controller;

    public ElevatorHttpServer() {
        resetSystem();
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/status", new StatusHandler());
        server.createContext("/call", new CallHandler());
        server.createContext("/internal", new InternalHandler());
        server.createContext("/step", new StepHandler());
        server.createContext("/reset", new ResetHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        System.out.println("Elevator backend started on http://localhost:" + PORT);
        System.out.println("Endpoints: /status, /call, /internal, /step, /reset");
    }

    private void resetSystem() {
        ElevatorRepository repo = new ElevatorRepository();
        NotificationService notifier = new NotificationService();
        ScanStrategy strategy = new ScanStrategy();
        ElevatorService service = new ElevatorService(repo, strategy, notifier, TOTAL_FLOORS);
        ElevatorController ctrl = new ElevatorController(service);

        for (int i = 0; i < LABELS.length; i++) {
            Elevator e = new Elevator(i, LABELS[i], START_FLOORS[i]);
            repo.save(e);
        }

        this.controller = ctrl;
    }

    private class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (handlePreflight(ex)) {
                return;
            }
            if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
                send(ex, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            synchronized (lock) {
                send(ex, 200, statusListJson(controller.getStatus()));
            }
        }
    }

    private class CallHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (handlePreflight(ex)) {
                return;
            }
            if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
                send(ex, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            Map<String, String> q = queryParams(ex);
            String floor = q.get("floor");
            String dir = q.get("dir");
            if (floor == null || dir == null) {
                send(ex, 400, "{\"error\":\"Missing floor or dir\"}");
                return;
            }

            try {
                int f = Integer.parseInt(floor);
                Direction direction = Direction.valueOf(dir.toUpperCase());
                synchronized (lock) {
                    controller.callElevator(f, direction);
                    send(ex, 200, "{\"ok\":true,\"status\":" + statusListJson(controller.getStatus()) + "}");
                }
            } catch (Exception e) {
                send(ex, 400, "{\"error\":\"Invalid request\"}");
            }
        }
    }

    private class InternalHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (handlePreflight(ex)) {
                return;
            }
            if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
                send(ex, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            Map<String, String> q = queryParams(ex);
            String elevId = q.get("elevId");
            String floor = q.get("floor");
            if (elevId == null || floor == null) {
                send(ex, 400, "{\"error\":\"Missing elevId or floor\"}");
                return;
            }

            try {
                int id = Integer.parseInt(elevId);
                int f = Integer.parseInt(floor);
                synchronized (lock) {
                    controller.pressFloorInside(id, f);
                    send(ex, 200, "{\"ok\":true}");
                }
            } catch (Exception e) {
                send(ex, 400, "{\"error\":\"Invalid request\"}");
            }
        }
    }

    private class StepHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (handlePreflight(ex)) {
                return;
            }
            if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
                send(ex, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            synchronized (lock) {
                controller.step();
                send(ex, 200, statusListJson(controller.getStatus()));
            }
        }
    }

    private class ResetHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            if (handlePreflight(ex)) {
                return;
            }
            if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
                send(ex, 405, "{\"error\":\"Method not allowed\"}");
                return;
            }

            synchronized (lock) {
                resetSystem();
                send(ex, 200, "{\"ok\":true}");
            }
        }
    }

    private static Map<String, String> queryParams(HttpExchange ex) {
        Map<String, String> out = new HashMap<>();
        String raw = ex.getRequestURI().getRawQuery();
        if (raw == null || raw.isEmpty()) {
            return out;
        }

        String[] pairs = raw.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String value = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            out.put(key, value);
        }
        return out;
    }

    private static boolean handlePreflight(HttpExchange ex) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
            ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");
            ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            ex.sendResponseHeaders(204, -1);
            ex.close();
            return true;
        }
        return false;
    }

    private static void send(HttpExchange ex, int code, String body) throws IOException {
        byte[] data = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        ex.sendResponseHeaders(code, data.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(data);
        }
    }

    private static String statusListJson(List<StatusDTO> statuses) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < statuses.size(); i++) {
            StatusDTO s = statuses.get(i);
            if (i > 0) {
                sb.append(',');
            }
            sb.append('{')
              .append("\"label\":\"").append(escape(s.getLabel())).append("\",")
              .append("\"floor\":").append(s.getCurrentFloor()).append(',')
              .append("\"direction\":\"").append(s.getDirection()).append("\",")
              .append("\"state\":\"").append(s.getState()).append("\",")
              .append("\"pendingRequests\":").append(s.getPendingRequests())
              .append('}');
        }
        sb.append(']');
        return sb.toString();
    }

    private static String escape(String input) {
        return input.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
