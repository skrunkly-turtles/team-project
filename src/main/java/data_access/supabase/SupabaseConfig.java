package data_access.supabase;

import java.net.URI;

/** Public configuration for Supabase requests. Never store service-role keys here. */
public record SupabaseConfig(String url, String publishableKey) {
  public SupabaseConfig {
    if (url == null || !url.matches("https://[A-Za-z0-9.-]+\\.supabase\\.co/?")) {
      throw new IllegalArgumentException("SUPABASE_URL must be a hosted https://...supabase.co URL");
    }
    if (publishableKey == null || !publishableKey.startsWith("sb_publishable_")) {
      throw new IllegalArgumentException("Set an sb_publishable_ key, never a secret or service-role key");
    }
    URI.create(url);
    if (url.endsWith("/")) {
      url = url.substring(0, url.length() - 1);
    }
  }

  public static SupabaseConfig fromEnvironment() {
    return new SupabaseConfig(System.getenv("SUPABASE_URL"),
        System.getenv("SUPABASE_PUBLISHABLE_KEY"));
  }
}
