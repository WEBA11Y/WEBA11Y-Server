package com.weba11y.server.service;

import com.weba11y.server.service.implement.AccessibilityServiceImpl;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AccessibilityServiceImplTest {

    @InjectMocks
    private AccessibilityServiceImpl accessibilityService;

    @Mock
    private Document mockDocument;

    @Test
    public void testCheckAccessibility_AltAttributeMissing() throws IOException {
        // Given
        String url = "http://example.com";

        // Mock Jsoup.connect().get() to return mockDocument
        when(mockDocument.select("img")).thenReturn(new Elements(new Element("img")));
        when(mockDocument.select("a")).thenReturn(new Elements());

        // When
        List<String> issues = accessibilityService.checkAccessibility(url);

        // Then
        assertEquals(1, issues.size());
        assertTrue(issues.get(0).contains("이미지 태그에 alt 속성이 없습니다"));
    }

    @Test
    public void testCheckAccessibility_LinkTextMissing() throws IOException {
        // Given
        String url = "http://example.com";

        // Mock Jsoup.connect().get() to return mockDocument
        when(mockDocument.select("img")).thenReturn(new Elements());
        Element linkElement = new Element("a");
        when(mockDocument.select("a")).thenReturn(new Elements(linkElement));

        // When
        List<String> issues = accessibilityService.checkAccessibility(url);

        // Then
        assertEquals(1, issues.size());
        assertTrue(issues.get(0).contains("링크에 텍스트가 없습니다"));
    }

    @Test
    public void testCheckAccessibility_NoIssues() throws IOException {
        // Given
        String url = "http://example.com";

        // Mock Jsoup.connect().get() to return mockDocument
        when(mockDocument.select("img")).thenReturn(new Elements());
        when(mockDocument.select("a")).thenReturn(new Elements());

        // When
        List<String> issues = accessibilityService.checkAccessibility(url);

        // Then
        assertTrue(issues.isEmpty());
    }

    @Test
    public void testCheckAccessibility_IOException() throws IOException {
        // Given
        String url = "http://example.com";

        // Mock Jsoup.connect().get() to throw IOException
        when(Jsoup.connect(url).get()).thenThrow(new IOException("Connection error"));

        // When
        List<String> issues = accessibilityService.checkAccessibility(url);

        // Then
        assertEquals(1, issues.size());
        assertTrue(issues.get(0).contains("URL 접속 중 오류 발생"));
    }
}
