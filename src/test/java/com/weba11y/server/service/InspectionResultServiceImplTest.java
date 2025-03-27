package com.weba11y.server.service;


import com.weba11y.server.service.implement.InspectionResultServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class InspectionResultServiceImplTest {

    @Autowired
    private InspectionResultServiceImpl inspectionResultService;


}
