# efimero

## Getting Started

Use `gradlew.bat bootRun` to run the application

In bash, use 
```bash
export APP_ENCRYPTION_KEY=$(openssl rand -base64 32)
./gradlew bootRun

```
In a new terminal,
```bash
curl -s -X POST http://localhost:8080/api/efimero \
  -H "Content-Type: application/json" \
  -d '{"text":"Is this the real life? Is this just fantasy?"}'
  
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