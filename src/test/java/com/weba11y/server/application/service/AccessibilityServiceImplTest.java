package com.weba11y.server.application.service;

import com.weba11y.server.api.dto.accessibilityViolation.AccessibilityViolationDto;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.weba11y.server.domain.enums.InspectionItems.*;


@ActiveProfiles("test")
@SpringBootTest
public class AccessibilityServiceImplTest {

    private static final String INSPECTION_URL = "https://www.naver.com";


    // 1. 대체 텍스트 검사
    @Test
    @DisplayName("대체 택스트 검사")
    void accessibilityAltTextChecker() {
        try {
            // HTML 문서 가져오기
            Document doc = Jsoup.connect(INSPECTION_URL).get();
            List<AccessibilityViolationDto> resultDtoList = new ArrayList<>();
            // 각 항목별 대체 텍스트 검사
            checkImages(doc, resultDtoList);
            checkLinks(doc, resultDtoList);
            checkButtons(doc, resultDtoList);
            checkVideos(doc, resultDtoList);
            checkAudios(doc, resultDtoList);
            checkSvgImages(doc, resultDtoList);
            checkIcons(doc, resultDtoList);
            checkCharts(doc, resultDtoList);

            for (AccessibilityViolationDto dto : resultDtoList)
                System.out.println("결과 : " + dto.getDescription());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void checkImages(Document doc, List<AccessibilityViolationDto> resultDtoList) {
        Elements images = doc.select("img");
        for (Element img : images) {
            if (!img.hasAttr("alt")) {
                resultDtoList.add(AccessibilityViolationDto.builder()
                        .inspectionItem(ALT_TEXT)
                        .description("이미지에 대체 텍스트가 없습니다.")
                        .codeLine(img.outerHtml())
                        .build());
            }
        }
    }

    private static void checkLinks(Document doc, List<AccessibilityViolationDto> resultDtoList) {
        Elements links = doc.select("a");
        for (Element link : links) {
            if (!link.hasAttr("aria-label") && !link.hasAttr("title") && link.ownText().isEmpty()) {
                resultDtoList.add(AccessibilityViolationDto.builder()
                        .inspectionItem(ALT_TEXT)
                        .description("링크에 대체 텍스트가 없습니다.")
                        .codeLine(link.outerHtml())
                        .build());
            }
        }
    }

    private static void checkButtons(Document doc, List<AccessibilityViolationDto> resultDtoList) {
        Elements buttons = doc.select("button");
        for (Element button : buttons) {
            if (!button.hasAttr("aria-label") && button.ownText().isEmpty()) {
                resultDtoList.add(AccessibilityViolationDto.builder()
                        .inspectionItem(ALT_TEXT)
                        .description("버튼에 대체 텍스트가 없습니다.")
                        .codeLine(button.outerHtml())
                        .build());
            }
        }
    }

    private static void checkVideos(Document doc, List<AccessibilityViolationDto> resultDtoList) {
        Elements videos = doc.select("video");
        for (Element video : videos) {
            if (!video.hasAttr("title")) {
                resultDtoList.add(AccessibilityViolationDto.builder()
                        .inspectionItem(ALT_TEXT)
                        .description("비디오에 대체 텍스트가 없습니다.")
                        .codeLine(video.outerHtml())
                        .build());
            }
        }
    }

    private static void checkAudios(Document doc, List<AccessibilityViolationDto> resultDtoList) {
        Elements audios = doc.select("audio");
        for (Element audio : audios) {
            if (!audio.hasAttr("title")) {
                resultDtoList.add(AccessibilityViolationDto.builder()
                        .inspectionItem(ALT_TEXT)
                        .description("오디오에 대체 텍스트가 없습니다.")
                        .codeLine(audio.outerHtml())
                        .build());
            }
        }
    }

    private static void checkSvgImages(Document doc, List<AccessibilityViolationDto> resultDtoList) {
        Elements svgs = doc.select("svg");
        for (Element svg : svgs) {
            if (!svg.hasAttr("aria-label") && !svg.hasAttr("title")) {
                resultDtoList.add(AccessibilityViolationDto.builder()
                        .inspectionItem(ALT_TEXT)
                        .description("SVG 이미지에 대체 텍스트가 없습니다.")
                        .codeLine(svg.outerHtml())
                        .build());
            }
        }
    }

    private static void checkIcons(Document doc, List<AccessibilityViolationDto> resultDtoList) {
        Elements icons = doc.select("[role='img'], .icon");
        for (Element icon : icons) {
            if (!icon.hasAttr("aria-label") && !icon.hasAttr("title")) {
                resultDtoList.add(AccessibilityViolationDto.builder()
                        .inspectionItem(ALT_TEXT)
                        .description("아이콘에 대체 텍스트가 없습니다.")
                        .codeLine(icon.outerHtml())
                        .build());
            }
        }
    }

    private static void checkCharts(Document doc, List<AccessibilityViolationDto> resultDtoList) {
        Elements charts = doc.select("canvas");
        for (Element chart : charts) {
            if (!chart.hasAttr("aria-label")) {
                resultDtoList.add(AccessibilityViolationDto.builder()
                        .inspectionItem(ALT_TEXT)
                        .description("차트에 대체 텍스트가 없습니다.")
                        .codeLine(chart.outerHtml())
                        .build());
            }
        }
    }
}
