package org.tajiro.ai;

import java.util.List;


public interface EmbeddingClient {
    List<List<Double>> embed(List<String> inputs);
}
