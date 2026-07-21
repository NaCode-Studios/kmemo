# kmemo examples

A runnable demo of the one thing kmemo is for: **catching a near-miss a plain similarity cache would
serve wrong.**

## Run it (no API key, no Docker)

```bash
./gradlew :examples:run
```

The embedder is local and deterministic, so this works anywhere. You'll see three lookups against a
warmed FAQ:

```
HIT   "How can I reverse a Python list?"
        served: Use reversed() or list.reverse().  (similarity 0.80)
MISS  "Convert 250 USD to EUR"
        reason: REJECTED_BY_GUARD — numeric: numbers differ: [250] vs [100]
MISS  "What is the best sourdough recipe?"
        reason: BELOW_THRESHOLD — best similarity 0.00 < 0.7
```

The middle line is the whole point. `Convert 100 USD to EUR` and `Convert 250 USD to EUR` embed at
~0.99 with any real model, so no threshold separates them — the **numeric guard** does. A plain
similarity cache would serve the 100-USD answer to the 250-USD question.

## Run it against a persistent store (Redis)

```bash
docker compose -f examples/docker-compose.yml up -d
KMEMO_REDIS_URL=redis://localhost:6379 ./gradlew :examples:run
```

With `KMEMO_REDIS_URL` set, the demo uses `kmemo-store-redis` (RediSearch KNN) instead of the
in-memory store, so the cache is shared across processes and survives a restart — the same code, a
different `CacheStore`.

## Swapping in a real embedder

`DemoEmbedder` is a stand-in. In a real app you pass your own `Embedder` — one line over your provider's
SDK — and nothing else changes:

```kotlin
val embedder = Embedder { text -> openAi.embeddings("text-embedding-3-small", text).vector() }
val cache = SemanticCache(embedder, store = store)
```
