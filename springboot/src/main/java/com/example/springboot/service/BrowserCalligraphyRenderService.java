package com.example.springboot.service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class BrowserCalligraphyRenderService {

    private static final Logger log = LoggerFactory.getLogger(BrowserCalligraphyRenderService.class);
    private static final String FONT_RESOURCE_PATH = "/fonts/YMAE07.TTF";

    public byte[] render(String text, int width, int height) {
        String normalizedText = text == null ? "" : text.trim();
        log.info(
                "Rendering browser calligraphy image. textLength={} width={} height={}",
                normalizedText.length(),
                width,
                height
        );

        try (Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                     .setHeadless(true)
                     .setArgs(List.of("--font-render-hinting=medium")))) {
            BrowserContext context = browser.newContext();
            try {
                Page page = context.newPage();
                page.setViewportSize(width, height);
                page.setContent(buildHtml(normalizedText, width, height));
                page.waitForFunction("window.__calligraphyReady === true");
                Locator stage = page.locator("#capture-root");
                return stage.screenshot(new Locator.ScreenshotOptions().setType(com.microsoft.playwright.options.ScreenshotType.PNG));
            } finally {
                context.close();
            }
        }
    }

    private String buildHtml(String text, int width, int height) {
        String escapedText = escapeHtml(text);
        String fontDataUrl = loadFontDataUrl();
        return """
                <!DOCTYPE html>
                <html lang=\"ko\">
                <head>
                  <meta charset=\"UTF-8\" />
                  <style>
                    @font-face {
                      font-family: 'YangjaeMaehwaS';
                      src: url('%s') format('truetype');
                      font-weight: 400;
                      font-style: normal;
                      font-display: swap;
                    }
                    html, body {
                      margin: 0;
                      padding: 0;
                      width: %dpx;
                      height: %dpx;
                      background: #ffffff;
                      overflow: hidden;
                    }
                    body {
                      display: flex;
                      align-items: center;
                      justify-content: center;
                    }
                    #capture-root {
                      width: %dpx;
                      height: %dpx;
                      display: flex;
                      align-items: center;
                      justify-content: center;
                      background: #ffffff;
                      overflow: hidden;
                      box-sizing: border-box;
                      padding: 18px 32px;
                    }
                    .calligraphy-text {
                      font-family: 'YangjaeMaehwaS', cursive;
                      font-size: 180px;
                      font-weight: 400;
                      color: #050505;
                      letter-spacing: 0.04em;
                      line-height: 1.1;
                      transform: skew(-3deg) rotate(-1deg);
                      filter: contrast(145%) brightness(85%);
                      display: inline-block;
                      white-space: nowrap;
                      transform-origin: center center;
                    }
                  </style>
                </head>
                <body>
                  <div id=\"capture-root\">
                    <span id=\"calligraphy-text\" class=\"calligraphy-text\">%s</span>
                  </div>
                  <script>
                    const root = document.getElementById('capture-root');
                    const target = document.getElementById('calligraphy-text');
                    const minFontSize = 72;
                    let size = 180;
                    const maxWidth = root.clientWidth * 0.92;
                    const maxHeight = root.clientHeight * 0.84;
                    while (size > minFontSize) {
                      target.style.fontSize = size + 'px';
                      const rect = target.getBoundingClientRect();
                      if (rect.width <= maxWidth && rect.height <= maxHeight) {
                        break;
                      }
                      size -= 2;
                    }
                    window.__calligraphyReady = true;
                  </script>
                </body>
                </html>
                """.formatted(fontDataUrl, width, height, width, height, escapedText);
    }

    private String loadFontDataUrl() {
        try (InputStream inputStream = BrowserCalligraphyRenderService.class.getResourceAsStream(FONT_RESOURCE_PATH)) {
            if (inputStream == null) {
                throw new IllegalStateException("calligraphy font resource not found.");
            }
            byte[] bytes = inputStream.readAllBytes();
            String base64 = Base64.getEncoder().encodeToString(bytes);
            return "data:font/ttf;base64," + base64;
        } catch (IOException e) {
            throw new IllegalStateException("failed to read calligraphy font resource.", e);
        }
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;")
                .replace("\n", "<br />");
    }

}
