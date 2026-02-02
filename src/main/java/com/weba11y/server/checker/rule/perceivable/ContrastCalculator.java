package com.weba11y.server.checker.rule.perceivable;

import java.awt.Color;

public class ContrastCalculator {

    /**
     * 두 색상(hex 또는 rgba 형식)의 명도 대비 비율을 계산한다.
     * @param foreground CSS color 문자열 (예: "#FFFFFF", "rgb(255,255,255)")
     * @param background CSS color 문자열 (예: "#000000", "rgba(0,0,0,1)")
     * @return 대비 비율 (double)
     */
    public static double calculate(String foreground, String background) {
        Color fg = parseColor(foreground);
        Color bg = parseColor(background);

        double l1 = getRelativeLuminance(fg);
        double l2 = getRelativeLuminance(bg);

        // 큰 값이 항상 분자에 오도록 (WCAG 공식)
        double lighter = Math.max(l1, l2);
        double darker = Math.min(l1, l2);

        return (lighter + 0.05) / (darker + 0.05);
    }

    /**
     * WCAG 2.1 명도 대비 기준 검증
     * @param ratio 대비 비율
     * @param isLargeText 18pt 이상 또는 Bold 14pt 이상 여부
     * @return 기준 충족 여부
     */
    public static boolean isContrastSufficient(double ratio, boolean isLargeText) {
        return isLargeText ? ratio >= 3.0 : ratio >= 4.5;
    }

    private static Color parseColor(String cssColor) {
        if (cssColor == null || cssColor.isBlank()) {
            return Color.BLACK;
        }
        cssColor = cssColor.trim();

        try {
            if (cssColor.startsWith("#")) {
                return Color.decode(cssColor);
            } else if (cssColor.startsWith("rgb")) {
                String[] parts = cssColor.replaceAll("[rgba()\\s]", "").split(",");
                int r = Integer.parseInt(parts[0]);
                int g = Integer.parseInt(parts[1]);
                int b = Integer.parseInt(parts[2]);
                return new Color(r, g, b);
            }
        } catch (Exception e) {
            // 파싱 실패 시 기본 검정색 반환
            return Color.BLACK;
        }
        return Color.BLACK;
    }

    /**
     * 상대 명도 계산 (sRGB → Linear 변환)
     */
    private static double getRelativeLuminance(Color color) {
        double r = adjust(color.getRed() / 255.0);
        double g = adjust(color.getGreen() / 255.0);
        double b = adjust(color.getBlue() / 255.0);

        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }

    private static double adjust(double channel) {
        return (channel <= 0.03928) ? channel / 12.92 : Math.pow((channel + 0.055) / 1.055, 2.4);
    }
}
