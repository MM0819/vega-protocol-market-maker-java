package com.vega.protocol.submission;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class BatchMarketInstruction {
    private List<OrderCancellation> cancellations = new ArrayList<>();
    private List<OrderAmendment> amendments = new ArrayList<>();
    private List<OrderSubmission> submissions = new ArrayList<>();
}