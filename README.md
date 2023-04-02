# zipkin-poc

## Hotrod demo

1. Start docker compose with

```docker compose up```

2. Access HotRod UI on http://localhost:8080/
3. Play with buttons
4. Find and analyze traces

Jaeger UI is on http://localhost:16686/

## Quarkus demo

1. Start docker compose with

```docker compose up```

2. Start quarkus app with

```./gradlew :quarkus:quarkusDev```

3. Access UI with

```text
http://localhost:8081/
http://localhost:8081/hello
http://localhost:8081/hello/greeting/Name
http://localhost:8081/fixapi/order/new
```

4. Observe traces on

```http://localhost:16686/```
