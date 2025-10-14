package ch.asiankitchen.service;

import ch.asiankitchen.model.WebPushSubscription;
import ch.asiankitchen.repository.WebPushSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.GeneralSecurityException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebPushService {

    private final WebPushSubscriptionRepository repo;

    @Value("${app.webpush.subject:}")
    private String subject;

    @Value("${app.webpush.public-key:}")
    private String publicKey;

    @Value("${app.webpush.private-key:}")
    private String privateKey;

    public String publicKey() { return publicKey; }

    @Transactional
    public void saveOrUpdate(String endpoint, String p256dh, String auth, String tag, String userAgent) {
        if (endpoint == null || endpoint.isBlank()) return;
        if (repo.existsByEndpoint(endpoint)) return;

        repo.save(WebPushSubscription.builder()
                .endpoint(endpoint)
                .p256dh(p256dh)
                .auth(auth)
                .tag((tag == null || tag.isBlank()) ? "admin" : tag)
                .userAgent(userAgent)
                .build());
    }

    /** Send JSON payload to all subs matching tag ("admin"). Removes dead subs automatically. */
    @Transactional
    public void broadcast(String tag, String jsonPayload) {
        final List<WebPushSubscription> targets =
                (tag == null || tag.isBlank()) ? repo.findAll() : repo.findByTag(tag);

        if (targets.isEmpty()) return;

        final PushService svc = build();

        for (WebPushSubscription s : targets) {
            try {
                // web-push 5.1.1 prefers using Subscription + Keys (base64url strings)
                Subscription sub = new Subscription(
                        s.getEndpoint(),
                        new Subscription.Keys(s.getP256dh(), s.getAuth())
                );

                Notification n = new Notification(sub, jsonPayload);
                var resp = svc.send(n);
                int code = resp.getStatusLine().getStatusCode();
                if (code == 404 || code == 410) {
                    repo.delete(s);
                    log.info("Removed dead web push subscription: {}", s.getEndpoint());
                }
            } catch (Exception e) {
                log.warn("Push failed for {}: {}", s.getEndpoint(), e.toString());
            }
        }
    }

    private PushService build() {
        try {
            // This ctor throws GeneralSecurityException (not JoseException)
            return new PushService(publicKey, privateKey, subject);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Invalid VAPID keys/config", e);
        }
    }
}
