# TellerMesh

## Java code style

Java 21. Apply these to all new and changed code:

- **`var` for local variables** whenever the type is clear from the right-hand side
  (`var reply = chatClient.prompt()...`). Java doesn't allow `var` for fields, parameters or
  return types, so declare those with explicit types.
- **`Optional` instead of null checks.** Return `Optional<T>` from methods that may have no
  result, and chain `map` / `filter` / `orElse` / `orElseThrow` / `ifPresent` rather than
  `isPresent()` + `get()`. Don't use `Optional` for fields, method parameters or collections
  (return an empty collection instead).
- **Streams where they make the code clearer**: transforming, filtering and collecting
  collections (`stream().map(...).toList()`). Keep a plain loop when it's simpler, for example
  when it needs early exit with side effects or checked exceptions.
- **Modern Java**: records for DTOs and value types, text blocks for multi-line strings
  (prompts, SQL), switch expressions and pattern matching (`instanceof` patterns, record
  patterns, pattern `switch`), sealed interfaces for closed hierarchies, `List.of` / `Map.of`
  for immutable collections, `String.formatted`.
