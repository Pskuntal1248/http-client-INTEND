package com.intend.providers;
import com.intend.context.ResolutionContext;
import com.intend.spi.HeaderProvider;
import com.intend.spi.HeaderResolution;
import java.util.HashMap;
import java.util.Map;

public class ProtocolProvider implements HeaderProvider {
    @Override
    public int getOrder() { return 10; }
    @Override
    public boolean supports(ResolutionContext ctx) { return true; }
    @Override
    public HeaderResolution resolve(ResolutionContext ctx) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "*/*");
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
}