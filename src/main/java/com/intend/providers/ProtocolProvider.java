package com.intend.providers;

import com.intend.context.ResolutionContext;
import com.intend.spi.HeaderProvider;
import com.intend.spi.HeaderResolution;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class ProtocolProvider implements HeaderProvider {

    private static final int[] CHROME_VERSIONS = {120, 121, 122, 123, 124, 125, 126, 127, 128, 129, 130, 131};

    @Override
    public int getOrder() { return 10; }

    @Override
    public boolean supports(ResolutionContext ctx) { return true; }

    @Override
    public HeaderResolution resolve(ResolutionContext ctx) {
        Map<String, String> headers = new HashMap<>();

        int chromeVer = pickChromeVersion();
        String os = System.getProperty("os.name", "").toLowerCase();

        headers.put("User-Agent", buildUserAgent(os, chromeVer));
        headers.put("Accept", "*/*");
        headers.put("Accept-Language", buildAcceptLanguage());
        headers.put("Accept-Encoding", "gzip, deflate");
        headers.put("Sec-Ch-Ua", buildSecChUa(chromeVer));
        headers.put("Sec-Ch-Ua-Mobile", "?0");
        headers.put("Sec-Ch-Ua-Platform", buildPlatform(os));
        headers.put("Sec-Fetch-Dest", "empty");
        headers.put("Sec-Fetch-Mode", "cors");
        headers.put("Sec-Fetch-Site", resolveFetchSite(ctx.intent().url()));
        headers.put("Origin", resolveOrigin(ctx.intent().url()));
        headers.put("Referer", resolveReferer(ctx.intent().url()));

        Object payload = ctx.intent().payload();
        if (payload != null && !payload.toString().isEmpty()) {
            String data = payload.toString().trim();
            if (data.startsWith("{") || data.startsWith("[")) {
                headers.put("Content-Type", "application/json");
            } else if (data.startsWith("<")) {
                headers.put("Content-Type", "application/xml");
            } else {
                headers.put("Content-Type", "text/plain");
            }
        }

        return HeaderResolution.success(headers);
    }

    // ── Dynamic builders ────────────────────────────────────────

    private static int pickChromeVersion() {
        return CHROME_VERSIONS[ThreadLocalRandom.current().nextInt(CHROME_VERSIONS.length)];
    }

    private static String buildUserAgent(String os, int chromeVer) {
        String platform;
        if (os.contains("mac")) {
            platform = "Macintosh; Intel Mac OS X 10_15_7";
        } else if (os.contains("linux")) {
            platform = "X11; Linux x86_64";
        } else {
            platform = "Windows NT 10.0; Win64; x64";
        }
        return String.format(
            "Mozilla/5.0 (%s) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/%d.0.0.0 Safari/537.36",
            platform, chromeVer
        );
    }

    private static String buildSecChUa(int chromeVer) {
        return String.format(
            "\"Google Chrome\";v=\"%d\", \"Chromium\";v=\"%d\", \"Not_A Brand\";v=\"24\"",
            chromeVer, chromeVer
        );
    }

    private static String buildPlatform(String os) {
        if (os.contains("mac"))   return "\"macOS\"";
        if (os.contains("linux")) return "\"Linux\"";
        return "\"Windows\"";
    }

    private static String buildAcceptLanguage() {
        String[] locales = {"en-US,en;q=0.9", "en-GB,en;q=0.9", "en-US,en;q=0.9,fr;q=0.8"};
        return locales[ThreadLocalRandom.current().nextInt(locales.length)];
    }

    private static String resolveFetchSite(URI url) {
        if (url == null || url.getHost() == null) return "none";
        return "same-origin";
    }

    private static String resolveOrigin(URI url) {
        if (url == null || url.getHost() == null) return "";
        String scheme = url.getScheme() != null ? url.getScheme() : "https";
        int port = url.getPort();
        if (port == -1 || port == 443 || port == 80) {
            return scheme + "://" + url.getHost();
        }
        return scheme + "://" + url.getHost() + ":" + port;
    }

    private static String resolveReferer(URI url) {
        if (url == null || url.getHost() == null) return "";
        return resolveOrigin(url) + "/";
    }
}
