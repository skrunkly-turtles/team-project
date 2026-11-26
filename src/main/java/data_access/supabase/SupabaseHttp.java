package data_access.supabase;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/** Small HTTP/JSON adapter. Keeps authentication and network details out of use cases. */
public final class SupabaseHttp {
  private static final Gson GSON = new Gson();
  private final SupabaseConfig config;
  private final HttpClient client = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(10)).build();

  public SupabaseHttp(SupabaseConfig config) {
    this.config = config;
  }

  public static String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  public JsonElement request(String method, String path, Object body, String jwt) {
    if (!path.startsWith("/") || path.startsWith("//")) {
      throw new IllegalArgumentException("API path must be relative");
    }
    HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(config.url() + path))
        .timeout(Duration.ofSeconds(40))
        .header("apikey", config.publishableKey())
        .header("Accept", "application/json");
    if (jwt != null && !jwt.isBlank()) {
      builder.header("Authorization", "Bearer " + jwt);
    }
    if (body != null) {
      builder.header("Content-Type", "application/json");
    }
    String json = body == null ? "" : GSON.toJson(body);
    builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
        : HttpRequest.BodyPublishers.ofString(json));
    try {
      HttpResponse<String> response = client.send(builder.build(),
          HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
      String contents = response.body();
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        String message = "Supabase returned HTTP " + response.statusCode();
        try {
          JsonElement parsed = JsonParser.parseString(contents);
          if (parsed.isJsonObject()) {
            var error = parsed.getAsJsonObject();
            for (String key : new String[] {"msg", "message", "error_description", "error"}) {
              if (error.has(key) && error.get(key).isJsonPrimitive()) {
                message += ": " + error.get(key).getAsString();
                break;
              }
            }
          }
        } catch (RuntimeException ignored) {
          // Keep the status code; do not dump request credentials or server responses.
        }
        throw new IllegalStateException(message);
      }
      return contents == null || contents.isBlank() ? JsonNull.INSTANCE
          : JsonParser.parseString(contents);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Supabase request interrupted", exception);
    } catch (java.io.IOException exception) {
      throw new IllegalStateException("Could not connect to Supabase", exception);
    }
  }
}
