package org.tajiro.finance.service.ai;

import java.util.List;


public interface FinanceEmbeddingClient {
    List<List<Double>> embed(List<String> inputs);
}
