package org.tajiro.policy.service;

import org.tajiro.policy.dto.PolicyEmbeddingResultDTO;

public interface PolicyEmbeddingService {
    PolicyEmbeddingResultDTO initializeAndClassify(int limit);
}
