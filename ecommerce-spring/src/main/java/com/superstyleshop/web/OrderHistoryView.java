package com.superstyleshop.web;

import com.superstyleshop.model.CustomerOrder;
import com.superstyleshop.model.OrderLine;
import java.util.List;

public record OrderHistoryView(CustomerOrder order, List<OrderLine> items) {
}
