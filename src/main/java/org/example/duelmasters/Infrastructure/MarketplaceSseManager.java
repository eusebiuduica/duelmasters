package org.example.duelmasters.Infrastructure;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MarketplaceSseManager {
    private final Map<Integer, SseEmitter> emitters = new ConcurrentHashMap();

    public SseEmitter addClient(Integer userId) {
        SseEmitter emitter = new SseEmitter(0L);

        emitters.put(userId, emitter);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError(e -> emitters.remove(userId));

        return emitter;
    }

    public void broadcast(Object data, String eventName) {
        for (Map.Entry<Integer, SseEmitter> entry : emitters.entrySet()) {
            SseEmitter emitter = entry.getValue();
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
            } catch (Exception e) {
                emitters.remove(entry.getKey()); // curăță emitter-ul mort
            }
        }
    }


    // notifică doar vânzătorul
    public void notifySale(Integer sellerId, String info, Integer totalGold) {
        SseEmitter emitter = emitters.get(sellerId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("SALE_COMPLETED")
                        .data(Map.of("info", info, "totalGold", totalGold)));
            } catch (Exception e) {
                emitters.remove(sellerId);
            }
        }
    }
}
