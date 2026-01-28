# CLI Grammar Contract (MVP)

## Syntax
intend <METHOD> <URL> [OPTIONS]

## Options
--auth <jwt|oauth|apikey|none>  (Default: none)
--trace                         (Enable tracing)
--explain                       (Show plan, do not execute)
--apply                         (Execute request)

## Rules
1. METHOD (GET, POST, etc.) and URL are mandatory.
2. NO raw headers allowed in MVP.
3. Default Behavior: If neither -explain nor -apply is passed - DEFAULT TO -EXPLAIN.
4. Conflict: explain and apply cannot both be explicit (Validation Error).
