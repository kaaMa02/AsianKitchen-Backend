package ch.asiankitchen.controller;

import ch.asiankitchen.service.WebPushService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/public/webpush")
@RequiredArgsConstructor
public class WebPushPublicController {

    private final WebPushService webpush;

    @GetMapping("/vapid-key")
    public Map<String, String> key() {
        return Map.of("publicKey", webpush.publicKey());
    }

    @PostMapping("/subscribe")
    public void subscribe(@RequestBody SubscriptionDTO dto,
                          @RequestHeader(value="User-Agent", required=false) String ua) {
        if (dto == null || dto.getEndpoint() == null || dto.getKeys() == null) return;
        webpush.saveOrUpdate(
                dto.getEndpoint(),
                dto.getKeys().getP256dh(),
                dto.getKeys().getAuth(),
                dto.getTag(),
                ua
        );
    }

    @Data
    public static class SubscriptionDTO {
        private String endpoint;
        private Keys keys;
        private String tag; // optional: defaults to "admin"
        @Data public static class Keys {
            private String p256dh;
            private String auth;
        }
    }
}