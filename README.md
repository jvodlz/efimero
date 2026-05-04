# efimero

A transient secret store service in Java Spring, using Gradle and Groovy. 
Create an encrypted secret, get a one-time URL, read it once, and poof: it's gone.

## Features
- Create a secret and receive a one-time retrieval key (URL)
- Atomic read-and-destroy semantics
- TTL expiry (default 60 min) with scheduled cleanup
  - Returns status 404 if missing or expired
- `AES-GCM` encryption at rest; key provided at runtime via environment variable
- No plaintext key or secret is stored in DB; only ciphertext and hashed key
- Responses include no-store cache headers; filter adds security headers.


## Getting Started

Use `gradlew.bat bootRun` to run the application

Export a 256-bit key
```bash
export APP_ENCRYPTION_KEY=$(openssl rand -base64 32)
# ./gradlew bootRun
```
Create a secret in a new terminal
```bash
curl -s -X POST http://localhost:8080/api/efimero \
  -H "Content-Type: application/json" \
  -d '{"text":"Is this the real life? Is this just fantasy?"}'
```

Read the secret (one-time)
```  
curl -i http://localhost:8080/api/efimero/<key>
```

## Security

In a production environment, the `h2-console` should be disabled. This is because the console gives direct SQL access to the application's database. Leaving them enabled exposes an attack surface that can allow data theft, modification, or privilege escalation. 

The following should also be removed for production so the security headers can instruct browsers how to handle responses and reduce client-side exploitation e.g. `MIME-sniffing`.

```java
// DEV ONLY. H2 console needs JavaScript - skip security headers
if (path.startsWith("/h2-console")) {
    chain.doFilter(req, res);
    return;
}
```

## Design Decisions & Learnings
- One-time key returned to caller; DB stores only SHA-256 (key) - same principle as password hashing
- `AES-GCM` with random `IV` ensures identical plaintexts encrypt differently
- `@Transactional` on `readAndDestroy` ensures atomicity and prevents race conditions
- Scheduled cleanup prevents expired secrets from accumulating
- Input validation (`@NotBlank`, `@Size`) prevents abusive payloads


## Next Improvements
- Add rate limiting and request logging (mask sensitive fields)
- Swap H2 with PostgreSQL for production