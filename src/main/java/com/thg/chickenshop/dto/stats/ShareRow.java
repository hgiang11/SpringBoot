// ShareRow.java  (dùng cho biểu đồ tròn: nhãn + giá trị)
package com.thg.chickenshop.dto.stats;

import java.math.BigDecimal;

public record ShareRow(String label, BigDecimal value) {}