package com.weba11y.server.service.implement;

import com.weba11y.server.service.AccessibilityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccessibilityServiceImpl implements AccessibilityService {

    @Override
    public List<String> checkAccessibility(String url) {
        List<String> issues = new ArrayList<>();

        try {
            // URL에서 HTML 문서 가져오기
            Document doc = Jsoup.connect(url).get();
            // 이미지 태그에서 alt 속성 검사
            checkImageAccessibility(issues, doc);
            // 링크 검사
            checkLinkAccessibility(issues, doc);

        } catch (IOException e) {
            issues.add("URL 접속 중 오류 발생: " + e.getMessage());
        }

        return issues;
    }

    private static void checkLinkAccessibility(List<String> issues, Document doc) {
        for (Element link : doc.select("a")) {
            if (link.text().trim().isEmpty()) {
                issues.add("링크에 텍스트가 없습니다: " + link.absUrl("href"));
            }
        }
    }

    private static void checkImageAccessibility(List<String> issues, Document doc) {
        for (Element img : doc.select("img")) {
            if (!img.hasAttr("alt")) {
                issues.add("이미지 태그에 alt 속성이 없습니다: " + img.absUrl("src"));
            }
        }
    }
}