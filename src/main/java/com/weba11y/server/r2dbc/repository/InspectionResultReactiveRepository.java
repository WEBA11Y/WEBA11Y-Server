package com.weba11y.server.r2dbc.repository;

import com.weba11y.server.domain.InspectionResultReactive;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface InspectionResultReactiveRepository extends ReactiveCrudRepository<InspectionResultReactive, Long> {
}
